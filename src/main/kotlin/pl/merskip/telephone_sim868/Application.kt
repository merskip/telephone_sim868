package pl.merskip.telephone_sim868

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
    }

    private fun String.dashed(vararg indexes: Int): String {
        var lastIndex = 0
        val chunks = mutableListOf<String>()
        for (index in indexes) {
            chunks.add(substring(lastIndex, index))
            lastIndex = index
        }
        chunks.add(substring(lastIndex))
        return chunks.joinToString("-")
    }
}