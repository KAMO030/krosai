package io.kamo.ktor.client.ai.core.chat.enhancer

/**
 * ChatMemorySupport is an interface that provides methods to retrieve conversation ID and number
 * of last messages from a map of conversation enhancerParams.
 *
 * @author KAMOsama
 */
interface ChatMemorySupport {
    companion object {
        /**
         * The key used to access the conversation ID in the enhancerParams map.
         */
        const val CONVERSATION_ID_KEY = "chat_memory_conversation_id"
        const val CONVERSATION_ID_DEFAULT = "default"

        /**
         * Memory key to retrieve the number of last messages to take.
         */
        const val TAKE_LAST_N_KEY = "chat_memory_take_last_n"
        const val TAKE_LAST_N_DEFAULT = 100
    }

    /**
     * Retrieves the conversation ID from a map of conversation enhancerParams.
     * If the conversation ID is not found or is not a String, the default conversation ID is returned.
     *
     * @return the conversation ID
     */
    fun Map<String, Any>.getConversationId(): String =
        get(CONVERSATION_ID_KEY) as? String ?: CONVERSATION_ID_DEFAULT

    /**
     * Retrieves the value of the 'chat_memory_take_last_n' key in a map.
     * the default value of 100 is returned.
     *
     * @return the value of the 'chat_memory_take_last_n' key as an integer
     */
    fun Map<String, Any>.getTakeLastN(): Int =
        get(TAKE_LAST_N_KEY).let {
            when (it) {
                is Int -> it
                is String -> it.toInt()
                is Number -> it.toInt()
                else -> TAKE_LAST_N_DEFAULT
            }
        }

}