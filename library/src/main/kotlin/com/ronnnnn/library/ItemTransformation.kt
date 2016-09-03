package com.ronnnnn.library

/**
 * Created by kokushiseiya on 2016/09/03.
 */
class ItemTransformation(scaleX: Float, scaleY: Float, translationX: Float, translationY: Float) {

    val scaleX: Float
    val scaleY: Float

    val translationX: Float
    val translationY: Float

    init {
        this.scaleX = scaleX
        this.scaleY = scaleY

        this.translationX = translationX
        this.translationY = translationY
    }
}