package com.singularitycoder.viewmodelstuff2.utils.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

// What about same dependencies for different scopes
// How to pass dependencies from one scope to another - maybe through params
// How to handle dependency cycles - A in B, B in C, C in A

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule
