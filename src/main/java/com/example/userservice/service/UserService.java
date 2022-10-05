package com.example.userservice.service;

import com.example.userservice.dto.TokenDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import java.nio.file.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    UserDto createUser(UserDto userDto);

    UserDto getUserByUserId(String userId);

    Iterable<UserEntity> getUserByAll();

    UserDto getUserDetailsByEmail(String userName);

    void updateRefreshToken(String userId, String token);

    TokenDto refresh(TokenDto dto) throws AccessDeniedException;
}
