# Tink (used transitively via androidx.security:security-crypto for
# EncryptedSharedPreferences) references compile-time-only annotations
# from Error Prone and JSR-305 that aren't packaged at runtime. R8
# would otherwise warn and fail the release build. These are safe to
# ignore — they only affect static analysis, never runtime behaviour.
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
