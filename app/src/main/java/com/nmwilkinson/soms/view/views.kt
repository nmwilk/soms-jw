package com.nmwilkinson.soms.view

import android.view.View
import android.widget.ProgressBar

fun ProgressBar.visible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}