package com.example.controller;


import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.RenameClientVO;
import com.example.entity.vo.request.RuntimeHistoryDetailsVO;
import com.example.entity.vo.request.SshConnectionVO;
import com.example.entity.vo.response.ClientDetailsVO;
import com.example.entity.vo.response.ClientPreviewVO;
import com.example.entity.vo.response.ClientSimpleVO;
import com.example.entity.vo.response.SshSettingsVO;
import com.example.service.AccountService;
import com.example.service.ClientService;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    @Resource
    ClientService service;

    @Resource
    AccountService accountService;

    @GetMapping("/simple-list")
    public RestBean<List<ClientSimpleVO>> simpleClientList() {
            return RestBean.success(service.listSimpleList());

    }
    @GetMapping("/list")
    public RestBean<List<ClientPreviewVO>> listAllClient()
    {
        return RestBean.success(service.listClients());
    }

//    @GetMapping("/list") //带ID控制
//    public RestBean<List<ClientPreviewVO>> listAllClient(@RequestAttribute(Const.ATTR_USER_ID) int userId,
//                                                         @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
//        List<ClientPreviewVO> clients = service.listClients();
//        if(this.isAdminAccount(userRole)) {
//            return RestBean.success(clients);
//        } else {
//            List<Integer> ids = this.accountAccessClients(userId);
//            return RestBean.success(clients.stream()
//                    .filter(vo -> ids.contains(vo.getId()))
//                    .toList());
//        }
//    }

    @GetMapping("/clientDetails")
    public RestBean<ClientDetailsVO> ClientDetails(int clientId)
    {
        return RestBean.success(service.ClientDetails(clientId));
    }



    @PostMapping("/rename")
    public RestBean<Void> renameClient(@RequestBody @Valid RenameClientVO vo,
                                       @RequestAttribute(Const.ATTR_USER_ID) int userId)
    {
            service.renameClient(vo);
            return RestBean.success();
    }


    @GetMapping("/runtime-history")
    public RestBean<RuntimeHistoryDetailsVO> runtimeDetailHistory(int clientId){
        return RestBean.success(service.clientRuntimeDetailsHistory(clientId));
    }

    @GetMapping("/gpuRuntime-history")
    public RestBean<RuntimeHistoryDetailsVO> gpuRuntimeDetailHistory(int clientId){
        return RestBean.success(service.clientGPURuntimeDetailsHistory(clientId));
    }


    private boolean isAdminAccount(String role) {
        role = role.substring(5);
        return Const.ROLE_ADMIN.equals(role);
    }

    @GetMapping("/register-token")
    public RestBean<String> getRegisterToken(){
        return  RestBean.success(service.registerToken());
    }

    @GetMapping("/deleteClient")
    public RestBean<Void> deleteClient(int clientId){
        service.deleteClient(clientId);
        return  RestBean.success();
    }

    private boolean permissionCheck(int uid, String role, int clientId) {
        if(this.isAdminAccount(role)) return true;
        return this.accountAccessClients(uid).contains(clientId);
    }

    private List<Integer> accountAccessClients(int uid) {
        Account account = accountService.getById(uid);
        return account.getClientList();
    }

    @PostMapping("/terminal/ssh-save")
    public RestBean<Void> saveSshConnection(@RequestBody @Valid SshConnectionVO vo,
                                            @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                            @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if(this.permissionCheck(userId, userRole, vo.getId())) {
            service.saveClientSshConnection(vo);
            return RestBean.success();
        } else {
            return RestBean.noPermission();
        }
    }

    @GetMapping("/terminal/ssh")
    public RestBean<SshSettingsVO> sshSettings(int clientId,
                                               @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                               @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if(this.permissionCheck(userId, userRole, clientId)) {
            return RestBean.success(service.sshSettings(clientId));
        } else {
            return RestBean.noPermission();
        }
    }

}


