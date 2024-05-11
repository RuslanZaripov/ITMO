package chatbot.dsl

import chatbot.api.*

fun chatBot(configure: Bot.Builder.() -> Unit): ChatBot {
    return Bot.Builder().apply(configure).build()
}

fun chatBot(client: Client, configure: Bot.Builder.() -> Unit): ChatBot {
    return Bot.Builder().apply(configure).apply { use(client) }.build()
}

object NullContext : ChatContext

fun ChatContextsManager?.context(message: Message): ChatContext {
    return this?.getContext(message.chatId) ?: NullContext
}

@ChatBotDsl
class Bot private constructor(
    override val logLevel: LogLevel,
    private val contextManager: ChatContextsManager?,
    private val behaviour: Behaviour,
    private val client: Client,
) : ChatBot {

    override fun processMessages(message: Message) {
        val messageProcessorContext = MessageProcessorContext(
            message,
            client,
            contextManager.context(message)
        ) { chatContext ->
            contextManager?.setContext(message.chatId, chatContext)
        }

        behaviour.messageHandlers.firstNotNullOfOrNull { (predicate, processor) ->
            processor.takeIf { predicate(message) }
        }?.let { processor ->
            processor(messageProcessorContext)
        } ?: log(message)
    }

    private fun log(message: Message) {
        println("[$logLevel] No behaviour found for message: $message")
    }

    @ChatBotDsl
    class Builder {
        private var logLevel: LogLevel = LogLevel.ERROR
        private lateinit var client: Client
        var contextManager: ChatContextsManager? = null

        val behaviour = Behaviour(mutableListOf())

        fun use(logLevel: LogLevel) {
            this.logLevel = logLevel
        }

        operator fun LogLevel.unaryPlus() {
            logLevel = this
        }

        fun use(client: Client) {
            this.client = client
        }

        fun use(contextManager: ChatContextsManager) {
            this.contextManager = contextManager
        }

        inline fun <reified T : ChatContext> conditionalBehaviour(
            configure: Behaviour.Builder<T>.() -> Unit,
            noinline condition: ChatBot.(Message) -> Boolean,
        ) {
            val behaviourBuilder = Behaviour.Builder<T>(this, condition).apply(configure)
            behaviour += behaviourBuilder.build()
        }

        inline fun <reified T : ChatContext> behaviourWithContext(configure: Behaviour.Builder<T>.() -> Unit) {
            conditionalBehaviour(configure) { contextManager.context(it) is T }
        }

        fun behaviour(configure: Behaviour.Builder<NullContext>.() -> Unit) = behaviourWithContext(configure)

        fun build() = Bot(
            logLevel,
            contextManager,
            behaviour,
            client
        )
    }
}
