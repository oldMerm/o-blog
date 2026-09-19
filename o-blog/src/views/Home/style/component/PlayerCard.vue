<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import router from '@/router';
import { httpInstance, type Response } from '@/utils/http';

interface UserInfo {
    username: string;
    article: number;
    like: number;
    attrURL: string;
}

const emit = defineEmits<{
    (e: 'toast', msg: string, icon?: string): void;
}>();

const logged = ref(false);
const username = ref('');
const article = ref(0);
const like = ref(0);
const avatar = ref('');

const hasAvatar = computed(() => !!avatar.value);

onMounted(async () => {
    const token = localStorage.getItem('token');
    const refreshToken = localStorage.getItem('refreshToken');
    if (!token && !refreshToken) return;

    try {
        const res = await httpInstance.get<any, Response>('/usr/info');
        if (res.code !== 200) {
            if (res.code === 401) {
                localStorage.removeItem('token');
                localStorage.removeItem('refreshToken');
            }
            return;
        }
        const data = res.data as UserInfo;
        logged.value = true;
        username.value = data.username;
        article.value = data.article;
        like.value = data.like;
        avatar.value = data.attrURL || '';
    } catch {
        // 未登录或网络异常时保持离线卡片
    }
});

const goLogin = () => {
    router.push({ name: 'login' });
};

const goManage = () => {
    if (!logged.value) {
        emit('toast', '请先登录后再进入创作台！', '🔒');
        return;
    }
    router.push({ name: 'manage' });
};

const showStats = () => {
    emit('toast', '玩家属性已加载：饥饿值 150/150，理智值 200/200！', '🍖');
};
</script>

<template>
    <article class="px-player" title="点击进入创作台" @click="goManage">
        <div>
            <div class="px-player-head">
                <span class="px-player-tag">PLAYER CARD</span>
                <span v-if="logged" class="px-player-status">● LV.99 ONLINE</span>
                <span v-else class="px-player-status offline">● OFFLINE</span>
            </div>

            <div class="px-player-body">
                <div class="px-avatar">
                    <img v-if="hasAvatar" :src="avatar" alt="avatar" />
                    <div v-else class="px-avatar-face">
                        <div class="px-eye left"></div>
                        <div class="px-eye right"></div>
                        <div class="px-pupil left"></div>
                        <div class="px-pupil right"></div>
                        <div class="px-mouth"></div>
                    </div>
                </div>

                <div class="px-player-info">
                    <template v-if="logged">
                        <div class="px-player-name">{{ username }}</div>
                        <div class="px-player-desc">全栈方块工程师 / 沼泽原住民</div>
                        <div class="px-player-tags">
                            <span>JS/TS</span>
                            <span>MC</span>
                            <span>DST</span>
                        </div>
                    </template>
                    <template v-else>
                        <div class="px-player-offline">未检测到玩家登入</div>
                        <button class="px-login-btn" @click.stop="goLogin">请先登录!</button>
                    </template>
                </div>
            </div>
        </div>

        <div class="px-player-foot">
            <div v-if="logged">
                HP: <span class="px-hearts">❤❤❤❤❤</span>
                <span> | 文章 {{ article }} · 点赞 {{ like }}</span>
            </div>
            <div v-else>HP: <span class="px-hearts">❤❤❤❤❤</span></div>
            <button class="px-stats" @click.stop="showStats">STATS →</button>
        </div>
    </article>
</template>
