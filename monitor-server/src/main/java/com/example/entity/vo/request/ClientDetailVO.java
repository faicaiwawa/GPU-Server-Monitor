package com.example.entity.vo.request;

import com.alibaba.fastjson2.JSONArray;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ClientDetailVO {  //these details are used for saving in database
    @NotNull
    String osArch;
    @NotNull
    String osName;
    @NotNull
    String osVersion;
    @NotNull
    int osBit;
    @NotNull
    int cpuCore;
    @NotNull
    double memory;
    @NotNull
    double disk;
    @NotNull
    String ip;
    @NotNull
    String cpuName;
    @NotNull
    String gpuDriverVersion;
    @NotNull
    String cudaVersion;
    @NotNull
    List<Map<String,String>> gpuName;


}
