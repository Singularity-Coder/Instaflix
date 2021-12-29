package com.singularitycoder.viewmodelstuff2.utils

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.annotations.Expose

// https://stackoverflow.com/questions/4802887/gson-how-to-exclude-specific-fields-from-serialization-without-annotations
// https://kotlinlang.org/docs/annotations.html#java-annotations

// Custom Exclude annotation
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Avoid(val serialize: Boolean = true, val deserialize: Boolean = true)

class SerializationExclusionStrategy : ExclusionStrategy {
    override fun shouldSkipField(f: FieldAttributes): Boolean {
        val expose = f.getAnnotation(Expose::class.java)  // You can replace it with the custom Annotation above as well
        return null != expose && !expose.serialize
    }
    override fun shouldSkipClass(clazz: Class<*>?): Boolean = false
}

class DeserializationExclusionStrategy : ExclusionStrategy {
    override fun shouldSkipField(f: FieldAttributes): Boolean {
        val expose = f.getAnnotation(Expose::class.java)
        return null != expose && !expose.deserialize
    }
    override fun shouldSkipClass(clazz: Class<*>?): Boolean = false
}