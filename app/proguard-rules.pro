# kotlinx.serialization: conservar los serializadores generados de los modelos de niveles y progreso.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers @kotlinx.serialization.Serializable class com.fotopalabra.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.fotopalabra.**$$serializer { *; }
