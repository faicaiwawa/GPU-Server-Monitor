package com.example.sms;


import com.example.service.AccountService;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

public class SmsAuthenticationProvider implements AuthenticationProvider {
    private final AccountService userDetailsServiceImpl;

    private final StringRedisTemplate stringRedisTemplate;

    public SmsAuthenticationProvider(AccountService userDetailsServiceImpl, StringRedisTemplate stringRedisTemplate) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsAuthenticationToken authenticationToken = (SmsAuthenticationToken) authentication;
        Object principal = authentication.getPrincipal();// 获取凭证也就是用户的手机号
        String phone = "";
        if (principal instanceof String) {
            phone = (String) principal;
        }
        String inputCode = (String) authentication.getCredentials(); // 获取输入的验证码
        // 1. 检验Redis手机号的验证码
        String redisCode = userDetailsServiceImpl.getEmailLoginVerifyCode(phone);

        if (redisCode == null ) {
            throw new BadCredentialsException("验证码已经过期或尚未发送，请重新发送验证码");
        }
        if (!inputCode.equals(redisCode)) {
            throw new BadCredentialsException("输入的验证码不正确，请重新输入");
        }
        stringRedisTemplate.delete(phone);// 删除验证码
        // 2. 根据手机号查询用户信息

        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(phone);
        if (userDetails == null) {
            throw new InternalAuthenticationServiceException("phone用户不存在，请注册");
        }
        // 3. 创建已认证对象
        SmsAuthenticationToken authenticationResult = new SmsAuthenticationToken(userDetails.getUsername(),userDetails, userDetails.getAuthorities());
        authenticationResult.setDetails(authenticationToken.getDetails());
        return authenticationResult;
    }


    @Override
    public boolean supports(Class<?> aClass) {
        // 当 SmsAuthenticationToken 认证时，匹配该类
        return SmsAuthenticationToken.class.isAssignableFrom(aClass);
    }
}
