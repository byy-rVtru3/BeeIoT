package com.app.mobile.ui.theme

import androidx.compose.ui.unit.dp

object Dimens {
    // ============================================
    // БАЗОВЫЕ РАЗМЕРЫ (Common Spacing Scale)
    // Общая шкала с кратностью 4
    // ============================================
    val Size2 = 2.dp
    val Size4 = 4.dp
    val Size8 = 8.dp
    val Size12 = 12.dp
    val Size16 = 16.dp
    val Size20 = 20.dp
    val Size24 = 24.dp
    val Size28 = 28.dp
    val Size32 = 32.dp
    val Size36 = 36.dp
    val Size40 = 40.dp
    val Size44 = 44.dp
    val Size48 = 48.dp
    val Size52 = 52.dp
    val Size56 = 56.dp
    val Size60 = 60.dp
    val Size64 = 64.dp
    val Size68 = 68.dp
    val Size72 = 72.dp
    val Size76 = 76.dp
    val Size80 = 80.dp

    // ============================================
    // СЕМАНТИЧЕСКИЕ РАЗМЕРЫ (Semantic Spacing)
    // Специфичные для компонентов и контекста
    // ============================================

    // --- Границы и разделители ---
    val BorderWidthThin = Size2           // Тонкая граница (нажатая кнопка)
    val BorderWidthNormal = Size4.minus(1.dp)  // Обычная граница (3dp для кнопок и подчеркиваний)

    // --- Отступы для текстовых полей ---
    val TextFieldPaddingHorizontal = Size4
    val TextFieldPaddingVertical = Size8
    val TextFieldIconSize = Size24
    val TextFieldIconEndPadding = Size4
    val TextFieldErrorTopPadding = Size4

    // --- Отступы для OTP поля ---
    val OtpCellSpacing = Size12
    val OtpCellPaddingTop = Size8
    val OtpCellPaddingBottom = Size4

    // --- Отступы для кнопок ---
    val ButtonTextPadding = Size8
    val ButtonBorderWidthPressed = BorderWidthThin
    val ButtonBorderWidthNormal = BorderWidthNormal // 3dp

    // --- Отступы для экранов ---
    val OpenScreenPaddingHorizontal = Size36  // Для открытых экранов (авторизация, регистрация, подтверждение)
    val OpenScreenPaddingVertical = Size36    // Для открытых экранів (авторизация, регистрация, подтверждение)
    val ScreenContentPadding = Size16         // Для обычных экранов (настройки, информация и т.д.)

    // --- Отступы для заголовков ---
    val TitleTopPadding = Size44

    // --- Отступы между элементами ---
    val ItemsSpacingSmall = Size4          // Между полями формы
    val ItemsSpacingMedium = Size16        // Между кнопками, элементами списка
    val ItemsSpacingLarge = Size20         // Между секциями

    // --- Отступы для кнопок (вертикальные) ---
    val ButtonSoloVerticalPadding = Size64      // Одна кнопка внизу экрана
    val ButtonTwiceVerticalPadding = Size48     // Две кнопки внизу экрана
    val ButtonHorizontalPadding = Size48        // Горизонтальные отступы для кнопок
    val ButtonHorizontalPaddingLarge = Size76   // Увеличенные горизонтальные отступы

    // --- Толщина линий прогресс-индикатора ---
    val ProgressIndicatorStrokeWidth = Size4
}
