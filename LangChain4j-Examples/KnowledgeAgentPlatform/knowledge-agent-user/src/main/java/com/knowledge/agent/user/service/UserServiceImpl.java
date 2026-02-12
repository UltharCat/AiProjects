package com.knowledge.agent.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.knowledge.agent.api.request.UserLoginRequest;
import com.knowledge.agent.api.service.UserService;
import com.knowledge.agent.common.exception.BusinessException;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.user.entity.User;
import com.knowledge.agent.user.entity.UserAgentConfig;
import com.knowledge.agent.user.mapper.RoleMapper;
import com.knowledge.agent.user.mapper.UserAgentConfigMapper;
import com.knowledge.agent.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@DubboService
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserAgentConfigMapper userAgentConfigMapper;

    /**
     * 密码加密器
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserMapper userMapper,
                           RoleMapper roleMapper,
                           UserAgentConfigMapper userAgentConfigMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userAgentConfigMapper = userAgentConfigMapper;
    }

    @Override
    public Result<String> login(UserLoginRequest request) {
        // 1.查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.username()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())){
            throw new BusinessException(401, "用户名或密码错误");
        } else if (user.getStatus() != 1) {
            throw new BusinessException(403, "用户已被禁用");
        }
        // 2.查询角色(后续可以鉴权)
        List<String> roles = roleMapper.selectRoleCodesByUserId(user.getId());

        // 3.生成token(后续实现JWT),格式:userId|role1,role2
        String mockToken = user.getId() + "|" + String.join(",", roles);
        log.info("用户 {} 登录成功，角色：{}", user.getUsername(), roles);
        return Result.success(mockToken);
    }

    @Override
    public Result<String> getUserProfile(Long userId) {
        UserAgentConfig config = userAgentConfigMapper.selectOne(new LambdaQueryWrapper<UserAgentConfig>()
                .eq(UserAgentConfig::getUserId, userId));
        return Result.success(config != null && config.getPersonalityTags() != null ? config.getPersonalityTags() : "{}");
    }

}
