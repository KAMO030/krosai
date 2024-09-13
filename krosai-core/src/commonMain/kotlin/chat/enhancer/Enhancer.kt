package org.krosai.core.chat.enhancer

import kotlinx.coroutines.flow.Flow
import org.krosai.core.chat.client.ChatClientRequest
import org.krosai.core.chat.model.ChatResponse

/**
 * The Enhancer interface defines the operations related to enhancing the chat request and response.
 *
 * @author KAMOsama
 */
interface Enhancer {

    /**
     * Enhance the request.
     * @param request The request to enhance.
     * @return The enhanced request.
     */
    fun enhanceRequest(request: ChatClientRequest): ChatClientRequest = request

    /**
     * Enhance the response.
     * @param response The response to enhance.
     * @param enhancerParams  taken from the enhancerParams of the request parameter.
     * @return The enhanced response.
     */
    fun enhanceResponse(response: ChatResponse, enhancerParams: Map<String, Any>): ChatResponse =
        response

    /**
     * Enhance the response flow.
     * @param responseFlow The response flow to enhance.
     * @param enhancerParams  taken from the enhancerParams of the request parameter.
     * @return The enhanced response flow.
     */
    fun enhanceResponse(
        responseFlow: Flow<ChatResponse>,
        enhancerParams: Map<String, Any>
    ): Flow<ChatResponse> = responseFlow

}

/**
 * Extension function for enhancing a list of enhancers.
 */
internal fun <T> List<Enhancer>.enhancing(
    default: T,
    enhance: Enhancer.(T) -> T
): T {
    var enhanced = default
    this.toList().forEach {
        enhanced = it.enhance(enhanced)
    }
    return enhanced
}