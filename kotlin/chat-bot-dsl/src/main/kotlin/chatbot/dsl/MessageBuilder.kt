package chatbot.dsl

import chatbot.api.Keyboard
import chatbot.api.MessageId

class MessageBuilder {
    var text: String = ""
    var replyTo: MessageId? = null
    private var keyboard: Keyboard? = null

    fun removeKeyboard() {
        keyboard = Keyboard.Remove
    }

    fun withKeyboard(configure: KeyboardBuilder.() -> Unit) {
        KeyboardBuilder().apply(configure).build()
            .takeUnless { it.keyboard.flatten().isEmpty() }
            ?.let { keyboard = it }
    }

    fun build() = Message(text, keyboard, replyTo)

    data class Message(
        val text: String,
        val keyboard: Keyboard?,
        val replyTo: MessageId?,
    )
}
