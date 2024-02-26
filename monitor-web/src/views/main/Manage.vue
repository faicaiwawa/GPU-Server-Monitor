<script setup>

import PreviewCard from "@/Component/PreviewCard.vue";
import {computed, reactive, ref} from "vue";
import {get} from "@/net";
import ClientDetails from "@/Component/ClientDetails.vue";
import RegisterCard from "@/component/RegisterCard.vue";
import {useStore} from "@/store";
import {Plus} from "@element-plus/icons-vue";
import TerminalWindow from "@/component/TerminalWindow.vue";
const list = ref([])
const store = useStore()

const updateLit = () => get('/api/monitor/list', data =>{
    list.value = data
    console.info(data)
})



setInterval(updateLit, 10000)
updateLit()

const detail = reactive({
  show:false,
  id:-1
})

const displayClientDetails = (id) => {
  detail.show = true
  detail.id = id
}

function openTerminal(id) {
  terminal.show = true
  terminal.id = id
  detail.show = false
}

const register =reactive({
  show:false,
  token:''
})

const terminal = reactive({
  show: false,
  id: -1
})

const refreshToken = () =>get('/api/monitor/register-token', token => register.token = token)

</script>

<template>

  <div class="manage-main">
    <div style="display: flex;justify-content: space-between;align-items: end">
        <div>
          <div class="title"><i class="fa-solid fa-server"></i> 管理主机列表</div>
          <div class="desc">  在这里管理所有已经注册的服务器实例，实时监控服务器运行状态，快速进行管理和操作。</div>
        </div>
        <div>
            <div>
              <el-button :icon="Plus" type="primary" plain
                         @click="register.show = true" v-if="store.isAdmin">添加新主机</el-button>
            </div>
        </div>
    </div>

        <el-divider style="margin: 10px 0"/>
        <div class="card-list">
          <preview-card  v-for="item in list" :data="item" :update="updateList" @click="displayClientDetails(item.id)"/>
        </div>
        <el-drawer size="520" :show-close="false" v-model="detail.show"
                   :with-header="false" v-if = "list.length" @close = "detail.id = -1">
            <client-details :id="detail.id" @terminal="openTerminal"/>
          </el-drawer>
        <el-drawer v-model="register.show" direction="btt" :with-header="false"
                   style="width: 600px;margin: 10px auto" size="320" @open="refreshToken">
          <register-card :token="register.token"/>
        </el-drawer>
    <el-drawer style="width: 800px" :size="520" direction="btt"
               @close="terminal.id = -1"
               v-model="terminal.show" :close-on-click-modal="false">
      <template #header>
        <div>
          <div style="font-size: 18px;color: dodgerblue;font-weight: bold;">SSH远程连接</div>
          <div style="font-size: 14px">
            远程连接的建立将由服务端完成，因此在内网环境下也可以正常使用。
          </div>
        </div>
      </template>
      <terminal-window :id="terminal.id"/>
    </el-drawer>

  </div>

</template>

<style scoped>
:deep(.el-drawer__header) {
  margin-bottom: 10px;
}

:deep(.el-checkbox-group .el-checkbox) {
  margin-right: 10px;
}

:deep(.el-drawer) {
  margin: 10px;
  height: calc(100% - 20px);
  border-radius: 10px;
}

:deep(.el-drawer__body) {
  padding: 0;
}

.manage-main {
  margin: 0 50px;

  .title {
    font-size: 22px;
    font-weight: bold;
  }

  .desc {
    font-size: 15px;
    color: grey;
  }
}

.card-list {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}
</style>