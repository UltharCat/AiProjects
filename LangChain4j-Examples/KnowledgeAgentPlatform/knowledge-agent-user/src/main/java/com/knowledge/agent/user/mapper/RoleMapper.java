package com.knowledge.agent.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.agent.user.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户ID查询角色编码列表
     * @param userId
     * @return
     */
    @Select("select r.code from roles r left join user_roles ur on r.id = ur.role_id where ur.user_id = #{userId}")
    List<String> selectRoleCodesByUserId(Long userId);

}
