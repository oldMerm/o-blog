<script setup lang="ts">
const emit = defineEmits<{
    (e: 'toggle-style'): void;
}>();
</script>

<template>
    <div class="tb">
        <div class="lt">
            <p style="padding-bottom: 5px;">oldmerman</p>
        </div>
        <div class="rb">
            <ul>
                <li>
                    <button class="style-btn" title="切换到像素风格(测试)" @click="emit('toggle-style')">
                        <img src="../../../static/样式.svg" alt="">
                    </button>
                </li>
                <li>
                    <a href="https://github.com/oldMerm">
                        <img src="../../../static/github.svg" width="32" height="32" alt="GitHub">
                    </a>
                </li>
            </ul>
        </div>
    </div>
</template>

<style scoped>
.tb {
    width: 100%;
    height: 8vh;
    font-size: 1.3rem;
    color: rgb(46, 46, 46);
    background-color: rgb(227, 227, 227);
    display: flex;
    align-items: center;
    justify-content: flex-end;
    margin-bottom: 5px;
}

.lt {
    width: 55%;
    font-size: larger;
}

.rb {
    width: 40%;
}

.rb ul {
    list-style: none;
    display: flex;
    justify-content: flex-end;
    align-items: center;
}

.rb li {
    margin-left: 1.5rem;
    width: 5rem;
}

.style-btn {
    width: 5rem;
    height: 2rem;
    border: none;
    background-color: transparent;
    color: rgb(46, 46, 46);
    font-size: 1rem;
    cursor: pointer;
    transition: all 0.2s ease;
}

.style-btn:hover {
    color: #409eff;
    border-color: #409eff;
}
</style>