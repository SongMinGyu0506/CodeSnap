package com.bi_side.CodeSnap.config.security.mapper;

import com.bi_side.CodeSnap.config.security.CustomUserDetails;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface SecurityMapper {
    List<Map<String,String>> loadRoles();
    Date getRolesLastUpdate();
    CustomUserDetails getUserInfo(String username);
}
