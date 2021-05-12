package pl.merskip.telephone_sim868.sim868

import com.fazecast.jSerialComm.SerialPort
import jdk.internal.util.xml.impl.Input
import pl.merskip.telephone_sim868.Logger
import pl.merskip.telephone_sim868.escaped
import java.io.BufferedInputStream
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

    fun observeUnsolicitedMessage(callback: (Message) -> Unit) {
        unsolicitedCodesThread = Thread {
            while (!Thread.currentThread().isInterrupted) {
                if (isExecutingCommand) continue
                if (!input.hasAvailable) continue

                val buffer = input.readBytes("\r\n\r\n")
                val response = buffer.decodeToString()
                logger.verbose("|== ${response.escaped()}")

                callback(ResponseParser().parse(response))

                Thread.sleep(10)
            }
        }
        unsolicitedCodesThread.start()
        logger.debug("Started thread (id=${unsolicitedCodesThread.id}, name=${unsolicitedCodesThread.name}) for observe unsolicited codes")
    }

    fun testAT(command: String) =
        sendCommand("AT$command=?")
            .withRequestedCommand(command)

    fun readAT(command: String) =
        sendCommand("AT$command?")
            .withRequestedCommand(command)

    fun writeAT(command: String, vararg values: Any) =
        sendCommand("AT$command=${convertValues(values)}")
            .withRequestedCommand(command)

    fun executeAT(command: String) = sendCommand("AT$command").withRequestedCommand(command)

    private fun convertValues(values: Array<out Any>) =
        values.joinToString(",") {
            when (it) {
                is String -> "\"$it\""
                is Char -> "$it"
                is Int -> it.toString()
                else -> throw Exception("Unsupported value type: ${it::class.java}")
            }
        }

    private fun sendCommand(command: String): Message {

        if (input.hasAvailable) {
            val dirtyBytes = input.readBytes()
            logger.warning("Input stream is dirty: ${dirtyBytes.decodeToString().escaped()}")
        }

        logger.verbose("--> $command")
        isExecutingCommand = true
        output.write("$command\r\n".toByteArray())
        output.flush()

        val buffer = input.readBytes("\r\nOK\r\n", "\r\nERROR\r\n")
        isExecutingCommand = false

        var response = buffer.decodeToString()

        if (response.startsWith(command))
            response = response.removePrefix(command + "\r\n")
        logger.verbose(" <- ${response.escaped()}")

        return ResponseParser().parse(response)
    }

    private fun InputStream.readBytes(vararg untilEndsWith: String): ByteArray {
        val expectedSuffixes = untilEndsWith.map { it.toByteArray() }
        val buffer = ByteArray(4 * 1024) // 4 kB
        var offset = 0

        // Wait for bytes...
        Thread.sleep(100)
        while (available() == 0) {
            Thread.sleep(10)
        }

        while (input.hasAvailable) {
            Thread.sleep(10)

            val nextByte = input.read().toByte()
            buffer[offset] = nextByte
            offset += 1

            if (expectedSuffixes.any { buffer.hasSuffix(it, offset) }) {
                break
            }

        }
        return buffer.take(offset).toByteArray()
    }

    private val InputStream.hasAvailable: Boolean get() = available() > 0

    private fun ByteArray.hasSuffix(suffix: ByteArray, length: Int): Boolean {
        if (length < suffix.size) return false
        val suffixStart = length - suffix.size
        suffix.forEachIndexed { index, suffixByte ->
            if (suffixByte != this[suffixStart + index]) {
                return false
            }
        }
        return true
    }
}