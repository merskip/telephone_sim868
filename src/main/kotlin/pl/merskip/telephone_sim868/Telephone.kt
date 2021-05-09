package pl.merskip.telephone_sim868

interface Telephone {
    val imei: String
    val iccid: String

    val signalQuality: Int?

    fun unlock(enterPin: () -> String, enterPuk: () -> String)

    fun call(phoneNumber: String)

    fun sendSMS(message: String)

    fun answerCall()

    fun hangUp()

    fun onIncomingCall(callback: (phoneNumber: String) -> Unit)

    fun onSMSReceived(callback: (phoneNumber: String, message: String) -> Unit)
}
