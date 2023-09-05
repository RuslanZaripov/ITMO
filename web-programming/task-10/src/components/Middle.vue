<template>
    <div class="middle">
        <Sidebar :posts="viewPosts"/>
        <main>
            <Index v-if="page === 'Index'" :comments="comments" :posts="posts" :users="users"/>
            <Enter v-if="page === 'Enter'"/>
            <WritePost v-if="page === 'WritePost'"/>
            <EditPost v-if="page === 'EditPost'"/>
            <Register v-if="page === 'Register'"/>
            <Users v-if="page === 'Users'" :users="users"/>
            <Post v-if="page === 'Post'"
                  :comments="viewComments"
                  :post="viewPost"
                  :users="viewUsers"/>
        </main>
    </div>
</template>

<script>
import Sidebar from "./sidebar/Sidebar";
import Index from "./page/Index";
import Enter from "./page/Enter";
import WritePost from "./page/WritePost";
import EditPost from "./page/EditPost";
import Register from "./page/Register";
import Users from "./page/Users";
import Post from "./page/Post";

export default {
    name: "Middle",
    data: function () {
        return {
            page: "Index",
            postId: null
        }
    },
    components: {
        Post,
        Register,
        WritePost,
        Enter,
        Index,
        Sidebar,
        EditPost,
        Users
    },
    props: ["posts", "users", "comments"],
    computed: {
        viewPosts: function () {
            return Object.values(this.posts).sort((a, b) => b.id - a.id).slice(0, 2);
        },
        viewComments: function () {
            return Object.values(this.comments).filter(c => c.postId === this.postId);
        },
        viewUsers: function () {
            return Object.values(this.viewComments).map(c => this.users[c.userId]).concat(this.users[this.posts[this.postId].userId]);
        },
        viewPost: function () {
            return this.posts[this.postId];
        }
    }, beforeCreate() {
        this.$root.$on("onChangePage", (page) => {
            this.page = page;
            this.postId = null;
        });

        this.$root.$on("onChangePost", (postId) => {
            this.page = "Post";
            this.postId = postId;
        });
    }
}
</script>

<style scoped>

</style>
