package com.example.uitesting

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.uitesting.databinding.ItemTextBinding

class ChatAdapter(

) : RecyclerView.Adapter<ViewHolder>(){

    private val messageCapability: MutableList<String> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun addNew(message: String){
        messageCapability.add(message)
        notifyDataSetChanged()
    }
    inner class TextViewHolder(private val binding: ItemTextBinding): ViewHolder(binding.root){
        fun bind(text: String){
            binding.text.text = text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TextViewHolder( ItemTextBinding.inflate(layoutInflater,parent,false))
    }

    override fun getItemCount(): Int  = messageCapability.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (holder as TextViewHolder).bind(messageCapability[position])
    }
}