# R8 keep rules for the release build.

# Our own class names stay readable: the tile diagnostics record the first stack frames of a crash
# or of a failed tile callback, and an obfuscated trace would make them worthless. Members are still
# removed when nothing uses them.
-keepnames class com.flxholle.quicktiles.** { *; }

# Stack traces are part of the diagnostics log
-keepattributes SourceFile,LineNumberTable

# Preferences are inflated from XML by class name
-keep public class * extends androidx.preference.Preference {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# SearchPreference builds and looks up its own views and result pages
-keep class com.bytehamster.lib.preferencesearch.** { *; }

# aboutlibraries reads its generated json and entity classes are mapped into it
-keep class com.mikepenz.aboutlibraries.** { *; }
-dontwarn com.mikepenz.**
