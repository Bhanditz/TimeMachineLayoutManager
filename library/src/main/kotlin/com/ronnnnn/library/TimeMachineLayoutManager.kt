package com.ronnnnn.library

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * Created by kokushiseiya on 2016/09/03.
 */
class TimeMachineLayoutManager(context: Context, listSize: Int) : LinearLayoutManager(context) {

    var layoutTop = 0
    var layoutBottom = 0

    init {
        // ひとまずvertical
        orientation = LinearLayoutManager.VERTICAL

        // 並び方を逆順に
        reverseLayout = true
        stackFromEnd = true

        // スクロールを一番下に
        // 並び方を逆順にしているので、一番下がposition = 0
        scrollToPosition(0)
    }

    /**
     * 初期レイアウト時のみ呼ばれる
     * 多少重い処理を行ってもいい?
     * todo: onResumeなどを考慮してViewの状態を復帰できるような実装
     */
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)

        // 一旦LinearLayoutManagerで配置されるViewを全部捨てる
        detachAndScrapAttachedViews(recycler)

        // 親レイアウトのそれぞれの位置や大きさ
        val parentTop = paddingTop
        val parentLeft = paddingLeft
        val parentRight = width - paddingRight
        val parentBottom = height - paddingBottom
        val parentHeight = height - paddingTop - paddingBottom
        val parentWidth = width - paddingLeft - paddingRight

        // 一番前の要素のレイアウトのそれぞれの位置や大きさ
        var firstBottom = 0
        var firstTop = 0
        var firstLeft = 0
        var firstRight = 0
        var firstHeight = 0
        var firstWidth = 0

        // 表示する要素の数
        val elementNum = 4

        // ここからひたすら計算
        if (itemCount > 0) {
            var counter = 0

            while (counter < elementNum && counter < itemCount) {
                val view = recycler.getViewForPosition(counter)
                addView(view, 0)
                measureChildWithMargins(view, 0, 0)

                if (counter == 0) {
                    // 先頭のviewの設定
                    firstWidth = getDecoratedMeasuredWidth(view)
                    firstHeight = getDecoratedMeasuredHeight(view)

                    firstBottom = parentBottom / 2 + firstHeight / 2
                    firstTop = firstBottom - firstHeight

                    firstRight = parentRight / 2 + firstWidth / 2
                    firstLeft = firstRight - firstWidth

                    layoutBottom = firstBottom
                    layoutTop = firstTop

                    layoutDecorated(view, firstLeft, firstTop, firstRight, firstBottom)
                } else {
                    // 先頭以外のviewの設定
                    val halfWidth = firstWidth.toDouble() / 2.0
                    val halfHeight = firstHeight.toDouble() / 2.0

                    val m = Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight)
                    val theta = Math.atan(halfHeight / halfWidth)
                    val kai = Math.atan((halfHeight + firstHeight) / halfWidth)

                    val diffHeight = m / elementNum * counter * Math.sin(theta)
                    val diffWidth = m / elementNum * counter * Math.cos(theta)
                    val diffHeight2 = diffWidth * Math.tan(kai)

                    val top = firstTop - diffHeight
                    val left = firstLeft + diffWidth
                    val right = firstRight - diffWidth
                    val bottom = firstBottom - diffHeight2

                    layoutTop = top.toInt()

                    layoutDecorated(view, left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
                }

                counter++
            }
        }
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        // 要素がなかったら0を返してスクロールさせない
        if (childCount <= 0) return 0

        var topPosition = findFirstVisibleItemPosition() // 一番奥の要素のposition 最初は要素数-1
        var backPosition = findLastVisibleItemPosition() // 一番手前の要素のposition 最初は0

        var backView = findViewByPosition(backPosition) // 一番奥にある要素のview
        var topView = findViewByPosition(topPosition) // 一番手前にある要素のview

        if (dy < 0) {
            // 下方向へのスクロール

            // スクロールの分だけ要素全体を移動
            offsetChildrenVertical(-dy)

            if (getDecoratedBottom(topView) < layoutBottom) {
                // 一番手前の要素が表示領域をはみ出た時
                removeAndRecycleViewAt(topPosition, recycler)
            }
        } else {
            // 上方向へのスクロール

            // スクロールの分だけ要素全体を移動
            offsetChildrenVertical(-dy)
            if (getDecoratedTop(backView) > layoutTop) {
                // 一番奥の要素が表示領域をはみ出た時
                removeAndRecycleViewAt(backPosition, recycler)
            }
        }

        return 0
    }
}