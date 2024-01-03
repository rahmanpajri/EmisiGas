package com.jri.emisigas.tips

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tips(
    val title: String = "",
    val id: String = "",
    val image: String ="",
    val description: String = ""
): Parcelable