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

    fun command(command: String): List<String> {
        logger.verbose("Running command: \"$command\"...")
        output.write("$command\r\n")
        output.flush()
        Thread.sleep(100)

        val buffer = mutableListOf<Byte>()
        while (input.ready()) {

            buffer.add(input.read().toByte())
            Thread.sleep(100)
        }
        val response = buffer
            .toByteArray()
            .decodeToString()
            .trim()
            .split("\n")
            .map { it.trim() }
        if (response.first() != command)
            throw Exception("Response doesn't start with command: \"$command\"")
        val result = response.drop(1)
        logger.verbose("Result: $result")
        return result
    }

}