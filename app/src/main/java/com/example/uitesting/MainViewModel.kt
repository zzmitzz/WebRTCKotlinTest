package com.example.uitesting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uitesting.network.ChatMessage
import com.example.uitesting.network.MessageEvent
import com.example.uitesting.network.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val socketManager: SocketManager
): ViewModel() {

    private var _textReceive = MutableLiveData<String>("")
    val textReceive = _textReceive
    fun initSocketConnection(){
        socketManager.startConnection("ws://172.20.19.112:443")
    }

    fun observeDataStream(){
        viewModelScope.launch {
            socketManager.receiveMessage.collect{ data ->
                _textReceive.postValue(data)
            }
        }
    }

    fun sendMessage(message: String){
        socketManager.sendAction(ChatMessage(
            type = ChatMessage.MessageType.CHAT,
            message = message,
            username = "Test ${listOf(1 .. 100).random()}",
            timeStamp = System.currentTimeMillis()
        ))
    }
}