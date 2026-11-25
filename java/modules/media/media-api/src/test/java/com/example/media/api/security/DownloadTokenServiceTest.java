package com.example.media.api.security;

import com.example.media.api.upload.UploadNotFoundException;
import com.example.media.contract.asset.AssetId;
import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.upload.Upload;
import com.example.media.contract.upload.UploadId;
import com.example.media.contract.upload.UploadKey;
import com.example.media.contract.upload.UploadVisibility;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The download token contract: the canonical string, the HMAC-SHA256 signature, the verification
 * rules, and the rotation and clock-skew overlap. The canonical string and its signature are fixed
 * vectors, so a change to the format or the algorithm breaks this test instead of silently
 * invalidating every issued token and diverging from the nginx edge verifier.
 */
class DownloadTokenServiceTest {

    /** base64 of "0123456789abcdef0123456789abcdef" (32 bytes). */
    private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    /** base64 of "fedcba9876543210fedcba9876543210" (32 bytes), the current key of the rotation. */
    private static final String NEW_SECRET = "ZmVkY2JhOTg3NjU0MzIxMGZlZGNiYTk4NzY1NDMyMTA=";
    private static final String KEY_ID = "k1";
    private static final String NEW_KEY_ID = "k2";
    private static final Duration CLOCK_SKEW = Duration.ofSeconds(5);
    private static final Instant NOW = Instant.parse("2026-11-25T12:00:00Z");

    private static final UploadKey KEY = new UploadKey("cover");
    private static final String VERSION = "1790000000123";
    private static final long EXPIRES = 1790000000L;
    /** A future expiry (relative to {@link #NOW}), so a token is valid unless its signature is wrong. */
    private static final long VALID_EXPIRES = NOW.getEpochSecond() + 600;
    private static final String CANONICAL = "m1\nread\nk1\ncover\n1790000000123\n1790000000";

    private final UploadRegistry registry = mock();
    private final DownloadTokenProperties properties = new DownloadTokenProperties(
            SECRET, KEY_ID, null, null, Duration.ofMinutes(15), Duration.ofHours(1), CLOCK_SKEW);
    private final DownloadTokenService service =
            new DownloadTokenService(registry, properties, Clock.fixed(NOW, ZoneOffset.UTC));
    /** A deployment in the middle of a rotation: {@code k2} signs, {@code k1} still verifies. */
    private final DownloadTokenProperties rotatedProperties = new DownloadTokenProperties(
            NEW_SECRET, NEW_KEY_ID, KEY_ID, SECRET,
            Duration.ofMinutes(15), Duration.ofHours(1), CLOCK_SKEW);
    private final DownloadTokenService rotatedService =
            new DownloadTokenService(registry, rotatedProperties, Clock.fixed(NOW, ZoneOffset.UTC));

    private final Upload upload = new Upload(
            new UploadId(UUID.randomUUID()), KEY, AssetId.generate(),
            UploadVisibility.PRIVATE, Instant.ofEpochMilli(Long.parseLong(VERSION)));

    @Test
    void signsTheFixedCanonicalVector() {
        // The signature must be exactly the HMAC-SHA256 of the documented canonical string.
        assertThat(service.sign(KEY, VERSION, EXPIRES)).isEqualTo(expectedSignature(CANONICAL));
    }

    @Test
    void verifiesATokenItIssued() {
        var credential = new DownloadCredential(VALID_EXPIRES, service.sign(KEY, VERSION, VALID_EXPIRES), KEY_ID);

        assertThat(service.verify(KEY, VERSION, credential)).isTrue();
    }

    @Test
    void aTamperedSignatureIsRejected() {
        var credential = new DownloadCredential(VALID_EXPIRES, service.sign(KEY, VERSION, VALID_EXPIRES) + "x", KEY_ID);

        assertThat(service.verify(KEY, VERSION, credential)).isFalse();
    }

    @Test
    void aTokenForAnotherKeyIdIsRejected() {
        var credential = new DownloadCredential(VALID_EXPIRES, service.sign(KEY, VERSION, VALID_EXPIRES), "other");

        assertThat(service.verify(KEY, VERSION, credential)).isFalse();
    }

    @Test
    void anExpiredTokenIsRejected() {
        // NOW (2026) is well after EXPIRES (epoch 1790000000), so the token is expired.
        var credential = new DownloadCredential(EXPIRES, service.sign(KEY, VERSION, EXPIRES), KEY_ID);

        assertThat(service.verify(KEY, VERSION, credential)).isFalse();
    }

    @Test
    void aTokenThatExpiredWithinTheClockSkewIsStillAccepted() {
        // The expiry passed 3 seconds ago; the 5-second tolerance covers the drift.
        var expires = NOW.getEpochSecond() - 3;
        var credential = new DownloadCredential(expires, service.sign(KEY, VERSION, expires), KEY_ID);

        assertThat(service.verify(KEY, VERSION, credential)).isTrue();
    }

