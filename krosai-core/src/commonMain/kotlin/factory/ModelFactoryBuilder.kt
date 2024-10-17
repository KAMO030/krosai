package org.krosai.core.factory

import org.krosai.core.chat.client.ChatClient
import org.krosai.core.chat.client.ChatClientRequestDefinition
import org.krosai.core.chat.client.DefaultChatClient
import org.krosai.core.chat.client.DefaultChatClientRequestScope
import org.krosai.core.chat.model.ChatModel
import org.krosai.core.embedding.model.EmbeddingModel
import org.krosai.core.image.ImageModel

/**
 * The interface for a factory that creates different models and a chat client.
 *
 *
 * @author KAMOsama
 */
interface ModelFactory {

    fun createChatModel(): ChatModel

    fun createEmbeddingModel(): EmbeddingModel

    fun createImageModel(): ImageModel

    fun createChatClient(scope: ChatClientRequestDefinition? = null): ChatClient {
        val chatModel = createChatModel()
        val defaultRequest = DefaultChatClientRequestScope(chatModel, null).also { scope?.invoke(it) }
        return DefaultChatClient(chatModel, defaultRequest)
    }

}

interface ModelFactoryBuilder<Config, M : ModelFactory> {

    val id: String

    fun createConfig(): Config

    fun build(factoryContext: ModelFactoryContext, config: Config): M

}

fun <Config : Any, M : ModelFactory> createModelFactory(
    id: String,
    configBuilder: () -> Config,
    builder: Config.(context: ModelFactoryContext) -> M
): ModelFactoryBuilder<Config, M> {
    return object : ModelFactoryBuilder<Config, M> {

        override val id: String = id

        override fun createConfig(): Config = configBuilder()

        override fun build(factoryContext: ModelFactoryContext, config: Config): M =
            builder.invoke(config, factoryContext)

    }
}

