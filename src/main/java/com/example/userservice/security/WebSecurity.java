package com.example.userservice.security;

import com.example.userservice.service.UserService;
import javax.servlet.Filter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurity extends WebSecurityConfigurerAdapter {

    // application.yml 파일에서 관리하는 jwt 토큰의 다양한 정보를 사용하려면 Environment 필요
    private final Environment env;
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptedPasswordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        //http.authorizeRequests().antMatchers("/users/**").permitAll();
        http.authorizeRequests()
            .antMatchers("/error/**").permitAll()
            .antMatchers("/**")
            //.access("hasIpAddress('" + "192.168.0.6" + "')")
            .hasIpAddress("127.0.0.1")
            .and()
            .addFilter(getAuthenticationFilter());
    }

    private AuthenticationFilter getAuthenticationFilter() throws Exception {
        AuthenticationFilter authenticationFilter =
            new AuthenticationFilter(authenticationManager(), userService, env);

        // 생성자로 authenticationManager를 넣어주기 때문에 굳이 set메소드로 할 필요 없어짐
        //authenticationFilter.setAuthenticationManager(authenticationManager());

        return authenticationFilter;
    }

    // db에서 이메일에 해당하는 패스워드 조회 <- userDetailsService에서 담당
    // db 패스워드(encrypted) == 입력한 패스워드(=> encrypted 해서) 확인
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(bCryptedPasswordEncoder);
    }
}
