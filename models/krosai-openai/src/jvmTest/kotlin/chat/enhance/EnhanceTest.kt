package org.krosai.openai.test.chat.enhance

import kotlinx.coroutines.test.runTest
import org.krosai.core.chat.enhancer.ChatMemorySupport
import org.krosai.core.chat.enhancer.MessageChatMemoryEnhancer
import org.krosai.core.chat.memory.InMemoryMessageStore
import org.krosai.core.factory.createModelFactory
import org.krosai.openai.factory.OpenAi
import org.krosai.openai.test.ModelFactorySupport
import org.krosai.openai.test.chat.function.dateTimeFun
import kotlin.test.Test

class EnhanceTest : ModelFactorySupport {

    @Test
    fun enhanceCallTest() = runTest {
        val chatClient = factory.createChatClient {
            enhancers {
                +MessageChatMemoryEnhancer(InMemoryMessageStore())
            }
        }
        chatClient.call {
            userText { "Please call me Kamo." }
        }.let {
            println("content: ${it.content}")
        }
        OpenAi.createModelFactory().createChatClient {

        }
        chatClient.call {
            userText { "Please answer in Japanese" }
        }.let {
            println("content: ${it.content}")
        }
    }

    fun then() {

    }
    @Test
    fun enhanceStreamTest() = runTest {
        val chatClient = factory.createChatClient {
            enhancers {
                +MessageChatMemoryEnhancer(InMemoryMessageStore())
            }
        }
        chatClient.stream {
            userText { "Please call me Kamo." }
        }.collect {
            println("content: ${it.content}")
        }


        chatClient.stream {
            userText { "Please answer in Japanese" }
        }.collect {
            println("content: ${it.content}")
            assert(it.content.contains("Kamo"))
        }

    }

    @Test
    fun enhanceParamsTest() = runTest {
        val chatClient = factory.createChatClient {
            enhancers {
                +MessageChatMemoryEnhancer(InMemoryMessageStore())
            }
        }
        chatClient.call {
            userText { "Please call me Kamo." }
            functions {
                +dateTimeFun
            }
            enhancers {
                ChatMemorySupport.CONVERSATION_ID_KEY to "test"
            }
        }.let {
            println("content: ${it.content}")
        }


        chatClient.call {
            userText { "Who am I" }
            enhancers {
                // if you want to use default conversation id, you can use this.
                ChatMemorySupport.CONVERSATION_ID_KEY to "test"
            }
        }.let {
            println("content: ${it.content}")
            assert(it.content.contains("Kamo"))
        }

    }

}
