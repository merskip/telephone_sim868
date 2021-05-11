package pl.merskip.telephone_sim868

import pl.merskip.telephone_sim868.sim868.SIM868
import pl.merskip.telephone_sim868.sim868.TelephoneSIM868
import java.io.File
import javax.sound.sampled.*
import kotlin.math.log


class Application {

    private val logger = Logger(this::class.java)

    fun start() {
        val telephone = TelephoneSIM868(SIM868("COM5"))
        telephone.unlock(
            enterPin = { TODO("Enter PIN") },
            enterPuk = { TODO("Enter PUK") }
        )
        logger.info("IMEI: ${telephone.imei.dashed(2, 8, 14)}")
        logger.info("ICCID: ${telephone.iccid}")
        logger.info("Signal quality: ${telephone.signalQuality} dBm")

        telephone.onIncomingCall { phoneNumber ->
            logger.info("Incoming call from \"$phoneNumber\"...")

            Thread.sleep(500)
            telephone.answerCall()

            playAudio {
                logger.info("Finishing call")
                Thread.sleep(500)
                telephone.hangUp()
            }
        }

        telephone.onDtmfReceived { key ->
            logger.info("Received DTMF key: \"$key\"")
        }

//        telephone.call("xxxxxxxxx",
//            onAnswer = {
//                playAudio {
//                    logger.info("Finishing call")
//                    Thread.sleep(500)
//                    telephone.hangUp()
//                }
//            },
//            onNoResponse = {
//                logger.info("No response")
//                telephone.hangUp()
//            })
    }

    private fun playAudio(completed: () -> Unit) {
        logger.info("Start playing audio")
        val audioStream = this::class.java.classLoader.getResourceAsStream("stachu_jest_stachu_w_domu.wav")
        val audioInput = AudioSystem.getAudioInputStream(audioStream)

        val info = DataLine.Info(Clip::class.java, audioInput.format)
        val audioClip = AudioSystem.getLine(info) as Clip

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