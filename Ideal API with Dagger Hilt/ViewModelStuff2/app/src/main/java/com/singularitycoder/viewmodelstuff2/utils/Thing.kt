package com.singularitycoder.viewmodelstuff2.utils

import kotlin.reflect.KClass

/**
 * [Thing] class will be of type T which extends Any
 * @param kClass is a kotlin class that is of type Any
 * A generic class to extend
 * */
class Thing<T : Any> constructor(kClass: KClass<T>)
