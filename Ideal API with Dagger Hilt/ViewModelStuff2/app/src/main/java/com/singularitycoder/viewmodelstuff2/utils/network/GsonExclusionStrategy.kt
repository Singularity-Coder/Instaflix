package com.singularitycoder.viewmodelstuff2.utils.network

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

// https://stackoverflow.com/questions/4802887/gson-how-to-exclude-specific-fields-from-serialization-without-annotations
// https://kotlinlang.org/docs/annotations.html#java-annotations

// Custom Exclude annotation to avoid serialization and deserialization
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Skip(val serialize: Boolean = false, val deserialize: Boolean = false)

class SerializationExclusionStrategy : ExclusionStrategy {
    override fun shouldSkipField(f: FieldAttributes): Boolean {
        val expose = f.getAnnotation(Skip::class.java)  // You can replace it with the default @Expose Annotation as well and override its functionality
        return null != expose && !expose.serialize
    }
    override fun shouldSkipClass(clazz: Class<*>?): Boolean = false
}

class DeserializationExclusionStrategy : ExclusionStrategy {
    override fun shouldSkipField(f: FieldAttributes): Boolean {
        val expose = f.getAnnotation(Skip::class.java)
        return null != expose && !expose.deserialize
    }
    override fun shouldSkipClass(clazz: Class<*>?): Boolean = false
}
