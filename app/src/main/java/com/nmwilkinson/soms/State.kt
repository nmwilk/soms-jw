package com.nmwilkinson.soms

/**
 * UI state model
 */
class State(val inProgress: Boolean, val success: Boolean, val error: String) {
    companion object {
        fun InProgress() = State(true, false, "")
        fun Success() = State(false, true, "")
        fun Error(error: String) = State(false, false, error)
    }
}