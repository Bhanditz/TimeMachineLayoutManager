package com.ronnnnn.library

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.CallSuper
import android.support.v4.view.ViewCompat
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by kokushiseiya on 2016/09/03.
 */
class TimeMachineLayoutManager : RecyclerView.LayoutManager {

    companion object {
        val HORIZONTAL = OrientationHelper.HORIZONTAL
        val VERTICAL = OrientationHelper.VERTICAL
    }

    private val INVALID_POSITION = -1

    val MAX_VISIBLE_ITEMS = 2

    private var mDecoratedChildWidth: Int? = null
    private var mDecoratedChildHeight: Int? = null

    private var mOrientation: Int
    private var mCircleLayout: Boolean
    private var mReverseLayout: Boolean = false
    private var mStackFromEnd: Boolean = false

    private var mPendingScrollPosition: Int = 0

    val mLayoutHelper = LayoutHelper(MAX_VISIBLE_ITEMS)

    private var mViewPostLayout: PostLayoutListener? = null

    private val mOnCenterItemSelectionListeners = ArrayList<OnCenterItemSelectionListener>()
    private var mCenterItemPosition = INVALID_POSITION
    private var mItemsCount: Int = 0

    private var mPendingCarouselSavedState: CarouselSavedState? = null

    var mPendingSavedState: RecyclerView.SavedState? = null

    /**
     * @param orientation should be [.VERTICAL] or [.HORIZONTAL]
     */
    @SuppressWarnings("unused")
    constructor(orientation: Int) : this(orientation, false)

    /**
     * If circleLayout is true then all items will be in cycle. Scroll will be infinite on both sides.

     * @param orientation  should be [.VERTICAL] or [.HORIZONTAL]
     * *
     * @param circleLayout true for enabling circleLayout
     */
    @SuppressWarnings("unused")
    constructor(orientation: Int, circleLayout: Boolean) {
        if (HORIZONTAL != orientation && VERTICAL != orientation) {
            throw IllegalArgumentException("orientation should be HORIZONTAL or VERTICAL")
        }
        mOrientation = orientation
        mCircleLayout = circleLayout
        mPendingScrollPosition = INVALID_POSITION
    }

    /**
     * Setup [CarouselLayoutManager.PostLayoutListener] for this LayoutManager.
     * Its methods will be called for each visible view item after general LayoutManager layout finishes.
     *
     * Generally this method should be used for scaling and translating view item for better (different) view presentation of layouting.

     * @param postLayoutListener listener for item layout changes. Can be null.
     */
    @SuppressWarnings("unused")
    fun setPostLayoutListener(postLayoutListener: PostLayoutListener?) {
        mViewPostLayout = postLayoutListener
        requestLayout()
    }

    /**
     * Setup maximum visible (layout) items on each side of the center item.
     * Basically during scrolling there can be more visible items (+1 item on each side), but in idle state this is the only reached maximum.

     * @param maxVisibleItems should be great then 0, if bot an [IllegalAccessException] will be thrown
     */
    @CallSuper
    @SuppressWarnings("unused")
    fun setMaxVisibleItems(maxVisibleItems: Int) {
        if (0 >= maxVisibleItems) {
            throw IllegalArgumentException("maxVisibleItems can't be less then 1")
        }
        mLayoutHelper.mMaxVisibleItems = maxVisibleItems
        requestLayout()
    }

