package chatbot.dsl

import chatbot.api.Keyboard

@ChatBotDsl
class RowBuilder {
    private var row: MutableList<Keyboard.Button> = mutableListOf()

    fun button(text: String) {
        row += Keyboard.Button(text)
    }

    operator fun String.unaryMinus() {
        row += Keyboard.Button(this)
    }

    fun build() = row
}
