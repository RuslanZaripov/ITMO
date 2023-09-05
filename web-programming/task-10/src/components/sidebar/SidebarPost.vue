<template>
    <section>
        <div class="header">
            {{ post.title }}
        </div>
        <div class="body">
            {{ post.text }}
        </div>
        <div class="footer">
            <a href="#" @click.prevent="onChangePost(post.id)">View all</a>
        </div>
    </section>
</template>

<script>
export default {
    name: "SidebarPost",
    props: ["post"],
    methods: {
        onChangePost: function (postId) {
            this.$root.$emit("onChangePost", postId);
        }
    }
}
</script>

<style scoped>
section {
    margin-bottom: 1rem;
}
</style>