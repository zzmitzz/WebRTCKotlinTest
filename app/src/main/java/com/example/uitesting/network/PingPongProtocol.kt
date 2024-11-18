package com.example.uitesting.network

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.sql.Time
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import kotlin.concurrent.schedule

enum class PingPongEventEnum {
    PING,
    RECONNECT
}


class PingPongProtocol @Inject constructor(
) {
    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineExceptionHandler { _, _ ->
        {}
    }
    private var clientSendHearBeatJob: Job? = null
    private var serverCheckHeartBeat: Job? = null

    private var pingInterval: Long = 1000
    private val _pingPongEffect = MutableSharedFlow<PingPongEventEnum>(
        0,
        1,
        BufferOverflow.SUSPEND
    )
    val pingPongEffect = _pingPongEffect.asSharedFlow()

    private var lastServerHeartBeat = AtomicLong(0)

    fun heartBeatHandshake() {
        if (pingInterval > 0) {
            shutdown()
            scheduleClientHeartbeat()
            scheduleServerHeartbeatCheck()
        }
    }

    fun shutdown() {
        clientSendHearBeatJob?.cancel()
        lastServerHeartBeat.set(0)
    }
    private fun scheduleServerHeartbeatCheck(){
        lastServerHeartBeat.set(System.currentTimeMillis())
        serverCheckHeartBeat = scope.launch {
            if(isActive){
                if(pingInterval>0){
                    delay(pingInterval)
                    checkServerHeartBeat()
                }
            }
        }
    }
    private fun checkServerHeartBeat(){
        val now = System.currentTimeMillis()
        // use a forgiving boundary as some heart beats can be delayed or lost.
        val boundary = now - MAX_SEND_PING_TIME
        // we need to check because the task could failed to abort
        if (lastServerHeartBeat.get() < boundary) {
            _pingPongEffect.tryEmit(PingPongEventEnum.RECONNECT)
        } else {
            lastServerHeartBeat.set(System.currentTimeMillis())
        }
    }

    fun skipCheckServerHeartBeat(){
        lastServerHeartBeat.set(System.currentTimeMillis())
        serverCheckHeartBeat?.cancel()
    }

    private fun scheduleClientHeartbeat() {
        clientSendHearBeatJob = scope.launch {
            if (isActive) {
                if (pingInterval > 0) {
                    while (true) {
                        _pingPongEffect.tryEmit(PingPongEventEnum.PING)
                        delay(pingInterval)
                    }
                }
            }
        }
    }

    companion object {
        private const val MAX_SEND_PING_TIME = 10000L
    }
}