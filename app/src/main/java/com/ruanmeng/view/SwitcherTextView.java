package com.ruanmeng.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.ruanmeng.model.CommonData;
import com.ruanmeng.village_arrival.R;

import java.util.ArrayList;
import java.util.List;

public class SwitcherTextView extends ViewSwitcher implements ViewSwitcher.ViewFactory {

    private List<CommonData> demoBeans = new ArrayList<>();
    private int mIndex;
    private ItemDataListener itemDataListener;
    private View view; //click view
    private static final int AUTO_RUN_FLIP_TEXT = 11;
    private static final int WAIT_TIME = 3000;

    public SwitcherTextView(Context context) {
        super(context);
        init();
    }

    public SwitcherTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setFactory(this);
        setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_bottom_to_top_in_fast));
        setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.trans_bottom_to_top_out_fast));
    }

    @Override
    public View makeView() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_switcher_list, null);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        view.setLayoutParams(params);
        return view;
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AUTO_RUN_FLIP_TEXT:

                    if (demoBeans.size() > 0) {
                        mHandler.sendEmptyMessageDelayed(AUTO_RUN_FLIP_TEXT, WAIT_TIME);
                        setText(mIndex);
                    }
                    mIndex++;
                    if (mIndex * 2 > demoBeans.size() - 1) {
                        mIndex = 0;
                    }
                    break;
            }
        }
    };

    public void setData(List<CommonData> datas, ItemDataListener listener, View v) {
        view = v;
        itemDataListener = listener;
        if (demoBeans.size() > 0) demoBeans.clear();
        demoBeans.addAll(datas);
        mIndex = 0;
        mHandler.removeMessages(AUTO_RUN_FLIP_TEXT);
        mHandler.sendEmptyMessage(AUTO_RUN_FLIP_TEXT);
    }

    public void setText(final int position) {
        final View view = getNextView();
        LinearLayout first = view.findViewById(R.id.switcher_first);
        LinearLayout second = view.findViewById(R.id.switcher_second);
        ImageView img1 = view.findViewById(R.id.switcher_img1);
        ImageView img2 = view.findViewById(R.id.switcher_img2);
        TextView title1 = view.findViewById(R.id.switcher_title1);
        TextView title2 = view.findViewById(R.id.switcher_title2);

        CommonData data1 = demoBeans.get(position * 2);
        img1.setImageResource(data1.getNewsType().equals("0") ? R.mipmap.ass_icon08 : R.mipmap.ass_icon09);
        title1.setText(data1.getTitle());

        if ((position + 1) * 2 > demoBeans.size()) second.setVisibility(View.INVISIBLE);
        else {
            second.setVisibility(View.VISIBLE);
            CommonData data2 = demoBeans.get(position * 2 + 1);
            img2.setImageResource(data2.getNewsType().equals("0") ? R.mipmap.ass_icon08 : R.mipmap.ass_icon09);
            title2.setText(data2.getTitle());
        }

        if (itemDataListener != null) {
            first.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemDataListener.onItemClick(position * 2);
                }
            });
            second.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemDataListener.onItemClick(position * 2 + 1);
                }
            });
        }

        showNext();
    }

    public abstract interface ItemDataListener {
        public void onItemClick(int position);
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return SwitcherTextView.class.getName();
    }

}
