package com.example.entity.vo.response;

import com.example.entity.dto.RuntimeGpuData;
import com.example.entity.vo.request.GpuRuntimeDetailVO;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ClientPreviewVO {  //these details are used for card_data
    int id;
    String name;
    boolean online;
    String node;
    String location;
    String ip;
    String cpuName;
    String osName;
    String osVersion;
    String cudaVersion;
    String gpuDriverVersion;
    double memory;
    double disk;
    int cpuCore;
    double cpuUsage;
    double memoryUsage;
    double networkUpload;
    double networkDownload;
    double diskRead;
    double diskWrite;
    List<Map<String,String>> gpuName;
    Map<String, GpuRuntimeDetailVO> runtimeGpuDataList;

}
