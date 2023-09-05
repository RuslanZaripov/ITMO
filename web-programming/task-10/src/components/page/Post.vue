<template>
    <div class="articleWithComments">
        <Article :commentsSize="Object.values(comments).length"
                 :post="post"
                 :userLogin="Object.values(users).filter(u => u.id === post.userId)[0].login"/>

        <div class="comments datatable">
            <div class="caption">Comments</div>
            <table>
                <thead>
                <tr>
                    <th>Login</th>
                    <th>Comment</th>
                </tr>
                </thead>
                <tbody>
                <tr v-if="Object.keys(comments).length === 0">
                    <td colspan="3">
                        No comments
                    </td>
                </tr>
                <tr v-for="comment in comments" v-else :key="comment.id">
                    <td class="login">{{ Object.values(users).filter(u => comment.userId === u.id)[0].login }}</td>
                    <td class="text">{{ comment.text }}</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script>
import Article from "../Article";

export default {
    name: "Post",
    components: {Article},
    props: ["comments", "post", "users"]
}

</script>

<style scoped>

</style>