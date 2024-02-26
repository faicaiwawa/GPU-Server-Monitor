package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Client;
import com.example.entity.dto.ClientDetail;
import com.example.entity.vo.request.ClientDetailVO;
import com.example.entity.vo.request.GpuRuntimeDetailVO;
import com.example.entity.vo.request.RuntimeDetailVO;
import com.example.service.ClientService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.example.utils.Const;

import java.util.List;

@RestController
@RequestMapping("/monitor")
public class ClientController {

    @Resource
    ClientService service;

    @GetMapping("/register")
    public RestBean<Void> registerClient(@RequestHeader("Authorization") String token)
    {
        return service.verifyAndRegister(token)?
                RestBean.success():RestBean.failure(401,"客户端注册失败，请检查token");
    }

    @PostMapping("/detail")
    public RestBean<Void> updateClientDetail(@RequestAttribute(Const.ATTR_CLIENT) Client client,
                                             @RequestBody @Valid ClientDetailVO vo)
    {

        service.updateClientDetail(vo,client);
        return RestBean.success();
    }

    @PostMapping("/runtime")
    public RestBean<Void> updateRuntimeDetails(@RequestAttribute(Const.ATTR_CLIENT) Client client,
                                               @RequestBody @Valid RuntimeDetailVO vo) {
        service.updateRuntimeDetail(vo, client);
        return RestBean.success();
    }

    @PostMapping("/gpuRuntime")
    public RestBean<Void> updateGpuRuntimeDetails(@RequestAttribute(Const.ATTR_CLIENT) Client client,
                                               @RequestBody @Valid List<GpuRuntimeDetailVO> voList) {
        service.updateGpuRuntimeDetail(voList, client);
        return RestBean.success();
    }







}
