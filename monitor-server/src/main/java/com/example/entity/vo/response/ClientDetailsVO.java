package com.example.entity.vo.response;

import com.example.entity.vo.request.GpuRuntimeDetailVO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class ClientDetailsVO {
    int id;
    String name ;
    String ip;
    boolean online;
    double diskRead;
    double diskWrite;
    String cpuName;
    String osName;
    String osVersion;
    String cudaVersion;
    String gpuDriverVersion;
    double memory;
    int cpuCore;
    double cpuUsage;
    double memoryUsage;
    double networkUpload;
    double networkDownload;
    double diskUsage;
    double disk;
    Map<String, GpuRuntimeDetailVO> runtimeGpuDataList;
}
