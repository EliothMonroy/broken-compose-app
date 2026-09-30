package com.interviewprep.brokencalc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class RatesResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Rates
)

@Serializable
data class Rates(
    @SerialName("USD") val usd: Double = 1.0,
    @SerialName("EUR") val eur: Double,
    @SerialName("MXN") val mxn: Double
)

interface RatesApi {
    // the API does the multiplication for us
    @GET("latest")
    fun latest(
        @Query("base") base: String,
        @Query("symbols") symbols: String,
        @Query("amount") amount: Double
    ): Call<RatesResponse>
}

// so we don't hit the API every time the dialog opens
val rateCache = HashMap<String, RatesResponse>()

fun loadRates(api: RatesApi, from: String, amount: Double): RatesResponse {
    val cached = rateCache[from]
    if (cached != null) {
        return cached
    }
    val response = api.latest(from, "USD,EUR,MXN", amount).execute()
    val body = response.body()!!
    rateCache[from] = body
    return body
}
