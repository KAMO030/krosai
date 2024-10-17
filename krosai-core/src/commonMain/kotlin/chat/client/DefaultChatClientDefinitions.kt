package org.krosai.core.chat.client

import kotlinx.coroutines.flow.Flow
import org.krosai.core.chat.enhancer.Enhancer
import org.krosai.core.chat.enhancer.enhancing
import org.krosai.core.chat.function.FunctionCall
import org.krosai.core.chat.function.FunctionCallOptions
import org.krosai.core.chat.message.Message
import org.krosai.core.chat.model.ChatModel
import org.krosai.core.chat.model.ChatResponse
import org.krosai.core.chat.prompt.ChatOptions
import org.krosai.core.chat.prompt.Prompt
import kotlin.reflect.KFunction


class DefaultChatClient(
    private val chatModel: ChatModel,
    private val defaultRequest: DefaultChatClientRequestScope,
) : ChatClient {

    override suspend fun call(requestScopeSpec: ChatClientRequestDefinition?): ChatResponse {
        val requestScope = defaultRequest.copy()
            .also { requestScopeSpec?.invoke(it) }

        var request = requestScope.chatClientRequest
        val enhancers = request.enhancers
        request = enhancers.enhancing(
            request,
            Enhancer::enhanceRequest
        )

        val prompt = createPrompt(request)


        return enhancers.enhancing(chatModel.call(prompt)) { response ->
            enhanceResponse(response, request.enhancerParams)
        }
    }

    override suspend fun stream(requestScopeSpec: ChatClientRequestDefinition?): Flow<ChatResponse> {
        val requestScope = defaultRequest.copy()
            .also { requestScopeSpec?.invoke(it) }

        var request = requestScope.chatClientRequest
        val enhancers = request.enhancers
        request = enhancers.enhancing(
            request,
            Enhancer::enhanceRequest
        )

        val prompt = createPrompt(request)

        return enhancers.enhancing(chatModel.stream(prompt)) { responseFlow ->
            enhanceResponse(responseFlow, request.enhancerParams)
        }

    }


    private fun createPrompt(request: ChatClientRequest): Prompt {

        val messages: List<Message> = request.messages + listOfNotNull(
            request.systemTextTemplate?.invoke(request.systemParams)?.let { Message.System(it) },
            request.userTextTemplate?.invoke(request.userParams)?.let { Message.User(it) },
        )
        if (request.chatOptions is FunctionCallOptions) {
            request.chatOptions.functionCalls.addAll(request.functionCalls)
            request.chatOptions.functionNames.addAll(request.functionNames)
        }
        val prompt = Prompt(
            instructions = messages,
            options = request.chatOptions
        )
        return prompt
    }

}


class DefaultChatClientRequestScope(
    val model: ChatModel,
    val chatClientRequest: ChatClientRequest,
) : ChatClientRequestScope {

    constructor(model: ChatModel, chatOptions: ChatOptions?) : this(
        model = model,
        chatClientRequest = ChatClientRequest(
            chatOptions = chatOptions?.copy() ?: model.defaultChatOptions.copy()
        ),
    )

    constructor(other: DefaultChatClientRequestScope) : this(
        model = other.model,
        chatClientRequest = other.chatClientRequest.copy()
    )

    fun copy(): DefaultChatClientRequestScope = DefaultChatClientRequestScope(this)

    override fun userText(template: TextTemplate) {
        chatClientRequest.userTextTemplate = template
    }

    override fun systemText(template: TextTemplate) {
        chatClientRequest.userTextTemplate = template
    }

    override fun user(userScope: PromptUserScope.() -> Unit) {
        DefaultPromptUserScope(this).apply(userScope)
    }

    override fun system(systemScope: PromptSystemScope.() -> Unit) {
        DefaultPromptSystemScope(this).apply(systemScope)
    }

    override fun enhancers(enhancersScope: EnhancersScope.() -> Unit) {
        DefaultEnhancersScope(this).apply(enhancersScope)
    }

    override fun functions(functionCallScope: FunctionCallScope.() -> Unit) {
        DefaultFunctionCallScope(this).apply(functionCallScope)
    }

}

class DefaultEnhancersScope(
    chatClientRequestScope: DefaultChatClientRequestScope,
) : EnhancersScope {

    private val params: MutableMap<String, Any>
            by chatClientRequestScope.chatClientRequest::enhancerParams

    private val enhancers: MutableList<Enhancer>
            by chatClientRequestScope.chatClientRequest::enhancers

    override fun String.to(value: Any) {
        params[this] = value
    }

    override fun Enhancer.unaryPlus() {
        enhancers.add(this)
    }

    override fun List<Enhancer>.unaryPlus() {
        enhancers.addAll(this)
    }

}

class DefaultPromptUserScope(
    chatClientRequestScope: DefaultChatClientRequestScope,
) : PromptUserScope {

    private var template: TextTemplate?
            by chatClientRequestScope.chatClientRequest::userTextTemplate

    private val params: MutableMap<String, Any>
            by chatClientRequestScope.chatClientRequest::userParams

    override fun text(template: TextTemplate) {
        this.template = template
    }

    override fun String.to(value: Any) {
        params[this] = value
    }

}

class DefaultPromptSystemScope(
    chatClientRequestScope: DefaultChatClientRequestScope,
) : PromptSystemScope {

    private var template: TextTemplate?
            by chatClientRequestScope.chatClientRequest::systemTextTemplate

    private val params: MutableMap<String, Any>
            by chatClientRequestScope.chatClientRequest::systemParams

    override fun text(template: TextTemplate) {
        this.template = template
    }

    override fun String.to(value: Any) {
        params[this] = value
    }

}

class DefaultFunctionCallScope(
    chatClientRequestScope: DefaultChatClientRequestScope,
) : FunctionCallScope {

    private val functionCalls: MutableList<FunctionCall>
            by chatClientRequestScope.chatClientRequest::functionCalls

    private val functionNames: MutableSet<String>
            by chatClientRequestScope.chatClientRequest::functionNames

    override fun FunctionCall.unaryPlus() {
        functionCalls.add(this)
    }

    override fun List<FunctionCall>.unaryPlus() {
        functionCalls.addAll(this)
    }

    override fun String.unaryPlus() {
        functionNames.add(this)
    }

    override fun KFunction<*>.unaryPlus() {
        functionNames.add(name)
    }

    override fun function(vararg functionNames: String) {
        this.functionNames.addAll(functionNames)
    }

}