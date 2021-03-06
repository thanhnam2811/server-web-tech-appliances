package com.hcmute.backendtechnologicalapplianceswebsite.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;

    public static final String JWT_SECRET = "secret";
    public static final Long JWT_EXPIRATION = 864_000_000L; // 1 day
    public static final Long JWT_REFRESH_EXPIRATION = 6048_000_000L; // 1 week
    public static final String JWT_TOKEN_HEADER = "AccessToken";
    public static final String JWT_REFRESH_TOKEN_HEADER = "RefreshToken";


    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        log.info("Login with username: {} and password: {}", username, password);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        User user = (User) authResult.getPrincipal();
        log.info("Login success with username: {}", user.getUsername());

        Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET.getBytes());
        String token = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles", user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);

        String refreshToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + JWT_REFRESH_EXPIRATION))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles", user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);

//        response.setHeader(JWT_TOKEN_HEADER, token);
//        response.setHeader(JWT_REFRESH_TOKEN_HEADER, refreshToken);

        Map<String, String> map = new HashMap<>();
        map.put(JWT_TOKEN_HEADER, token);
        map.put(JWT_REFRESH_TOKEN_HEADER, refreshToken);
        map.put("role", user.getAuthorities().iterator().next().getAuthority());
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), map);
    }
}
