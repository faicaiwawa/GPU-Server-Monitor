package com.example.entity.vo.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ClientSimpleVO {
    int id;
    String name;
    String osName;
    String osVersion;
    String ip;
    List<Map<String , String>>  gpuName;
}
