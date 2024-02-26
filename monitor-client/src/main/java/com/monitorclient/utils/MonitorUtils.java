package com.monitorclient.utils;

import com.monitorclient.entity.BaseDetail;
import com.monitorclient.entity.GpuRuntimeDetail;
import com.monitorclient.entity.RuntimeDetail;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.util.*;

@Slf4j
@Component
public class MonitorUtils {

    private final SystemInfo info = new SystemInfo();
    private final Properties properties = System.getProperties();
    public BaseDetail monitorBaseDetail() {
        OperatingSystem os= info.getOperatingSystem();
        HardwareAbstractionLayer hardware = info.getHardware();
        double memory = hardware.getMemory().getTotal() / 1024.0 /1024/1024 ;
        double diskSize = Arrays.stream(File.listRoots()).mapToLong(File::getTotalSpace).sum()/1024.0 /1024/1024;
        String ip = Objects.requireNonNull(this.findNetworkInterface(hardware)).getIPv4addr()[0];
        String cudaVersion ;
        String driverVersion;
        List<Map<String, String>> list = new ArrayList<>();
        try{
            String command = "nvidia-smi -q -x";
            Process process = Runtime.getRuntime().exec(command);
            InputStream inputStream = process.getInputStream();
            String xmlGpu = convertInputStreamToString(inputStream);
            xmlGpu = xmlGpu.replaceAll("<!DOCTYPE.*.dtd\">", "");
            Document document = DocumentHelper.parseText(xmlGpu);
            cudaVersion = document.getRootElement().elementText("cuda_version");
            driverVersion = document.getRootElement().elementText("driver_version");
            List<Element> gpu = document.getRootElement().elements("gpu");
            Integer[] i = {1};
            gpu.forEach(element -> {
                String gpuName = element.element("product_name").getText();
                Map<String, String> data = new HashMap<>();
                data.put(i[0].toString() , gpuName);
                list.add(data);
                i[0]++;
            });
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return new BaseDetail()
                    .setOsArch(properties.getProperty("os.arch"))
                    .setOsName(os.getFamily())
                    .setOsVersion(os.getVersionInfo().getVersion())
                    .setOsBit(os.getBitness())
                    .setCpuName(hardware.getProcessor().getProcessorIdentifier().getName())
                    .setCpuCore(hardware.getProcessor().getPhysicalProcessorCount())
                    .setMemory(memory)
                    .setDisk(diskSize)
                    .setCudaVersion(cudaVersion)
                    .setGpuDriverVersion(driverVersion)
                    .setGpuName(list)
                    .setIp(ip);


    }

    private NetworkIF findNetworkInterface(HardwareAbstractionLayer hardware) {
        try {
            for (NetworkIF network : hardware.getNetworkIFs()) {
                String[] ipv4Addr = network.getIPv4addr();
                NetworkInterface ni = network.queryNetworkInterface();
                if(!ni.isLoopback() && !ni.isPointToPoint() && ni.isUp() && !ni.isVirtual()
                        && (ni.getName().startsWith("eth") || ni.getName().startsWith("en")|| ni.getName().startsWith("wlan"))
                        && ipv4Addr.length > 0) {
                    return network;
                }
            }
        } catch (IOException e) {
            log.error("读取网络接口信息时出错", e);
        }
        return null;
    }


    public RuntimeDetail monitorRuntimeDetail()
    {
        double statisticTime = 10;
        try{
            HardwareAbstractionLayer harware = this.info.getHardware();
            NetworkIF networkInterface =Objects.requireNonNull(this.findNetworkInterface(harware));
              CentralProcessor processor = harware.getProcessor();
              double upload = networkInterface.getBytesSent();
              double download = networkInterface.getBytesRecv();
              double read = harware.getDiskStores().stream().mapToLong(HWDiskStore::getReadBytes).sum();
              double write = harware.getDiskStores().stream().mapToLong(HWDiskStore::getWriteBytes).sum();
              long[] ticks = processor.getSystemCpuLoadTicks();
              Thread.sleep((long )statisticTime * 1000);

            networkInterface =Objects.requireNonNull(this.findNetworkInterface(harware));
            upload = (networkInterface.getBytesSent() - upload) / statisticTime ;
            download = (networkInterface.getBytesRecv() - download) / statisticTime;
            read =( harware.getDiskStores().stream().mapToLong(HWDiskStore::getReadBytes).sum() - read ) /statisticTime;
            write =  (  harware.getDiskStores().stream().mapToLong(HWDiskStore::getWriteBytes).sum() - read ) /statisticTime;

            double memory = (harware.getMemory().getTotal() - harware.getMemory().getAvailable()) /1024.0 /1024 / 1024;
            double disk = Arrays.stream(File.listRoots())
                    .mapToLong(file -> file.getTotalSpace() - file.getFreeSpace()).sum() / 1024.0 / 1024 / 1024;
            return  new RuntimeDetail()
                    .setCpuUsage(this.calculateCpuUsage(processor, ticks))
                    .setMemoryUsage(memory)
                    .setDiskUsage(disk)
                    .setNetworkUpload(upload / 1024)
                    .setNetworkDownload(download / 1024)
                    .setDiskRead(read /1024 / 1024)
                    .setDiskWrite(write /1024 /1024)
                    .setTimestamp(new Date().getTime());

        } catch (Exception e) {
           log.error("读取运行时数据错误");
        }
        return null;
    }

    public List<GpuRuntimeDetail> gpuMonitorRuntimeDetail()
    {
        List<GpuRuntimeDetail> gpuRuntimeDetailsList = new ArrayList<>();
        try{
            String command = "nvidia-smi -q -x";
            Process process = Runtime.getRuntime().exec(command);
            InputStream inputStream = process.getInputStream();
            String xmlGpu = convertInputStreamToString(inputStream);
            xmlGpu = xmlGpu.replaceAll("<!DOCTYPE.*.dtd\">", "");
            Document document = DocumentHelper.parseText(xmlGpu);
            List<Element> gpu = document.getRootElement().elements("gpu");

            gpu.forEach(element -> {
                //名称
                String uuid = element.element("uuid").getText();
                //GPU内存信息
                String gpuName = element.element("product_name").getText();
                Element fb_memory_usage = element.element("fb_memory_usage");
                //总内存
                String totalMemory_ = fb_memory_usage.element("total").getText();
                totalMemory_ = totalMemory_.substring(0, totalMemory_.length() - 4);
                //已用内存
                String usedMemory_  = fb_memory_usage.element("used").getText();
                usedMemory_ = usedMemory_.substring(0, usedMemory_.length() - 4);
                //空闲内存
                String freeMemory_ = fb_memory_usage.element("free").getText();
                freeMemory_ = freeMemory_.substring(0, freeMemory_.length() - 4);

                int freeMemory = Integer.parseInt(freeMemory_);
                int usedMemory = Integer.parseInt(usedMemory_);
                int totalMemory = Integer.parseInt(totalMemory_);
                //Element gpu_power_readings = element.element("gpu_power_readings");//windows
                Element gpu_power_readings = element.element("power_readings");//linux
                //String maxPower_ = gpu_power_readings.element("current_power_limit").getText();//windows
                String maxPower_ = gpu_power_readings.element("power_limit").getText();//linux
                maxPower_ = maxPower_.substring(0, maxPower_.length() - 5);
                int maxPower = Integer.parseInt(maxPower_);
                String currentPower_ = gpu_power_readings.element("power_draw").getText();
                currentPower_ = currentPower_.substring(0, currentPower_.length() - 5);
                int currentPower = Integer.parseInt(currentPower_);
                Element utilization = element.element("utilization");
                
                String gpuUtil_ = utilization.element("gpu_util").getText();
                gpuUtil_ = gpuUtil_.substring(0, gpuUtil_.length() - 2);
                int gpuUtil = Integer.parseInt(gpuUtil_);
                GpuRuntimeDetail gpuRuntimeDetail = new GpuRuntimeDetail()
                        .setTimestamp(new Date().getTime())
                        .setGpuUtil(gpuUtil)
                        .setGpuCurrentPower(currentPower)
                        .setGpuTotalMemory(totalMemory)
                        .setGpuMaxPower(maxPower)
                        .setGpuFreeMemory(freeMemory)
                        .setGpuName(gpuName)
                        .setUuid(uuid)
                        .setGpuUsedMemory(usedMemory);

                gpuRuntimeDetailsList.add(gpuRuntimeDetail);

            });
            return  gpuRuntimeDetailsList;



        } catch (Exception e) {
            System.out.println(e);
            log.error("读取gpu运行时数据错误");
        }
        return null;
    }





    private double calculateCpuUsage(CentralProcessor processor, long[] prevTicks) {
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()]
                - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()]
                - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softIrq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()]
                - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()]
                - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()]
                - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long cUser = ticks[CentralProcessor.TickType.USER.getIndex()]
                - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long ioWait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()]
                - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()]
                - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = cUser + nice + cSys + idle + ioWait + irq + softIrq + steal;
        return (cSys + cUser) * 1.0 / totalCpu;
    }

    private static String convertInputStreamToString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }


}
