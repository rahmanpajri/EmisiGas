package com.jri.emisigas.result

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Result(
    val date: String = "",
    val distance: String = "",
    val jenis_bb_id: String = "",
    val result: String = "",
    val result_CH4: String = "",
    val result_N2O: String = "",
    val tahun_pembuatan_id: String = "",
    val user_id: String = ""
): Parcelable
