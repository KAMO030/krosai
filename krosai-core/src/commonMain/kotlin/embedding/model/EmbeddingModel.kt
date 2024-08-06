@file:Suppress("unused")

package io.github.krosai.core.embedding.model

interface EmbeddingModel {

    fun embed(vararg texts: String): List<List<Double>>

    fun embed(text: String): List<Double> = this.embed(*arrayOf(text)).first()

}