package com.guruprasad.callingapp

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast

class MyPhoneStateListener(private val context: Context) : PhoneStateListener() {

    companion object {
        var lastDialedNumber: String? = null
    }


    private var lastState = TelephonyManager.CALL_STATE_IDLE
    private var isOutgoing = false

//    override fun onCallStateChanged(state: Int, incomingNumber: String?) {
//        super.onCallStateChanged(state, incomingNumber)
//
//        when (state) {
//            TelephonyManager.CALL_STATE_OFFHOOK -> {
//                isOutgoing = true
//                Log.d("CallListener", "Call started to: ${lastDialedNumber}")
//            }
//
//            TelephonyManager.CALL_STATE_IDLE -> {
//                if (lastState == TelephonyManager.CALL_STATE_OFFHOOK && isOutgoing) {
//                    lastDialedNumber?.let {
//                        sendSMS(it)
//                    }
//                    isOutgoing = false
//                }
//            }
//        }
//
//        lastState = state
//    }

    override fun onCallStateChanged(state: Int, incomingNumber: String?) {
        super.onCallStateChanged(state, incomingNumber)

        if (state == TelephonyManager.CALL_STATE_IDLE && lastState == TelephonyManager.CALL_STATE_OFFHOOK) {

            lastDialedNumber?.let {
                Log.d("CallListener", "Call ended. Sending SMS to: $it")
                sendSMS(it)
                lastDialedNumber = null // Reset
            }
        }

        lastState = state
    }


    private fun sendSMS(number: String) {
        val cleanedNumber = number.trim()

        if (cleanedNumber.isEmpty() || cleanedNumber.equals("null", true)) {
            Log.e("CallListener", "Invalid number: '$cleanedNumber'")
            return
        }

        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(cleanedNumber, null, "Thanks for calling!", null, null)
            Toast.makeText(context, "SMS sent to $cleanedNumber", Toast.LENGTH_SHORT).show()
            Log.d("CallListener", "SMS sent to $cleanedNumber")
        } catch (e: Exception) {
            Log.e("CallListener", "SMS sending failed: ${e.message}")
            e.printStackTrace()
        }
    }

}
