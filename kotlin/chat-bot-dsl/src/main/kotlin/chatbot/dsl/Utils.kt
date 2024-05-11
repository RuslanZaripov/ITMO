package chatbot.dsl

inline fun <reified T> Any?.tryCastOrElse(block: T.() -> Unit, emptyAction: Runnable) {
    if (this is T) {
        block()
    } else {
        emptyAction.run()
    }
}
