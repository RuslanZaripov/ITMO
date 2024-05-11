package chatbot

import chatbot.api.ChatContext

class A<C : ChatContext> {
    lateinit var a: C
}

inline fun <reified C : ChatContext> f(a: A<C>) {

}

object AskNameContext : ChatContext

class WithNameContext(val name: String) : ChatContext

fun main() {
}
