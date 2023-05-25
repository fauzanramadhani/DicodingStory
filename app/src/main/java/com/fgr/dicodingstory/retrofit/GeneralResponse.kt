package com.fgr.dicodingstory.retrofit

import com.google.gson.annotations.SerializedName

data class GeneralResponse(
    @SerializedName("error") val isError: Boolean,
    @SerializedName("message") val message: String
)
