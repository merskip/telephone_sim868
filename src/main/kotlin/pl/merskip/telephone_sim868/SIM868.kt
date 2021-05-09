package pl.merskip.telephone_sim868

import com.fazecast.jSerialComm.SerialPort
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

    init {
        open()
    }

    private fun open() {
        logger.info("Opening port: ${serialPort.systemPortName}...")
        if (!serialPort.openPort())
            throw Exception("Failed while openPort")
    }

    fun dispose() {
        logger.info("Closing port: ${serialPort.systemPortName}...")
        if (!serialPort.closePort())
            throw Exception("Failed while closePort")
    }

    fun executeAT(command: String, trimPrefix: Boolean = true): List<String> {
        return sendCommand("AT$command")
            .toMutableList()
            .apply {
                if (trimPrefix)
                    set(0, this[0].removePrefix("$command: "))
            }
    }

    fun readAT(command: String, trimPrefix: Boolean = true): List<String> {
        return sendCommand("AT$command?")
            .toMutableList()
            .apply {
                if (trimPrefix)
                    set(0, this[0].removePrefix("$command: "))
            }
    }

    private fun sendCommand(command: String): List<String> {
        logger.verbose("Sending command: \"$command\"...")
        output.write("$command\r\n".toByteArray())
        output.flush()

        while (input.available() == 0) {
            Thread.sleep(10)
        }

        val buffer = ByteArray(4 * 1024) // 4 kB
        var offset = 0
        while (true) {
            val bytesAvailable = input.available()
            if (bytesAvailable == 0)
                break
            input.read(buffer, offset, bytesAvailable)
            offset += bytesAvailable
            Thread.sleep(10)
        }

        var response = buffer
            .take(offset)
            .toByteArray()
            .decodeToString()
            .trim()
            .split("\n")
            .map { it.trim() }

        logger.verbose("Result: $response")

        if (response.first() == command)
            response = response.drop(1)

        return response
    }
}