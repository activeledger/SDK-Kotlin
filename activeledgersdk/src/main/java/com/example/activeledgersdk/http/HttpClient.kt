/*
 * MIT License (MIT)
 * Copyright (c) 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.example.activeledgersdk.http

import com.example.activeledgersdk.event.ServerEventListener
import com.example.activeledgersdk.utility.Utility
import com.here.oksse.OkSse
import com.here.oksse.ServerSentEvent

import java.util.concurrent.TimeUnit

import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


class HttpClient// this method creates and HttpClient that is further user to execute transactions
private constructor() {
    private val apiService: APIService
    private val okHttpClient: OkHttpClient
    private val oksse: OkSse

    // does an HTTP hit and return territoriality details
    val territorialityStatus: Observable<String>
        get() = apiService.territorialityStatus

    init {
        okHttpClient = provideOkhttpClient()
        oksse = OkSse(okHttpClient)

        val retrofit = Retrofit.Builder().baseUrl(Utility.getInstance().httpurl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(okHttpClient)
                .build()

        apiService = retrofit.create(APIService::class.java)
    }

    private fun provideOkhttpClient(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        httpClient.writeTimeout(60, TimeUnit.MINUTES)
        httpClient.connectTimeout(60, TimeUnit.MINUTES)
        httpClient.readTimeout(60, TimeUnit.MINUTES)
        return httpClient.build()
    }

    // this method can be used to send transaction as an HTTP request to the ledger
    fun sendTransaction(transaction: String): Observable<String> {

        return apiService.sendTransaction(transaction)
    }


    fun getTransactionData(id: String): Observable<String> {
        return apiService.getTransactionData(id)
    }

    fun subscribeToEvent(url: String, listener: ServerEventListener): ServerSentEvent {
        val request = Request.Builder().url(url).build()
        return oksse.newServerSentEvent(request, listener)
    }

    companion object {

        private var instance: HttpClient? = null

        fun getInstance(): HttpClient {
            if (instance == null) {
                instance = HttpClient()
            }
            return instance as HttpClient
        }
    }

}
