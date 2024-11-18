package com.example.uitesting.componentsUI.appWidget

import android.appwidget.AppWidgetProviderInfo
import android.text.Layout
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.uitesting.databinding.ItemViewTypeBinding



class ItemWidgetAdapter(a : List<AppWidgetProviderInfo>): RecyclerView.Adapter<ItemWidgetAdapter.WidgetAdapterVH>() {

    private val listWidgetType: List<AppWidgetProviderInfo> = a

    lateinit var callbackPinWidget: (AppWidgetProviderInfo) -> Unit
    inner class WidgetAdapterVH(private val binding: ItemViewTypeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppWidgetProviderInfo) {
            binding.tvUI.apply {
                text = item.loadLabel(context.packageManager)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetAdapterVH {
        val layoutInflater = LayoutInflater.from(parent.context)
        return WidgetAdapterVH(ItemViewTypeBinding.inflate(layoutInflater,parent,false))
    }

    override fun getItemCount(): Int = listWidgetType.size

    override fun onBindViewHolder(holder: WidgetAdapterVH, position: Int) {
        holder.bind(listWidgetType[position])
        holder.itemView.setOnClickListener {
            callbackPinWidget.invoke(listWidgetType[position])
        }
    }
}