package com.example.service;

import com.example.entity.dto.Client;
import com.example.entity.vo.request.*;
import com.example.entity.vo.response.ClientDetailsVO;
import com.example.entity.vo.response.ClientPreviewVO;
import com.example.entity.vo.response.ClientSimpleVO;
import com.example.entity.vo.response.SshSettingsVO;

import java.util.List;

public interface ClientService {

    String registerToken();
    Client findClientById(int id);
    Client findClientByToken(String token);

    boolean verifyAndRegister(String token);
    List<ClientSimpleVO> listSimpleList();
    void updateClientDetail(ClientDetailVO vo, Client client);
    void updateRuntimeDetail(RuntimeDetailVO vo, Client client);
    void updateGpuRuntimeDetail(List<GpuRuntimeDetailVO> voList, Client client);
    List<ClientPreviewVO> listClients();
    ClientDetailsVO ClientDetails(int clientId);
    void renameClient(RenameClientVO vo);
    void renameNode(RenameNodeVO vo);
    RuntimeHistoryDetailsVO clientRuntimeDetailsHistory(int clientId);
    RuntimeHistoryDetailsVO clientGPURuntimeDetailsHistory(int clientId);
    void deleteClient(int clientId);

    void saveClientSshConnection(SshConnectionVO vo);
    SshSettingsVO sshSettings(int clientId);

}

