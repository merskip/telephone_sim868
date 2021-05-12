package pl.merskip.telephone_sim868

interface Telephone {
    val imei: String
    val iccid: String

    val signalQuality: Int?

    val currentCall: Call?

    fun unlock(enterPin: () -> String, enterPuk: () -> String)

    fun call(phoneNumber: String): Call

    fun onIncomingCall(callback: (incomingCall: IncomingCall) -> Unit): Call

    fun sendSMS(phoneNumber: String, message: String)

    fun onSMSReceived(callback: (phoneNumber: String, message: String) -> Unit)

    interface IncomingCall {
        val phoneNumber: String

        fun answer()

        fun hangUp()
    }

    interface Call {
        val phoneNumber: String

        fun onDialing(callback: (call: Call) -> Unit): Call

        fun onRinging(callback: (call: Call) -> Unit): Call

        fun onAnswerCall(callback: (call: Call) -> Unit): Call

        fun onReceivedDTMF(callback: (call: Call, key: String) -> Unit): Call

        fun onFinishCall(callback: (call: Call) -> Unit): Call

        fun sendDTMF(keys: String)

        fun hangUp()
    }
}
