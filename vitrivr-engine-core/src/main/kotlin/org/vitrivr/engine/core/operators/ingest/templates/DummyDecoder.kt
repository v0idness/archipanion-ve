package org.vitrivr.engine.core.operators.ingest.templates

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.source.Source

private val logger: KLogger = KotlinLogging.logger {}

/***
 * A Template for a [Decoder].
 *
 * @author Raphael Waltenspül
 * @version 1.0
 */
class DummyDecoder(
    override val input: Operator<Source>,
    val parameters: Map<String, Any>
) : Decoder {
    override fun toFlow(scope: CoroutineScope): Flow<ContentElement<*>> {
        return this.input.toFlow(scope).map { value: Source ->
            logger.info { "Performed Dummy Decoder with options ${parameters} on ${value}" }
            value as ContentElement<*>
        }
    }
}