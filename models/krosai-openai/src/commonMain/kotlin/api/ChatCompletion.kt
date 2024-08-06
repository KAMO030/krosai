package io.github.krosai.openai.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a chat completion response returned by model, based on the provided input.
 *
 * @param id A unique identifier for the chat completion.
 * @param choices A list of chat completion choices. Can be more than one if n is greater than 1.
 * @param created The Unix timestamp (in seconds) of when the chat completion was created.
 * @param model The model used for the chat completion.
 * @param systemFingerprint This fingerprint represents the backend configuration that the model runs with. Can be
 * used in conjunction with the seed request parameter to understand when backend changes have been made that might
 * impact determinism.
 * @param obj The object type, which is always chat.completion.
 * @param usage Usage statistics for the completion request.
 */
@Serializable
data class ChatCompletion(
    @SerialName("id") val id: String,
    @SerialName("choices") val choices: List<Choice>,
    @SerialName("created") val created: Long,
    @SerialName("model") val model: String,
    @SerialName("system_fingerprint") val systemFingerprint: String?,
    @SerialName("object") val obj: String,
    @SerialName("usage") val usage: Usage? = null
) {

    /**
     * Chat completion choice.
     *
     * @param finishReason The reason the model stopped generating tokens.
     * @param index The index of the choice in the list of choices.
     * @param message A chat completion message generated by the model.
     * @param logprobs Log probability information for the choice.
     */
    @Serializable
    data class Choice(
        @SerialName("finish_reason") val finishReason: ChatCompletionFinishReason?,
        @SerialName("index") val index: Int,
        @SerialName("message") val message: ChatCompletionMessage,
        @SerialName("logprobs") val logprobs: LogProbs? = null
    )

}