package com.nmwilkinson.soms

import android.util.Log
import android.view.View
import android.widget.TextView
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.afterTextChangeEvents
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

/**
 * UI events
 */
sealed class SubmitUiEvent(val value: String) {
    class SubmitEvent(value: String) : SubmitUiEvent(value)
    class CheckNameEvent(value: String) : SubmitUiEvent(value)
}

/**
 * UI event model
 */
fun submitEvents(button: View, valueField: TextView)
        = button.clicks().map { SubmitUiEvent.SubmitEvent(valueField.text.toString()) }

fun checkNameEvents(textView: TextView)
        = textView.afterTextChangeEvents()
        .map { SubmitUiEvent.CheckNameEvent(it.editable().toString()) }
        .filter { it.value.isNotEmpty() }

fun allEvents(button: View, valueField: TextView, textView: TextView)
        = Observable.merge(submitEvents(button, valueField), checkNameEvents(textView))

fun submit(api: Api): ObservableTransformer<SubmitUiEvent, State> = ObservableTransformer { events ->
    events.flatMap { submission: SubmitUiEvent ->
        api.sendValue(submission.value)
                .doOnNext { result: Boolean -> Log.d("submit", "onNext($result)") }
                .doOnComplete { Log.d("submit", "doOnComplete()") }
                .map { State.Success() }
                .onErrorReturn { State.Error(it.message ?: "Unknown") }
                .observeOn(AndroidSchedulers.mainThread())
                .startWith(State.InProgress())
    }
}

fun checkName(api: Api): ObservableTransformer<SubmitUiEvent.CheckNameEvent, State> = ObservableTransformer { events ->
    events.switchMap { event ->
        api.checkName(event.value)
                .delay(1000, TimeUnit.MILLISECONDS)
                .doOnNext { result: Boolean -> Log.d("checkName", "onNext($result)") }
                .doOnComplete { Log.d("checkName", "doOnComplete()") }
                .map { State.Success() }
                .onErrorReturn { State.Error(it.message ?: "Unknown") }
                .observeOn(AndroidSchedulers.mainThread())
                .startWith(State.InProgress())
    }
}

fun submitUI(api: Api): ObservableTransformer<SubmitUiEvent, State> = ObservableTransformer {
    events ->
    events.publish { shared ->
        Observable.merge(
                shared.ofType(SubmitUiEvent.SubmitEvent::class.java).compose(submit(api)),
                shared.ofType(SubmitUiEvent.CheckNameEvent::class.java).compose(checkName(api))
        )
    }
}