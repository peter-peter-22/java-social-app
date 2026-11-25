// The njs access handler of the /cdn/ location: it verifies the signed download token of a read
// before the request reaches the media API, so the token contract is exercised at the edge and not
// only in Java.
//
// The token is the same stateless credential the API issues and verifies (DownloadTokenService in
// media-api); nothing is stored and nothing is encrypted. The edge proves that the presented
// signature is the HMAC of this upload key, version, and expiry under the shared secret, and that
// the token has not expired. It cannot know the current version of the upload (that needs the
// database), so a correctly signed token for an old version passes here and the API rejects it.
// The API therefore stays authoritative for the visibility, the current version, and the expiry.
//
// A key rotation is an overlap: the current key signs, and the previous key still verifies tokens
// minted before the switch. The clock-skew tolerance matches the API's, so a token that is a few
// seconds past its expiry at the issuer is not rejected by a verifier whose clock runs ahead.
//
// A read without any token parameter is passed through: the API serves a public upload and rejects
// a private one with 403. A partial token (some but not all of v, e, s, k) is always invalid and is
// rejected here, so the edge and the API cannot disagree about which parameters make up a token.

import crypto from 'crypto';

// The canonical string the API signs: the domain separator, the key id, the upload key, the version,
// and the expiry, each on its own line. It must stay byte-identical to the Java implementation.
const CANONICAL_PREFIX = 'm1\nread\n';

// The upload key charset of the contract. It excludes the path separator and the extension dot, so
// the first path segment after /cdn/ is exactly the upload key of an original or a variant URL.
const UPLOAD_KEY = /^\/cdn\/([A-Za-z0-9_-]{1,100})(?:[./]|$)/;

function authorize(r) {
    const match = UPLOAD_KEY.exec(r.uri);
    if (match === null) {
        reject(r, 'the request path carries no upload key');
        return;
    }
    const uploadKey = match[1];

    // r.args yields an array for a repeated parameter, which is never a token.
    const values = [r.args.v, r.args.e, r.args.s, r.args.k];
    if (values.every(value => value === undefined))
        return;
    if (!values.every(value => typeof value === 'string')) {
        reject(r, 'the download token of ' + uploadKey + ' is incomplete');
        return;
    }
    // njs does not support destructuring assignment, so the four parameters are read by index.
    const version = values[0];
    const expires = values[1];
    const signature = values[2];
    const keyId = values[3];

    if (!/^[0-9]{1,18}$/.test(expires)) {
        reject(r, 'the expiry of the download token of ' + uploadKey + ' is not epoch seconds');
        return;
    }
    const secret = secretFor(r, keyId);
    if (secret === '') {
        reject(r, 'the download token of ' + uploadKey + ' names an unknown key id');
        return;
    }
    const expected = sign(secret, keyId, uploadKey, version, expires);
    if (!equal(expected, signature)) {
        reject(r, 'the signature of the download token of ' + uploadKey + ' is invalid');
        return;
    }
    if (Math.floor(Date.now() / 1000) >= Number(expires) + clockSkew(r)) {
        reject(r, 'the download token of ' + uploadKey + ' is expired');
        return;
    }
}

/**
 * The secret that verifies a token of this key id: the current signing key, or the previous key of
 * a rotation. An unknown key id yields an empty string, so a token can never be verified with a key
 * it does not name.
 */
function secretFor(r, keyId) {
    if (keyId === r.variables.download_token_key_id)
        return r.variables.download_token_secret;
    const previousKeyId = r.variables.download_token_previous_key_id;
    if (previousKeyId !== '' && keyId === previousKeyId)
        return r.variables.download_token_previous_secret;
    return '';
}

/** The clock-skew tolerance of the API in seconds; an absent or non-numeric value means none. */
function clockSkew(r) {
    const seconds = Number(r.variables.download_token_clock_skew_seconds);
    return isNaN(seconds) || seconds <= 0 ? 0 : Math.floor(seconds);
}

/** The signature of a read token, exactly as DownloadTokenService.sign computes it. */
function sign(secret, keyId, uploadKey, version, expires) {
    const canonical = CANONICAL_PREFIX + keyId + '\n' + uploadKey + '\n' + version + '\n' + expires;
    const hmac = crypto.createHmac('sha256', Buffer.from(secret, 'base64'));
    return base64Url(hmac.update(canonical).digest());
}

/** base64url without padding, like Base64.getUrlEncoder().withoutPadding() on the API side. */
function base64Url(digest) {
    return Buffer.from(digest).toString('base64')
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=+$/, '');
}

/** Compares two signatures without leaking the position of the first difference through timing. */
function equal(expected, presented) {
    if (expected.length !== presented.length)
        return false;
    let difference = 0;
    for (let i = 0; i < expected.length; i++)
        difference |= expected.charCodeAt(i) ^ presented.charCodeAt(i);
    return difference === 0;
}

function reject(r, reason) {
    r.error('cdn: ' + reason);
    r.return(403);
}

export default {authorize};
