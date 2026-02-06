package com.knowledge.agent.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.agent.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
