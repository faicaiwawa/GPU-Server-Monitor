package com.example.entity.vo.request;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GpuRuntimeDetailVO {
    @NotNull
    long timestamp;
    @NotNull
    String gpuName;
    @NotNull
    String uuid;
    @NotNull
    int gpuTotalMemory;
    @NotNull
    int gpuUsedMemory ;
    @NotNull
    int gpuFreeMemory ;
    @NotNull
    int gpuMaxPower ;
    @NotNull
    int gpuCurrentPower ;
    @NotNull
    int gpuUtil ;

}
