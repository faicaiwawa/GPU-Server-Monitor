package com.monitorclient.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GpuRuntimeDetail {

    long timestamp;

    String gpuName ;

    String uuid;

    int gpuTotalMemory;

    int gpuUsedMemory ;

    int gpuFreeMemory ;

    int gpuMaxPower ;

    int gpuCurrentPower ;

    int gpuUtil ;

}
