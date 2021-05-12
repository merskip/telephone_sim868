package pl.merskip.telephone_sim868.sim868

import pl.merskip.telephone_sim868.Logger
import pl.merskip.telephone_sim868.Telephone
import java.lang.Exception
import kotlin.math.log

class TelephoneSIM868(
    private val sim868: SIM868
) : Telephone {

    private val logger = Logger(this::class.java)

    override val imei: String
        get() = sim868.executeAT("+GSN").data!!

    override val iccid: String
        get() = sim868.executeAT("+CCID").data!!

    override val signalQuality: Int?
        get() {
            val rssi = sim868.executeAT("+CSQ").integer

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

    override var currentCall: BasicCall? = null

    init {
        logger.debug("Configuring SIM868...")
        sim868.executeAT("E0") // Disable echo mode
        sim868.writeAT("+CLIP", 1) // Enable caller phone number while ring
        sim868.writeAT("+VTD", 3) // Set DTMF to 300 ms
        sim868.writeAT("+DDET", "1,200,0,0") // Enable detect DTMF
        sim868.writeAT("+CLCC", 1) // Enable notifications when current call state changes

        sim868.observeUnsolicitedMessage { message ->
            when (message.entities.firstOrNull()?.command) {
//                "RING" -> unsolicitedCodeRing(message)
//                "+DTMF" -> unsolicitedCodeDtmf(message)
                "+CLCC" -> unsolicitedCodeCLCC(message)
                else -> logger.warning("Unknown unsolicited code=${message.entities.firstOrNull()?.command}, data=${message.data}")
            }
        }
    }

    fun dispose() {
        sim868.dispose()
    }

//    private fun unsolicitedCodeRing(message: Message) {
//        val phoneNumber = message[0].string
////        val type = message["+CLIP"].getInt(1)
////        val subAddress = message["+CLIP"].getString(2)
////        val subAddressType = message["+CLIP"].getInt(3)
////        val phoneBookIndex = message["+CLIP"].getString(4)
////        val cliValidity = message["+CLIP"].getInt(5)
////        incomingCallCallbacks.forEach { it(phoneNumber) }
//    }

//    private fun unsolicitedCodeDtmf(message: SIM868.UnsolicitedMessage) {
//        val key = message["+DTMF"].value
//            ?: throw Exception("No value while +DTMF")
//        dtmfReceivedCallbacks.forEach { it(key) }
//    }

    private fun unsolicitedCodeCLCC(message: Message) {
        val currentCall = currentCall
        if (currentCall == null) {
            logger.warning("Received +CLCC without set current call")
            return
        }
        message.getList("+CLCC")
            .map { CallState(it) }
            .forEach { callState ->
                if (callState.phoneNumber == currentCall.phoneNumber) {
                    when (callState.state) {
                        CallState.State.Dialing -> currentCall.onDialingCallback?.invoke(currentCall)
                        CallState.State.Alerting -> currentCall.onRingingCallCallback?.invoke(currentCall)
                        CallState.State.Active -> currentCall.onAnswerCallCallback?.invoke(currentCall)
                        CallState.State.Disconnect -> currentCall.onFinishCallCallback?.invoke(currentCall)
                        else -> { }
                    }
                } else {
                    logger.warning("Unknown call state: $callState")
                }
            }
    }

    override fun unlock(enterPin: () -> String, enterPuk: () -> String) {
        when (sim868.readAT("+CPIN").string) {
            "READY" -> return
            "SIM PIN" -> {
                val pin = enterPin()
                throw NotImplementedError()
            }
            "SIM PUK" -> {
                val puk = enterPuk()
                throw NotImplementedError()
            }
            else -> throw Exception("Unknown code")
        }
    }

    override fun call(phoneNumber: String): Telephone.Call {
        sim868.executeAT("D$phoneNumber;")
        return BasicCall(phoneNumber).apply {
            currentCall = this
        }
    }

    override fun onIncomingCall(callback: (incomingCall: Telephone.IncomingCall) -> Unit): Telephone.Call {
        TODO("Not yet implemented")
    }

    override fun sendSMS(phoneNumber: String, message: String) {
        TODO("Not yet implemented")
    }

    override fun onSMSReceived(callback: (phoneNumber: String, message: String) -> Unit) {
        TODO("Not yet implemented")
    }

    inner class BasicCall(
        override val phoneNumber: String
    ) : Telephone.Call {

        var onDialingCallback: ((call: Telephone.Call) -> Unit)? = null
        var onRingingCallCallback: ((call: Telephone.Call) -> Unit)? = null
        var onAnswerCallCallback: ((call: Telephone.Call) -> Unit)? = null
        var onReceivedDTMFCallback: ((call: Telephone.Call, key: String) -> Unit)? = null
        var onFinishCallCallback: ((call: Telephone.Call) -> Unit)? = null

        override fun onDialing(callback: (call: Telephone.Call) -> Unit): Telephone.Call {
            onDialingCallback = callback
            return this
        }

        override fun onRinging(callback: (call: Telephone.Call) -> Unit): Telephone.Call {
            onRingingCallCallback = callback
            return this
        }

        override fun onAnswerCall(callback: (call: Telephone.Call) -> Unit): Telephone.Call {
            onAnswerCallCallback = callback
            return this
        }

        override fun onReceivedDTMF(callback: (call: Telephone.Call, key: String) -> Unit): Telephone.Call {
            onReceivedDTMFCallback = callback
            return this
        }

        override fun onFinishCall(callback: (call: Telephone.Call) -> Unit): Telephone.Call {
            onFinishCallCallback = callback
            return this
        }

        override fun sendDTMF(keys: String) {
            val allowedChars = listOf(
                '1', '2', '3', '4', '5', '6', '7', '8',
                '9', '0', 'A', 'B', 'C', 'D', '*', '#'
            )
            for (keyChar in keys) {
                if (!allowedChars.contains(keyChar)) {
                    logger.warning("Illegal character for DTMF: $keyChar")
                    continue
                }
                sim868.writeAT("+VTS", keyChar)
            }
        }

        override fun hangUp() {
            sim868.executeAT("H")
        }
    }
}