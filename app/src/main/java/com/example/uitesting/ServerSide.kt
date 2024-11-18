package com.example.uitesting

package org.example

import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import java.util.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.util.concurrent.ConcurrentHashMap
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.server.plugins.callloging.*
import kotlinx.coroutines.isActive
import org.slf4j.event.Level

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

object ChatMessageSerializer {
    private val mapper = jacksonObjectMapper()

    fun serializer(data: ChatMessage): String {
        return mapper.writeValueAsString(data)
    }

    fun deserializer(incomeMessage: String): ChatMessage {
        return mapper.readValue<ChatMessage>(incomeMessage)
    }
}

class ChatServerMessage() {
    val connections = ConcurrentHashMap<String, DefaultWebSocketSession>()

    suspend fun handleMessageRequest(chatMessage: ChatMessage, newSession: DefaultWebSocketSession) {
        println(chatMessage.message)
        when (chatMessage.type) {
            ChatMessage.MessageType.PINGPONG -> {
                println("PING from ${chatMessage.username}")
                newSession.send(Frame.Text(ChatMessageSerializer.serializer(
                    ChatMessage(
                        type = ChatMessage.MessageType.PINGPONG,
                        message = "PONG + ${(System.currentTimeMillis() - chatMessage.timeStamp)}",
                        username = "System",
                        timeStamp = System.currentTimeMillis()
                    )
                )))
            }

            ChatMessage.MessageType.JOIN -> {
                val username = chatMessage.username
                val session = newSession
                if (!connections.containsKey(username)) {
                    connections[username] = session
                }
            }

            ChatMessage.MessageType.CHAT -> {
                val username = chatMessage.username
                val session = newSession
                if (!connections.containsKey(username)) {
                    connections[username] = session
                }
                println("Receive + ${chatMessage.message} from ${chatMessage.username}")
                broadcast(chatMessage)
            }

            ChatMessage.MessageType.LEAVE -> {
                val username = chatMessage.username
                val session = connections[username]
                if (session != null && session.isActive) {
                    connections[username] = session
                }
                broadcast(
                    ChatMessage(
                        type = ChatMessage.MessageType.CHAT,
                        username = "System",
                        message = "$username left the chat"
                    )
                )
            }
        }
    }

    suspend fun broadcast(chatMessage: ChatMessage) {
        connections.values.forEach {
            it.send(Frame.Text("${chatMessage.message} from ${chatMessage.username}"))
        }
    }
}

// Main function to start the server
fun main() {
    embeddedServer(Netty, port = 443) {
        module()
    }.start(wait = true)
}

fun Application.module() {

    val chatManager = ChatServerMessage()

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    install(CallLogging) {
        level = Level.INFO
    }
    routing {
        webSocket {
            println("New Connected")
            for (frame in incoming) {
                if (frame !is Frame.Text) continue
                val messageRequest = frame.readText()
                println(messageRequest)
                if (messageRequest.contains("PING")) {
                    this.send("PONG")
                } else {
                    try {
                        val chatMessage = ChatMessageSerializer.deserializer(messageRequest)
                        chatManager.handleMessageRequest(chatMessage, this)
                    }catch (e: Exception){
                        println(e)
                    }
                }
            }
            println("Disconnected")
        }
    }
}