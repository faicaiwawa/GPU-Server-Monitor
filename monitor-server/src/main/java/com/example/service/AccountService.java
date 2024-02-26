package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Account;
import com.example.entity.dto.Client;
import com.example.entity.vo.request.*;
import com.example.entity.vo.response.SubAccountVO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface AccountService extends IService<Account>, UserDetailsService {
    Account findAccountByNameOrEmail(String text);
    String sendEmailVerifyCode(String type, String email, String address);

    String resetEmailAccountPassword(EmailResetVO info);
    String emailVerifyCodetConfirm(EmailVerifyCodeConfirmVO info);
    boolean changePassword(int id, String oldPass, String newPass);
    String modifyEmail(int id, ModifyEmailVO vo);
    String getEmailLoginVerifyCode(String email);


    void createSubAccount(CreateSubAccountVO vo);
    void deleteSubAccount(int uid);
    List<SubAccountVO> listSubAccount();

}
