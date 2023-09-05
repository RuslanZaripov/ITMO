<template>
    <article>
        <div class="title"><a href="#" @click.prevent="onChangePost(post.id)">{{ post.title }}</a></div>
        <div class="information">By {{ userLogin }} 14.12.2021</div>
        <div class="body">{{ post.text }}</div>
        <ul class="attachment">
            <li>Announcement of <a href="#">Codeforces Round #510 (Div. 1)</a></li>
            <li>Announcement of <a href="#">Codeforces Round #510 (Div. 2)</a></li>
        </ul>
        <div class="footer">
            <div class="left">
                <img alt="Vote Up" src="../assets/img/voteup.png" title="Vote Up"/>
                <span class="positive-score">+173</span>
                <img alt="Vote Down" src="../assets/img/votedown.png" title="Vote Down"/>
            </div>
            <div class="right">
                <img alt="Publish Time" src="../assets/img/date_16x16.png" title="Publish Time"/>
                14.12.2021
                <img alt="Comments" src="../assets/img/comments_16x16.png" title="Comments"/>
                <a ref="comments" href="" @click.prevent="onChangePost(post.id)">{{ commentsSize }}</a>
            </div>
        </div>
    </article>
</template>

<script>
export default {
    name: "Article",
    props: ["post", "userLogin", "commentsSize"],
    methods: {
        onChangePost: function (postId) {
            this.$root.$emit("onChangePost", postId);
        }
    }
}
</script>

<style scoped>
article {
    margin-bottom: 1rem;
}

article .attachment li {
    background: url("../assets/img/paperclip-16x16.png") 0 2px no-repeat;
}
</style>