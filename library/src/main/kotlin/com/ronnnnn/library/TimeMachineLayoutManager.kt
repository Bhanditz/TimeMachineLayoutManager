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

    var elementCounterBack = 0
    var elementCounterTop = 0

    // 一番前の要素のレイアウトのそれぞれの位置や大きさ
    var firstBottom = 0
    var firstTop = 0
    var firstLeft = 0
    var firstRight = 0
    var firstHeight = 0
    var firstWidth = 0

    // 一番奥の要素のレイアウトのそれぞれの位置や大きさ
    var lastBottom = 0
    var lastTop = 0
    var lastLeft = 0
    var lastRight = 0

    var diff = 0
    var diff2 = 0

    var theta = 0.0

    var originX = 0.0
    var originY = 0.0

    val elementNum = 4

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

                    if (counter == elementNum - 1) {
                        lastTop = firstTop
                        lastLeft = firstLeft
                        lastRight = firstRight
                        lastBottom = firstBottom
                    }


                    val halfWidth = firstWidth.toDouble() / 2.0
                    val halfHeight = firstHeight.toDouble() / 2.0

                    originX = firstLeft + halfWidth
                    originY = firstTop - halfHeight

                    layoutDecorated(view, firstLeft, firstTop, firstRight, firstBottom)
                } else {
                    // 先頭以外のviewの設定
                    val halfWidth = firstWidth.toDouble() / 2.0
                    val halfHeight = firstHeight.toDouble() / 2.0

                    val m = Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight)
                    theta = Math.atan(halfHeight / halfWidth)
                    val kai = Math.atan((halfHeight + firstHeight) / halfWidth)

                    val diffHeight = m / elementNum * counter * Math.sin(theta)
                    diff = diffHeight.toInt()
                    val diffWidth = m / elementNum * counter * Math.cos(theta)
                    val diffHeight2 = diffWidth * Math.tan(kai)
                    diff2 = diffHeight2.toInt()

                    val top = firstTop - diffHeight
                    val left = firstLeft + diffWidth
                    val right = firstRight - diffWidth
                    val bottom = firstBottom - diffHeight2

                    layoutTop = top.toInt()



                    if (counter == elementNum - 1) {
                        lastTop = top.toInt()
                        lastLeft = left.toInt()
                        lastRight = right.toInt()
                        lastBottom = bottom.toInt()
                    }

                    layoutDecorated(view, left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
                }

                counter++
            }
        }

        elementCounterBack = elementNum
        elementCounterTop = itemCount - 1
    }

    /**
     * スクロールした時に呼ばれる処理
     * 重いことはやらない
     */
    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State?): Int {
        // 要素がなかったら0を返してスクロールさせない
        if (childCount <= 0) return 0

        var backPosition = findLastVisibleItemPosition() // 一番最初は0
        var topPosition = findFirstVisibleItemPosition() // 一番最初は要素数-1

        var backView = findViewByPosition(topPosition) // 一番奥にある要素のview
        var topView = findViewByPosition(backPosition) // 一番手前にある要素のview

        var backIndex = 0
        var topIndex = childCount - 1

        if (dy < 0) {
            // 下方向へのスクロール

            // スクロールの分だけ要素全体を移動
            offsetChildrenVertical(-dy)

            topView.alpha = topView.alpha / 1.5f

            // 各要素のスケール調整
            for (i in 0..itemCount) {
                val view = findViewByPosition(i)

                if (view != null) {
                    val left = getDecoratedLeft(view)
                    val right = getDecoratedRight(view)
                    val top = getDecoratedTop(view)
                    val bottom = getDecoratedBottom(view)
                    val width = getDecoratedMeasuredWidth(view)
                    val height = getDecoratedMeasuredHeight(view)

                    val y = top - originY
                    val param = (-dy / Math.sin(theta) + y / Math.sin(theta)) / (y / Math.sin(theta))

                    val newLeft = left - (width * param / 2 - width / 2)
                    val newRight = right + (width * param / 2 - width / 2)
                    val newTop = top - dy
                    val newBottom = top + height * param

                    layoutDecorated(view, newLeft.toInt(), newTop, newRight.toInt(), newBottom.toInt())
                }
            }

            if (topView != null && getDecoratedBottom(topView) > layoutBottom + diff / elementNum) {
                // 一番手前の要素が表示領域をはみ出た時

                if (topPosition >= 0) {
                    // 一番奥に新しいviewの生成
                    backView = recycler.getViewForPosition(elementCounterBack)
                    addView(backView, 0)
                    measureChildWithMargins(backView, 0, 0)
                    layoutDecorated(backView, lastLeft, lastTop, lastRight, lastBottom)

                    // 一番手前のviewを削除
                    removeViewAt(childCount - 1)

                    elementCounterBack++
                    elementCounterTop++

                    if (elementCounterBack >= itemCount) {
                        elementCounterBack = 0
                    }

                    if (elementCounterTop >= itemCount) {
                        elementCounterTop = 0
                    }
                }

            }
        } else {
            // 上方向へのスクロール

            // スクロールの分だけ要素全体を移動
            offsetChildrenVertical(-dy)

            // 各要素のスケール調整
            for (i in 0..itemCount) {
                val view = findViewByPosition(i)

                if (view != null) {
                    val left = getDecoratedLeft(view)
                    val right = getDecoratedRight(view)
                    val top = getDecoratedTop(view)
                    val bottom = getDecoratedBottom(view)
                    val width = getDecoratedMeasuredWidth(view)
                    val height = getDecoratedMeasuredHeight(view)

                    val y = top - originY
                    val param = (-dy / Math.sin(theta) + y / Math.sin(theta)) / (y / Math.sin(theta))

                    val newLeft = left - (width * param / 2 - width / 2)
                    val newRight = right + (width * param / 2 - width / 2)
                    val newTop = top - dy
                    val newBottom = top + height * param

                    layoutDecorated(view, newLeft.toInt(), newTop, newRight.toInt(), newBottom.toInt())
                }
            }

            if (backView != null && getDecoratedTop(backView) < layoutTop - diff / elementNum) {
                // 一番奥の要素が表示領域をはみ出た時

                if (backPosition + 1 < itemCount) {
                    // 一番手前に新しいviewの生成
                    topView = recycler.getViewForPosition(elementCounterTop)
                    addView(topView, childCount)
                    measureChildWithMargins(topView, 0, 0)
                    layoutDecorated(topView, firstLeft, firstTop, firstRight, firstBottom)

                    // 一番奥のviewを削除
                    removeViewAt(0)

                    elementCounterBack--
                    elementCounterTop--

                    if (elementCounterBack < 0) {
                        elementCounterBack = itemCount - 1
                    }

                    if (elementCounterTop < 0) {
                        elementCounterTop = itemCount - 1
                    }
                }
            }
        }

        return dy
    }
}