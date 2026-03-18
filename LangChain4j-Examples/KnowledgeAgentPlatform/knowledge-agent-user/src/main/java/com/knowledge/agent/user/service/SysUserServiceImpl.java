package com.knowledge.agent.user.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.knowledge.agent.api.request.UserLoginRequest;
import com.knowledge.agent.api.service.UserService;
import com.knowledge.agent.common.auth.JwtTokenUtils;
import com.knowledge.agent.common.exception.BizException;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.user.entity.SysUser;
import com.knowledge.agent.user.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@DubboService
public class SysUserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;

    @Value("${knowledge-agent.auth.token-issuer:knowledge-agent-platform}")
    private String tokenIssuer;

    @Value("${knowledge-agent.auth.token-secret:knowledge-agent-dev-secret}")
    private String tokenSecret;

    @Value("${knowledge-agent.auth.token-ttl:PT12H}")
    private Duration tokenTtl;

    /**
     * 密码加密器
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SysUserServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public Result<String> login(UserLoginRequest request) {
        // 1.查询用户
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.username()));

        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())){
            throw new BizException(401, "用户名或密码错误");
        } else if (user.getDeleted() != 0) {
            throw new BizException(403, "用户已被删除");
        }

        // 3.生成token(后续实现JWT)
        String accessToken = JwtTokenUtils.generateToken(
                user.getId(),
                user.getUsername(),
                user.getLearningStyle(),
                tokenIssuer,
                tokenSecret,
                tokenTtl
        );
        log.info("用户 {} 登录成功，学习风格：{}", user.getUsername(), user.getLearningStyle());
        return Result.success(accessToken);
    }

    @Override
    public Result<String> getUserProfile(Long userId) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, userId));
        return Result.success(user != null && StrUtil.isNotBlank(user.getLearningStyle()) ? user.getLearningStyle() : "{}");
    }

}
