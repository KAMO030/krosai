package org.krosai.openai.factory

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.serialization.kotlinx.json.*
import org.krosai.core.util.DefaultJsonConverter
import org.krosai.openai.options.OpenAiChatOptions
import org.krosai.openai.options.OpenAiEmbeddingOptions
import org.krosai.openai.options.OpenAiImageOptions

class OpenAiModelFactoryConfig {

    companion object {
        val DefaultClientBlock: HttpClientConfig<*>.() -> Unit = {
            install(ContentNegotiation) {
                json(DefaultJsonConverter)
            }
            install(SSE)
            expectSuccess = true
        }
    }

    var clientBlock: HttpClientConfig<*>.() -> Unit = {}


    var baseUrl: String = "https://api.openai.com"

    var apiKey: String? = null

    @PublishedApi
    internal val chatOptions: OpenAiChatOptions = OpenAiChatOptions()

    @PublishedApi
    internal val embeddingOptions: OpenAiEmbeddingOptions = OpenAiEmbeddingOptions()

    @PublishedApi
    internal val imageOptions: OpenAiImageOptions = OpenAiImageOptions()

    inline fun chatOptions(build: OpenAiChatOptions.() -> Unit) = chatOptions.apply(build)

    inline fun embeddingOptions(build: OpenAiEmbeddingOptions.() -> Unit) = embeddingOptions.apply(build)

    inline fun imageOptions(build: OpenAiImageOptions.() -> Unit) = imageOptions.apply(build)

}