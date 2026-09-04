# Room database rules
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabaseKt
-keep class * implements androidx.room.RoomDatabase
-keep interface * extends androidx.room.RoomDatabase

# Keep our data model classes and entities
-keep class com.example.data.model.** { *; }
-keep class com.example.ui.model.** { *; }
-keep class com.example.data.dao.** { *; }
-keep interface com.example.data.dao.** { *; }

# Keep Moshi and JSON serialization classes
-keep class com.example.data.model.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class com.squareup.moshi.** { *; }

# Keep Retrofit classes and interfaces
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @retrofit2.http.** <methods>;
}
-keep class retrofit2.** { *; }

# Keep Compose/Jetpack rules
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Ignore warnings for OkHttp optional dependencies
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn okhttp3.**
