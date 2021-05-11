package pl.merskip.telephone_sim868.sim868

import pl.merskip.telephone_sim868.Logger
import java.lang.Exception

class ResponseParser {

    private val logger = Logger(this::class.java)

    fun parse(text: String): Response {
        val lines = text.split("\n")
            .map { it.trim() }

        val entities = mutableListOf<Response.Entity>()
        var status: Response.Status? = null
        var data: String? = null

        val lineIterator = lines.listIterator()
        while (lineIterator.hasNext()) {
            val line = lineIterator.next()
            if (line.startsWith("+")) {
                val (command, value) = line.split(": ", limit = 2)
                val values = value.split(Regex(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"))
                    .map { Response.Entity.Value(it) }

                val data: String?
                val nextLine = lineIterator.next()
                if (nextLine.isNotEmpty() && !nextLine.startsWith("+")) {
                    data = nextLine
                }
                else {
                    data = null
                    lineIterator.previous()
                }

                entities.add(Response.Entity(command, values, data))
            }
            else if (line.isEmpty()) {
                continue
            }
            else {
                 when (line) {
                    "OK" -> status = Response.Status.OK
                    "ERROR" -> status = Response.Status.ERROR
                    else -> data = line
                }
            }
        }

        if (status == null) {
            logger.error("Not found status in reponse")
            status = Response.Status.OK
        }

        return Response(status, entities.toList(), data)
    }
}