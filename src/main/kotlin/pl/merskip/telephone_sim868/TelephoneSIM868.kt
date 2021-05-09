package pl.merskip.telephone_sim868

import java.lang.Exception

class TelephoneSIM868(
    private val sim868: SIM868
): Telephone {

    private val logger = Logger(this::class.java)

    override val imei: String
        get() = sim868.executeAT("+GSN")
            .dropTrailingOk().single()

    override val iccid: String
        get() = sim868.executeAT("+CCID")
            .dropTrailingOk().single()

    override val signalQuality: Int?
        get() {
            val (rssi, ber) = sim868.executeAT("+CSQ")
                .dropTrailingOk().single()
                .split(',')
                .map { it.toInt() }
            // Mapping rssi to dBm
            return mapOf(
                0 to -115, 1 to -111, 2 to -110, 3 to -108,
                4 to -106, 5 to -104, 6 to -102, 7 to -100,
                8 to -98, 9 to -96, 10 to -94, 11 to -92,
                12 to -90, 13 to -88, 14 to -86, 15 to -84,
                16 to -82, 17 to -80, 18 to -78, 19 to -76,
                20 to -74, 21 to -72, 22 to -70, 23 to -68,
                24 to -66, 25 to -64, 26 to -62, 27 to -60,
                28 to -58, 29 to -56, 30 to -54, 31 to -52,
                99 to null
            )[rssi]
        }

    override fun unlock(enterPin: () -> String, enterPuk: () -> String) {
        val result = sim868.readAT("+CPIN")
        when (result[0]) {
            "READY" -> return
            "SIM PIN" -> {
                val pin = enterPin()
                throw NotImplementedError()
            }
            "SIM PUK" -> {
                val puk = enterPuk()
                throw NotImplementedError()
            }
            else -> throw Exception("Unknown code: ${result[0]}")
        }
    }

    override fun call(phoneNumber: String) {
        TODO("Not yet implemented")
    }

    override fun sendSMS(phoneNumber: String, message: String) {
        TODO("Not yet implemented")
    }

    override fun answerCall() {
        TODO("Not yet implemented")
    }

    override fun hangUp() {
        TODO("Not yet implemented")
    }

    override fun onIncomingCall(callback: (phoneNumber: String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun onSMSReceived(callback: (phoneNumber: String, message: String) -> Unit) {
        TODO("Not yet implemented")
    }

    private fun List<String>.dropTrailingOk(): List<String> {
        return if (last() == "OK") {
            if (this[size - 2] == "")
                dropLast(2)
            else
                dropLast(1)
        } else {
            throw Exception("Something is bad :-(")
        }
    }
}