package dev.bradburylabs.homedrive.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import dev.bradburylabs.homedrive.entity.AppUser;
import dev.bradburylabs.homedrive.model.user.UserModel;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserModel map(AppUser appUser);
}
