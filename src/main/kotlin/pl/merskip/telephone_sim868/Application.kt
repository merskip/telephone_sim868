package pl.merskip.telephone_sim868

class Application {

    private val logger = Logger(this::class.java)

    fun start() {
        val telephone = TelephoneSIM868(SIM868("COM5"))
        logger.info("IMEI: ${telephone.imei}")
        logger.info("Signal quality: ${telephone.signalQuality} dBm")
    }
}