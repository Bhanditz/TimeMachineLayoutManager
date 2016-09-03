package com.ronnnnn.timemachinelayoutmanager

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView

/**
 * Created by kokushiseiya on 2016/09/03.
 */
class ExampleAdapter(private val context: Context): RecyclerView.Adapter<ExampleAdapter.ViewHolder>() {

    var lists: List<Int> = emptyList()

    override fun getItemCount(): Int = lists.size

    override fun getItemId(position: Int): Long = 0

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recycler_view, parent, false))

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        if (holder != null) {
            holder.numberTextView.text = lists[position].toString()
            var color: Int = 0
            when (lists[position]) {
                1 -> color = Color.BLACK
                2 -> color = Color.RED
                3 -> color = Color.BLUE
                4 -> color = Color.CYAN
                5 -> color = Color.DKGRAY
                6 -> color = Color.GREEN
                7 -> color = Color.YELLOW
                8 -> color = Color.LTGRAY
                9 -> color = Color.MAGENTA
                10 -> color = Color.GRAY
            }
            holder.rootView.setBackgroundColor(color)
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val numberTextView: TextView
        val rootView: RelativeLayout

        init {
            numberTextView = view.findViewById(R.id.number_text) as TextView
            rootView = view.findViewById(R.id.root) as RelativeLayout
        }
    }
}
