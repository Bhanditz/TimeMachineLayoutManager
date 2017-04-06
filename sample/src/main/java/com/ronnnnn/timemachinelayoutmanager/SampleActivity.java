package com.ronnnnn.timemachinelayoutmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.ronnnnn.library.CarouselLayoutManager;
import com.ronnnnn.library.CarouselZoomPostLayoutListener;

import java.util.ArrayList;
import java.util.List;

public class SampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        SampleRecyclerAdapter sampleRecyclerAdapter = new SampleRecyclerAdapter(this);
        sampleRecyclerAdapter.setAndRefreshItems(getSampleItemList());

        CarouselLayoutManager carouselLayoutManager =
                new CarouselLayoutManager(CarouselLayoutManager.VERTICAL, true);
        carouselLayoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.sample_recycler_view);
        recyclerView.setAdapter(sampleRecyclerAdapter);
        recyclerView.setLayoutManager(carouselLayoutManager);
    }

    private List<Item> getSampleItemList() {
        List<Item> itemList = new ArrayList<>();
        itemList.add(new Item(R.string.color_name_1, android.R.color.white, R.color.background_color_1));
        itemList.add(new Item(R.string.color_name_2, android.R.color.white, R.color.background_color_2));
        itemList.add(new Item(R.string.color_name_3, android.R.color.white, R.color.background_color_3));
        itemList.add(new Item(R.string.color_name_4, android.R.color.white, R.color.background_color_4));
        itemList.add(new Item(R.string.color_name_5, android.R.color.white, R.color.background_color_5));
        itemList.add(new Item(R.string.color_name_6, android.R.color.white, R.color.background_color_6));
        itemList.add(new Item(R.string.color_name_7, android.R.color.black, R.color.background_color_7));
        itemList.add(new Item(R.string.color_name_8, android.R.color.black, R.color.background_color_8));
        itemList.add(new Item(R.string.color_name_9, android.R.color.white, R.color.background_color_9));
        itemList.add(new Item(R.string.color_name_10, android.R.color.black, R.color.background_color_10));
        itemList.add(new Item(R.string.color_name_11, android.R.color.black, R.color.background_color_11));
        itemList.add(new Item(R.string.color_name_12, android.R.color.black, R.color.background_color_12));
        itemList.add(new Item(R.string.color_name_13, android.R.color.black, R.color.background_color_13));
        itemList.add(new Item(R.string.color_name_14, android.R.color.black, R.color.background_color_14));
        itemList.add(new Item(R.string.color_name_15, android.R.color.black, R.color.background_color_15));
        itemList.add(new Item(R.string.color_name_16, android.R.color.white, R.color.background_color_16));
        itemList.add(new Item(R.string.color_name_17, android.R.color.white, R.color.background_color_17));
        itemList.add(new Item(R.string.color_name_18, android.R.color.black, R.color.background_color_18));
        itemList.add(new Item(R.string.color_name_19, android.R.color.white, R.color.background_color_19));

        return itemList;
    }
}
