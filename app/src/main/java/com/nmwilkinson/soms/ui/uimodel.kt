package com.nmwilkinson.soms.ui

import android.view.View
import android.widget.TextView
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.afterTextChangeEvents
import com.nmwilkinson.soms.model.Action
import com.nmwilkinson.soms.model.Result
import io.reactivex.Observable

/**
 * UI state model
 */
class UiModel(val inProgress: Boolean, val submitted: Boolean, val error: String) {
    companion object {
        fun Idle() = UiModel(false, false, "")
        fun InProgress() = UiModel(true, false, "")
        fun Success() = UiModel(false, true, "")
        fun Error(error: String) = UiModel(false, false, error)
    }
}

/**
 * Action-View bindings
 */
fun submitEvents(button: View, valueField: TextView)
        = button.clicks().map { Action.SubmitAction(valueField.text.toString()) }

fun checkValueEvents(textView: TextView)
        = textView.afterTextChangeEvents()
        .map { Action.CheckValueAction(it.editable().toString()) }
        .filter { it.value.isNotEmpty() }

fun allEvents(button: View, valueField: TextView, textView: TextView)
        = Observable.merge(submitEvents(button, valueField), checkValueEvents(textView))

/**
 * Result-View binding.
 */
fun resultMapper(results: Observable<Result>): Observable<UiModel>
        = results.scan(UiModel.Idle(), { _, result ->
    when (result) {
        is Result.CheckValueResult -> {
            when (result.state) {
                Result.IN_FLIGHT -> UiModel.InProgress()
                Result.ERROR -> UiModel.Error(result.value)
                else -> UiModel.Idle()
            }
        }
        is Result.SubmitResult -> {
            when (result.state) {
                Result.IN_FLIGHT -> UiModel.InProgress()
                Result.SUCCESS -> UiModel.Success()
                Result.ERROR -> UiModel.Error(result.value)
                else -> UiModel.Idle()
            }
        }
        else -> throw IllegalStateException("Unhandled ${Result::class.java.simpleName} subclass")
    }
})
