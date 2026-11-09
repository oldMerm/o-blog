<template>
    <div class="notfound-main">
        <h1>404 Not Found</h1>
        <p>页面跑丢了，此处<RouterLink to="/">返回主页</RouterLink></p>
    </div>
</template>

<style scoped>
.notfound-main {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 1.5rem;
    min-height: 100vh;
    padding: 2rem;
    text-align: center;
    color: #111;
    background: #fff;
    font-family: var(--font-sans);
}

.notfound-main h1 {
    font-family: var(--font-mono);
    font-size: clamp(2rem, 4vw, 3.25rem);
    font-weight: 500;
    line-height: 1.1;
    letter-spacing: 0.04em;
}

.notfound-main p {
    font-size: 1rem;
    letter-spacing: 0.08em;
    color: #555;
}

.notfound-main a {
    margin-left: 0.35em;
    color: #111;
    border-bottom: 1px solid currentColor;
    padding-bottom: 1px;
    transition: opacity 0.2s ease;
}

.notfound-main a:hover {
    opacity: 0.5;
}
</style>
