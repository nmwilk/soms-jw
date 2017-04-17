package com.nmwilkinson.soms

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.nmwilkinson.soms.model.Result
import com.nmwilkinson.soms.model.submitAction
import com.nmwilkinson.soms.ui.allEvents
import com.nmwilkinson.soms.ui.resultMapper
import com.nmwilkinson.soms.view.visible
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    val disposables = CompositeDisposable()

    val api = Api()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        disposables.add(allEvents(submitButton, valueField, valueField)
                .compose(submitAction(api, AndroidSchedulers.mainThread()))
                .startWith(Result.SubmitResult(Result.IDLE))
                .compose { resultMapper(it) }
                .subscribe({
                    submitButton.isEnabled = !it.inProgress
                    progressView.visible(it.inProgress)
                    when {
                        it.submitted -> Toast.makeText(this@MainActivity, "Success", Toast.LENGTH_SHORT).show()
                        it.error.isNotEmpty() -> Toast.makeText(this@MainActivity, "Error: ${it.error}", Toast.LENGTH_SHORT).show()
                    }
                }, {
                    throw IllegalStateException("Unhandled error")
                }))
    }

    override fun onDestroy() {
        super.onDestroy()

        disposables.clear()
    }
}

