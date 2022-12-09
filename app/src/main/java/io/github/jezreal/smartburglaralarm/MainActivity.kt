package io.github.jezreal.smartburglaralarm

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import io.github.jezreal.smartburglaralarm.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    private val picker =
        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(10)
            .setTitleText("Select From time")
            .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }

    fun selectFromTime(view: View) {
        picker.show(supportFragmentManager, "tag")
    }

    fun selectToTime(view: View) {
        picker.show(supportFragmentManager, "tag")
    }
}