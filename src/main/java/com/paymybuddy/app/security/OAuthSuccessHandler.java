package com.paymybuddy.app.security;

import com.paymybuddy.app.user.User;
import com.paymybuddy.app.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final JwtService jwtService;
    private final UserService userService;

    public OAuthSuccessHandler(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        final String uriPart = "/login/oauth2/code/";
        final String uri = request.getRequestURI();

        if (!uri.startsWith(uriPart)) {
            log.warn("Unknown provider uri: {}", uri);
            return;
        }

        final String provider = uri.substring(uriPart.length());

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        oauth2User.getAttributes().forEach((k, v) -> log.trace("{}: {}", k, v));

        // TODO: gérer le changement de username
        // TODO: gérer le cas où un user similaire existe déjà (ex: même adresse mail)

        User user;
        String id = oauth2User.getName();
        Optional<User> opt = userService.getByOAuth(provider, id);
        if (opt.isPresent()) {
            user = opt.get();
        } else {
            user = new User();
            user.setProvider(provider);
            user.setProviderId(id);
            switch (provider) {
                case "google":
                case "github":
                    user.setUsername(oauth2User.getAttribute("name"));
                    user.setEmail(oauth2User.getAttribute("email"));
                    break;
                default:
                    log.warn("Unknown provider: {}", provider);
                    return;
            }
            user = userService.createUser(user);
        }

        AuthController.createJWTCookie(response, jwtService.generateToken(user.toUserDetails()));
    }
}