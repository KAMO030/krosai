package io.kamo.ktor.client.ai.samples.data

import androidx.compose.runtime.MutableState

data class ChatMessage(
    val textState: MutableState<String>,
    val isUser: Boolean
)