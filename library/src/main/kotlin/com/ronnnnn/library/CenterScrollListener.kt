package com.ronnnnn.library

import android.support.v7.widget.RecyclerView

/**
 * Created by kokushiseiya on 2016/09/03.
 */
class CenterScrollListener: RecyclerView.OnScrollListener() {
    var mAutoSet = true

    override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        val layoutManager = recyclerView!!.layoutManager
        if (layoutManager !is TimeMachineLayoutManager) {
            mAutoSet = true
            return
        }

        val lm = layoutManager
        if (!mAutoSet) {
            if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                val scrollNeeded = lm.getOffsetCenterView()
                if (TimeMachineLayoutManager.HORIZONTAL === lm.getOrientation()) {
                    recyclerView.smoothScrollBy(scrollNeeded, 0)
                } else {
                    recyclerView.smoothScrollBy(0, scrollNeeded)
                }
                mAutoSet = true
            }
        }
        if (RecyclerView.SCROLL_STATE_DRAGGING == newState || RecyclerView.SCROLL_STATE_SETTLING == newState) {
            mAutoSet = false
        }
    }
}