package pl.merskip.telephone_sim868

import com.fazecast.jSerialComm.SerialPort
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception

class SIM868(
    port: String
) {

    private val logger = Logger(this::class.java)

    private val serialPort = SerialPort.getCommPort(port)!!

    private val output = OutputStreamWriter(serialPort.outputStream)
    private val input = InputStreamReader(serialPort.inputStream)

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
        output.write("$command\r\n")
        output.flush()
        Thread.sleep(100)

        val buffer = mutableListOf<Byte>()
        while (input.ready()) {
            buffer.add(input.read().toByte())
            Thread.sleep(100)
        }
        var response = buffer
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