    /**
     * @return current setup for maximum visible items.
     * *
     * @see .setMaxVisibleItems
     */
    @SuppressWarnings("unused")
    fun getMaxVisibleItems(): Int {
        return mLayoutHelper.mMaxVisibleItems
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    /**
     * @return current layout orientation
     * *
     * @see .VERTICAL

     * @see .HORIZONTAL
     */
    fun getOrientation(): Int {
        return mOrientation
    }

    override fun canScrollHorizontally(): Boolean {
        return 0 != childCount && HORIZONTAL == mOrientation
    }

    override fun canScrollVertically(): Boolean {
        return 0 != childCount && VERTICAL == mOrientation
    }

    /**
     * @return current layout center item
     */
    fun getCenterItemPosition(): Int {
        return mCenterItemPosition
    }

    /**
     * @param onCenterItemSelectionListener listener that will trigger when ItemSelectionChanges. can't be null
     */
    fun addOnItemSelectionListener(onCenterItemSelectionListener: OnCenterItemSelectionListener) {
        mOnCenterItemSelectionListeners.add(onCenterItemSelectionListener)
    }

    /**
     * @param onCenterItemSelectionListener listener that was previously added by [.addOnItemSelectionListener]
     */
    fun removeOnItemSelectionListener(onCenterItemSelectionListener: OnCenterItemSelectionListener) {
        mOnCenterItemSelectionListeners.remove(onCenterItemSelectionListener)
    }

    @SuppressWarnings("RefusedBequest")
    override fun scrollToPosition(position: Int) {
        if (0 > position) {
            throw IllegalArgumentException("position can't be less then 0. position is : " + position)
        }
        if (position >= mItemsCount) {
            throw IllegalArgumentException("position can't be great then adapter items count. position is : " + position)
        }
        mPendingScrollPosition = position
        requestLayout()
    }

    @SuppressWarnings("RefusedBequest")
    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State, position: Int) {
        val mySmoothScroller = object : TimeMachineSmoothScroller(recyclerView.context) {
            override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                if (0 > position) {
                    throw IllegalArgumentException("position can't be less then 0. position is : " + position)
                }
                if (position >= state.itemCount) {
                    throw IllegalArgumentException("position can't be great then adapter items count. position is : " + position)
                }
                return this@TimeMachineLayoutManager.computeScrollVectorForPosition(targetPosition)
            }
        }
        mySmoothScroller.targetPosition = position
        startSmoothScroll(mySmoothScroller)
    }

    fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        if (0 == childCount) {
            return null
        }
        val currentScrollPosition = makeScrollPositionInRange0ToCount(getCurrentScrollPosition(), mItemsCount)
        val direction = if (targetPosition < currentScrollPosition) -1 else 1
        if (HORIZONTAL == mOrientation) {
            return PointF(direction.toFloat(), 0f)
        } else {
            return PointF(0f, direction.toFloat())
        }
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        if (HORIZONTAL == mOrientation) {
            return 0
        }
        return scrollBy(dy, recycler, state)
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        if (VERTICAL == mOrientation) {
            return 0
        }
        return scrollBy(dx, recycler, state)
    }

    /**
     * This method is called from [.scrollHorizontallyBy] and
     * [.scrollVerticallyBy] to calculate needed scroll that is allowed.
     *
     * This method may do relayout work.

     * @param diff     distance that we want to scroll by
     * *
     * @param recycler Recycler to use for fetching potentially cached views for a position
     * *
     * @param state    Transient state of RecyclerView
     * *
     * @return distance that we actually scrolled by
     */
    @CallSuper
    fun scrollBy(diff: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        if (0 == childCount || 0 == diff) {
            return 0
        }
        val resultScroll: Int
        if (mCircleLayout) {
            resultScroll = diff

            mLayoutHelper.mScrollOffset += resultScroll

            val maxOffset = getScrollItemSize() * mItemsCount
            while (0 > mLayoutHelper.mScrollOffset) {
                mLayoutHelper.mScrollOffset += maxOffset
            }
            while (mLayoutHelper.mScrollOffset > maxOffset) {
                mLayoutHelper.mScrollOffset -= maxOffset
            }

            mLayoutHelper.mScrollOffset -= resultScroll
        } else {
            val maxOffset = getMaxScrollOffset()

            if (0 > mLayoutHelper.mScrollOffset + diff) {
                resultScroll = -mLayoutHelper.mScrollOffset //to make it 0
            } else if (mLayoutHelper.mScrollOffset + diff > maxOffset) {
                resultScroll = maxOffset - mLayoutHelper.mScrollOffset //to make it maxOffset
            } else {
                resultScroll = diff
            }
        }
        if (0 != resultScroll) {
            mLayoutHelper.mScrollOffset += resultScroll
            fillData(recycler, state, false)
        }
        return resultScroll
    }

    override fun onMeasure(recycler: RecyclerView.Recycler, state: RecyclerView.State, widthSpec: Int, heightSpec: Int) {
        mDecoratedChildHeight = null
        mDecoratedChildWidth = null

        super.onMeasure(recycler, state, widthSpec, heightSpec)
    }


    /**
     * View生成時やデータセットが変更れた時など、初回のレイアウト決定時にのみ実行される
     */
    @SuppressWarnings("RefusedBequest")
    @CallSuper
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (0 == state.itemCount) {
            removeAndRecycleAllViews(recycler)
            selectItemCenterPosition(INVALID_POSITION)
            return
        }

        var childMeasuringNeeded = false
        if (null == mDecoratedChildWidth) {
            val view = recycler.getViewForPosition(0)
            addView(view)
            measureChildWithMargins(view, 0, 0)

            mDecoratedChildWidth = getDecoratedMeasuredWidth(view)
            mDecoratedChildHeight = getDecoratedMeasuredHeight(view)
            removeAndRecycleView(view, recycler)

            if (INVALID_POSITION == mPendingScrollPosition && null == mPendingCarouselSavedState) {
                mPendingScrollPosition = mCenterItemPosition
            }

            childMeasuringNeeded = true
        }

        if (INVALID_POSITION != mPendingScrollPosition) {
            mLayoutHelper.mScrollOffset = calculateScrollForSelectingPosition(mPendingScrollPosition, state)
            mPendingScrollPosition = INVALID_POSITION
            mPendingCarouselSavedState = null
        } else if (null != mPendingCarouselSavedState) {
            mLayoutHelper.mScrollOffset = calculateScrollForSelectingPosition(mPendingCarouselSavedState!!.mCenterItemPosition, state)
            mPendingCarouselSavedState = null
        } else if (state.didStructureChange() && INVALID_POSITION != mCenterItemPosition) {
            mLayoutHelper.mScrollOffset = calculateScrollForSelectingPosition(mCenterItemPosition, state)
        }

        fillData(recycler, state, childMeasuringNeeded)
    }

    private fun calculateScrollForSelectingPosition(itemPosition: Int, state: RecyclerView.State): Int {
        val fixedItemPosition = if (itemPosition < state.itemCount) itemPosition else state.itemCount - 1
        return if (VERTICAL == mOrientation) fixedItemPosition * mDecoratedChildHeight!!  else fixedItemPosition * mDecoratedChildWidth!!
    }

    private fun fillData(recycler: RecyclerView.Recycler, state: RecyclerView.State, childMeasuringNeeded: Boolean) {
        val currentScrollPosition = getCurrentScrollPosition()
        generateLayoutOrder(currentScrollPosition, state)
        removeAndRecycleUnusedViews(mLayoutHelper, recycler)

        val width = getWidthNoPadding()
        val height = getHeightNoPadding()
        if (VERTICAL == mOrientation) {
            fillDataVertical(recycler, width, height, childMeasuringNeeded)
        } else {
            fillDataHorizontal(recycler, width, height, childMeasuringNeeded)
        }

        recycler.clear()

        detectOnItemSelectionChanged(currentScrollPosition, state)
    }

    private fun detectOnItemSelectionChanged(currentScrollPosition: Float, state: RecyclerView.State) {
        val absCurrentScrollPosition = makeScrollPositionInRange0ToCount(currentScrollPosition, state.itemCount)
        val centerItem = Math.round(absCurrentScrollPosition)

        if (mCenterItemPosition != centerItem) {
            mCenterItemPosition = centerItem
            Handler(Looper.getMainLooper()).post { selectItemCenterPosition(centerItem) }
        }
    }

    private fun selectItemCenterPosition(centerItem: Int) {
        for (onCenterItemSelectionListener in mOnCenterItemSelectionListeners) {
            onCenterItemSelectionListener.onCenterItemChanged(centerItem)
        }
    }

    /**
     * recyclerViewの全体の位置を調整
     */
    private fun fillDataVertical(recycler: RecyclerView.Recycler, width: Int, height: Int, childMeasuringNeeded: Boolean) {
        //val start = (width - mDecoratedChildWidth!!) / 2
        val start = (width - mDecoratedChildWidth!!) / 2
        val end = start + mDecoratedChildWidth!!

        // val centerViewTop = (height - mDecoratedChildHeight!!) / 2
        val centerViewTop = (height - mDecoratedChildHeight!!)

        var i = 0
        val count = mLayoutHelper.mLayoutOrder.size
        while (i < count) {
            val layoutOrder = mLayoutHelper.mLayoutOrder[i]
            val offset = getCardOffsetByPositionDiff(layoutOrder.mItemPositionDiff)
            val top = centerViewTop + offset
            val bottom = top + mDecoratedChildHeight!!
            fillChildItem(start, top, end, bottom, layoutOrder, recycler, i, childMeasuringNeeded)
            ++i
        }
    }

    private fun fillDataHorizontal(recycler: RecyclerView.Recycler, width: Int, height: Int, childMeasuringNeeded: Boolean) {
        val top = (height - mDecoratedChildHeight!!) / 2
        val bottom = top + mDecoratedChildHeight!!

        val centerViewStart = (width - mDecoratedChildWidth!!) / 2

        var i = 0
        val count = mLayoutHelper.mLayoutOrder.size
        while (i < count) {
            val layoutOrder = mLayoutHelper.mLayoutOrder[i]
            val offset = getCardOffsetByPositionDiff(layoutOrder.mItemPositionDiff)
            val start = centerViewStart + offset
            val end = start + mDecoratedChildWidth!!
            fillChildItem(start, top, end, bottom, layoutOrder, recycler, i, childMeasuringNeeded)
            ++i
        }
    }

    private fun removeAndRecycleUnusedViews(layoutHelper: LayoutHelper, recycler: RecyclerView.Recycler) {
        val viewsToRemove = ArrayList<View>()
        var i = 0
        val size = childCount
        while (i < size) {
            val child = getChildAt(i)
            val lp = child.layoutParams
            if (lp !is RecyclerView.LayoutParams) {
                viewsToRemove.add(child)
                ++i
                continue
            }
            val adapterPosition = lp.viewAdapterPosition
            if (lp.isItemRemoved || !layoutHelper.hasAdapterPosition(adapterPosition)) {
                viewsToRemove.add(child)
            }
            ++i
        }


        for (view in viewsToRemove) {
            removeAndRecycleView(view, recycler)
        }
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    private fun fillChildItem(start: Int, top: Int, end: Int, bottom: Int, layoutOrder: LayoutOrder, recycler: RecyclerView.Recycler, i: Int, childMeasuringNeeded: Boolean) {
        val view = bindChild(layoutOrder.mItemAdapterPosition, recycler, childMeasuringNeeded)
        ViewCompat.setElevation(view, i.toFloat())
        var transformation: ItemTransformation? = null
        if (null != mViewPostLayout) {
            transformation = mViewPostLayout!!.transformChild(view, layoutOrder.mItemPositionDiff, mOrientation)
        }
        if (null == transformation) {
            view.layout(start, top, end, bottom)
        } else {
            view.layout(Math.round(start + transformation.translationX), Math.round(top + transformation.translationY),
                    Math.round(end + transformation.translationX), Math.round(bottom + transformation.translationY))

            ViewCompat.setScaleX(view, transformation.scaleX)
            ViewCompat.setScaleY(view, transformation.scaleY)
        }
    }

    /**
     * @return current scroll position of center item. this value can be in any range if it is cycle layout.
     * * if this is not, that then it is in [0, [- 1][.mItemsCount]]
     */
    private fun getCurrentScrollPosition(): Float {
        val fullScrollSize = getMaxScrollOffset()
        if (0 == fullScrollSize) {
            return 0f
        }
        return 1.0f * mLayoutHelper.mScrollOffset / getScrollItemSize()
    }

    /**
     * @return maximum scroll value to fill up all items in layout. Generally this is only needed for non cycle layouts.
     */
    private fun getMaxScrollOffset(): Int {
        return getScrollItemSize() * (mItemsCount - 1)
    }

    /**
     * Because we can support old Android versions, we should layout our children in specific order to make our center view in the top of layout
     * (this item should layout last). So this method will calculate layout order and fill up [.mLayoutHelper] object.
     * This object will be filled by only needed to layout items. Non visible items will not be there.

     * @param currentScrollPosition current scroll position this is a value that indicates position of center item
     * *                              (if this value is int, then center item is really in the center of the layout, else it is near state).
     * *                              Be aware that this value can be in any range is it is cycle layout
     * *
     * @param state                 Transient state of RecyclerView
     * *
     * @see .getCurrentScrollPosition
     */
    private fun generateLayoutOrder(currentScrollPosition: Float, state: RecyclerView.State) {
        mItemsCount = state.itemCount
        val absCurrentScrollPosition = makeScrollPositionInRange0ToCount(currentScrollPosition, mItemsCount)
        val centerItem = Math.round(absCurrentScrollPosition)

        if (mCircleLayout && 1 < mItemsCount) {
            val layoutCount = Math.min(mLayoutHelper.mMaxVisibleItems * 2 + 3, mItemsCount)// + 3 = 1 (center item) + 2 (addition bellow maxVisibleItems)

            mLayoutHelper.initLayoutOrder(layoutCount)

            val countLayoutHalf = layoutCount / 2
            // before center item
            for (i in 1..countLayoutHalf) {
                val position = Math.round(absCurrentScrollPosition - i + mItemsCount) % mItemsCount
                mLayoutHelper.setLayoutOrder(countLayoutHalf - i, position, centerItem.toFloat() - absCurrentScrollPosition - i.toFloat())
            }
            // after center item
            for (i in layoutCount - 1 downTo countLayoutHalf + 1) {
                val position = Math.round(absCurrentScrollPosition - i + layoutCount) % mItemsCount
                mLayoutHelper.setLayoutOrder(i - 1, position, centerItem - absCurrentScrollPosition + layoutCount - i)
            }
            mLayoutHelper.setLayoutOrder(layoutCount - 1, centerItem, centerItem - absCurrentScrollPosition)

        } else {
            val firstVisible = Math.max(centerItem - mLayoutHelper.mMaxVisibleItems - 1, 0)
            val lastVisible = Math.min(centerItem + mLayoutHelper.mMaxVisibleItems + 1, mItemsCount - 1)
            val layoutCount = lastVisible - firstVisible + 1

            mLayoutHelper.initLayoutOrder(layoutCount)

            for (i in firstVisible..lastVisible) {
                if (i == centerItem) {
                    mLayoutHelper.setLayoutOrder(layoutCount - 1, i, i - absCurrentScrollPosition)
                } else if (i < centerItem) {
                    mLayoutHelper.setLayoutOrder(i - firstVisible, i, i - absCurrentScrollPosition)
                } else {
                    mLayoutHelper.setLayoutOrder(layoutCount - (i - centerItem) - 1, i, i - absCurrentScrollPosition)
                }
            }
        }
    }

    fun getWidthNoPadding(): Int {
        return width - paddingStart - paddingEnd
    }

    fun getHeightNoPadding(): Int {
        return height - paddingEnd - paddingStart
    }

    private fun bindChild(position: Int, recycler: RecyclerView.Recycler, childMeasuringNeeded: Boolean): View {
        val view = findViewForPosition(recycler, position)

        if (null == view.parent) {
            addView(view)
            measureChildWithMargins(view, 0, 0)
        } else {
            view.bringToFront()
            if (childMeasuringNeeded) {
                measureChildWithMargins(view, 0, 0)
            }
        }

        return view
    }

    private fun findViewForPosition(recycler: RecyclerView.Recycler, position: Int): View {
        var i = 0
        val size = childCount
        while (i < size) {
            val child = getChildAt(i)
            val lp = child.layoutParams
            if (lp !is RecyclerView.LayoutParams) {
                ++i
                continue
            }
            val adapterPosition = lp.viewAdapterPosition
            if (adapterPosition == position) {
                if (lp.isItemChanged) {
                    recycler.bindViewToPosition(child, position)
                    measureChildWithMargins(child, 0, 0)
                }
                return child
            }
            ++i
        }
        val view = recycler.getViewForPosition(position)
        recycler.bindViewToPosition(view, position)
        return view
    }

    /**
     * Called during [.fillData] to calculate item offset from layout center line.
     *
     * Returns [.convertItemPositionDiffToSmoothPositionDiff] * (size off area above center item when it is on the center).
     * Sign is: plus if this item is bellow center line, minus if not
     *
     * ----- - area above it
     * ||||| - center item
     * ----- - area bellow it (it has the same size as are above center item)

     * @param itemPositionDiff current item difference with layout center line. if this is 0, then this item center is in layout center line.
     * *                         if this is 1 then this item is bellow the layout center line in the full item size distance.
     * *
     * @return offset in scroll px coordinates.
     */
    fun getCardOffsetByPositionDiff(itemPositionDiff: Float): Int {
        val smoothPosition = convertItemPositionDiffToSmoothPositionDiff(itemPositionDiff)

        val dimenDiff: Int
        if (VERTICAL == mOrientation) {
            dimenDiff = (getHeightNoPadding() - mDecoratedChildHeight!!) / 2
        } else {
            dimenDiff = (getWidthNoPadding() - mDecoratedChildWidth!!) / 2
        }
        //noinspection NumericCastThatLosesPrecision
        return Math.round(Math.signum(itemPositionDiff).toDouble() * dimenDiff.toDouble() * smoothPosition).toInt()
    }

    /**
     * Called during [.getCardOffsetByPositionDiff] for better item movement.
     * Current implementation speed up items that are far from layout center line and slow down items that are close to this line.
     * This code is full of maths. If you want to make items move in a different way, probably you should override this method.
     * Please see code comments for better explanations.

     * @param itemPositionDiff current item difference with layout center line. if this is 0, then this item center is in layout center line.
     * *                         if this is 1 then this item is bellow the layout center line in the full item size distance.
     * *
     * @return smooth position offset. needed for scroll calculation and better user experience.
     * *
     * @see .getCardOffsetByPositionDiff
     */
    @SuppressWarnings("MagicNumber", "InstanceMethodNamingConvention")
    fun convertItemPositionDiffToSmoothPositionDiff(itemPositionDiff: Float): Double {
        // generally item moves the same way above center and bellow it. So we don't care about diff sign.
        val absIemPositionDiff = Math.abs(itemPositionDiff)

        // we detect if this item is close for center or not. We use (1 / maxVisibleItem) ^ (1/3) as close definer.
        if (absIemPositionDiff > StrictMath.pow((1.0f / mLayoutHelper.mMaxVisibleItems).toDouble(), (1.0f / 3).toDouble())) {
            // this item is far from center line, so we should make it move like square root function
            return StrictMath.pow((absIemPositionDiff / mLayoutHelper.mMaxVisibleItems).toDouble(), (1 / 2.0f).toDouble())
        } else {
            // this item is close from center line. we should slow it down and don't make it speed up very quick.
            // so square function in range of [0, (1/maxVisible)^(1/3)] is quite good in it;
            return StrictMath.pow(absIemPositionDiff.toDouble(), 2.0)
        }
    }

    /**
     * @return full item size
     */
    fun getScrollItemSize(): Int {
        if (VERTICAL == mOrientation) {
            return mDecoratedChildHeight!!
        } else {
            return mDecoratedChildWidth!!
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        if (null != mPendingCarouselSavedState) {
            return CarouselSavedState(mPendingCarouselSavedState!!)
        }
        val savedState = CarouselSavedState(super.onSaveInstanceState())
        savedState.mCenterItemPosition = mCenterItemPosition
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is CarouselSavedState) {
            mPendingCarouselSavedState = state as CarouselSavedState?

            super.onRestoreInstanceState(mPendingCarouselSavedState!!.mSuperState)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    /**
     * @return Scroll offset from nearest item from center
     */
    internal fun getOffsetCenterView(): Int {
        return Math.round(getCurrentScrollPosition()) * getScrollItemSize() - mLayoutHelper.mScrollOffset
    }

    internal fun getOffsetForCurrentView(view: View): Int {
        val position = getPosition(view)
        val fullCircles = mLayoutHelper.mScrollOffset / (mItemsCount * getScrollItemSize())
        var fullOffset = fullCircles * mItemsCount * getScrollItemSize()
        if (0 > mLayoutHelper.mScrollOffset) {
            fullOffset -= 1
        }

        if (0 == fullOffset || 0 < Math.signum(fullOffset.toFloat())) {
            return mLayoutHelper.mScrollOffset - position * getScrollItemSize() - fullOffset
        } else {
            return mLayoutHelper.mScrollOffset + position * getScrollItemSize() - fullOffset
        }
    }

    /**
     * Helper method that make scroll in range of [0, count). Generally this method is needed only for cycle layout.

     * @param currentScrollPosition any scroll position range.
     * *
     * @param count                 adapter items count
     * *
     * @return good scroll position in range of [0, count)
     */
    private fun makeScrollPositionInRange0ToCount(currentScrollPosition: Float, count: Int): Float {
        var absCurrentScrollPosition = currentScrollPosition
        while (0 > absCurrentScrollPosition) {
            absCurrentScrollPosition += count.toFloat()
        }
        while (Math.round(absCurrentScrollPosition) >= count) {
            absCurrentScrollPosition -= count.toFloat()
        }
        return absCurrentScrollPosition
    }

    /**
     * This interface methods will be called for each visible view item after general LayoutManager layout finishes.
     *
     * Generally this method should be used for scaling and translating view item for better (different) view presentation of layouting.
     */
    @SuppressWarnings("InterfaceNeverImplemented")
    interface PostLayoutListener {

        /**
         * Called after child layout finished. Generally you can do any translation and scaling work here.

         * @param child                    view that was layout
         * *
         * @param itemPositionToCenterDiff view center line difference to layout center. if > 0 then this item is bellow layout center line, else if not
         * *
         * @param orientation              layoutManager orientation [.getLayoutDirection]
         */
        fun transformChild(child: View, itemPositionToCenterDiff: Float, orientation: Int): ItemTransformation
    }

    interface OnCenterItemSelectionListener {

        /**
         * Listener that will be called on every change of center item.
         * This listener will be triggered on **every** layout operation if item was changed.
         * Do not do any expensive operations in this method since this will effect scroll experience.

         * @param adapterPosition current layout center item
         */
        fun onCenterItemChanged(adapterPosition: Int)
    }

    /**
     * Helper class that holds currently visible items.
     * Generally this class fills this list.
     *
     * This class holds all scroll and maxVisible items state.

     * @see .getMaxVisibleItems
     */
    class LayoutHelper(var mMaxVisibleItems: Int) {

        var mScrollOffset: Int = 0

        var mLayoutOrder = ArrayList<LayoutOrder>()

        val mReusedItems = ArrayList<WeakReference<LayoutOrder>>()

        /**
         * Called before any fill calls. Needed to recycle old items and init new array list. Generally this list is an array an it is reused.

         * @param layoutCount items count that will be layout
         */
        fun initLayoutOrder(layoutCount: Int) {
            if (mLayoutOrder.size != layoutCount) {
                recycleItems(mLayoutOrder)
                fillLayoutOrder(layoutCount)
            }
        }

        /**
         * Called during layout generation process of filling this list. Should be called only after [.initLayoutOrder] method call.

         * @param arrayPosition       position in layout order
         * *
         * @param itemAdapterPosition adapter position of item for future data filling logic
         * *
         * @param itemPositionDiff    difference of current item scroll position and center item position.
         * *                            if this is a center item and it is in real center of layout, then this will be 0.
         * *                            if current layout is not in the center, then this value will never be int.
         * *                            if this item center is bellow layout center line then this value is greater then 0,
         * *                            else less then 0.
         */
        fun setLayoutOrder(arrayPosition: Int, itemAdapterPosition: Int, itemPositionDiff: Float) {
            val item = mLayoutOrder[arrayPosition]
            item.mItemAdapterPosition = itemAdapterPosition
            item.mItemPositionDiff = itemPositionDiff
        }

        /**
         * Checks is this screen Layout has this adapterPosition view in layout

         * @param adapterPosition adapter position of item for future data filling logic
         * *
         * @return true is adapterItem is in layout
         */
        fun hasAdapterPosition(adapterPosition: Int): Boolean {
            for (layoutOrder in mLayoutOrder) {
                if (layoutOrder.mItemAdapterPosition == adapterPosition) {
                    return true
                }
            }

            return false
        }

        @SuppressWarnings("VariableArgumentMethod")
        private fun recycleItems(layoutOrders: ArrayList<LayoutOrder>) {
            for (layoutOrder in layoutOrders) {
                //noinspection ObjectAllocationInLoop
                mReusedItems.add(WeakReference(layoutOrder))
            }
        }

        private fun fillLayoutOrder(layoutCount: Int) {
            for (i in 0..layoutCount - 1) {
                mLayoutOrder.add(i, createLayoutOrder())
            }
        }

        private fun createLayoutOrder(): LayoutOrder {
            val iterator = mReusedItems.iterator()
            while (iterator.hasNext()) {
                val layoutOrderWeakReference = iterator.next()
                val layoutOrder = layoutOrderWeakReference.get()
                iterator.remove()
                if (null != layoutOrder) {
                    return layoutOrder
                }
            }
            return LayoutOrder()
        }
    }

    /**
     * Class that holds item data.
     * This class is filled during [.generateLayoutOrder] and used during [.fillData]
     */
    class LayoutOrder {

        /**
         * Item adapter position
         */
        var mItemAdapterPosition: Int = 0
        /**
         * Item center difference to layout center. If center of item is bellow layout center, then this value is greater then 0, else it is less.
         */
        var mItemPositionDiff: Float = 0.toFloat()
    }

    class CarouselSavedState : Parcelable {

        var mSuperState: Parcelable? = null
        var mCenterItemPosition: Int = 0

        constructor(superState: Parcelable?) {
            superState ?: return
            mSuperState = superState
        }

        private constructor(parcel: Parcel) {
            mSuperState = parcel.readParcelable<Parcelable>(Parcelable::class.java.classLoader)
            mCenterItemPosition = parcel.readInt()
        }

        constructor(other: CarouselSavedState) {
            mSuperState = other.mSuperState
            mCenterItemPosition = other.mCenterItemPosition
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeParcelable(mSuperState, flags)
            dest.writeInt(mCenterItemPosition)
        }

        companion object {

            val CREATOR: Parcelable.Creator<CarouselSavedState> = object : Parcelable.Creator<CarouselSavedState> {
                override fun createFromParcel(source: Parcel): CarouselSavedState {
                    return CarouselSavedState(source)
                }

                override fun newArray(size: Int): Array<CarouselSavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}