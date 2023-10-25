package org.vitrivr.engine.core.context

import kotlinx.serialization.Serializable

@Serializable
data class QueryContext(
    /** properties applicable to all operators */
    private val global: Map<String, String>,
    /** properties per operator*/
    private val local: Map<String, Map<String, String>>
) {

    fun getProperty(operator: String, property: String): String? = local[operator]?.get(property) ?: global[property]

}
