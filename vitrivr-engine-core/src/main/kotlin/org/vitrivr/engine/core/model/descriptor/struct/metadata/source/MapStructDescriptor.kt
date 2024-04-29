package org.vitrivr.engine.core.model.descriptor.struct.metadata.source

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import java.util.*
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.internal.impl.types.TypeCheckerState.SupertypesPolicy.None

data class MapStructDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val columnTypes: Map<String, String>,
    val columnValues: Map<String, Any?>,
    override val transient: Boolean = false
) : StructDescriptor {

    companion object{
        fun prototype(columnTypes: Map<String, String>): MapStructDescriptor {
            val columnValues = columnTypes.mapValues { (_, type) ->
                when (Type.valueOf(type)) {
                    Type.STRING -> ""
                    Type.BOOLEAN -> false
                    Type.BYTE -> 0.toByte()
                    Type.SHORT -> 0.toShort()
                    Type.INT -> 0
                    Type.LONG -> 0L
                    Type.FLOAT -> 0.0f
                    Type.DOUBLE -> 0.0
                    Type.DATETIME -> Date()
                    else -> throw(IllegalArgumentException("Unsupported type $type"))
                }
            }
            return MapStructDescriptor(UUID.randomUUID(), UUID.randomUUID(), columnTypes, columnValues)

        }
    }

    override fun schema(): List<FieldSchema> {
        return this.columnTypes.map { (key, type) ->
            FieldSchema(key, Type.valueOf(type), nullable=true)
        }
    }

    override fun values(): List<Pair<String, Any?>> {
        return this.columnTypes.map { (key, type) ->
            val value = this.columnValues[key] // This will be null if key is not present in columnValues
            val pairedValue = when (Type.valueOf(type)) {
                Type.STRING -> value as? String
                Type.BOOLEAN -> value as? Boolean
                Type.BYTE -> value as? Byte
                Type.SHORT -> value as? Short
                Type.INT -> value as? Int
                Type.LONG -> value as? Long
                Type.FLOAT -> value as? Float
                Type.DOUBLE -> value as? Double
                Type.DATETIME -> value as? Date
                else -> throw IllegalArgumentException("Unsupported type $type")
            }
            Pair(key, pairedValue)
        }

    }
}