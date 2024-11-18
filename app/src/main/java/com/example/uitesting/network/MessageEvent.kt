package com.example.uitesting.network

import kotlinx.serialization.Serializable


@Serializable
data class MessageEvent(
    val message: String)