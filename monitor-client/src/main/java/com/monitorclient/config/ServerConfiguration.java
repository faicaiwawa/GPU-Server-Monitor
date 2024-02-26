package com.monitorclient.config;

import com.alibaba.fastjson2.JSONObject;
import com.monitorclient.entity.Response;
import com.monitorclient.utils.MonitorUtils;
import com.monitorclient.utils.NetUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import com.monitorclient.entity.ConnectionConfig;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.geom.RectangularShape;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Slf4j
@Configuration
public class ServerConfiguration implements ApplicationRunner {
    @Resource
    NetUtils net;

    @Resource
    MonitorUtils monitor;

    @Bean
    ConnectionConfig connectionConfig()
    {
        log.info("正在加载服务端连接配置......")  ;
        ConnectionConfig config = this.readConfigurationFromFile();
        if(config == null)
            config = this.registerToServer();
        System.out.println(monitor.monitorBaseDetail());
        return config;
    }

    private ConnectionConfig registerToServer()
    {
        Scanner scanner = new Scanner(System.in);
        String token, address;
        do{
            log.info("请输入需要注册的服务端访问地址，例如'192.168.9.2:8080': ");
            address = scanner.nextLine();
            log.info("请输入管理员提供的token密钥: ");
            token = scanner.nextLine();
        }while(!net.registerToServer(address, token));

        ConnectionConfig config = new ConnectionConfig(address, token);
        this.saveConfigurationToFile(config);
        return config;
    }



    private void saveConfigurationToFile(ConnectionConfig config){
        File dir = new File("config");
        if(!dir.exists() && dir.mkdir()){
            log.info("创建用于保存服务端连接信息的目录已完成");
        }
        File file = new File("config/server.json");
        try(FileWriter writer = new FileWriter(file))
        {
            writer.write(JSONObject.from(config).toJSONString());
        }catch (IOException e)
        {
            log.error("保存配置文件时出现问题", e);
        }
        log.info("服务端链接信息已保存");
    }


    private ConnectionConfig readConfigurationFromFile()
    {
        File configurationFile = new File("config/server.json");
        if(configurationFile.exists()){
            try(FileInputStream stream = new FileInputStream(configurationFile))
            {
                String raw = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                return JSONObject.parseObject(raw).to(ConnectionConfig.class);
            }
            catch (IOException e)
            {
                log.error("配置文件出错", e);
            }
        }
        return null;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("正在向服务端更新基本信息....");
        net.updateBaseDetails(monitor.monitorBaseDetail());
    }
}
