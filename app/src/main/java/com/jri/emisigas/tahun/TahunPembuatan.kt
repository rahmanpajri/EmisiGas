package com.jri.emisigas.tahun

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TahunPembuatan(
    val id: String = "",
    val name: String = ""
): Parcelable
