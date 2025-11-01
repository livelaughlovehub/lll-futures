package com.lll.futures.config;

import com.lll.futures.model.User;
import com.lll.futures.repository.UserRepository;
import com.lll.futures.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");
        
        log.info("OAuth2 login successful for: {}", email);
        
        // Find or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Create new user from OAuth
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(email.split("@")[0] + "_" + System.currentTimeMillis());
                    newUser.setBio(name); // Store name in bio field
                    newUser.setProfilePicture(picture);
                    newUser.setPassword(""); // OAuth users don't need password
                    newUser.setIsAdmin(false);
                    return userRepository.save(newUser);
                });
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getIsAdmin());
        
        // Redirect to frontend with token
        String redirectUrl = frontendUrl + "/auth/callback?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}

