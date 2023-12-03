package com.jri.emisigas.jenis

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class JenisBB(
    val id: String = "",
    val name: String = ""
): Parcelable
