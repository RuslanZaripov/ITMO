package chatbot.dsl

import chatbot.api.Keyboard

@ChatBotDsl
class KeyboardBuilder {
    var oneTime: Boolean = false
    var keyboard: MutableList<MutableList<Keyboard.Button>> = mutableListOf()

    fun row(configure: RowBuilder.() -> Unit) {
        keyboard += RowBuilder().apply(configure).build()
    }

    fun build() = Keyboard.Markup(oneTime, keyboard)
}
