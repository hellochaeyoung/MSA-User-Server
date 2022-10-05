package com.example.userservice.controller;

import com.example.userservice.dto.TokenDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@AllArgsConstructor
public class UserController {

    private final Environment env;
    private final Greeting greeting;
    private final UserService userService;

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's Working in User Service"
                + ", port(local.server.port)=" + env.getProperty("local.server.port")
                + ", port(server.port)=" + env.getProperty("server.port")
                + ", token secret=" + env.getProperty("token.secret")
                + ", token expiration time=" + env.getProperty("token.expiration_time"));
    }

    @GetMapping("/welcome")
    public String welcome() {
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody RequestUser user) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto dto = mapper.map(user, UserDto.class);
        userService.createUser(dto);

        ResponseUser responseUser = mapper.map(dto, ResponseUser.class);

        return new ResponseEntity(responseUser, HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader(value = "access_token") String accessToken,
                                     @RequestHeader(value = "refresh_token") String refreshToken,
                                     @RequestBody TokenDto dto)
        throws AccessDeniedException {

        String userId = dto.getUserId();
        System.out.println(userId);
        TokenDto result = userService.refresh(TokenDto.builder()
                                                .userId(userId)
                                                .accessToken(accessToken)
                                                .refreshToken(refreshToken)
                                                .build());

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        Iterable<UserEntity> userList = userService.getUserByAll();

        List<ResponseUser> result = new ArrayList<>();
        userList.forEach(u -> {
            result.add(new ModelMapper().map(u, ResponseUser.class));
        });

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUsers(@PathVariable("userId") String userId) {
        UserDto dto = userService.getUserByUserId(userId);

        ResponseUser returnValue = new ModelMapper().map(dto, ResponseUser.class);

        return new ResponseEntity<>(returnValue, HttpStatus.OK);
    }

}
