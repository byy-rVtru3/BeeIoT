package com.app.mobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Типографика приложения BeeIoT
 *
 * Иерархия стилей:
 * - displayLarge: Очень крупные заголовки (64sp) - splash screen, главные экраны
 * - headlineLarge: Крупные заголовки (48sp) - заголовки экранов авторизации/регистрации
 * - headlineMedium: Средние заголовки (36sp) - подзаголовки экранов
 * - headlineSmall: Малые заголовки (32sp) - OTP цифры, крупный акцентный текст
 * - titleLarge: Заголовки секций (24sp)
 * - titleMedium: Средние заголовки секций (20sp)
 * - bodyLarge: Крупный текст (18sp) - важная информация
 * - bodyMedium: Основной текст (16sp) - поля ввода, обычный текст
 * - bodySmall: Мелкий текст (14sp) - подсказки, вспомогательный текст
 * - labelLarge: Крупные кнопки (16sp, SemiBold)
 * - labelMedium: Средние кнопки (14sp, SemiBold)
 * - labelSmall: Мелкие метки (12sp) - chips, badges
 */
val Typography = Typography(
    // Очень крупный заголовок (splash, главный экран)
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 64.sp,
        lineHeight = 72.sp,
        letterSpacing = (-0.2).sp
    ),

    // Крупный заголовок экрана (Регистрация, Авторизация)
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.2).sp
    ),

    // Средний заголовок экрана (Подтверждение регистрации)
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.1).sp
    ),

    // Малый заголовок / OTP цифры
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    // Заголовок секции
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Средний заголовок секции
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    // Малый заголовок
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),

    // Крупный основной текст
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),

    // Основной текст (поля ввода, обычный текст)
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),

    // Мелкий текст (подсказки, ошибки)
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),

    // Крупные кнопки
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),

    // Средние кнопки, текстовые кнопки
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    // Мелкие метки (chips, badges)
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
)