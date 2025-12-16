<script setup lang="ts">
import topbar from './components/topbar.vue';
import ontice from './components/onotice.vue';
import mcontent from './components/mcontent.vue';
import userInfo from './components/userInfo.vue';
import ucontent from './components/ucontent.vue';
import utitle from './components/utitle.vue';
import bcontent from './components/bcontent.vue';
</script>

<template>
    <div class="main">
        <topbar/>
        <ontice/>
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

.a {
    width: 28%;
}

.b {
    position: absolute;
    width: 100%;
    bottom: 0;
}
</style>