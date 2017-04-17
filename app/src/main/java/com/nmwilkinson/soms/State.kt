package com.nmwilkinson.soms

/**
 * UI state model
 */
class State(val inProgress: Boolean, val submitted: Boolean, val entryValid: Boolean, val error: String) {
    companion object {
        fun Idle() = State(false, false, false, "")
        fun InProgress() = State(true, false, false, "")
        fun EntryValid() = State(false, false, true, "")
        fun Success() = State(false, true, false, "")
        fun Error(error: String) = State(false, false, false, error)
    }
}