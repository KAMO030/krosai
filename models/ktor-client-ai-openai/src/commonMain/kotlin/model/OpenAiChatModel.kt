package io.kamo.ktor.client.ai.openai.model

import io.kamo.ktor.client.ai.core.chat.model.ChatModel
import io.kamo.ktor.client.ai.core.chat.model.ChatResponse
import io.kamo.ktor.client.ai.core.chat.model.Generation
import io.kamo.ktor.client.ai.core.chat.prompt.Prompt
import io.kamo.ktor.client.ai.openai.api.ChatCompletion
import io.kamo.ktor.client.ai.openai.api.ChatCompletionChunk
import io.kamo.ktor.client.ai.openai.api.ChatCompletionRequest
import io.kamo.ktor.client.ai.openai.config.OpenAiOptions
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
/** OpenAI Chat API implementation. */

/**
 * Whether to stream back partial progress.
 * If set, tokens will be sent as data-only server-sent events as they become available,
 * with the stream terminated by a data: [[DONE]] message.
 */
private val SSE_PREDICATE :(String)-> Boolean = { it.isNotEmpty() && it != "[DONE]" }

class OpenAiChatModel(
    private val options: OpenAiOptions,
    private val httpClient: HttpClient
) : ChatModel {

    private val requestUrl = "${options.baseUrl}/v1/chat/completions"

    override suspend fun call(request: Prompt): ChatResponse {
        return httpClient.post {
            url(requestUrl)
            contentType(ContentType.Application.Json)
            setBody(ChatCompletionRequest.build(options, request, false))
            bearerAuth(options.apiKey)
        }.body<ChatCompletion>().toChatResponse()

    }

    override suspend fun stream(request: Prompt): Flow<ChatResponse> {
        return httpClient.serverSentEventsSession {
            url(requestUrl)
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
            setBody(ChatCompletionRequest.build(options, request, true))
            bearerAuth(options.apiKey)
        }.incoming
            .mapNotNull { it.data }
            .filter(SSE_PREDICATE)
            .map {
                Json.decodeFromString<ChatCompletionChunk>(it)
                    .toChatCompletion()
                    .toChatResponse()
            }

    }


}

fun ChatCompletionChunk.toChatCompletion(): ChatCompletion {
    val choices = choices.map { cc ->
        ChatCompletion.Choice(cc.finishReason, cc.index, cc.delta, cc.logprobs)
    }
    return ChatCompletion(
        id = id,
        choices = choices,
        created = created,
        model = model,
        systemFingerprint = systemFingerprint,
        obj = "chat.completion",
        usage = usage,
    )
}

fun ChatCompletion.toChatResponse(): ChatResponse {
    return ChatResponse(
        choices.map {
            Generation(it.message.content.orEmpty())
        }
    )
}

//{
//  "id": "chatcmpl-9hBQDNOWPnolxTAHvScpoE4BltBR8",
//  "object": "chat.completion",
//  "created": 1720079037,
//  "model": "gpt-4o-2024-05-13",
//  "choices": [
//    {
//      "index": 0,
//      "message": {
//        "role": "assistant",
//        "content": "你好！有什么我可以帮你的吗？"
//      },
//      "logprobs": null,
//      "finish_reason": "stop"
//    }
//  ],
//  "usage": {
//    "prompt_tokens": 8,
//    "completion_tokens": 9,
//    "total_tokens": 17
//  },
//  "system_fingerprint": "fp_d576307f90"
//}