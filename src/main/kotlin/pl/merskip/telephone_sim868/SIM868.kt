package pl.merskip.telephone_sim868

import com.fazecast.jSerialComm.SerialPort
import java.io.InputStream
import java.lang.Exception

class SIM868(
    port: String,
    baudRate: Int = 9600
) {

    private val logger = Logger(this::class.java)

    private val serialPort = SerialPort.getCommPort(port).apply {
        this.baudRate = baudRate
    }

    private val output = serialPort.outputStream
    private val input = serialPort.inputStream

    private lateinit var unsolicitedCodesThread: Thread
    private var isExecutingCommand = false

    init {
        open()
    }

    private fun open() {
        logger.info("Opening port: ${serialPort.systemPortName}...")
        if (!serialPort.openPort())
            throw Exception("Failed while openPort")
    }

    fun dispose() {
        unsolicitedCodesThread.interrupt()
        logger.info("Closing port: ${serialPort.systemPortName}...")
        if (!serialPort.closePort())
            throw Exception("Failed while closePort")
    }

    fun observeUnsoliciteMessage(callback: (UnsolicitedMessage) -> Unit) {
        unsolicitedCodesThread = Thread {
            while (!Thread.currentThread().isInterrupted) {
                if (isExecutingCommand) continue
                if (input.available() == 0) continue

                val messages = input
                    .readBytes()
                    .decodeToString()
                    .trim()
                    .split("\n")
                    .map { it.trim() }

                logger.verbose("Received unsolicited messages=$messages")

                val codes = messages.mapNotNull { message ->
                    if (message.isEmpty()) null
                    else if (message.startsWith("+") && message.contains(": ")) {
                        val (code, value: String?) = message.split(": ", limit = 2)
                        UnsolicitedCode(code, value)
                    } else UnsolicitedCode(message, null)
                }
                callback(UnsolicitedMessage(codes))

                Thread.sleep(10)
            }
        }
        unsolicitedCodesThread.start()
        logger.debug("Started thread (id=${unsolicitedCodesThread.id}, name=${unsolicitedCodesThread.name}) for observe unsolicited codes")
    }



    fun readAT(command: String): List<String> {
        return sendCommand("AT$command?")
            .removeLeadingCommand(command)
    }

    fun writeAT(command: String, value: String) {
        val result = sendCommand("AT$command=$value")
        if (result.single() != "OK")
            throw Exception("Failed write AT")
    }

    fun executeAT(command: String): List<String> {
        return sendCommand("AT$command")
            .removeLeadingCommand(command)
    }

    private fun sendCommand(command: String): List<String> {
        logger.verbose("Sending command: \"$command\"...")
        isExecutingCommand = true
        output.write("$command\r\n".toByteArray())
        output.flush()

        input.waitForBytes()
        val buffer = input.readBytes()
        isExecutingCommand = false

        var response = buffer
            .decodeToString()
            .trim()
            .split("\n")
            .map { it.trim() }

        logger.verbose("Result: $response")

        if (response.first() == command)
            response = response.drop(1)

        return response
    }

    data class UnsolicitedMessage(
        val codes: List<UnsolicitedCode>
    ) {

        val primaryCode: String get() = codes.first().code

        operator fun get(code: String): UnsolicitedCode =
            codes.first { it.code == code }
    }

    data class UnsolicitedCode(
        val code: String,
        val value: String?
    ) {
        private val values = value?.split(",")

        fun getString(index: Int): String =
            values!![index].removePrefix("\"").removeSuffix("\"")

        fun getInt(index: Int): Int =
            values!![index].toInt()
    }

    private fun InputStream.waitForBytes() {
        while (available() == 0) {
            Thread.sleep(10)
        }
    }

    private fun InputStream.readBytes(): ByteArray {
        val buffer = ByteArray(4 * 1024) // 4 kB
        var offset = 0
        while (true) {
            val bytesAvailable = available()
            if (bytesAvailable == 0)
                break
            read(buffer, offset, bytesAvailable)
            offset += bytesAvailable
            Thread.sleep(10)
        }
        return buffer.take(offset).toByteArray()
    }

    private fun List<String>.removeLeadingCommand(command: String): List<String> =
        toMutableList().apply {
            if (isNotEmpty() && this[0].startsWith("$command: "))
                set(0, this[0].removePrefix("$command: "))
        }.toList()
}