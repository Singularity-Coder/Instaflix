package com.singularitycoder.viewmodelstuff2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.singularitycoder.viewmodelstuff2.model.AnimeList
import com.singularitycoder.viewmodelstuff2.viewmodel.FavAnimeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// TODO Integrate Ktlint
// TODO Work manager
// TODO Auth token in Cpp file
// TODO Sealed Class for data models
// TODO Migrations
// TODO Weak Reference context for Sharedprefs

// TODO Groovy to Kotlin DSL version
// TODO Compose version
// TODO Kotlin Flows version
// TODO paging version
// TODO without Hilt version

// Before u implement API, always create model first before anything else. Then the views. It makes ur job easy and fluent. It sets the flow
// Hilt constructs classes, provides containers and manages lifecycles automatically
// Hilt has compile-time correctness, runtime performance, scalabitliy,
// Hilt auto generates containers

// Hilt
// 1. Activities and Fragments must have @AndroidEntryPoint
// 2. Must have Application class annotated with @HiltAndroidApp
// @Module provides the dependency for us
// Hilt provides dependencies through the constructor - ex; ViewModels get repository from its constructor
// Dagger cannot inject dependency into a private or local field
// For application context use the predefined qualifier @ApplicationContext and for activity context use @ActivityContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Its good to just create a single instance of Gson rather than creating multiple objects. Performance thing.
    @Inject lateinit var gson: Gson
    private lateinit var viewModel: FavAnimeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(FavAnimeViewModel::class.java)
        viewModel.loadAnimeList()
        setUpObservers()
    }

    private fun setUpObservers() {
        viewModel.getAnimeList().observe(this) { it: AnimeList? ->
            println("AnimeList chan: ${gson.toJson(it)}")
        }
    }
}