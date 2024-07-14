package com.harvey.mybatic.mapper;

import com.harvey.mybatic.model.po.UserPo;

public interface UserMapper {
    UserPo selectById(Long id);
}
