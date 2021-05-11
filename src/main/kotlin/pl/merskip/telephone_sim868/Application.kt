package pl.merskip.telephone_sim868

import pl.merskip.telephone_sim868.sim868.SIM868
import pl.merskip.telephone_sim868.sim868.TelephoneSIM868

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

            Thread.sleep(2000)
            telephone.answerCall()
        }

        telephone.onDtmfReceived { key ->
            logger.info("Received DTMF key: \"$key\"")
        }
    }
}