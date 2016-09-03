package com.ronnnnn.library

import android.view.View

/**
 * Created by kokushiseiya on 2016/09/03.
 */
class TimeMachineZoomPostLayoutListener: TimeMachineLayoutManager.PostLayoutListener {

    override fun transformChild(child: View, itemPositionToCenterDiff: Float, orientation: Int): ItemTransformation {
        val scale = (2 * (2 * -StrictMath.atan(Math.abs(itemPositionToCenterDiff) + 1.0) / Math.PI + 1)).toFloat()

        // because scaling will make view smaller in its center, then we should move this item to the top or bottom to make it visible
        val translateY: Float
        val translateX: Float
        if (TimeMachineLayoutManager.VERTICAL === orientation) {
            val translateYGeneral = child.measuredHeight * (1 - scale) / 2f
            translateY = Math.signum(itemPositionToCenterDiff) * translateYGeneral
            translateX = 0f
        } else {
            val translateXGeneral = child.measuredWidth * (1 - scale) / 2f
            translateX = Math.signum(itemPositionToCenterDiff) * translateXGeneral
            translateY = 0f
        }

        return ItemTransformation(scale, scale, translateX, translateY)
    }
}