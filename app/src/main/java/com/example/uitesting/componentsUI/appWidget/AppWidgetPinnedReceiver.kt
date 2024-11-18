package com.example.uitesting.componentsUI.appWidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AppWidgetPinnedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "Successfully add widget", Toast.LENGTH_LONG).show()
    }

}
