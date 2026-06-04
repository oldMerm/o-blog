<script setup lang="ts">
const githubUrl = 'https://github.com/oldMerm'
</script>

<template>
  <header class="topbar">
    <span class="brand">oldmerman</span>
    <a
      :href="githubUrl"
      target="_blank"
      rel="noopener noreferrer"
      class="github-link"
      aria-label="GitHub"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="22"
        height="22"
        viewBox="0 0 16 16"
        fill="rgb(46,46,46)"
      >
        <path
          d="M8 0c4.42 0 8 3.58 8 8a8.013 8.013 0 0 1-5.45 7.59c-.4.08-.55-.17-.55-.38 0-.27.01-1.13.01-2.2 0-.75-.25-1.23-.54-1.48 1.78-.2 3.65-.88 3.65-3.95 0-.88-.31-1.59-.82-2.15.08-.2.36-1.02-.08-2.12 0 0-.67-.22-2.2.82-.64-.18-1.32-.27-2-.27-.68 0-1.36.09-2 .27-1.53-1.03-2.2-.82-2.2-.82-.44 1.1-.16 1.92-.08 2.12-.51.56-.82 1.28-.82 2.15 0 3.06 1.86 3.75 3.64 3.95-.23.2-.44.55-.51 1.07-.46.21-1.61.55-2.33-.66-.15-.24-.6-.83-1.23-.82-.67.01-.27.38.01.53.34.19.73.9.82 1.13.16.45.68 1.31 2.69.94 0 .67.01 1.3.01 1.49 0 .21-.15.45-.55.38A7.995 7.995 0 0 1 0 8c0-4.42 3.58-8 8-8Z"
        />
      </svg>
    </a>
  </header>
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:ital@1&display=swap');
</style>

<style scoped>
.topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: rgb(235, 235, 235);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.brand {
  font-size: 1.25rem;
  font-weight: 700;
  color: rgb(46, 46, 46);
  letter-spacing: 0.04em;
  font-family: 'Playfair Display', 'Times New Roman', serif;
  font-style: italic;
}

.github-link {
  display: flex;
  align-items: center;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.github-link:active {
  opacity: 1;
}
</style>
