package org.krosai.openai.factory

import io.ktor.client.*
import org.krosai.core.chat.function.FunctionCall
import org.krosai.core.chat.model.ChatModel
import org.krosai.core.embedding.model.EmbeddingModel
import org.krosai.core.factory.ModelFactory
import org.krosai.core.factory.ModelFactoryBuilder
import org.krosai.core.factory.ModelFactoryContext
import org.krosai.core.image.ImageModel
import org.krosai.openai.api.OpenAiApi
import org.krosai.openai.factory.OpenAiModelFactoryConfig.Companion.DefaultClientBlock
import org.krosai.openai.model.OpenAiChatModel
import org.krosai.openai.model.OpenAiEmbeddingModel
import org.krosai.openai.model.OpenAiImageModel

object OpenAi : ModelFactoryBuilder<OpenAiModelFactoryConfig, OpenAiModelFactory> {

    override val id: String = "OpenAi"

    override fun createConfig(): OpenAiModelFactoryConfig = OpenAiModelFactoryConfig()

    override fun build(config: OpenAiModelFactoryConfig, factoryContext: ModelFactoryContext?): OpenAiModelFactory =
        OpenAiModelFactory(config) {
            factoryContext?.getFunctionCallsByName(it) ?: emptyList()
        }

}

class OpenAiModelFactory(
    private val config: OpenAiModelFactoryConfig,
    private val getFunctionCall: (Set<String>) -> List<FunctionCall>,
) : ModelFactory {

    private val client: HttpClient by lazy {
        HttpClient {
            DefaultClientBlock()
            config.clientBlock(this)
        }
    }

    private val api by lazy {
        val apiKey = checkNotNull(config.apiKey) {
            "OpenAi API key is required"
        }
        // support custom base url
        val baseUrl = buildString {
            append(config.baseUrl)
            if (last() != '/') append('/')
            if (contains(Regex("/v[0-9]+/")).not()) append("v1/")
            if (last() != '/') append('/')
        }
        OpenAiApi(
            baseUrl,
            apiKey,
            client
        )
    }

    override fun createImageModel(): ImageModel =
        OpenAiImageModel(api, config.imageOptions)


    override fun createChatModel(): ChatModel =
        OpenAiChatModel(api, config.chatOptions, getFunctionCall)

    override fun createEmbeddingModel(): EmbeddingModel =
        OpenAiEmbeddingModel(api, config.embeddingOptions)

}

