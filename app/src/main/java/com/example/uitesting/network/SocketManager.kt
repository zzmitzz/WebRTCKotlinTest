package com.example.uitesting.network

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.timeout
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketDeflateExtension.Companion.install
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton


interface SocketActionInterface {
    fun getDataStream(): Flow<String>?
    fun sendAction(action: ChatMessage)
    fun close()
}

@Serializable
data class ChatMessage(
    val type: MessageType,
    val username: String,
    val message: String,
    val timeStamp: Long = System.currentTimeMillis()
) {
    enum class MessageType {
        JOIN,
        CHAT,
        LEAVE,
        PINGPONG
    }
}
@Singleton
class SocketManager @Inject constructor(
    @KtorSocketClient val socketClient: HttpClient,
    @ApplicationContext val context: Context,
    val pingPongProtocol: PingPongProtocol
) : SocketActionInterface {

    private var urlServer: String? = null
    private val _receivedMessage = MutableSharedFlow<String>()
    val receiveMessage = _receivedMessage
    fun startConnection(urlServer: String){
        this.urlServer = urlServer
        urlServer.let {
            scope.launch {
                initConnection()
            }
        }
    }

    private var scope: CoroutineScope =
        CoroutineScope(Dispatchers.IO) + CoroutineExceptionHandler { _, throwable ->
            run {
                Log.d("Error", "$throwable error")
            }
        }
    private var pingPongCoroutine = CoroutineScope(Dispatchers.IO) + CoroutineExceptionHandler { _, throwable ->
        run {
            Log.d("Error", "PingPong: $throwable error")
        }
    }
    private var webSocketSession: DefaultClientWebSocketSession? = null

    init {
        observeConnectionStatus()
    }
    private fun observeConnectionStatus(){
        scope.launch {
            pingPongProtocol.pingPongEffect.collect{
                when(it){
                    PingPongEventEnum.PING -> {
                        sendAction(
                            ChatMessage(
                                type = ChatMessage.MessageType.PINGPONG,
                                username = "Test",
                                message = "Ping Ping",
                                timeStamp = System.currentTimeMillis()
                            )
                        )

                    }

                    PingPongEventEnum.RECONNECT -> {

                    }
                }
            }
        }
    }

    private suspend fun initConnection() {
        socketClient.webSocket(
            urlString = urlServer!!,
            request = {
            }
        ){
            webSocketSession = this
            pingPongProtocol.heartBeatHandshake()
            getDataStream()?.collect{
                _receivedMessage.emit(it)
            }
        }
    }

    override fun getDataStream(): Flow<String>? {
        return if (webSocketSession == null) null
        else {
            flow {
                val message =
                    webSocketSession?.incoming?.consumeAsFlow()?.filterIsInstance<Frame.Text>()
                        ?.mapNotNull {
                            pingPongProtocol.skipCheckServerHeartBeat()
                            it.readText()
                        }
                emitAll(message ?: flow { })
            }
        }
    }

    override fun sendAction(action: ChatMessage) {
        scope.launch {
            try {
                if(webSocketSession?.isActive == true){
                    webSocketSession!!.outgoing.send(Frame.Text(Json.encodeToString(action)))
                }
                Log.d("DEBUG", webSocketSession!!.isActive.toString())
                Log.d("DEBUG", "Message sent")

            }catch (e: NullPointerException){
                throw e
            }
        }
    }

    override fun close() {
        scope.launch {
            webSocketSession?.cancel()
        }
    }

}