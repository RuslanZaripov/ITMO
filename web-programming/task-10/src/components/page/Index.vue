<template>
    <div>
        <Article v-for="post in Object.values(this.posts).slice().reverse()"
                 :key="post.id"
                 :commentsSize="commentsSize(post.id)"
                 :post="post"
                 :userLogin="users[post.userId].login"/>
    </div>
</template>

<script>
import Article from "../Article";

export default {
    name: "Index",
    components: {Article},
    props: ["posts", "users", "comments"],
    methods: {
        commentsSize: function (postId) {
            return Object.values(this.comments).filter(c => c.postId === postId).length;
        }
    }
}
</script>

<style scoped>

</style>
