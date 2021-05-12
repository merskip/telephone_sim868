package pl.merskip.telephone_sim868

import pl.merskip.telephone_sim868.sim868.SIM868
import pl.merskip.telephone_sim868.sim868.TelephoneSIM868
import java.awt.SystemColor.info
import java.lang.Exception
import javax.sound.sampled.*
import kotlin.math.log


class Application {

    private val logger = Logger(this::class.java)

    private lateinit var telephone: TelephoneSIM868

    fun start() {
        logger.debug("Startup procedure...")
        telephone = TelephoneSIM868(SIM868("COM5"))
        telephone.unlock(
            enterPin = { TODO("Enter PIN") },
            enterPuk = { TODO("Enter PUK") }
        )
        logger.info("IMEI: ${telephone.imei.dashed(2, 8, 14)}")
        logger.info("ICCID: ${telephone.iccid}")
        logger.info("Signal quality: ${telephone.signalQuality} dBm")

        telephone.onIncomingCall { incomingCall ->
            logger.info("Incoming call from ${incomingCall.phoneNumber}. Type 'a' or 'answer' to answer the call.")
        }

        println("Welcum!")
        println("Type 'help' for help,")
        println("type 'quit' or 'q' for 'quit'")
        println()

        readUserInput(
            onInput = { command, parameters ->
                when (command) {
                    "help" -> commandHelp(parameters)
                    "call" -> commandCall(parameters)
                    "a", "answer" -> commandAnswer(parameters)
                    "h", "hangup" -> commandHangUp(parameters)
                    "dtmf" -> commandDTMF(parameters)
                    else -> logger.warning("Unknown command: $command")
                }
            },
            onQuit = {
                telephone.dispose()
                println("Bye!")
            }
        )
    }

    private fun commandHelp(parameters: List<String>) {
        println(" help                      Prints help. It's this")
        println(" q, quit                   Terminate application")
        println(" call <phoneNumber>        Make phone call to <phoneNumber>")
        println(" a, answer                 Answer the incoming call")
        println(" h, hangup                 End current call")
        println(" dtmf <0-9A-D*#>...        Send DTMF (Dual-tone multi-frequency signaling)")
    }

    private fun commandCall(parameters: List<String>) {
        val phoneNumber = parameters.getOrNull(0)
        if (phoneNumber == null) {
            logger.warning("No phoneNumber parameter")
            return
        }

        telephone.call(phoneNumber)
            .onDialing { call ->
                logger.info("Dialing to ${call.phoneNumber}...")
            }
            .onRinging {
                logger.info("Ringing...")
            }
            .onAnswerCall {
                logger.info("Caller answered")
            }
            .onReceivedDTMF { _, key ->
                logger.info("Received DTMF: $key")
            }
            .onFinishCall { call ->
                logger.info("Call with ${call.phoneNumber} finished")
            }
    }

    private fun commandAnswer(parameters: List<String>) {
        val incomingCall = telephone.currentCall as? Telephone.IncomingCall
        if (incomingCall == null) {
            logger.warning("No current incoming call")
            return
        }
        incomingCall.answer()
    }

    private fun commandHangUp(parameters: List<String>) {
        if (telephone.currentCall == null) {
            logger.warning("No current call")
            return
        }
        telephone.currentCall?.hangUp()
    }

    private fun commandDTMF(parameters: List<String>) {
        if (telephone.currentCall == null) {
            logger.warning("No current call")
            return
        }
        val keys = parameters.joinToString("")
        telephone.currentCall?.sendDTMF(keys)
    }

    private fun readUserInput(onInput: (command: String, parameters: List<String>) -> Unit, onQuit: () -> Unit) {
        logger.onLogPrinted = {
            printPrompt()
        }

        while (true) {
            printPrompt()
            val line = readLine()
            if (line == "q" || line == "quit") {
                onQuit()
                return
            } else if (line != null) {
                if (line.isBlank()) continue
                val chunks = line.split(" ")
                onInput(chunks.first(), chunks.drop(1))
            }

        }
    }

    private fun printPrompt() {
        print("\r${telephone.currentCall?.phoneNumber ?: ""}> ")
    }

    private fun playAudio(completed: () -> Unit) {
        logger.info("Start playing audio")

        var mixerInfo: Mixer.Info? = null

        for (info in AudioSystem.getMixerInfo()) {
            if (info.name == "S?uchawki (Realtek(R) Audio)") {
                logger.debug("Selecting output: $info")
                mixerInfo = info
            }
        }
        if (mixerInfo == null) throw Exception("No mixer info")

        val audioStream = this::class.java.classLoader.getResourceAsStream("stachu_jest_stachu_w_domu_eddited.wav")
        val audioInput = AudioSystem.getAudioInputStream(audioStream)

        val audioClip = AudioSystem.getClip(mixerInfo)
        audioClip.open(audioInput)
        audioClip.addLineListener { event ->
            logger.verbose("Audio event: ${event.type}")

            if (event.type == LineEvent.Type.STOP) {
                logger.info("Playing audio finished")
                completed()
            }
        }
        audioClip.start()
    }
}