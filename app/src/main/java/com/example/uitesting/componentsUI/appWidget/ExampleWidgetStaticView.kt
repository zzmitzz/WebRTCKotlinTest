package com.example.uitesting.componentsUI.appWidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.uitesting.R

class ExampleWidgetStaticView : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Update all the Widget
        for(appWidget in appWidgetIds){
                appWidgetManager.updateAppWidget(appWidget, RemoteViews(context.packageName, R.layout.widget_static_view) )
            }
    }
}