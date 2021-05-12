package pl.merskip.telephone_sim868

import pl.merskip.telephone_sim868.sim868.SIM868
import pl.merskip.telephone_sim868.sim868.TelephoneSIM868
import java.awt.SystemColor.info
import java.lang.Exception
import javax.sound.sampled.*
import kotlin.math.log


class Application {

    private val logger = Logger(this::class.java)

    fun start() {
        logger.debug("Startup procedure...")
        val telephone = TelephoneSIM868(SIM868("COM5"))
        telephone.unlock(
            enterPin = { TODO("Enter PIN") },
            enterPuk = { TODO("Enter PUK") }
        )
        logger.info("IMEI: ${telephone.imei.dashed(2, 8, 14)}")
        logger.info("ICCID: ${telephone.iccid}")
        logger.info("Signal quality: ${telephone.signalQuality} dBm")

        println("Welcum!")
        println("Type 'help' for help,")
        println("type 'quit' or 'q' for 'quit'")
        println()

        readUserInput(
            onInput = { command, parameters ->
                when (command) {
                    "help" -> commandHelp(telephone, parameters)
                    "call" -> commandCall(telephone, parameters)
                    else -> logger.warning("Unknown command: $command")
                }
            },
            onQuit = {
                telephone.dispose()
                println("Bye!")
            }
        )
    }

    private fun commandHelp(telephone: Telephone, parameters: List<String>) {
        println(" help                      Prints help. It's this")
        println(" q, quit                   Terminate application")
        println(" call <phoneNumber>        Make phone call to <phoneNumber>")
    }

    private fun commandCall(telephone: Telephone,  parameters: List<String>) {
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

    private fun readUserInput(onInput: (command: String, parameters: List<String>) -> Unit, onQuit: () -> Unit) {
        logger.onLogPrinted = {
            print("\r> ")
        }

        while (true) {
            print("\r> ")
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