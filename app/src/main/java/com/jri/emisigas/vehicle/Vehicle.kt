package com.jri.emisigas.vehicle

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vehicle(
    val brand: String = "",
    val capacity: String = "",
    val plate: String = "",
    val user_id: String = ""
): Parcelable {
    fun toMap(): Map<String, Any> {
        val result: HashMap<String, Any> = HashMap()
        result["brand"] = brand
        result["capacity"] = capacity
        result["plate"] = plate
        result["user_id"] = user_id
        return result
    }
}
