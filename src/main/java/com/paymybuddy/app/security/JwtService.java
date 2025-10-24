package com.paymybuddy.app.security;

import com.paymybuddy.app.user.User;
import com.paymybuddy.app.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.function.Function;

@Service
public class JwtService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final SecretKey key;

    // How long should the token be valid, in seconds
    private static final int validityPeriod = 60 * 60 * 24;

    //    public JwtService() {
//        try {
//            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
//            keyGen.init(256);
//            key = keyGen.generateKey();
//            log.debug("JWT key created: {}", Base64.getEncoder().encodeToString(key.getEncoded()));
//        }
//        catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public JwtService(@Value("${app.security.jwt-secret}") String secret) {
        key = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        log.debug("JWT key created from secret string");
    }

    static public int getValidityPeriod() {
        return validityPeriod;
    }

    static public Long getLoggedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName()); // Returns the ID (not the username)
    }

    static public boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    static public void logOut() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authentication.setAuthenticated(false);
    }

    public String generateToken(UserDetails userDetails) {
        log.debug("Generating JWT token");

        String token = Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", UserRole.getHighest(userDetails.getAuthorities()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + validityPeriod * 1000))
                .signWith(key)
                .compact();

        log.debug("JWT created: {}", token);
        return token;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractSubject(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public Set<UserRole> extractRoles(String token) {
        String role = extractAllClaims(token).get("role", String.class);
        return UserRole.expandHighest(role);
    }

    public Date extractExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    private Boolean isExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token) {
        try {
            if (isExpired(token)) {
                log.debug("JWT token expired");
                return false;
            }
            return true;
        }
        catch (Exception e) {
            log.error("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    public UserDetails getUserDetails(String token) {
        User user = new User();
        String subject = extractSubject(token);
        user.setId(Long.parseLong(subject));
        user.setRoles(extractRoles(token));
        return user.toUserDetails();
    }
}