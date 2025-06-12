package com.bi_side.CodeSnap.config.aop.impl;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface AopMapper {
    int insertApiLog(Map<String,Object> dto);
}
