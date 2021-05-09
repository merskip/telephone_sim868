package pl.merskip.telephone_sim868

class TelephoneSIM868(
    private val sim868: SIM868
): Telephone {

    private val logger = Logger(this::class.java)

    override val imei: String
        get() = TODO("Not yet implemented")

    override val signalQuality: Int
        get() = TODO("Not yet implemented")

    override fun unlock(enterPin: () -> String, enterPuk: () -> String) {
        TODO("Not yet implemented")
    }

    override fun call(phoneNumber: String) {
        TODO("Not yet implemented")
    }

    override fun sendSMS(message: String) {
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
}