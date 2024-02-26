<script setup>
import {computed, reactive, watch} from "vue";
import {get, post} from "@/net";
import {copyIp, cpuNameToImage, fitByUnit, osNameToIcon, percentageToStatus, rename} from "@/tools";
import {ElMessage, ElMessageBox} from "element-plus";
import {Connection, Delete} from "@element-plus/icons-vue";
import RuntimeHistory from "@/Component/RuntimeHistory.vue";

const locations = [
  {name: 'cn', desc: '中国大陆'},
  {name: 'hk', desc: '香港'},
  {name: 'jp', desc: '日本'},
  {name: 'us', desc: '美国'},
  {name: 'sg', desc: '新加坡'},
  {name: 'kr', desc: '韩国'},
  {name: 'de', desc: '德国'}
]

const props = defineProps({
  id: Number,
  update: Function
})
const emits = defineEmits(['delete', 'terminal'])

const details = reactive({
  base: {},
  runtime: {
    list: []
  },
  gpuRuntime: {
    list: []
  },
  editNode: false
})
const nodeEdit = reactive({
  name: '',
  location: ''
})
const enableNodeEdit = () => {
  details.editNode = true
  nodeEdit.name = details.base.node
  nodeEdit.location = details.base.location
}
const submitNodeEdit = () => {
  post('/api/monitor/node', {
    id: props.id,
    node: nodeEdit.name,
    location: nodeEdit.location
  }, () => {
    details.editNode = false
    updateDetails()
    ElMessage.success('节点信息已更新')
  })
}

function deleteClient() {
  ElMessageBox.confirm('删除此主机后所有统计数据都将丢失，您确定要这样做吗？', '删除主机', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    get(`/api/monitor/deleteClient?clientId=${props.id}`, () => {
      emits('delete')
      props.update();
      ElMessage.success('主机已成功移除')
    })
  }).catch(() => {})
}

function updateDetails() {
  props.update()
}





const now = computed(() => details.runtime.list[details.runtime.list.length - 1])
setInterval(()=> {
  if(props.id!=-1 && details.runtime){
    get(`/api/monitor/runtime-history?clientId=${props.id}`,data=>{
      if(details.runtime.list.length >-360)
        details.runtime.list.splice(details.runtime.list.length - 1)
      details.runtime.list.unshift(data)
    })
    get(`/api/monitor/gpuRuntime-history?clientId=${props.id}`,data=>{
      details.gpuRuntime.list.splice(details.gpuRuntime.list.length - 1)
      details.gpuRuntime.list.unshift(data)
    })
    get(`/api/monitor/clientDetails?clientId=${props.id}`,data =>Object.assign(details.base ,data))
  }
},10000)


watch(() => props.id, value => {
  if(value !== -1){
    details.base={}
    details.runtime={ list:[]}
    details.gpuRuntime={ list:[]}
    get(`/api/monitor/clientDetails?clientId=${props.id}`,data =>Object.assign(details.base ,data))
    get(`/api/monitor/runtime-history?clientId=${props.id}`,data =>Object.assign(details.runtime ,data))
    get(`/api/monitor/gpuRuntime-history?clientId=${props.id}`,data =>Object.assign(details.gpuRuntime ,data))
  }
},{immediate:true})

</script>

