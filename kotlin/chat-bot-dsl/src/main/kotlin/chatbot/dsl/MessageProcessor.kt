package chatbot.dsl

import chatbot.api.ChatContext
import chatbot.api.ChatId
import chatbot.api.Client
import chatbot.api.Message

typealias MessageProcessor<C> = MessageProcessorContext<C>.() -> Unit

@ChatBotDsl
class MessageProcessorContext<C : ChatContext?>(
    val message: Message,
    val client: Client,
    val context: C,
    val setContext: (c: ChatContext?) -> Unit,
)

fun <C : ChatContext?> MessageProcessorContext<C>.sendMessage(
    chatId: ChatId,
    configure: MessageBuilder.() -> Unit,
) {
    val message = MessageBuilder().apply(configure).build()

    if (message.text.isEmpty() && message.keyboard == null) return

    client.sendMessage(chatId, message.text, message.keyboard, message.replyTo)
}
