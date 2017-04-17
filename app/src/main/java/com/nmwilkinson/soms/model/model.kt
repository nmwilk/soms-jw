package com.nmwilkinson.soms.model

import com.nmwilkinson.soms.Api
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import java.util.concurrent.TimeUnit

/**
 * Generic Action
 */
sealed class Action(val value: String) {
    class CheckValueAction(value: String) : Action(value)
    class SubmitAction(value: String) : Action(value)
}

/**
 * Generic Result
 */
sealed class Result(val state: Int, val value: String) {
    class CheckValueResult(state: Int, value: String = "") : Result(state, value)
    class SubmitResult(state: Int, value: String = "") : Result(state, value)

    companion object {
        val IDLE = 0
        val IN_FLIGHT = 1
        val SUCCESS = 2
        val ERROR = 3
    }
}


fun submit(api: Api, observeOn: Scheduler): ObservableTransformer<Action, Result> = ObservableTransformer { events ->
    events.flatMap { action: Action ->
        api.sendValue(action.value)
                .map { Result.SubmitResult(Result.SUCCESS) }
                .onErrorReturn { Result.SubmitResult(Result.ERROR, "Submit failed") }
                .observeOn(observeOn)
                .startWith(Result.SubmitResult(Result.IN_FLIGHT))
    }
}

fun checkValue(api: Api, observeOn: Scheduler): ObservableTransformer<Action, Result> = ObservableTransformer { events ->
    events.switchMap { action ->
        api.checkValue(action.value)
                .delay(1000, TimeUnit.MILLISECONDS)
                .map { if (it) Result.CheckValueResult(Result.SUCCESS) else Result.CheckValueResult(Result.ERROR, "Validation failed") }
                .onErrorReturn { Result.CheckValueResult(Result.ERROR, "Check value failed") }
                .observeOn(observeOn)
                .startWith(Result.CheckValueResult(Result.IN_FLIGHT))
    }
}

fun submitAction(api: Api, observeOn: Scheduler): ObservableTransformer<Action, Result> = ObservableTransformer {
    events ->
    events.publish { shared ->
        Observable.merge(
                shared.ofType(Action.SubmitAction::class.java).compose(submit(api, observeOn)),
                shared.ofType(Action.CheckValueAction::class.java).compose(checkValue(api, observeOn))
        )
    }
}

