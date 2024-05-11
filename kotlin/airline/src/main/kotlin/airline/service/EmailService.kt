package airline.service

import kotlinx.coroutines.channels.Channel

interface EmailService {
    suspend fun send(to: String, text: String)
}

open class EmailServiceDecorator(private val emailService: EmailService) : EmailService {
    override suspend fun send(to: String, text: String) {
        emailService.send(to, text)
    }
}

class BufferedEmailService(
    emailService: EmailService,
    bufferSize: Int = 100,
) : EmailServiceDecorator(emailService) {
    private val buffer = Channel<Pair<String, String>>(bufferSize)

    override suspend fun send(to: String, text: String) {
        buffer.send(to to text)
    }

    suspend fun run() {
        for ((to, text) in buffer) super.send(to, text)
    }
}
