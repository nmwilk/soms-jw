package com.nmwilkinson.soms

/**
 * UI state model
 */
class State(val inProgress: Boolean, val submitted: Boolean, val error: String) {
    companion object {
        fun Idle() = State(false, false, "")
        fun InProgress() = State(true, false, "")
        fun Success() = State(false, true, "")
        fun Error(error: String) = State(false, false, error)
    }
}