package com.nmwilkinson.soms

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    val disposables = CompositeDisposable()

    val api = Api()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        disposables.add(allEvents(submitButton, valueField, valueField)
                .compose(submitUI(api))
                .subscribe({
                    submitButton.isEnabled = !it.inProgress
                    progressView.visible(it.inProgress)
                    if (!it.inProgress) {
                        val resultMsg = if (it.success) "Success" else "Error: ${it.error}"
                        Toast.makeText(this@MainActivity, resultMsg, Toast.LENGTH_SHORT).show()
                    }
                }, {
                    throw IllegalStateException("Unhandled error")
                }, {
                    Toast.makeText(this@MainActivity, "Complete", Toast.LENGTH_SHORT).show()
                }))
    }

    override fun onDestroy() {
        super.onDestroy()

        disposables.clear()
    }
}

