package com.example.handler;


import com.alibaba.fastjson2.JSON;
import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.service.AccountService;
import com.example.service.impl.AccountServiceImpl;
import com.example.utils.GetBeanUtil;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author TangDan
 * @version 1.0
 * @since 2023/3/29
 */

public class JsonAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * 登录成功后直接返回 JSON
     *
     * @param request        请求
     * @param response       响应
     * @param authentication 成功认证的用户信息
     */

    private JwtUtils jwtUtils = GetBeanUtil.getBean(JwtUtils.class);

    private AccountService accountService = GetBeanUtil.getBean(AccountService.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        Account account = accountService.findAccountByNameOrEmail(authentication.getName());
        UserDetails user = accountService.loadUserByUsername(authentication.getName());
        String jwt = jwtUtils.createJwt(user, account.getUsername(), account.getId());
         // 返回JSON
        if(jwt == null) {
            writer.write(RestBean.forbidden("登录验证频繁，请稍后再试").asJsonString());
        } else {
            AuthorizeVO vo = account.asViewObject(AuthorizeVO.class, o -> o.setToken(jwt));
            vo.setExpire(jwtUtils.expireTime());
            writer.write(RestBean.success(vo).asJsonString());
        }

    }
}
