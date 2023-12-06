package com.jri.emisigas.vehicle

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vehicle(
    val brand: String = "",
    val capacity: String = "",
    val plate: String = "",
    val user_id: String = ""
): Parcelable
