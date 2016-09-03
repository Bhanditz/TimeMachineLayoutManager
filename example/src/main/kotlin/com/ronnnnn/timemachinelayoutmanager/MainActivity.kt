package com.ronnnnn.timemachinelayoutmanager

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import com.ronnnnn.library.CenterScrollListener
import com.ronnnnn.library.TimeMachineLayoutManager
import com.ronnnnn.library.TimeMachineZoomPostLayoutListener
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val timeMachineLayoutManager = TimeMachineLayoutManager(TimeMachineLayoutManager.VERTICAL, true)
        timeMachineLayoutManager.setPostLayoutListener(TimeMachineZoomPostLayoutListener())

        val exampleAdapter = ExampleAdapter(applicationContext)

        exampleAdapter.lists = createDummy()

        (findViewById(R.id.recycler_view) as RecyclerView).apply {
            layoutManager = timeMachineLayoutManager
            setHasFixedSize(true)
            adapter = exampleAdapter
            addOnScrollListener(CenterScrollListener())
        }
    }

    fun createDummy(): ArrayList<Int> {
        var lists = ArrayList<Int>()
        for (i in 1..10) {
            lists.add(i)
        }

        return lists
    }
}
