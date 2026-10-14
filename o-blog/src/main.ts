import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import './style/cover.css'
import 'prismjs/themes/prism-tomorrow.css'

import '@fontsource-variable/inter'
import '@fontsource/jetbrains-mono/400.css'
import '@fontsource/jetbrains-mono/500.css'
import '@fontsource/jetbrains-mono/700.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
