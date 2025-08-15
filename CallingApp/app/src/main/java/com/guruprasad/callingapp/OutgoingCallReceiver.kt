package com.guruprasad.callingapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class OutgoingCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val number = intent?.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
        Log.d("OutgoingCallReceiver", "Outgoing call detected: $number")
        MyPhoneStateListener.lastDialedNumber = number
    }
}
