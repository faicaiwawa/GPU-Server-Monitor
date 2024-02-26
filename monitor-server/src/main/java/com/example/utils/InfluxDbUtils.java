package com.example.utils;

import com.alibaba.fastjson2.JSONObject;
import com.example.entity.dto.RuntimeData;
import com.example.entity.dto.RuntimeGpuData;
import com.example.entity.vo.request.GpuRuntimeDetailVO;
import com.example.entity.vo.request.RuntimeDetailVO;
import com.example.entity.vo.request.RuntimeHistoryDetailsVO;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class InfluxDbUtils {
    private InfluxDBClient client;
    @Value("${spring.influx.url}")
    String url;
    @Value("${spring.influx.user}")
    String user;
    @Value("${spring.influx.password}")
    String password;

    String token = System.getenv("INFLUX_TOKEN");
    String bucket = "monitor";
    String org = "ocean";




    @PostConstruct
    public void  init()
    {
        client = InfluxDBClientFactory.create(url, user, password.toCharArray());
    }

    public void writeRuntimeData(int clientId, RuntimeDetailVO vo)
    {
        RuntimeData data = new RuntimeData();
        BeanUtils.copyProperties(vo, data);
        data.setTimestamp(new Date(vo.getTimestamp()).toInstant());
        data.setClientId(clientId);

        WriteApiBlocking writeApi = client.getWriteApiBlocking();
        writeApi.writeMeasurement(bucket, org, WritePrecision.NS, data);
    }

    public void writeGpuRuntimeData(int clientId, GpuRuntimeDetailVO vo)
    {
        RuntimeGpuData data = new RuntimeGpuData();
        BeanUtils.copyProperties(vo, data);
        data.setTimestamp(new Date(vo.getTimestamp()).toInstant());
        data.setClientId(clientId);
        WriteApiBlocking writeApi = client.getWriteApiBlocking();
        writeApi.writeMeasurement(bucket, org, WritePrecision.NS, data);
    }

    public RuntimeHistoryDetailsVO readRuntimeData(int clientId) {
        RuntimeHistoryDetailsVO vo = new RuntimeHistoryDetailsVO();
        String query = """
                from(bucket: "%s")
                |> range(start: %s)
                |> filter(fn: (r) => r["_measurement"] == "runtime")
                |> filter(fn: (r) => r["clientId"] == "%s")
                """;
        String format = String.format(query, bucket, "-1h", clientId);
        List<FluxTable> tables = client.getQueryApi().query(format, org);
        int size = tables.size();
        if (size == 0) return vo;
        List<FluxRecord> records = tables.get(0).getRecords();
        for (int i = 0; i < records.size(); i++) {
            JSONObject object = new JSONObject();
            object.put("timestamp", records.get(i).getTime());
            for (int j = 0; j < size; j++) {
                FluxRecord record = tables.get(j).getRecords().get(i);
                object.put(record.getField(), record.getValue());
            }
            vo.getList().add(object);
        }
        return vo;
    }

    public RuntimeHistoryDetailsVO readGpuRuntimeData(int clientId) {
        RuntimeHistoryDetailsVO vo = new RuntimeHistoryDetailsVO();
        String query = """
                from(bucket: "%s")
                |> range(start: %s)
                |> filter(fn: (r) => r["_measurement"] == "gpuRuntime")
                |> filter(fn: (r) => r["clientId"] == "%s")
                """;
        String format = String.format(query, bucket, "-1h", clientId);
        List<FluxTable> tables = client.getQueryApi().query(format, org);
        int size = tables.size();
        if (size == 0) return vo;
        List<FluxRecord> records = tables.get(0).getRecords();
        for (int i = 0; i < records.size(); i++) {
            JSONObject object = new JSONObject();
            object.put("timestamp", records.get(i).getTime());
            for (int j = 0; j < size; j++) {
                FluxRecord record = tables.get(j).getRecords().get(i);
                object.put(record.getField(), record.getValue());
            }
            vo.getList().add(object);
        }
        return vo;
    }
}
