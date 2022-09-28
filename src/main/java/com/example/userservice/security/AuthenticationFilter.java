package com.example.userservice.security;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestLogin;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity.JwtSpec;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private UserService userService;
    private Environment env;

    public AuthenticationFilter(AuthenticationManager authenticationManager,
                                UserService userService,
                                Environment env) {
        super(authenticationManager);
        this.userService = userService;
        this.env = env;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
        HttpServletResponse response) throws AuthenticationException {

        // request에서 로그인 정보 받아와서 인증하여 이메일과 패스워드 일치하는지 판단
        try {
            RequestLogin creds = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);

            return getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(creds.getEmail(), creds.getPassword(), new ArrayList<>()
                )
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 로그인 성공 후 인증 성공여부, 실패 처리, 토큰 만료 처리 등과 같은 기능과 jwt 토큰 발행 기능을 수행
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                        HttpServletResponse response, FilterChain chain, Authentication authResult)
                        throws IOException, ServletException {

        String userName = ((User)authResult.getPrincipal()).getUsername();
        UserDto userDetails = userService.getUserDetailsByEmail(userName);

        // token 생성
        String token = Jwts.builder()
            .setSubject(userDetails.getUserId())
            .setExpiration(new Date(System.currentTimeMillis() +
                Long.parseLong(env.getProperty("token.expiration_time"))))
            .signWith(SignatureAlgorithm.HS512, env.getProperty("token.secret"))
            .compact();

        response.addHeader("token", token);
        response.addHeader("userId", userDetails.getUserId());
    }
}
