package com.example.util

/**
 * Arabic Number to Words Converter (التفقيط المالي المحاسبي المهني)
 * Transforms numeric values into grammatically sound Arabic text
 * Supports Yemeni Rial (ر.ي.), Saudi Rial (ر.س.), and generic numbers.
 */
object TafqeetHelper {

    private val ONES = arrayOf(
        "", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة", "سبعة", "ثمانية", "تسعة",
        "عشرة", "أحد عشر", "اثنا عشر", "ثلاثة عشر", "أربعة عشر", "خمسة عشر", "ستة عشر",
        "سبعة عشر", "ثمانية عشر", "تسعة عشر"
    )

    private val TENS = arrayOf(
        "", "عشرة", "عشرون", "ثلاثون", "أربعون", "خمسون", "ستون", "سبعون", "ثمانون", "تسعون"
    )

    private val HUNDREDS = arrayOf(
        "", "مائة", "مائتان", "ثلاثمائة", "أربعمائة", "خمسمائة", "ستمائة", "سبعمائة", "ثمانمائة", "تسعمائة"
    )

    /**
     * Converts a number to Arabic words with currency
     * @param amount The numeric amount
     * @param currency "YER" for Yemeni Rial, "SAR" for Saudi Rial, "NONE" for just number
     * @param withPrefixAndSuffix Whether to include "فقط" and "لا غير"
     */
    fun convert(
        amount: Double,
        currency: String = "YER",
        withPrefixAndSuffix: Boolean = true
    ): String {
        if (amount == 0.0 || amount.isNaN()) {
            val zeroText = when (currency) {
                "YER" -> "صفر ريال يمني"
                "SAR" -> "صفر ريال سعودي"
                "USD" -> "صفر دولار أمريكي"
                "EUR" -> "صفر يورو"
                "AED" -> "صفر درهم إماراتي"
                "KWD" -> "صفر دينار كويتي"
                "OMR" -> "صفر ريال عماني"
                "QAR" -> "صفر ريال قطري"
                "EGP" -> "صفر جنيه مصري"
                else -> "صفر"
            }
            return if (withPrefixAndSuffix) "فقط $zeroText لا غير" else zeroText
        }

        val isNegative = amount < 0
        val positiveAmount = kotlin.math.abs(amount)

        if (!positiveAmount.isFinite()) {
            return if (withPrefixAndSuffix) "فقط مبلغ غير محدد لا غير" else "مبلغ غير محدد"
        }

        val integerPart = try { positiveAmount.toLong() } catch (_: Exception) { 0L }
        val fractionalPart = try { kotlin.math.round((positiveAmount - integerPart) * 100).toLong() } catch (_: Exception) { 0L }

        val integerWords = convertIntegerPart(integerPart)

        val currencyText = when (currency) {
            "YER" -> {
                when {
                    integerPart == 1L -> "ريال يمني"
                    integerPart == 2L -> "ريالان يمنيان"
                    integerPart in 3..10 -> "ريالات يمنية"
                    else -> "ريال يمني"
                }
            }
            "SAR" -> {
                when {
                    integerPart == 1L -> "ريال سعودي"
                    integerPart == 2L -> "ريالان سعوديان"
                    integerPart in 3..10 -> "ريالات سعودية"
                    else -> "ريال سعودي"
                }
            }
            "USD" -> {
                when {
                    integerPart == 1L -> "دولار أمريكي"
                    integerPart == 2L -> "دولاران أمريكيان"
                    integerPart in 3..10 -> "دولارات أمريكية"
                    else -> "دولار أمريكي"
                }
            }
            "EUR" -> {
                when {
                    integerPart == 1L -> "يورو"
                    integerPart == 2L -> "يورو"
                    integerPart in 3..10 -> "يورو"
                    else -> "يورو"
                }
            }
            "AED" -> {
                when {
                    integerPart == 1L -> "درهم إماراتي"
                    integerPart == 2L -> "درهمان إماراتيان"
                    integerPart in 3..10 -> "دراهم إماراتية"
                    else -> "درهم إماراتي"
                }
            }
            "KWD" -> {
                when {
                    integerPart == 1L -> "دينار كويتي"
                    integerPart == 2L -> "ديناران كويتيان"
                    integerPart in 3..10 -> "دنانير كويتية"
                    else -> "دينار كويتي"
                }
            }
            "OMR" -> {
                when {
                    integerPart == 1L -> "ريال عماني"
                    integerPart == 2L -> "ريالان عمانيان"
                    integerPart in 3..10 -> "ريالات عمانية"
                    else -> "ريال عماني"
                }
            }
            "QAR" -> {
                when {
                    integerPart == 1L -> "ريال قطري"
                    integerPart == 2L -> "ريالان قطريان"
                    integerPart in 3..10 -> "ريالات قطرية"
                    else -> "ريال قطري"
                }
            }
            "EGP" -> {
                when {
                    integerPart == 1L -> "جنيه مصري"
                    integerPart == 2L -> "جنيهان مصريان"
                    integerPart in 3..10 -> "جنيهات مصرية"
                    else -> "جنيه مصري"
                }
            }
            else -> ""
        }

        val result = StringBuilder()
        if (isNegative) {
            result.append("سالب ")
        }

        result.append(integerWords)
        if (currencyText.isNotBlank()) {
            result.append(" ").append(currencyText)
        }

        // Handle fractions (فلوس / هللات / سنتات / قروش)
        if (fractionalPart > 0) {
            val fractionWords = convertIntegerPart(fractionalPart)
            val fractionUnit = when (currency) {
                "YER" -> "فلس"
                "SAR" -> "هللة"
                "USD", "EUR" -> "سنت"
                "AED", "KWD" -> "فلس"
                "OMR" -> "بيسة"
                "QAR" -> "درهم"
                "EGP" -> "قرش"
                else -> "جزء من مائة"
            }
            result.append(" و ").append(fractionWords).append(" ").append(fractionUnit)
        }

        val fullString = result.toString().trim()
        return if (withPrefixAndSuffix) {
            "فقط $fullString لا غير"
        } else {
            fullString
        }
    }

