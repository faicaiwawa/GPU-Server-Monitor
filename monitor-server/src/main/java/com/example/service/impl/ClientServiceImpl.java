package com.example.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Client;
import com.example.entity.dto.ClientDetail;
import com.example.entity.dto.ClientSsh;
import com.example.entity.vo.request.*;
import com.example.entity.vo.response.ClientDetailsVO;
import com.example.entity.vo.response.ClientPreviewVO;
import com.example.entity.vo.response.ClientSimpleVO;
import com.example.entity.vo.response.SshSettingsVO;
import com.example.mapper.ClientDetailMapper;
import com.example.mapper.ClientMapper;
import com.example.mapper.ClientSshMapper;
import com.example.service.ClientService;
import com.example.utils.InfluxDbUtils;
import com.rabbitmq.client.DnsSrvRecordAddressResolver;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ClientServiceImpl extends ServiceImpl<ClientMapper, Client> implements ClientService {

    @Resource
    ClientDetailMapper detailMapper;
    private String registerToken = this.generateNewToken();

    private final Map<Integer, Client> clientIdCache = new ConcurrentHashMap<>();
    private final Map<String, Client> clientTokenCache = new ConcurrentHashMap<>();

    @Resource
    ClientSshMapper sshMapper;



    @Resource
    InfluxDbUtils influx;

    public void initClientCache()
    {
        clientTokenCache.clear();
        clientIdCache.clear();
        this.list().forEach(this::addClientCache);
    }

    @PostConstruct
    public void init() {
        initClientCache();
    }

    @Override
    public Client findClientById(int id) {
        return clientIdCache.get(id);
    }

    @Override
    public Client findClientByToken(String token) {
        return clientTokenCache.get(token);
    }



    @Override
    public String registerToken()
    {
        return registerToken;
    }

    private void addClientCache(Client client){
            clientIdCache.put(client.getId(), client);
            clientTokenCache.put(client.getToken(), client);
    }

    @Override
    public List<ClientSimpleVO> listSimpleList() {
        return clientIdCache.values().stream().map(client -> {
            ClientSimpleVO vo = client.asViewObject(ClientSimpleVO.class);
            String  gpuName = detailMapper.selectById(vo.getId()).getGpuName();
            List<Map<String,String>> gpuNameList
                    = JSON.parseObject(gpuName, new TypeReference<List<Map<String, String>>>(){});
            vo.setGpuName(gpuNameList);
            BeanUtils.copyProperties(detailMapper.selectById(vo.getId()), vo);
            return vo;
        }).toList();
    }
    @Override
    public boolean verifyAndRegister(String token)
    {
        if(this.registerToken.equals(token))
        {
            int id = this.randomClientId();
            Client client = new Client(id, "未命名主机", token , new Date(),"未命名主机","cn");
            if(this.save(client))
            {
                registerToken = this.generateNewToken();
                this.addClientCache(client);
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateClientDetail(ClientDetailVO vo, Client client) {
        String gpuName = JSON.toJSONString(vo.getGpuName());
        ClientDetail detail = new ClientDetail();
        BeanUtils.copyProperties(vo, detail);
        detail.setId(client.getId());
        detail.setGpuName(gpuName);
        if(Objects.nonNull(detailMapper.selectById(client.getId())) )
        {
            detailMapper.updateById(detail);

        }
        else {
            detailMapper.insert(detail);
        }
    }

    private final Map<Integer, RuntimeDetailVO> currentRuntime = new ConcurrentHashMap<>();
    private final Map<Integer,Map<String, GpuRuntimeDetailVO>> currentGpuRuntime = new ConcurrentHashMap<>();

    @Override
    public void updateRuntimeDetail(RuntimeDetailVO vo, Client client) {
        currentRuntime.put(client.getId(), vo);
        influx.writeRuntimeData(client.getId(), vo);
    }

    @Override
    public void updateGpuRuntimeDetail(List<GpuRuntimeDetailVO> voList, Client client) {

        for(GpuRuntimeDetailVO vo : voList)
        {
            currentGpuRuntime.computeIfAbsent(client.getId(), k -> new ConcurrentHashMap<>())
                    .put(vo.getUuid(), vo);
            influx.writeGpuRuntimeData(client.getId(), vo);
        }
    }
    private boolean isOnline(RuntimeDetailVO runtime) {
        return runtime != null && System.currentTimeMillis() - runtime.getTimestamp() < 60 * 1000;
    }

    @Override
    public List<ClientPreviewVO> listClients() {
        return clientIdCache.values().stream().map(client -> {
            ClientPreviewVO vo = client.asViewObject(ClientPreviewVO.class);
            String  gpuName = detailMapper.selectById(vo.getId()).getGpuName();
            List<Map<String,String>> gpuNameList
                    = JSON.parseObject(gpuName, new TypeReference<List<Map<String, String>>>(){});
            BeanUtils.copyProperties(detailMapper.selectById(vo.getId()), vo);
            vo.setGpuName(gpuNameList);
            RuntimeDetailVO runtime = currentRuntime.get(client.getId());
            Map<String, GpuRuntimeDetailVO> gpuRuntime = currentGpuRuntime.get(client.getId());
            if(this.isOnline(runtime) ) {
                BeanUtils.copyProperties(runtime, vo);
                vo.setRuntimeGpuDataList(gpuRuntime);
                vo.setOnline(true);
            }
            return vo;
        }).toList();
    }

    @Override
    public ClientDetailsVO ClientDetails(int clientId) {

        ClientDetailsVO vo = this.clientIdCache.get(clientId).asViewObject(ClientDetailsVO.class);
        BeanUtils.copyProperties(detailMapper.selectById(clientId), vo);
        RuntimeDetailVO runtime = currentRuntime.get(clientId);
        Map<String, GpuRuntimeDetailVO> gpuRuntime = currentGpuRuntime.get(clientId);
        if(this.isOnline(runtime) ) {
            BeanUtils.copyProperties(runtime, vo);
            vo.setRuntimeGpuDataList(gpuRuntime);
            vo.setOnline(true);
            }
        return vo;

    }

    @Override
    public void renameClient(RenameClientVO vo) {
        this.update(Wrappers.<Client>update().eq("id", vo.getId()).set("name", vo.getName()));
        this.initClientCache();
    }

    @Override
    public void renameNode(RenameNodeVO vo) {
        this.update(Wrappers.<Client>update().eq("id", vo.getId())
                .set("node", vo.getNode()).set("location", vo.getLocation()));
        this.initClientCache();
    }

    @Override
    public RuntimeHistoryDetailsVO clientRuntimeDetailsHistory(int clientId) {
        RuntimeHistoryDetailsVO vo = influx.readRuntimeData(clientId);
        ClientDetail detail = detailMapper.selectById(clientId);
        BeanUtils.copyProperties(detail, vo);
        return vo;
    }

    @Override
    public RuntimeHistoryDetailsVO clientGPURuntimeDetailsHistory(int clientId) {
        RuntimeHistoryDetailsVO vo = influx.readGpuRuntimeData(clientId);
        ClientDetail detail = detailMapper.selectById(clientId);
        BeanUtils.copyProperties(detail, vo);
        return vo;
    }

    @Override
    public void deleteClient(int clientId) {
        this.removeById(clientId);
        detailMapper.deleteById(clientId);
        this.initClientCache();
        currentRuntime.remove(clientId);
        currentGpuRuntime.remove(clientId);
    }


    @Override
    public void saveClientSshConnection(SshConnectionVO vo) {
        Client client = clientIdCache.get(vo.getId());
        if(client == null) return;
        ClientSsh ssh = new ClientSsh();
        BeanUtils.copyProperties(vo, ssh);
        if(Objects.nonNull(sshMapper.selectById(client.getId()))) {
            sshMapper.updateById(ssh);
        } else {
            sshMapper.insert(ssh);
        }
    }

    @Override
    public SshSettingsVO sshSettings(int clientId) {
        ClientDetail detail = detailMapper.selectById(clientId);
        ClientSsh ssh = sshMapper.selectById(clientId);
        SshSettingsVO vo;
        if(ssh == null) {
            vo = new SshSettingsVO();
        } else {
            vo = ssh.asViewObject(SshSettingsVO.class);
        }
        vo.setIp(detail.getIp());
        return vo;
    }


    private int randomClientId(){
        return new Random().nextInt(89999999)+10000000;
    }

    private String generateNewToken()
    {
        String CHARACTES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(24);
        for(int i = 0 ; i < 25 ; i++)
        {
            sb.append(CHARACTES.charAt(random.nextInt(CHARACTES.length())));
        }
        log.info("token:{}",sb.toString());
        return sb.toString();
    }
}
