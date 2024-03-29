# Digital-Ocean-GPU-Server-Monitoring-System
算力服务器监控系统，系统分三个部分：Server，Client，Web。 Client部署在各个算力服务器上，实时收集显存、内存、读出、写入等状态并上传至Server。Server接收Client上传的数据并存储，同时Server提供系统登录的鉴权和认证，用户限权等。Web端获取Server中存储的数据，并可视化展示。同时Web端提供对应各Client的SSH远程连接，经服务器转发命令到Client，以方便管理员管理。
各部分技术点：
* SpringBoot3作为服务端和客户端基本框架，SpringSecurity作为权限校验框架并集成短信验证码登录，手动整合Jwt校验方案。
* MySQL存储用户和服务器数据，InfluxDB存储服务器的运行历史数据；采用JSCH框架实现远程SSH连接；WebSocket与前端对接实现前端Shell操作；Redis存储的登录/重置操作验证码并进行IP地址限流处理；RabbitMQ积压短信发送任务。
* 使用Swagger作为接口文档自动生成，自动配置登录相关接口；采用过滤器实现对所有请求自动生成雪花ID方便线上定位问题。
* Web端基于Vue3和ElementUI；Xterm.js模拟Terminal窗口，ECharts将历史数据以折线图形式展示。
  
![图片1](pic/fig3.png)

![图片2](pic/fig1.png)

![图片1](pic/fig4.png)

![图片2](pic/fig2.png)
