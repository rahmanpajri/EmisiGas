package com.jri.emisigas.history

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class History(
    val uid: Int,
    val fullName: String,
    val email: String,
): Parcelable