    /**
     * Helper to convert an integer number up to 999,999,999,999 to Arabic words
     */
    private fun convertIntegerPart(number: Long): String {
        if (number == 0L) return "صفر"

        val billions = number / 1_000_000_000L
        val millions = (number % 1_000_000_000L) / 1_000_000L
        val thousands = (number % 1_000_000L) / 1_000L
        val remainder = number % 1_000L

        val parts = mutableListOf<String>()

        if (billions > 0) {
            parts.add(formatGroup(billions, "مليار", "ملياران", "مليارات", "ملياراً"))
        }

        if (millions > 0) {
            parts.add(formatGroup(millions, "مليون", "مليونان", "ملايين", "مليوناً"))
        }

        if (thousands > 0) {
            parts.add(formatGroup(thousands, "ألف", "ألفان", "آلاف", "ألفاً"))
        }

        if (remainder > 0) {
            parts.add(convertHundreds(remainder.toInt()))
        }

        return parts.joinToString(" و ")
    }

    private fun formatGroup(
        value: Long,
        singular: String,
        dual: String,
        plural: String,
        accusative: String
    ): String {
        return when (value) {
            1L -> singular
            2L -> dual
            in 3..10 -> "${convertHundreds(value.toInt())} $plural"
            else -> "${convertHundreds(value.toInt())} $accusative"
        }
    }

    private fun convertHundreds(n: Int): String {
        if (n == 0) return ""

        val hundreds = n / 100
        val tensAndOnes = n % 100

        val parts = mutableListOf<String>()

        if (hundreds > 0) {
            parts.add(HUNDREDS[hundreds])
        }

        if (tensAndOnes > 0) {
            if (tensAndOnes < 20) {
                parts.add(ONES[tensAndOnes])
            } else {
                val ones = tensAndOnes % 10
                val tens = tensAndOnes / 10

                if (ones > 0) {
                    parts.add("${ONES[ones]} و ${TENS[tens]}")
                } else {
                    parts.add(TENS[tens])
                }
            }
        }

        return parts.joinToString(" و ")
    }
}
