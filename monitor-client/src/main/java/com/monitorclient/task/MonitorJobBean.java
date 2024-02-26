package com.monitorclient.task;

import com.monitorclient.entity.GpuRuntimeDetail;
import com.monitorclient.entity.RuntimeDetail;
import com.monitorclient.utils.MonitorUtils;
import com.monitorclient.utils.NetUtils;
import jakarta.annotation.Resource;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MonitorJobBean extends QuartzJobBean {

    @Resource
    MonitorUtils monitor;
    @Resource
    NetUtils net;
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        RuntimeDetail detail = monitor.monitorRuntimeDetail();
        List<GpuRuntimeDetail> GpuRuntimeDetailList = monitor.gpuMonitorRuntimeDetail();
        //System.out.println(detail);
        net.updateRuntimeDetails(detail,GpuRuntimeDetailList);

    }
}
