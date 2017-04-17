package com.nmwilkinson.soms

import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Dummy API calls.
 */
class Api {
    val allDigits = Regex("^([0-9]*)$")

    fun sendValue(value: String): Observable<Boolean> {
        return Observable.just(true)
                .delay(500, TimeUnit.MILLISECONDS)
    }

    fun checkValue(value: String): Observable<Boolean> {
        return Observable.just(allDigits.matches(value))
                .delay(500, TimeUnit.MILLISECONDS)
    }

}