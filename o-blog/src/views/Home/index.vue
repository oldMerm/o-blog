<script setup lang="ts">
import topbar from './components/topbar.vue';
import ontice from './components/onotice.vue';
import mcontent from './components/mcontent.vue';
import userInfo from './components/userInfo.vue';
import ucontent from './components/ucontent.vue';
import utitle from './components/utitle.vue';
import utitle2 from './components/utitle2.vue';
import bcontent from './components/bcontent.vue';
import chatCard from './components/chatCard.vue';
</script>

<template>
    <div class="main">
        <topbar/>
        <div class="c">
            <ontice/>
            <utitle2/>
            <chat-card/>
        </div>
        <mcontent/>
        <div class="a">
            <user-info/>
            <utitle/>
            <ucontent/>
        </div>
        <div class="b">
            <bcontent/>
        </div>
</div>
</template>

<style scoped>
.main {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    align-items: flex-start;
}

.c {
    width: 15%;
    margin: 10px 10px;
}

.a {
    width: 28%;
}

.b {
    position: absolute;
    width: 100%;
    bottom: 0;
}
</style>