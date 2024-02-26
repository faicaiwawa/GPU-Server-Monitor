<script setup>
import {copyIp, fitByUnit, osNameToIcon, percentageToStatus, rename} from '@/tools'
const props = defineProps({
  data: Object,
  update: Function
})
</script>
<template>
  <div class = "instance-card">
    <div style = "display: flex;justify-content: space-between">
      <div>
        <div class="name">
          <span :class="`flag-icon flag-icon-cn`"></span>
          <span style="margin: 0 5px">{{data.name}}</span>
          <i class="fa-solid fa-pen-to-square interact-item" @click.stop="rename(data.id, data.name, update)"></i>
        </div>
        <div class="os">
         操作系统：
          <i :style="{color: osNameToIcon(data.osName).color}"
             :class="`fa-brands ${osNameToIcon(data.osName).icon}`"></i>
          {{`${data.osName} ${data.osVersion}`}}
        </div>


    </div>
      <div class="status" v-if="data.online">
        <i style="color: #18cb18" class="fa-solid fa-circle-play"></i>
        <span style="margin-left: 5px">运行中</span>
      </div>
      <div class="status" v-else>
        <i style="color: #8a8a8a" class="fa-solid fa-circle-stop"></i>
        <span style="margin-left: 5px">离线</span>
      </div>
    </div>
    <el-divider style="margin: 10px 0"/>

    <div class="network">
      <span style="margin-right: 10px">公网IP：{{data.ip}}</span>
      <i class="fa-solid fa-copy interact-item" @click.stop="copyIp(data.ip)" style="color: dodgerblue"></i>
    </div>

    <div class="cpu">
      <span style="margin-right: 10px">处理器：{{data.cpuName}}</span>
    </div>

    <div class="hardware">
      <i class="fa-solid fa-microchip"></i>
      <span style="margin-right: 10px">{{` ${data.cpuCore} CPU`}}</span>
      <i class="fa-solid fa-memory"></i>
      <span>{{` ${data.memory.toFixed(1)} GB`}}</span>
    </div>

    <div class="progress">
      <span>{{`CPU使用率: ${(data.cpuUsage * 100).toFixed(1)}%`}}</span>
    </div>

    <div class="progress">
      <span>内存: <b>{{data.memoryUsage.toFixed(1)}}</b> GB</span>
      <el-progress :status="percentageToStatus(data.memoryUsage/data.memory * 100)"
                   :percentage="data.memoryUsage/data.memory * 100" :stroke-width="5" :show-text="false"/>
    </div>



    <div v-if="data.runtimeGpuDataList">
      <div v-for="(gpuData, gpuId,index) in data.runtimeGpuDataList" :key="gpuId">
        <div class="divider"></div>
        <div class="cpu">
          <span style="margin-right: 10px">显卡{{ index + 1 }}：{{ data.gpuName[index][index + 1] }}</span>
        </div>
        <div class="cpu">
        <span>显卡利用率：{{gpuData.gpuUtil}}</span>
        </div>
        <div class="cpu">
          <span style="margin-right: 10px">显存使用：{{gpuData.gpuUsedMemory}} MiB / {{gpuData.gpuTotalMemory}} MiB</span>
          <span style="margin-right: 10px">  </span>
        </div>
        <div class="progress">
          <el-progress :status="percentageToStatus(gpuData.gpuUsedMemory/gpuData.gpuTotalMemory * 100)"
                       :percentage="gpuData.gpuUsedMemory/gpuData.gpuTotalMemory * 100" :stroke-width="5" :show-text="false"/>
        </div>
      </div>
    </div>
    <div v-else>
      <div class="divider"></div>
      <div class="cpu">
        <div v-for="(gpuData, index) in data.gpuName" :key="index">
          <span style="margin-right: 10px">显卡{{ index + 1 }}：{{ data.gpuName[index][index+1] }}</span>
        </div>
      </div>
    </div>








    <div class="network-flow">
      <div>网络流量</div>
      <div>
        <i class="fa-solid fa-arrow-up"></i>
        <span>{{` ${fitByUnit(data.networkUpload, 'KB')}/s`}}</span>
        <el-divider direction="vertical"/>
        <i class="fa-solid fa-arrow-down"></i>
        <span>{{` ${fitByUnit(data.networkDownload, 'KB')}/s`}}</span>
      </div>
    </div>


  </div>
</template>




<style scoped>
.dark .instance-card { color: #d9d9d9 }
.interact-item {
  transition: .3s;

  &:hover {
    cursor: pointer;
    scale: 1.1;
    opacity: 0.8;
  }
}

.instance-card {
  width: 320px;
  padding: 15px;
  background-color: var(--el-bg-color);
  border-radius: 5px;
  box-sizing: border-box;
  color: #606060;
  transition: .3s;

  &:hover {
    cursor: pointer;
    scale: 1.02;
  }

  .divider {
    margin-top: 15px;
    margin-bottom: 15px;
    border-bottom: 1px solid lightgray;
  }
  .name {
    font-size: 15px;
    font-weight: bold;
  }

  .status {
    font-size: 14px;
  }
  .network {
    font-size: 13px;
  }
  .os {
    font-size: 13px;
    color: grey;
  }

  .network {
    font-size: 13px;
  }

  .hardware {
    margin-top: 5px;
    font-size: 13px;
  }

  .progress {
    margin-top: 5px;
    font-size: 13px;
  }

  .cpu {
    font-size: 13px;
  }
  .gpu {
    font-size: 13px;
    margin-bottom: 5px;
  }

  .network-flow {
    margin-top: 10px;
    font-size: 12px;
    display: flex;
    justify-content: space-between;
  }
}
</style>