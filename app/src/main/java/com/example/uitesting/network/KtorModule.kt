package com.example.uitesting.network

import android.system.Os.accept
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Qualifier
import javax.inject.Singleton

import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.WebsocketContentConverter
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.charsets.Charset
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object KtorModule {

    @Singleton
    @Provides
    fun provideHttpClient() : HttpClient{
        return HttpClient(CIO){
            engine {
                requestTimeout = 0
                maxConnectionsCount = 1000
                endpoint {
                    connectTimeout = 10000
                    socketTimeout = 10000
                    keepAliveTime = 5000
                }
            }
        }
    }

    @KtorSocketClient
    @Provides
    fun provideSocketClient(httpClient: HttpClient): HttpClient{
        return httpClient.config {
            install(WebSockets) {
                maxFrameSize = Long.MAX_VALUE
                pingInterval = 500
            }
            install(HttpTimeout){
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 30000
                socketTimeoutMillis = 30000
            }
            install(ContentNegotiation) {
                register(ContentType.Application.Json, KotlinxSerializationConverter(Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                }))
            }
            install(Logging){
                level = LogLevel.ALL
            }

        }
    }

}


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KtorSocketClient

