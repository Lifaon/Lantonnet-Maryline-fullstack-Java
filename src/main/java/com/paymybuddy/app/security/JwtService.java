package com.paymybuddy.app.security;

import com.paymybuddy.app.user.User;
import com.paymybuddy.app.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.function.Function;

@Service
public class JwtService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final SecretKey key;

    public JwtService() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            keyGen.init(256);
            key = keyGen.generateKey();
            log.debug("JWT key created: {}", Base64.getEncoder().encodeToString(key.getEncoded()));
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToken(UserDetails userDetails) {
        String token = Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", UserRole.getHighest(userDetails.getAuthorities()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
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

    public String extractUsername(String token) {
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
            return !isExpired(token);
        }
        catch (Exception e) {
            log.error("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    public UserDetails getUserDetails(String token) {
        User user = new User();
        user.setUsername(extractUsername(token));
        user.setRoles(extractRoles(token));
        return user;
    }
}