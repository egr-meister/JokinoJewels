# Jokino Jewels — R8 rules.
# R8 is OFF for the first release (isMinifyEnabled = false). These rules are kept
# ready so that enabling minify + shrinkResources later is a one-line change.

# --- Kotlinx Serialization ---------------------------------------------------
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
-dontnote kotlinx.serialization.AnnotationsKt
-dontnote kotlinx.serialization.SerializationKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# App serializable classes (GameData, Settings, navigation routes)
-keep,includedescriptorclasses class com.jokinojewels.game.**$$serializer { *; }
-keepclassmembers class com.jokinojewels.game.** {
    *** Companion;
}
-keepclasseswithmembers class com.jokinojewels.game.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class com.jokinojewels.game.** { *; }

# Serializable objects (type-safe navigation routes declared as data objects)
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
