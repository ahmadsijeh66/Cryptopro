# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Room
-keep class androidx.room.** { *; }
-keepclasseswithmembers class * {
    @androidx.room.* <fields>;
}

# Coroutines
-keep class kotlinx.coroutines.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.* <fields>;
}
