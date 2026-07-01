package com.aequicor.missionreview.core.theme

/**
 * Semantic color tokens shared by review UI adapters.
 */
data class MissionReviewColorTokens(
    val surface: Long = 0xFFF8F9FB,
    val panel: Long = 0xFFFFFFFF,
    val textPrimary: Long = 0xFF111827,
    val textSecondary: Long = 0xFF4B5563,
    val accent: Long = 0xFF2563EB,
    val required: Long = 0xFFB91C1C,
)

/**
 * Spacing scale in density-independent units.
 */
data class MissionReviewSpacingTokens(
    val xs: Int = 4,
    val sm: Int = 8,
    val md: Int = 12,
    val lg: Int = 16,
    val xl: Int = 24,
)

/**
 * Typography sizes in scale-independent units.
 */
data class MissionReviewTypographyTokens(
    val bodySize: Int = 14,
    val titleSize: Int = 18,
    val codeSize: Int = 13,
)

/**
 * Shared theme token bundle.
 */
data class MissionReviewThemeTokens(
    val colors: MissionReviewColorTokens = MissionReviewColorTokens(),
    val spacing: MissionReviewSpacingTokens = MissionReviewSpacingTokens(),
    val typography: MissionReviewTypographyTokens = MissionReviewTypographyTokens(),
)
