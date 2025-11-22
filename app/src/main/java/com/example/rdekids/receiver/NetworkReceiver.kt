package com.example.rdekids.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.rdekids.remote.GoogleSheetsService
import com.example.rdekids.utils.NetworkUtils

class NetworkReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (NetworkUtils.hayInternet(context)) {
            GoogleSheetsService.reintentarEnvio(context)
        }
    }
}