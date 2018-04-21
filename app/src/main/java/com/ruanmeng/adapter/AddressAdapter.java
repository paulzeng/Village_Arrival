package com.ruanmeng.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.ruanmeng.model.CommonData;
import com.ruanmeng.village_arrival.R;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter {

    private List<CommonData> mDataSet;
    private LayoutInflater mInflater;
    private OnItemClickListener onItemClickListener;
    private OnItemDeleteClickListener onItemDeleteClickListener;
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();

    public AddressAdapter(Context context, List<CommonData> dataSet) {
        mDataSet = dataSet;
        mInflater = LayoutInflater.from(context);

        // uncomment if you want to open only one row at a time
        binderHelper.setOpenOnlyOne(true);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_address_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        final ViewHolder holder = (ViewHolder) h;

        if (mDataSet != null && 0 <= position && position < mDataSet.size()) {
            final CommonData data = mDataSet.get(position);

            // Use ViewBindHelper to restore and save the open/close state of the SwipeRevealView
            // put an unique string id as value, can be any string which uniquely define the data
            binderHelper.bind(holder.swipeLayout, data.getCommonAddressId());

            // Bind your data here
            holder.bind(data);
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet == null ? 0 : mDataSet.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemDeleteClickListener(OnItemDeleteClickListener onItemDeleteClickListener) {
        this.onItemDeleteClickListener = onItemDeleteClickListener;
    }

    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onSaveInstanceState(Bundle)}
     */
    public void saveStates(Bundle outState) {
        binderHelper.saveStates(outState);
    }

    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onRestoreInstanceState(Bundle)}
     */
    public void restoreStates(Bundle inState) {
        binderHelper.restoreStates(inState);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private SwipeRevealLayout swipeLayout;
        private View frontLayout;
        private TextView tv_addr, tv_name, tv_del;

        ViewHolder(View itemView) {
            super(itemView);
            swipeLayout = itemView.findViewById(R.id.swipe_layout);
            frontLayout = itemView.findViewById(R.id.front_layout);
            tv_addr = itemView.findViewById(R.id.item_address_addr);
            tv_name = itemView.findViewById(R.id.item_address_name);
            tv_del = itemView.findViewById(R.id.item_address_delete);
        }

        @SuppressLint("SetTextI18n")
        void bind(final CommonData data) {
            tv_addr.setText(data.getAddress() + data.getDetailAdress());
            tv_name.setText(data.getName() + "    " + data.getMobile());

            tv_del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemDeleteClickListener != null) {
                        onItemDeleteClickListener.onDelete(getAdapterPosition());
                    }
                }
            });

            frontLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) onItemClickListener.onClick(getAdapterPosition());
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public interface OnItemDeleteClickListener {
        void onDelete(int position);
    }
}
