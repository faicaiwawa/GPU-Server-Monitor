import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from "axios";
import ElementUI from 'element-plus'
import 'element-plus'
import '@/assets/css/element.less'
import {createPinia} from "pinia";
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
axios.defaults.baseURL = 'http://localhost:8080'

const app = createApp(App)

app.use(router)
const pinia = createPinia()
app.use(pinia)



pinia.use(piniaPluginPersistedstate)
app.mount('#app')
