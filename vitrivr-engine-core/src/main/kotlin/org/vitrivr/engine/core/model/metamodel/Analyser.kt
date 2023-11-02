package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import kotlin.reflect.KClass

/**
 * An [Analyser] is a formal specification of a type an analysis performed for a [Schema.Field] to derive a [Descriptor].
 *
 * - During indexing, the analysis step involves analysing the media content to derive a [Descriptor]
 * - During retrieval, the analysis step involves the execution of a query using the derived [Descriptor]s.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Analyser<C: ContentElement<*>, D: Descriptor> {
    /** The [KClass] of the [ContentElement] accepted by this [Analyser].  */
    val contentClass: KClass<C>

    /** The [KClass] of the [Descriptor] generated by this [Analyser].  */
    val descriptorClass: KClass<D>

    /**
     * Generates a specimen of the [Descriptor] produced / consumed by this [Analyser].
     *
     * This is a required operation.
     *
     * @return A [Descriptor] specimen of type [D].
     */
    fun prototype(): D

    /**
     * Generates and returns a new [Extractor] instance for this [Analyser].
     *
     * Some [Analyser]s may not come with their own [Extractor], in which case the implementation of this method should throw an [UnsupportedOperationException]
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     * @param persisting True, if the results of the [Extractor] should be persisted.
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    fun newExtractor(field: Schema.Field<C, D>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean = true, parameters: Map<String, Any> = emptyMap()): Extractor<C, D>

    /**
     * Generates and returns a new [Retriever] instance for this [Analyser].
     *
     * Some [Analyser]s may not come with their own [Retriever], in which case the implementation of this method should throw an [UnsupportedOperationException]
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [Descriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    fun newRetrieverForDescriptors(field: Schema.Field<C, D>, descriptors: Collection<D>, context: QueryContext): Retriever<C, D>

    /**
     * Generates and returns a new [Retriever] instance for this [Analyser].
     *
     * Some [Analyser]s may not come with their own [Retriever], in which case the implementation of this method should throw an [UnsupportedOperationException]
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [Content] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    fun newRetrieverForContent(field: Schema.Field<C, D>, content: Collection<C>, context: QueryContext): Retriever<C, D>
}