<template>
  <el-scrollbar>
    <div class="client-details" v-loading="Object.keys(details.base).length === 0">
      <div v-if="Object.keys(details.base).length">
        <div style="display: flex;justify-content: space-between">

          <div class="title">
            <i class="fa-solid fa-server"></i>
            服务器信息
          </div>
          <div>
            <el-button :icon="Connection" type="info"
                       @click="emits('terminal', id)" plain text>SSH远程连接</el-button>
            <el-button :icon="Delete" type="danger" style="margin-left: 0"
                       @click="deleteClient" plain text>删除此主机</el-button>
          </div>
        </div>
        <el-divider style="margin: 10px 0"/>
        <div class="details-list">
          <div>
            <span>服务器ID</span>
            <span>{{details.base.id}}</span>
          </div>
          <div>
            <span>服务器名称</span>
            <span>{{details.base.name}}</span>&nbsp;
            <i @click.stop="rename(details.base.id, details.base.name, updateDetails)"
               class="fa-solid fa-pen-to-square interact-item"/>
          </div>
          <div>
            <span>运行状态</span>
            <span>
            <i style="color: #18cb18" class="fa-solid fa-circle-play" v-if="details.base.online"></i>
            <i style="color: #8a8a8a" class="fa-solid fa-circle-stop" v-else></i>
            {{details.base.online ? '运行中' : '离线'}}
          </span>
          </div>
          <div>
            <span>公网IP地址</span>
            <span>
            {{details.base.ip}}
            <i class="fa-solid fa-copy interact-item" style="color: dodgerblue" @click.stop="copyIp(details.base.ip)"></i>
          </span>
          </div>
          <div style="display: flex">
            <span>处理器</span>
            <span>{{details.base.cpuName}}</span>

          </div>
          <div>
            <span>CPU / 内存</span>
            <span>
            <i class="fa-solid fa-microchip"></i>
            <span style="margin-right: 10px">{{` ${details.base.cpuCore} CPU 核心数 /`}}</span>
            <i class="fa-solid fa-memory"></i>
            <span>{{` ${details.base.memory.toFixed(1)} GB 内存容量`}}</span>
          </span>
          </div>
          <div>
            <span>硬盘</span>
            <span>
            <i class="fa-solid fa-disk"></i>
            <span style="margin-right: 10px">{{` ${(details.base.disk).toFixed(1)} GB 存储 `}}</span>

          </span>
          </div>

          <div>
            <span>操作系统</span>
            <i :style="{color: osNameToIcon(details.base.osName).color}"
               :class="`fa-brands ${osNameToIcon(details.base.osName).icon}`"></i>
            <span style="margin-left: 10px">{{`${details.base.osName} ${details.base.osVersion}`}}</span>
          </div>
          <div>
            <span>显卡驱动版本</span>
            <span>{{details.base.gpuDriverVersion}} </span>&nbsp;
          </div>
          <div>
            <span>CUDA版本</span>
            <span> {{details.base.cudaVersion}}</span>&nbsp;
          </div>


          <div>
            <div v-for="(gpuData, gpuId,index) in details.base.runtimeGpuDataList" :key="gpuId">
              <el-divider></el-divider>
              <div>
              <span>显卡{{ index + 1 }}型号</span>
              <span>{{ gpuData.gpuName }}</span>
            </div>
              <div>
                <span>显卡UUID</span>
                <span>{{ gpuData.uuid }}</span>
              </div>
              <div>
                <span>显卡显存</span>
                <span>{{ gpuData.gpuTotalMemory }} MiB</span>
              </div>
              <div>
                <span>显卡功率</span>
                <span>  {{ gpuData.gpuMaxPower }}</span>
              </div>
            </div>
          </div>

        </div>
        <div class="title" style="margin-top: 20px">
          <i class="fa-solid fa-gauge-high"></i>
          实时监控
        </div>
        <el-divider style="margin: 10px 0"/>
        <div v-if="details.base.online" style="min-height: 200px">
          <div style="display: flex" v-if="details.base.runtimeGpuDataList">
            <el-progress style="margin-left: 20px" type="dashboard" :width="100"
                         :percentage="details.base.memoryUsage / details.base.memory * 100"
                         :status="percentageToStatus(details.base.memoryUsage / details.base.memory * 100)">
              <div style="font-size: 12px;font-weight: bold;color: initial">内存</div>
              <div style="font-size: 13px;color: grey;margin-top: 5px">{{ (details.base.memoryUsage).toFixed(1) }} GB</div>
            </el-progress>
            <el-progress style="margin-left: 20px" type="dashboard" :width="100" :percentage="details.base.cpuUsage * 100"
                         :status="percentageToStatus(details.base.cpuUsage * 100)">
              <div style="font-size: 12px;font-weight: bold;color: initial">CPU利用率</div>
              <div style="font-size: 13px;color: grey;margin-top: 5px">{{ (details.base.cpuUsage * 100).toFixed(1) }}%</div>
            </el-progress>
            <el-progress style="margin-left: 20px" type="dashboard" :width="100"
                         :percentage="(details.base.diskUsage / details.base.disk * 100)"
                         :status="percentageToStatus(details.base.diskUsage / details.base.disk *100 )">
              <div style="font-size: 12px;font-weight: bold;color: initial">硬盘</div>
              <div style="font-size: 13px;color: grey;margin-top: 5px">{{ (details.base.diskUsage).toFixed(1) }} GB</div>
            </el-progress>
          </div>
          <div v-for="(gpuData, gpuId,index) in details.base.runtimeGpuDataList" :key="gpuId">
              <el-divider style="margin: 10px 0"/>
              <el-progress style="margin-left: 20px" type="dashboard" :width="100"
                           :percentage="gpuData.gpuCurrentPower / gpuData.gpuMaxPower"
                           :status="percentageToStatus(gpuData.gpuCurrentPower / gpuData.gpuMaxPower * 100 )">
                <div style="font-size: 12px;font-weight: bold;color: initial">显卡{{ index + 1 }}功率</div>
                <div style="font-size: 13px;color: grey;margin-top: 5px">{{ (gpuData.gpuCurrentPower) }} W </div>
              </el-progress>


              <el-progress style="margin-left: 20px" type="dashboard" :width="100"
                           :percentage="gpuData.gpuUsedMemory / gpuData.gpuTotalMemory * 100"
                           :status="percentageToStatus(gpuData.gpuUsedMemory / gpuData.gpuTotalMemory * 100)">
                <div style="font-size: 12px;font-weight: bold;color: initial">显卡{{ index + 1 }}显存</div>
                <div style="font-size: 13px;color: grey;margin-top: 5px">{{ (gpuData.gpuUsedMemory) }} MiB  </div>
              </el-progress>

              <el-progress style="margin-left: 20px" type="dashboard" :width="100" :percentage="details.base.cpuUsage * 100"
                           :status="percentageToStatus(gpuData.gpuUtil)">
                <div style="font-size: 12px;font-weight: bold;color: initial">GPU利用率</div>
                <div style="font-size: 13px;color: grey;margin-top: 5px">{{ (gpuData.gpuUtil).toFixed(1) }}%</div>
              </el-progress>
            </div>
          <el-divider style="margin: 10px 0"/>
          <runtime-history style="margin-top: 20px" :data="details.runtime.list"/>
        </div>
        <el-empty description="服务器处于离线状态" v-else/>
        </div>

      </div>
  </el-scrollbar>
</template>

<style scoped>
.interact-item {
  transition: .3s;

  &:hover {
    cursor: pointer;
    scale: 1.1;
    opacity: 0.8;
  }
}

.client-details {
  height: 100%;
  padding: 20px;

  .title {
    color: dodgerblue;
    font-size: 18px;
    font-weight: bold;
  }

  .details-list {
    font-size: 14px;

    & div {
      margin-bottom: 10px;

      & span:first-child {
        color: gray;
        font-size: 13px;
        font-weight: normal;
        width: 120px;
        display: inline-block;
      }

      & span {
        font-weight: bold;
      }
    }
  }
}
</style>