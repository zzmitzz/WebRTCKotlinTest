package com.example.uitesting.componentsUI.appWidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.uitesting.databinding.ActivityListWidgetsBinding

class ListAppWidget : AppCompatActivity() {

    private lateinit var widgetManager: AppWidgetManager
    private lateinit var widgetProvider: MutableList<AppWidgetProviderInfo>
    private lateinit var mWidgetAdapter: ItemWidgetAdapter
    private val binding by lazy {
        ActivityListWidgetsBinding.inflate(layoutInflater, null, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initAllWidgets()
        initRcv()
    }
    private fun initRcv() {
        Log.d("TEST", widgetProvider.size.toString())
        mWidgetAdapter = ItemWidgetAdapter(widgetProvider).apply {
            callbackPinWidget = {
                it.pin(this@ListAppWidget)
            }
        }
        binding.recyclerView.apply {
            adapter = mWidgetAdapter
            setHasFixedSize(true)
        }

    }
    private fun initAllWidgets() {
        widgetManager = AppWidgetManager.getInstance(this)
        widgetProvider = widgetManager.getInstalledProvidersForPackage(packageName, null)
    }

    private fun AppWidgetProviderInfo.pin(context: Context) {
        val successCallback = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, AppWidgetPinnedReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        AppWidgetManager.getInstance(context).requestPinAppWidget(provider, null, successCallback)
    }
}