package com.ronnnnn.library

import android.content.Context
import android.support.v7.widget.LinearSmoothScroller
import android.view.View

/**
 * Created by kokushiseiya on 2016/09/03.
 */
open class TimeMachineSmoothScroller(context: Context) : LinearSmoothScroller(context) {

    @SuppressWarnings("RefusedBequest")
    override fun calculateDyToMakeVisible(view: View, snapPreference: Int): Int {
        val layoutManager = layoutManager as TimeMachineLayoutManager?
        if (null == layoutManager || !layoutManager.canScrollVertically()) {
            return 0
        }

        return layoutManager.getOffsetForCurrentView(view)
    }

    @SuppressWarnings("RefusedBequest")
    override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
        val layoutManager = layoutManager as TimeMachineLayoutManager?
        if (null == layoutManager || !layoutManager.canScrollHorizontally()) {
            return 0
        }
        return layoutManager.getOffsetForCurrentView(view)
    }
}