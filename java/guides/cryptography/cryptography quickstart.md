# Cryptography quickstart

The cryptography tools can be grouped by goal.

## Symmetric encryption

Goal:
Encrypt and decrypt data with a secret key.

Typical use:
Encrypting files before storing them on a less trusted storage, for example an online object storage.

Algorithms:
- AES-GCM — common recommended choice in Java.
- ChaCha20-Poly1305 — modern AEAD alternative, especially good where AES hardware acceleration is unavailable.
- AES-CBC — older mode; easy to misuse because it needs a separate integrity check.
- AES-ECB — do not use; it leaks visual/data patterns.

## Asymmetric encryption

Goal:
Encrypt data with a public key and decrypt it with the private key.

Algorithms:
- RSA-OAEP — RSA encryption using the modern OAEP padding scheme.
- ECIES — elliptic-curve integrated encryption, less commonly exposed directly in standard Java APIs.
- In most real systems: public-key crypto encrypts a temporary symmetric key, while AES-GCM encrypts the actual data.

Why hybrid encryption is standard:

RSA is computationally expensive and has message-size limits.

For large content, applications use hybrid encryption:
- Generate a random AES key.
- Encrypt the data with AES-GCM.
- Encrypt the AES key using the recipient’s RSA public key.
- Send/store:
    - RSA-encrypted AES key
    - AES nonce
    - AES-encrypted data
    - This is broadly how TLS and many secure file-sharing systems work conceptually.

## Hash

Goal: Produce a fixed-length fingerprint of data.

A hash is not encryption. You cannot decrypt a hash to recover the original value.

Typical use: check if two files are equal without comparing every byte.

Algorithms:
- SHA-256, SHA-384, SHA-512 — modern standard choices.
- SHA-3 — a newer hash family.
- MD5 and SHA-1 — broken for security-sensitive collision resistance; do not use for new security designs.

## Password hash

Goal: check if a password is correct by comparing it with the hash.

The original password is never stored.

The generation is deliberately expensive.

Typical use: the standard way to store passwords to prevent a data breach less harmful.

Algorithms
- Argon2id — generally preferred for new systems.
- bcrypt — mature and widely used.
- scrypt — memory-hard and still good.
- PBKDF2-HMAC-SHA-256 — built into Java; accepted when configured with a strong iteration count.
- Plain SHA-256/SHA-512 — not suitable for password storage.

In production, prefer a mature password library/framework rather than implementing password-storage format and comparison yourself:
- Spring Security’s PasswordEncoder supports bcrypt, PBKDF2, scrypt, and Argon2 options.
- Argon2 and bcrypt are commonly supplied by third-party libraries rather than the base JDK.****

## Hash-based Message Authentication Code

Goal: Verify that a message has not been modified and was produced by someone holding a shared secret.

Algorithms:
- HmacSHA256
- HmacSHA512

Typical uses:
- Signed webhook verification.
- Authenticating internal service messages.
- API request signing.
- Symmetric-key JWTs (HS256).

## Digital signature

Goal: Let anyone verify that a message was signed by the holder of a private key.

Unlike HMAC, the verifier does not need a shared secret. They verify using the signer’s public key.

Algorithms:
- Ed25519 — excellent modern default where supported.
- ECDSA — common elliptic-curve signature family, such as SHA256withECDSA.
- RSA-PSS — modern RSA signature scheme.
- Avoid creating new systems with legacy SHA256withRSA / PKCS#1 v1.5 where RSA-PSS is an option.

Typical uses:
- Signing software releases.
- Signed documents.
- JWTs using RS256, PS256, or EdDSA.
- Verifying package updates.
- Authenticating a server or issuer with certificates.

## Key exchange / key agreement

Goal: Establish a shared secret across an untrusted network.

Algorithms:
- Diffie–Hellman (DH)
- Elliptic Curve Diffie–Hellman (ECDH)
- X25519 — modern and common elliptic-curve key agreement scheme.

This is not data encryption itself. It lets two parties calculate the same shared secret without sending that secret directly. They then use the resulting secret with symmetric encryption, usually AES-GCM or ChaCha20-Poly1305.

Typical use: TLS/HTTPS uses ephemeral key exchange so your browser and server can create a fresh session key.

In Java, this generally involves KeyAgreement, often with "X25519" or "ECDH".

## Summary
| Requirement                                   | 	Use                                                                 |
|-----------------------------------------------|---------------------------------------------------------------------|
| Store user passwords                          | Argon2id or bcrypt; PBKDF2 if constrained to JDK-only tools         |
| Encrypt a database field or file              | AES-GCM                                                             |
| Encrypt a small symmetric key for a recipient | 	RSA-OAEP, usually as part of hybrid encryption                      |
| Verify webhook requests                       | 	HMAC-SHA-256                                                        |
| Verify a file/release came from a publisher   | 	Ed25519, ECDSA, or RSA-PSS signature                                |
| Create HTTPS-like session secrets             | 	ECDH/X25519, then AES-GCM or ChaCha20-Poly1305                      |
| Detect whether a public file changed          | 	SHA-256 hash                                                        |
| Authenticate a JWT                            | 	JWS signature: preferably asymmetric for multi-service verification |
| Keep JWT claims confidential                  | 	JWE, or avoid carrying sensitive claims in the token                |