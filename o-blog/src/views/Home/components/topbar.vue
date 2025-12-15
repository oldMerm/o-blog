<script setup lang="ts">

</script>

<template>
    <div class="tb">
        <div class="lt">
            <p style="padding-bottom: 5px;">oldmerman</p>
        </div>
        <div class="rb">
            <ul>
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
    background-color: rgb(241, 241, 241);
    display: flex;
    align-items: center;
    justify-content: flex-end;
}

.lt {
    width: 55%;
    font-size: larger;
    cursor: pointer
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
</style>