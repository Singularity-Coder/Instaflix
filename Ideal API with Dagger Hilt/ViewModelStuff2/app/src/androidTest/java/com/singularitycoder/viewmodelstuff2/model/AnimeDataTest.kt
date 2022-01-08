package com.singularitycoder.viewmodelstuff2.model

import org.junit.Assert.*

import org.junit.After
import org.junit.Before

// Android has 2 types of tests - Local Unit tests and Instrumented Tests
// Test package -Local Unit Tests run on ur computer in JVM. So they dont have access to native android classes like Parcelable. If u try to run android stuff they u will error.
// AndroidTest package - For Android components u need to use Instrumented tests on a real device or emulator or use a framework like Robolectric which allows u to run local unit tests with all of the native classes like Parcelable
class AnimeDataTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }
}
