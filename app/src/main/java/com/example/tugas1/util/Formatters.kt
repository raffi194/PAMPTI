package com.example.tugas1.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Mengubah angka Double menjadi format mata uang Rupiah (contoh: Rp150.000).
 */
fun Double.toRupiahFormat(): String {
    val localeID = Locale("in", "ID")
    val format = NumberFormat.getCurrencyInstance(localeID)
    format.maximumFractionDigits = 0 // Tidak menampilkan angka desimal
    return format.format(this)
}
    