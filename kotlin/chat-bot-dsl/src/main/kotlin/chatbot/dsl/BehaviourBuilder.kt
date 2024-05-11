package chatbot.dsl

import chatbot.api.ChatBot
import chatbot.api.ChatContext
import chatbot.api.Message

typealias MessagePredicate = ChatBot.(Message) -> Boolean

typealias MessageProcessorPredicate = ChatBot.(Message) -> Boolean

infix operator fun MessageProcessorPredicate.times(other: MessageProcessorPredicate): MessageProcessorPredicate {
    return { this@times(it) and other(it) }
}

private infix fun MessagePredicate.and(other: MessagePredicate): MessagePredicate {
    return { this@and(it) and other(it) }
}

class Behaviour(
    val messageHandlers: MutableList<MessageHandler<ChatContext>>,
) {
    @ChatBotDsl
    class Builder<C : ChatContext>(
        val botBuilder: Bot.Builder,
        private val condition: ChatBot.(Message) -> Boolean,
    ) {
        private var messageHandlers = mutableListOf<MessageHandler<C>>()
        private var isDefaultSet = false

        fun onCommand(command: String, f: MessageProcessor<C>) {
            onMessagePrefix("/$command", f)
        }

        fun onMessage(predicate: MessagePredicate, f: MessageProcessor<C>) {
            if (isDefaultSet) return
            messageHandlers.add(MessageHandler(condition and predicate, f))
        }

        fun onMessage(f: MessageProcessor<C>) {
            onMessage({ true }, f)
            isDefaultSet = true
        }

        fun onMessagePrefix(prefix: String, f: MessageProcessor<C>) {
            onMessage({ it.text.startsWith(prefix) }, f)
        }

        fun onMessageContains(text: String, f: MessageProcessor<C>) {
            onMessage({ it.text.contains(text) }, f)
        }

        fun onMessage(messageTextExactly: String, f: MessageProcessor<C>) {
            onMessage({ it.text == messageTextExactly }, f)
        }

        inline infix fun <reified T : ChatContext> T.into(configure: Builder<T>.() -> Unit) =
            botBuilder.behaviourWithContext(configure)

        inline fun <reified T : ChatContext> into(configure: Builder<T>.() -> Unit) =
            botBuilder.behaviourWithContext(configure)

        fun MessageProcessorPredicate.into(configure: Builder<NullContext>.() -> Unit) =
            botBuilder.conditionalBehaviour(configure, condition and this@into)

        fun build() = messageHandlers.toList()
    }
}

inline operator fun <reified C : ChatContext> Behaviour.plusAssign(other: List<MessageHandler<C>>) {
    messageHandlers += other.map { messageHandler ->
        val abstractProcessor: MessageProcessorContext<ChatContext>.() -> Unit = {
            tryCastOrElse<MessageProcessorContext<C>>({
                messageHandler.processor(this)
            }) {
                println("Cast did not succeed")
            }
        }
        MessageHandler(messageHandler.predicate, abstractProcessor)
    }
}
