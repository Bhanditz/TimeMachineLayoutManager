package com.ronnnnn.timemachinelayoutmanager

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import com.ronnnnn.library.TimeMachineLayoutManager
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val exampleAdapter = ExampleAdapter(applicationContext)

        exampleAdapter.lists = createDummy()

        val timeMachineLayoutManager = TimeMachineLayoutManager(applicationContext, exampleAdapter.lists.size)

        (findViewById(R.id.recycler_view) as RecyclerView).apply {
            layoutManager = timeMachineLayoutManager
            adapter = exampleAdapter
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
