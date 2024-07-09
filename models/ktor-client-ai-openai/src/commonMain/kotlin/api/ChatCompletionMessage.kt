package io.kamo.ktor.client.ai.openai.api

import io.kamo.ktor.client.ai.core.message.MessageType
import io.kamo.ktor.client.ai.openai.api.ChatCompletionMessage.Role
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Message comprising the conversation.
 *
 * @param content The contents of the message..
 * The response message content is always a [String].
 * @param role The role of the messages author. Could be one of the [Role] types.
 * @param name An optional name for the participant. Provides the model information to differentiate between
 * participants of the same role. In case of Function calling, the name is the function name that the message is
 * responding to.
 * @param toolCallId Tool call that this message is responding to. Only applicable for the [Role.TOOL] role
 * and null otherwise.
 * @param toolCalls The tool calls generated by the model, such as function calls. Applicable only for
 * [Role.TOOL] role and null otherwise.
 */
@Serializable
data class ChatCompletionMessage(
    @SerialName("content") val content: String? = null,
    @SerialName("role") val role: Role = Role.ASSISTANT ,
    @SerialName("name") val name: String? = null,
    @SerialName("tool_call_id") val toolCallId: String? = null,
    @SerialName("tool_calls") val toolCalls: List<ToolCall>? = null,
) {

    /**
     * The role of the author of this message.
     */
    @Serializable
    enum class Role(val value: String) {
        /**
         * System message.
         */
        @SerialName("system")
        SYSTEM("system"),

        /**
         * User message.
         */
        @SerialName("user")
        USER("user"),

        /**
         * Assistant message.
         */
        @SerialName("assistant")
        ASSISTANT("assistant"),

        /**
         * Tool message.
         */
        @SerialName("tool")
        TOOL("tool");

        companion object {
            fun fromMessageType(messageType: MessageType): Role {
                if (MessageType.FUNCTION == messageType) return TOOL
                return entries.first { it.value == messageType.value }
            }
        }

    }

    /**
     * The relevant tool call.
     *
     * @param id The ID of the tool call. This ID must be referenced when you submit the tool outputs in using the
     * Submit tool outputs to run endpoint.
     * @param type The type of tool call the output is required for. For now, this is always function.
     * @param function The function definition.
     */
    @Serializable
    data class ToolCall(
        @SerialName("id") val id: String,
        @SerialName("type") val type: String,
        @SerialName("function") val function: ChatCompletionFunction
    )

    /**
     * The function definition.
     *
     * @param name The name of the function.
     * @param arguments The arguments that the model expects you to pass to the function.
     */
    @Serializable
    data class ChatCompletionFunction(
        @SerialName("name") val name: String,
        @SerialName("arguments") val arguments: String
    )

}