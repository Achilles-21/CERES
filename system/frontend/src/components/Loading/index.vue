<template>
  <div class="w-full h-[100vh] bg-[#131313] flex justify-center items-center">

<div class="loader"></div>

  </div>
</template>

<script setup>
</script>

<style scoped>
.loader {
  display: inline-flex;
  gap: 10px;
}
.loader:before,
.loader:after {
  content: "";
  height:120px;
  aspect-ratio: 1;
  border-radius: 50%;
  background:
    radial-gradient(farthest-side,#000 95%,#0000) 50%/16px 16px no-repeat
    #fff;
  aspect-ratio: 1;
  animation: l10 1.5s infinite alternate;
}
.loader:after {
  --s:-1;
}
@keyframes l10 {
  0% ,20% {transform:scaleX(var(--s,1)) rotate(0deg) ;clip-path:inset(0)}
  60%,100%{transform:scaleX(var(--s,1)) rotate(30deg);clip-path:inset(40% 0 0)}
}
</style>
