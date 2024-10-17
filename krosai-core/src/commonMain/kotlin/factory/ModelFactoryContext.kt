package org.krosai.core.factory

import org.krosai.core.chat.function.FunctionCall

class ModelFactoryContext {

    private val factories = mutableMapOf<ModelFactoryBuilder<*, *>, ModelFactory>()

    // TODO: use interface for functionCallRegister
    private val functionCallRegister: MutableMap<String, FunctionCall> = mutableMapOf()

    private fun registerFunctionCall(vararg functionCall: FunctionCall) {
        functionCall.forEach {
            functionCallRegister[it.name] = it
        }
    }

    fun getFunctionCallsByName(functionNames: Set<String>): List<FunctionCall> {
        return functionNames.mapNotNull {
            functionCallRegister[it]
        }
    }

    fun functions(vararg functionCall: FunctionCall) {
        registerFunctionCall(*functionCall)
    }

    operator fun <F : ModelFactory> get(factoryKey: ModelFactoryBuilder<*, F>): F {
        val modelFactory =
            factories[factoryKey] ?: throw IllegalArgumentException("Model factory not found: $factoryKey")
        @Suppress("UNCHECKED_CAST")
        return modelFactory as F
    }

    @PublishedApi
    internal fun register(builder: ModelFactoryBuilder<*, *>, modelFactory: ModelFactory) {
        factories[builder] = modelFactory
    }

    inline fun <Builder : ModelFactoryBuilder<Config, M>, Config, M : ModelFactory> factory(
        factoryBuilder: Builder,
        noinline block: (Config.() -> Unit)? = null
    ) {
        val modelFactory = create(factoryBuilder, block)
        register(factoryBuilder, modelFactory)
    }

    inline fun <Builder : ModelFactoryBuilder<Config, M>, Config, M : ModelFactory> create(
        factoryBuilder: Builder,
        noinline block: (Config.() -> Unit)? = null,
    ): ModelFactory = factoryBuilder.createModelFactory(block, this)


}

fun buildModelFactoryContext(
    builder: ModelFactoryContext.() -> Unit
): ModelFactoryContext = ModelFactoryContext().apply(builder)