    @Test
    void aTokenThatExpiredBeyondTheClockSkewIsRejected() {
        var expires = NOW.getEpochSecond() - CLOCK_SKEW.toSeconds() - 1;
        var credential = new DownloadCredential(expires, service.sign(KEY, VERSION, expires), KEY_ID);

        assertThat(service.verify(KEY, VERSION, credential)).isFalse();
    }

    @Test
    void aTokenSignedWithThePreviousKeyIsAcceptedDuringARotation() {
        // A token minted before the key change: signed with k1, verified while k2 is the current key.
        var canonical = "m1\nread\n" + KEY_ID + "\ncover\n" + VERSION + "\n" + VALID_EXPIRES;
        var credential = new DownloadCredential(VALID_EXPIRES, signature(SECRET, canonical), KEY_ID);

        assertThat(rotatedService.verify(KEY, VERSION, credential)).isTrue();
        assertThatCode(() -> rotatedService.authorizeRead(upload, KEY, VERSION, credential))
                .doesNotThrowAnyException();
    }

    @Test
    void issuanceSignsWithTheCurrentKeyOfARotation() {
        when(registry.findUpload(any())).thenReturn(Optional.of(upload));

        var issued = rotatedService.issue(KEY, null);

        assertThat(issued.keyId()).isEqualTo(NEW_KEY_ID);
        assertThat(rotatedService.verify(KEY, issued.version(),
                new DownloadCredential(issued.expires(), issued.signature(), issued.keyId()))).isTrue();
    }

    @Test
    void aTokenForAnotherVersionIsRejected() {
        var credential = new DownloadCredential(VALID_EXPIRES, service.sign(KEY, "other-version", VALID_EXPIRES), KEY_ID);

        // The signature is over "other-version"; verifying against the real version must fail.
        assertThat(service.verify(KEY, VERSION, credential)).isFalse();
    }

    @Test
    void aTokenForAnotherUploadKeyIsRejected() {
        var credential = new DownloadCredential(VALID_EXPIRES, service.sign(KEY, VERSION, VALID_EXPIRES), KEY_ID);

        assertThat(service.verify(new UploadKey("other"), VERSION, credential)).isFalse();
    }

    @Test
    void authorizesAPublicReadWithoutACredential() {
        var publicUpload = new Upload(upload.id(), KEY, upload.assetId(), UploadVisibility.PUBLIC, upload.createdAt());

        assertThatCode(() -> service.authorizeRead(publicUpload, KEY, null, null)).doesNotThrowAnyException();
    }

    @Test
    void deniesAPrivateReadWithoutACredential() {
        assertThatThrownBy(() -> service.authorizeRead(upload, KEY, VERSION, null))
                .isInstanceOf(DownloadDeniedException.class);
    }

    @Test
    void deniesAPrivateReadWithAWrongVersion() {
        var credential = new DownloadCredential(EXPIRES, service.sign(KEY, VERSION, EXPIRES), KEY_ID);

        assertThatThrownBy(() -> service.authorizeRead(upload, KEY, "999", credential))
                .isInstanceOf(DownloadDeniedException.class);
    }

    @Test
    void authorizesAPrivateReadWithAValidCurrentVersionToken() {
        var validExpires = NOW.getEpochSecond() + 600;
        var credential = new DownloadCredential(validExpires, service.sign(KEY, VERSION, validExpires), KEY_ID);

        assertThatCode(() -> service.authorizeRead(upload, KEY, VERSION, credential)).doesNotThrowAnyException();
    }

    @Test
    void issueComputesTheVersionAndClampsTheTtl() {
        when(registry.findUpload(any())).thenReturn(Optional.of(upload));

        var issued = service.issue(KEY, 7200L);

        assertThat(issued.version()).isEqualTo(VERSION);
        assertThat(issued.keyId()).isEqualTo(KEY_ID);
        // The requested TTL exceeds max-ttl (1h), so the expiry is clamped to the maximum.
        assertThat(issued.expires()).isEqualTo(NOW.getEpochSecond() + 3600);
        assertThat(service.verify(KEY, issued.version(),
                new DownloadCredential(issued.expires(), issued.signature(), issued.keyId()))).isTrue();
    }

    @Test
    void issueWithoutAnUploadIsNotFound() {
        when(registry.findUpload(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.issue(KEY, null)).isInstanceOf(UploadNotFoundException.class);
    }

    /** The reference HMAC-SHA256 of the fixed canonical vector, computed independently of the service. */
    private static String expectedSignature(String canonical) {
        return signature(SECRET, canonical);
    }

    /** The reference HMAC-SHA256 of a canonical string under a base64 secret. */
    private static String signature(String base64Secret, String canonical) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(Base64.getDecoder().decode(base64Secret), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}