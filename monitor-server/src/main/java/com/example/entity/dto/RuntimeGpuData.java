package com.example.entity.dto;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
@Measurement(name = "gpuRuntime")
public class RuntimeGpuData {
    @Column(tag = true)
    int clientId;
    @Column(timestamp = true)
    Instant timestamp;
    @Column(tag = true)
    String uuid;
    @Column
    int gpuTotalMemory;
    @Column
    int gpuUsedMemory ;
    @Column
    int gpuFreeMemory ;
    @Column
    int gpuMaxPower ;
    @Column
    int gpuCurrentPower ;
    @Column
    int gpuUtil ;
    @Column
    int gpuMemoryUtil;
}


