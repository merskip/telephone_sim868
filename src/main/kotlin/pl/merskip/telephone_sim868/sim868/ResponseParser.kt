package pl.merskip.telephone_sim868.sim868

import pl.merskip.telephone_sim868.Logger

class ResponseParser {

    private val logger = Logger(this::class.java)

    fun parse(text: String): Message {
        val lines = text.split("\n")
            .map { it.trim() }

        val entities = mutableListOf<Message.Entity>()
        var status: Message.Status? = null
        var messageData: String? = null

        val lineIterator = lines.listIterator()
        while (lineIterator.hasNext()) {
            val line = lineIterator.next()
            if (line.startsWith("+")) {
                val (command, value) = line.split(": ", limit = 2)
                val values = value.split(Regex(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"))
                    .map { Message.Entity.Value(it) }

                val data: String?
                val nextLine = lineIterator.next()
                if (nextLine.isNotEmpty() && !nextLine.startsWith("+")) {
                    data = nextLine
                }
                else {
                    data = null
                    lineIterator.previous()
                }

                entities.add(Message.Entity(command, values, data))
            }
            else if (line.isEmpty()) {
                continue
            }
            else {
                 when (line) {
                    "OK" -> status = Message.Status.OK
                    "ERROR" -> status = Message.Status.ERROR
                    else -> messageData = line
                }
            }
        }

        if (status == null) {
            status = Message.Status.OK
        }

        return Message(null, status, entities.toList(), messageData)
    }
}