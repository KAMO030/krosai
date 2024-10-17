package org.krosai.core.chat.enhancer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import org.krosai.core.chat.client.ChatClientRequest
import org.krosai.core.chat.memory.MessageStore
import org.krosai.core.chat.memory.get
import org.krosai.core.chat.memory.plusAssign
import org.krosai.core.chat.message.Message
import org.krosai.core.chat.model.ChatResponse

/**
 * This class represents a MessageChatMemoryEnhancer which implements the ChatMemorySupport and Enhancer interfaces.
 * It enhances the chat request and response by adding messages to the message store.
 *
 * @param messageStore The message store used to save and retrieve messages in a conversation.
 *
 * @author KAMOsama
 */
class MessageChatMemoryEnhancer(
    private val messageStore: MessageStore
) : ChatMemorySupport, Enhancer {

    override fun enhanceRequest(request: ChatClientRequest): ChatClientRequest {
        val conversationId = request.enhancerParams.getConversationId()
        val takeLastN = request.enhancerParams.getTakeLastN()
        request.messages += messageStore[conversationId to takeLastN]
        request.userTextTemplate?.invoke(request.userParams)?.let {
            messageStore += conversationId to Message.User(it)
        }
        return request
    }

    override fun enhanceResponse(response: ChatResponse, enhancerParams: Map<String, Any>): ChatResponse =
        response.also {
            val conversationId = enhancerParams.getConversationId()
            messageStore += conversationId to Message.User(it.content)
        }

    override fun enhanceResponse(
        responseFlow: Flow<ChatResponse>,
        enhancerParams: Map<String, Any>
    ): Flow<ChatResponse> {
        val stringBuilder = StringBuilder()
        return responseFlow
            .onEach { response ->
                stringBuilder.append(response.content)
            }.onCompletion { error ->
                if (error != null) return@onCompletion
                val conversationId = enhancerParams.getConversationId()
                val assistant = Message.Assistant(stringBuilder.toString())
                messageStore += conversationId to assistant
            }
    }

}