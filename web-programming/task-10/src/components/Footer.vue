<template>
    <footer>
      <div>Total number of registered users: {{ usersNumber }}</div>
      <div>Total number of published posts: {{ postsNumber }}</div>
      <a href="/">Codeforces</a> &copy; 2021 by Mike Mirzayanov
    </footer>
</template>

<script>
export default {
    name: "Footer",
    props: ["usersNumber", "postsNumber"]
}
</script>

<style scoped>

</style>