package com.birbuket.authservice.mapper;

import com.birbuket.authservice.dto.UserRegisterRequest;
import com.birbuket.authservice.dto.UserRegisterResponse;
import com.birbuket.authservice.models.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserEntity toUserEntity(UserRegisterRequest userRegisterRequest);
    UserRegisterResponse toUserRegisterResponse(UserEntity userEntity);
}
