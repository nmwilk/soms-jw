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
sealed class UiEvent(val value: String) {
    class Event(value: String) : UiEvent(value)
    class CheckValueEvent(value: String) : UiEvent(value)
}

/**
 * UI event model
 */
fun submitEvents(button: View, valueField: TextView)
        = button.clicks().map { UiEvent.Event(valueField.text.toString()) }

fun checkValueEvents(textView: TextView)
        = textView.afterTextChangeEvents()
        .map { UiEvent.CheckValueEvent(it.editable().toString()) }
        .filter { it.value.isNotEmpty() }

fun allEvents(button: View, valueField: TextView, textView: TextView)
        = Observable.merge(submitEvents(button, valueField), checkValueEvents(textView))

fun submit(api: Api): ObservableTransformer<UiEvent, State> = ObservableTransformer { events ->
    events.flatMap { submission: UiEvent ->
        api.sendValue(submission.value)
                .doOnNext { result: Boolean -> Log.d("submit", "onNext($result)") }
                .doOnComplete { Log.d("submit", "doOnComplete()") }
                .map { State.Success() }
                .onErrorReturn { State.Error(it.message ?: "Unknown") }
                .observeOn(AndroidSchedulers.mainThread())
                .startWith(State.InProgress())
    }
}

fun checkValue(api: Api): ObservableTransformer<UiEvent.CheckValueEvent, State> = ObservableTransformer { events ->
    events.switchMap { event ->
        api.checkValue(event.value)
                .delay(1000, TimeUnit.MILLISECONDS)
                .doOnNext { result: Boolean -> Log.d("checkValue", "onNext($result)") }
                .doOnComplete { Log.d("checkValue", "doOnComplete()") }
                .map { State.EntryValid() }
                .onErrorReturn { State.Error(it.message ?: "Unknown") }
                .observeOn(AndroidSchedulers.mainThread())
                .startWith(State.InProgress())
    }
}

fun submitUI(api: Api): ObservableTransformer<UiEvent, State> = ObservableTransformer {
    events ->
    events.publish { shared ->
        Observable.merge(
                shared.ofType(UiEvent.Event::class.java).compose(submit(api)),
                shared.ofType(UiEvent.CheckValueEvent::class.java).compose(checkValue(api))
        )
    }
}