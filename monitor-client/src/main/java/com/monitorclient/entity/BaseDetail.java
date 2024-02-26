package com.monitorclient.entity;

import com.alibaba.fastjson2.JSONArray;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class BaseDetail {
    String osArch;
    String osName;
    String osVersion;
    int osBit;
    String cpuName;
    int cpuCore;
    double memory;
    double disk;
    String ip;
    String gpuDriverVersion ;
    String cudaVersion ;
    List<Map<String, String>> gpuName;

}
