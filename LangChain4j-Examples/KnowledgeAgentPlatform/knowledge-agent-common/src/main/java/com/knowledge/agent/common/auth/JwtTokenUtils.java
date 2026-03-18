package com.knowledge.agent.common.auth;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight HS256 JWT helper shared by token issuer and verifier.
 */
public final class JwtTokenUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final String HMAC_SHA_256 = "HmacSHA256";

    private JwtTokenUtils() {
    }

    public static String generateToken(Long userId,
                                       String username,
                                       String learningStyle,
                                       String issuer,
                                       String secret,
                                       Duration ttl) {
        requireUserId(userId);
        requireSecurityConfig(issuer, secret);
        Duration effectiveTtl = ttl == null || ttl.isNegative() || ttl.isZero() ? Duration.ofHours(12) : ttl;
        Instant now = Instant.now();

        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("iss", issuer);
        payload.put("sub", String.valueOf(userId));
        payload.put("userId", userId);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plus(effectiveTtl).getEpochSecond());
        if (StrUtil.isNotBlank(username)) {
            payload.put("username", username);
        }
        if (StrUtil.isNotBlank(learningStyle)) {
            payload.put("learningStyle", learningStyle);
        }

        String headerPart = encodeJson(header);
        String payloadPart = encodeJson(payload);
        String signingInput = headerPart + "." + payloadPart;
        return signingInput + "." + sign(signingInput, secret);
    }

    public static AuthTokenClaims parseAndValidate(String token, String expectedIssuer, String secret) {
        requireSecurityConfig(expectedIssuer, secret);
        if (StrUtil.isBlank(token)) {
            throw new IllegalArgumentException("Token must not be blank");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Token format is invalid");
        }

        String signingInput = parts[0] + "." + parts[1];
        byte[] expectedSignature = sign(signingInput, secret).getBytes(StandardCharsets.UTF_8);
        byte[] providedSignature = parts[2].getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expectedSignature, providedSignature)) {
            throw new IllegalArgumentException("Token signature is invalid");
        }

        Map<String, Object> payload = decodeJson(parts[1]);
        String issuer = stringClaim(payload, "iss");
        if (!expectedIssuer.equals(issuer)) {
            throw new IllegalArgumentException("Token issuer is invalid");
        }

        Long userId = longClaim(payload, "userId");
        requireUserId(userId);

        Instant expiresAt = Instant.ofEpochSecond(longClaim(payload, "exp"));
        if (expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Token has expired");
        }

        Instant issuedAt = Instant.ofEpochSecond(longClaim(payload, "iat"));
        return new AuthTokenClaims(
                userId,
                stringClaim(payload, "username"),
                stringClaim(payload, "learningStyle"),
                issuer,
                issuedAt,
                expiresAt
        );
    }

    private static void requireUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User id must be positive");
        }
    }

    private static void requireSecurityConfig(String issuer, String secret) {
        if (StrUtil.isBlank(issuer) || StrUtil.isBlank(secret)) {
            throw new IllegalArgumentException("Issuer and secret must not be blank");
        }
    }

    private static String encodeJson(Map<String, Object> value) {
        try {
            return URL_ENCODER.encodeToString(OBJECT_MAPPER.writeValueAsBytes(value));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize token payload", e);
        }
    }

    private static Map<String, Object> decodeJson(String encodedJson) {
        try {
            byte[] json = URL_DECODER.decode(encodedJson);
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decode token payload", e);
        }
    }

    private static String sign(String signingInput, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256));
            return URL_ENCODER.encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign token", e);
        }
    }

    private static String stringClaim(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static Long longClaim(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String str && StrUtil.isNotBlank(str)) {
            return Long.parseLong(str);
        }
        throw new IllegalArgumentException("Missing numeric claim: " + key);
    }
}
