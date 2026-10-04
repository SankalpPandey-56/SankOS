# SankOS R8 rules.
# Minification is disabled for v0.1 to keep behavior predictable; these rules
# are prepared for when R8 is enabled in a later milestone.

# Keep crash-report-readable stack traces
-keepattributes SourceFile,LineNumberTable

# Compose
-dontwarn androidx.compose.**
