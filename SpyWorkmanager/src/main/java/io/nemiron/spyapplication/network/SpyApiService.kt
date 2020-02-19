package io.nemiron.spyapplication.network



import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body

import retrofit2.http.POST

interface SpyApiService {
    @POST("ScYap71fmaRFf8h7qC9B")
    fun uploadData(@Body spyData: SpyData ) : Call<ResponseBody>
}