<template>
    <div class="register form-box">
        <div class="header">Register</div>
        <div class="body">
            <form @submit.prevent="onRegister">
                <div class="field">
                    <div class="name">
                        <label for="name">Name</label>
                    </div>
                    <div class="value">
                        <input id="name" v-model="name" autofocus name="name"/>
                    </div>
                </div>

                <div class="field">
                    <div class="name">
                        <label for="login">Login</label>
                    </div>
                    <div class="value">
                        <input id="login" v-model="login" autofocus name="login"/>
                    </div>
                </div>

                <div class="field error">{{ error }}</div>

                <div class="button-field">
                    <input type="submit" value="Register">
                </div>
            </form>
        </div>
    </div>
</template>

<script>
export default {
    name: "Register",
    data: function () {
        return {
            name: "",
            login: "",
            error: ""
        }
    },
    methods: {
        onRegister: function () {
            this.$root.$emit("onRegister", this.name, this.login);
        }
    },
    beforeCreate() {
        this.$root.$on("onRegisterValidationError", (error) => this.error = error);
    }
}
</script>

<style scoped>

</style>