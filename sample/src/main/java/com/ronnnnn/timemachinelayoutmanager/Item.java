package com.ronnnnn.timemachinelayoutmanager;

/**
 * Created by kokushiseiya on 2017/04/06.
 */

class Item {

    private int stringId;
    private int textColorId;
    private int backgroundColorId;

    public Item(int stringId, int textColorId, int backgroundColorId) {
        this.stringId = stringId;
        this.textColorId = textColorId;
        this.backgroundColorId = backgroundColorId;
    }

    int getStringId() {
        return stringId;
    }

    int getTextColorId() {
        return textColorId;
    }

    int getBackgroundColorId() {
        return backgroundColorId;
    }
}
