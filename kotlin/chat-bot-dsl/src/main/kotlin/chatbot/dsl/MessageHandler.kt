package chatbot.dsl

import chatbot.api.ChatContext

data class MessageHandler<C : ChatContext?>(
    val predicate: MessagePredicate,
    val processor: MessageProcessor<C>,
)
