package org.krosai.core.chat.client

import org.krosai.core.chat.enhancer.Enhancer
import org.krosai.core.chat.function.FunctionCall
import org.krosai.core.chat.message.Message
import org.krosai.core.chat.prompt.ChatOptions

/**
 * The data class representing a chat client request.
 *
 *
 * @property chatOptions The chat options for the request.
 * @property systemTextTemplate A lambda function that returns the system text for the request.
 * @property userTextTemplate   A lambda function that returns the user text for the request.
 * @property userParams The user parameters for the request.
 * @property systemParams The system parameters for the request.
 * @property functionCalls The list of function calls for the request.
 * @property functionNames The set of function names used in the request.
 * @property enhancers The list of enhancers for the request.
 * @property enhancerParams The enhancer parameters for the request.
 * @property messages The list of messages for the request.
 * @constructor Creates a new instance of [ChatClientRequest].
 *
 * @author KAMOsama
 */
data class ChatClientRequest(
    val chatOptions: ChatOptions,
    var systemTextTemplate: TextTemplate? = null,
    var userTextTemplate: TextTemplate? = null,
    val userParams: MutableMap<String, Any> = mutableMapOf(),
    val systemParams: MutableMap<String, Any> = mutableMapOf(),
    val functionCalls: MutableList<FunctionCall> = mutableListOf(),
    val functionNames: MutableSet<String> = mutableSetOf(),
    val enhancers: MutableList<Enhancer> = mutableListOf(),
    val enhancerParams: MutableMap<String, Any> = mutableMapOf(),
    val messages: MutableList<Message> = mutableListOf(),
) {
    constructor(clientRequest: ChatClientRequest) : this(
        chatOptions = clientRequest.chatOptions,
        systemTextTemplate = clientRequest.systemTextTemplate,
        userTextTemplate = clientRequest.userTextTemplate,
        functionNames = clientRequest.functionNames.toMutableSet(),
        functionCalls = clientRequest.functionCalls.toMutableList(),
        userParams = clientRequest.userParams.toMutableMap(),
        systemParams = clientRequest.systemParams.toMutableMap(),
        enhancers = clientRequest.enhancers.toMutableList(),
        enhancerParams = clientRequest.enhancerParams.toMutableMap(),
        messages = clientRequest.messages.toMutableList(),
    )

    fun copy(): ChatClientRequest = ChatClientRequest(this)

}
