package com.singularitycoder.viewmodelstuff2.helpers

// Import Alias. Same as type alias, just on imports. This is useful if multiple imports have similar signatures
import com.singularitycoder.viewmodelstuff2.helpers.network.LoadingState as LS

// https://medium.com/androiddevelopers/alter-type-with-typealias-4c03302fbe43
// Type Alias. If any "type" name is too long then give it a short name and use it as an alias or proxy.
// it: MostAwesomeSuperDuperBatShitCrazyAnimeList -------> this can be ------> typealias AniList = MostAwesomeSuperDuperBatShitCrazyAnimeList
// the above u can use as an alias name to shorten the type name. Its just giving a specific type an extra name and using the extra name inplace of the original name. Not a fan
typealias NoArgMethod = () -> Unit
typealias OneArgMethod<S> = (S) -> Unit
typealias TwoArgMethod<T, String> = (data: T?, message: String) -> Unit
typealias ShowLoader = (loadingState: LS) -> Unit
