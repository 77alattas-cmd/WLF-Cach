package com.example.ui.util

object ArabicNumberConverter {
    private val ones = arrayOf("", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة", "سبعة", "ثمانية", "تسعة")
    private val teens = arrayOf("عشرة", "أحد عشر", "اثنا عشر", "ثلاثة عشر", "أربعة عشر", "خمسة عشر", "ستة عشر", "سبعة عشر", "ثمانية عشر", "تسعة عشر")
    private val tens = arrayOf("", "", "عشرون", "ثلاثون", "أربعون", "خمسون", "ستون", "سبعون", "ثمانون", "تسعون")
    private val hundreds = arrayOf("", "مائة", "مائتان", "ثلاثمائة", "أربعمائة", "خمسمائة", "ستمائة", "سبعمائة", "ثمانمائة", "تسعمائة")

    fun convert(numberStr: String): String {
        val clean = numberStr.trim().replace(Regex("[^0-9.]"), "")
        if (clean.isBlank()) return ""
        val parts = clean.split(".")
        val intPart = parts[0].toLongOrNull() ?: 0L
        val word = convertLong(intPart)
        if (parts.size > 1 && parts[1].toLongOrNull() != null && parts[1].toLongOrNull()!! > 0) {
            val decPart = convertLong(parts[1].toLongOrNull()!!)
            return "$word و فاصلة $decPart"
        }
        return word
    }

    private fun convertLong(n: Long): String {
        if (n == 0L) return "صفر"
        if (n < 0) return "سالب " + convertLong(-n)
        
        if (n < 10) return ones[n.toInt()]
        if (n in 10..19) return teens[(n - 10).toInt()]
        if (n in 20..99) {
            val t = (n / 10).toInt()
            val o = (n % 10).toInt()
            return if (o == 0) tens[t] else "${ones[o]} و${tens[t]}"
        }
        if (n in 100..999) {
            val h = (n / 100).toInt()
            val rem = n % 100
            val hStr = hundreds[h]
            return if (rem == 0L) hStr else "$hStr و${convertLong(rem)}"
        }
        if (n in 1000..999999L) {
            val th = n / 1000
            val rem = n % 1000
            val thStr = when (th) {
                1L -> "ألف"
                2L -> "ألفان"
                in 3..10 -> "${convertLong(th)} آلاف"
                else -> "${convertLong(th)} ألفاً"
            }
            return if (rem == 0L) thStr else "$thStr و${convertLong(rem)}"
        }
        if (n in 1000000L..999999999L) {
            val m = n / 1000000
            val rem = n % 1000000
            val mStr = when (m) {
                1L -> "مليون"
                2L -> "مليونان"
                in 3..10 -> "${convertLong(m)} ملايين"
                else -> "${convertLong(m)} مليوناً"
            }
            return if (rem == 0L) mStr else "$mStr و${convertLong(rem)}"
        }
        return n.toString()
    }
}
