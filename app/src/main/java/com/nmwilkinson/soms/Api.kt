package com.nmwilkinson.soms

import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Dummy API call.
 */
class Api {
    fun sendValue(value: String): Observable<Boolean> {
        return Observable.just(true)
                .delay(500, TimeUnit.MILLISECONDS)
    }

    fun checkName(value: String): Observable<Boolean> {
        return Observable.just(true)
                .delay(500, TimeUnit.MILLISECONDS)
    }

}