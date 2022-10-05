package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.TokenDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.exp.codes.CommonErrorCode;
import com.example.userservice.exp.exceptions.AllAuthExpiredException;
import com.example.userservice.exp.exceptions.InvalidRefreshTokenException;
import com.example.userservice.exp.exceptions.WrongLoginInfoException;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.security.TokenProvider;
import com.example.userservice.vo.ResponseOrder;
import feign.FeignException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Environment env;
    //private final RestTemplate restTemplate;
    private final OrderServiceClient orderServiceClient;

    private final TokenProvider tokenProvider;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Call UserService loadUserByUsername");
        UserEntity userEntity = userRepository.findByEmail(username);

        if(userEntity == null) {
            log.error("Cannot found User");
            throw new WrongLoginInfoException(CommonErrorCode.BAD_REQUEST_LOGIN.getMessage(),
                CommonErrorCode.BAD_REQUEST_LOGIN);
        }

        return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(),
            true, true, true, true,
            new ArrayList<>());
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity entity = mapper.map(userDto, UserEntity.class);
        entity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));

        userRepository.save(entity);

        UserDto dto = mapper.map(entity, UserDto.class);

        return dto;

    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity entity = userRepository.findByUserId(userId);

        if(entity == null) {
            throw new UsernameNotFoundException("User not found");
        }
        UserDto userDto = new ModelMapper().map(entity, UserDto.class);

        /*String orderUrl = String.format(env.getProperty("order_service.url"), userId);
        ResponseEntity<List<ResponseOrder>> orderListResponse =
            restTemplate.exchange(orderUrl, HttpMethod.GET, null,
                                    new ParameterizedTypeReference<List<ResponseOrder>>() {
            });
        List<ResponseOrder> ordersList = orderListResponse.getBody();*/

        // FeinClient
        // Feign Exception Handling
        /*List<ResponseOrder> ordersList = null;
        try {
            ordersList = orderServiceClient.getOrders(userId);
        } catch (FeignException ex) {
            log.error(ex.getMessage());
        }*/

        // ErrorDecoder
        List<ResponseOrder> ordersList = orderServiceClient.getOrders(userId);

        userDto.setOrderList(ordersList);

        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        log.info("Call UserService getUserDetailsByEmail");
        UserEntity userEntity = userRepository.findByEmail(email);

        if(userEntity == null) {
            throw new UsernameNotFoundException(email);
        }

        UserDto dto = new ModelMapper().map(userEntity, UserDto.class);
        return dto;
    }

    @Override
    public void updateRefreshToken(String userId, String token) {
        UserEntity user = userRepository.findByUserId(userId);

        user.setRefreshToken(token);
        userRepository.save(user);
    }

    @Override
    public TokenDto refresh(TokenDto dto) throws AccessDeniedException {

        String newAccessToken = "";
        String refreshToken = dto.getRefreshToken();
        String accessToken = dto.getAccessToken();

        if(validateTokenExceptionExpiration(refreshToken)) {
            UserEntity user = userRepository.findByUserId(dto.getUserId());

            String savedRefreshToken = user.getRefreshToken();

            if(savedRefreshToken.equals(refreshToken)) {
                newAccessToken = tokenProvider.createAccessToken(dto.getUserId());
            }else {
                throw new InvalidRefreshTokenException(
                    CommonErrorCode.BAD_REQUEST_REFRESH_TOKEN.getMessage(),
                    CommonErrorCode.BAD_REQUEST_REFRESH_TOKEN);
            }

        }else {
            throw new AllAuthExpiredException(
                CommonErrorCode.EXPIRED_REFRESH_TOKEN.getMessage(),
                CommonErrorCode.EXPIRED_REFRESH_TOKEN
            );
        }

        return TokenDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public boolean validateTokenExceptionExpiration(String token) {

        // 로그아웃 확인 필요

        Jws<Claims> claims = Jwts.parser().setSigningKey(env.getProperty("token.secret"))
            .parseClaimsJws(token);

        return claims.getBody().getExpiration().after(new Date());
    }

}
