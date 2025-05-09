![alt text](https://github.com/Singularity-Coder/Ideal-API-Call/blob/main/assets/logo192.png)
# Instaflix
A video and audio streaming app like Netflix and Spotify.

# Screenshots
![alt text](https://github.com/Singularity-Coder/Ideal-API-Call/blob/main/assets/s1.png)
![alt text](https://github.com/Singularity-Coder/Ideal-API-Call/blob/main/assets/s2.png)
![alt text](https://github.com/Singularity-Coder/Ideal-API-Call/blob/main/assets/s3.png)
![alt text](https://github.com/Singularity-Coder/Ideal-API-Call/blob/main/assets/s4.png)
![alt text](https://github.com/Singularity-Coder/Ideal-API-Call/blob/main/assets/s5.png)
![alt text](https://github.com/Singularity-Coder/Ideal-API-Call/blob/main/assets/s6.png)
![alt text](https://github.com/Singularity-Coder/Ideal-API-Call/blob/main/assets/s7.png)
![alt text](https://github.com/Singularity-Coder/Ideal-API-Call/blob/main/assets/s8.png)
![alt text](https://github.com/Singularity-Coder/Ideal-API-Call/blob/main/assets/s9.png)
![alt text](https://github.com/Singularity-Coder/Ideal-API-Call/blob/main/assets/s10.png)
![alt text](https://github.com/Singularity-Coder/Ideal-API-Call/blob/main/assets/s11.png)
![alt text](https://github.com/Singularity-Coder/Ideal-API-Call/blob/main/assets/s12.png)
![alt text](https://github.com/Singularity-Coder/Ideal-API-Call/blob/main/assets/s13.png)
![alt text](https://github.com/Singularity-Coder/Ideal-API-Call/blob/main/assets/s14.png)

## Tech stack & Open-source libraries
- Minimum SDK level 21
-  [Kotlin](https://kotlinlang.org/) based, [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) + [LiveData](https://developer.android.com/topic/libraries/architecture/livedatahttps://developer.android.com/topic/libraries/architecture/livedata) for asynchronous.
- Jetpack
  - Lifecycle: Observe Android lifecycles and handle UI states upon the lifecycle changes.
  - ViewModel: Manages UI-related data holder and lifecycle aware. Allows data to survive configuration changes such as screen rotations.
  - DataBinding: Binds UI components in your layouts to data sources in your app using a declarative format rather than programmatically.
  - Room: Constructs Database by providing an abstraction layer over SQLite to allow fluent database access.
  - [Hilt](https://dagger.dev/hilt/): for dependency injection.
  - WorkManager: WorkManager allows you to schedule work to run one-time or repeatedly using flexible scheduling windows.
- Architecture
  - MVVM Architecture (View - DataBinding - ViewModel - Model)
  - Repository Pattern
- [Retrofit2 & OkHttp3](https://github.com/square/retrofit): Construct the REST APIs and paging network data.
- [gson](https://github.com/google/gson): A Java serialization/deserialization library to convert Java Objects into JSON and back.
- [ExoPlayer](https://github.com/google/ExoPlayer): An extensible media player for Android.
- [Material-Components](https://github.com/material-components/material-components-android): Material design components for building ripple animation, and CardView.
- [jsoup](https://mvnrepository.com/artifact/org.jsoup/jsoup): jsoup is a Java library that simplifies working with real-world HTML and XML.
- [Glide](https://github.com/bumptech/glide): Loading images from the network.
- [Lottie](https://github.com/airbnb/lottie-android): Render After Effects animations natively on Android and iOS, Web, and React Native.
- [YouTube Data API](https://mvnrepository.com/artifact/com.google.apis/google-api-services-youtube): Provides YouTube data.
- [zxing](https://github.com/zxing/zxing): ZXing ("Zebra Crossing") barcode scanning library for Java, Android.
- [zxing-android-embedded](https://github.com/journeyapps/zxing-android-embedded): Barcode scanner library for Android, based on the ZXing decoder.

## Architecture
![alt text](https://github.com/Singularity-Coder/Ideal-API-Call/blob/main/assets/arch.png)

This App is based on the MVVM architecture and the Repository pattern, which follows the [Google's official architecture guidance](https://developer.android.com/topic/architecture).

The overall architecture of this App is composed of two layers; the UI layer and the data layer. Each layer has dedicated components and they have each different responsibilities.