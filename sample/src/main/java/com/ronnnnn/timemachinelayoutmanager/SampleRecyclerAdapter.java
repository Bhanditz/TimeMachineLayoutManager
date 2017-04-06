package com.ronnnnn.timemachinelayoutmanager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kokushiseiya on 2017/04/06.
 */

class SampleRecyclerAdapter extends RecyclerView.Adapter<SampleRecyclerAdapter.ItemViewHolder> {

    private final Context context;
    private final List<Item> itemList;

    public SampleRecyclerAdapter(Context context) {
        this.context = context;
        itemList = new ArrayList<>();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(
                LayoutInflater.from(context).inflate(R.layout.list_item_sample_recycler_view, parent, false),
                context
        );
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.onBind(itemList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    void setAndRefreshItems(List<Item> itemList) {
        this.itemList.clear();
        this.itemList.addAll(itemList);
        notifyDataSetChanged();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final Context context;

        private final TextView colorTextView;

        public ItemViewHolder(View itemView, Context context) {
            super(itemView);

            this.context = context;

            colorTextView = (TextView) itemView.findViewById(R.id.color_text_view);
        }

        public void onBind(Item item) {
            itemView.setBackgroundResource(item.getBackgroundColorId());

            colorTextView.setText(item.getStringId());
            colorTextView.setTextColor(context.getResources().getColor(item.getTextColorId()));
        }
    }
}
