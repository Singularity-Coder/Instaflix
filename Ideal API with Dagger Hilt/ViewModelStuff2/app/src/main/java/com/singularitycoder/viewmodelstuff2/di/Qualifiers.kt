package com.singularitycoder.viewmodelstuff2.di

import javax.inject.Qualifier

// You need to specify Qualifiers if you have 2 providers that inject the same type to let hilt differentiate what to provide appropriately
// Not working - keeps throwing error - retrofit needs @Inject - which it already has
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GsonBuilderCore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GsonBuilderForRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RetrofitServiceCore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RetrofitServiceForAuth

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RetrofitCore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RetrofitForAuth
