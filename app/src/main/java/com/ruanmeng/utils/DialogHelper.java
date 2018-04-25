package com.ruanmeng.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.flyco.dialog.widget.base.BottomBaseDialog;
import com.maning.mndialoglibrary.MProgressDialog;
import com.ruanmeng.base.BaseDialog;
import com.ruanmeng.view.DropPopWindow;
import com.ruanmeng.village_arrival.R;
import com.weigan.loopview.LoopView;

import net.idik.lib.slimadapter.SlimAdapter;
import net.idik.lib.slimadapter.SlimInjector;
import net.idik.lib.slimadapter.viewinjector.IViewInjector;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DialogHelper {

    @SuppressLint("StaticFieldLeak")
    private static MProgressDialog mMProgressDialog;

    private DialogHelper() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static void showDialog(Context context) {
        dismissDialog();

        mMProgressDialog = new MProgressDialog.Builder(context)
                .setCancelable(true)
                .isCanceledOnTouchOutside(false)
                .setDimAmount(0.5f)
                .build();

        mMProgressDialog.show();
    }

    public static void dismissDialog() {
        if (mMProgressDialog != null && mMProgressDialog.isShowing())
            mMProgressDialog.dismiss();
    }

    public static void showHintDialog(
            final Context context,
            final String title,
            final CharSequence content,
            final String left,
            final String right,
            final boolean cancel,
            final ClickCallBack callBack) {
        BaseDialog dialog = new BaseDialog(context) {
            @Override
            public View onCreateView() {
                widthScale(0.85f);
                View view = View.inflate(context, R.layout.dialog_cancel_hint, null);

                TextView tv_title = view.findViewById(R.id.dialog_title);
                TextView tv_content = view.findViewById(R.id.dialog_content);
                TextView tv_left = view.findViewById(R.id.dialog_left);
                TextView tv_right = view.findViewById(R.id.dialog_right);

                tv_title.setText(title);
                tv_content.setText(content);
                tv_left.setText(left);
                tv_right.setText(right);

                tv_left.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        callBack.onClick("left");
                    }
                });
                tv_right.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        callBack.onClick("right");
                    }
                });

                return view;
            }
        };

        dialog.setCanceledOnTouchOutside(cancel);
        dialog.show();
    }

    public static void showDropWindow(
            final Context context,
            @LayoutRes int resoureId,
            View arrow,
            View anchor,
            final List<String> items,
            final ItemCallBack callBack) {
        DropPopWindow dropPopWindow = new DropPopWindow(context, resoureId, arrow) {
            @Override
            public void afterInitView(@NotNull View view) {
                RecyclerView recyclerView = view.findViewById(R.id.pop_container);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                SlimAdapter adapter = SlimAdapter.create()
                        .register(R.layout.item_area_list, new SlimInjector<String>() {
                            @Override
                            public void onInject(@NonNull final String data, @NonNull IViewInjector injector) {
                                injector.text(R.id.item_area, data)
                                        .visibility(
                                                R.id.item_area_divider1,
                                                items.indexOf(data) == items.size() - 1 ? View.GONE : View.VISIBLE)
                                        .visibility(
                                                R.id.item_area_divider2,
                                                items.indexOf(data) != items.size() - 1 ? View.GONE : View.VISIBLE)
                                        .clicked(R.id.item_area, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dismiss();
                                                callBack.doWork(items.indexOf(data), data);
                                            }
                                        });
                            }
                        })
                        .attachTo(recyclerView);
                adapter.updateData(items);
            }
        };
        dropPopWindow.showAsDropDown(anchor);
    }

    public static void showItemDialog(
            final Context context,
            final String title,
            final List<String> items,
            final ItemCallBack callBack) {

        BottomBaseDialog dialog = new BottomBaseDialog(context) {

            private LoopView loopView;

            @Override
            public View onCreateView() {
                View view = View.inflate(context, R.layout.dialog_select_one, null);

                TextView tv_title = view.findViewById(R.id.tv_dialog_select_title);
                TextView tv_cancel = view.findViewById(R.id.tv_dialog_select_cancle);
                TextView tv_ok = view.findViewById(R.id.tv_dialog_select_ok);
                loopView = view.findViewById(R.id.lv_dialog_select_loop);

                tv_title.setText(title);
                loopView.setTextSize(15f);
                loopView.setDividerColor(context.getResources().getColor(R.color.divider));
                loopView.setNotLoop();

                tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });

                tv_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        callBack.doWork(loopView.getSelectedItem(), items.get(loopView.getSelectedItem()));
                    }
                });

                return view;
            }

            @Override
            public void setUiBeforShow() {
                loopView.setItems(items);
            }

        };

        dialog.show();
    }

    public static void showDateDialog(final Context context, final DateItemCallBack callback) {

        BottomBaseDialog dialog = new BottomBaseDialog(context) {

            private LoopView loop_day, loop_hour, loop_minute;

            @Override
            public View onCreateView() {
                View view = View.inflate(context, R.layout.dialog_send_time, null);

                TextView tv_title = view.findViewById(R.id.tv_dialog_select_title);
                TextView tv_cancel = view.findViewById(R.id.tv_dialog_select_cancle);
                TextView tv_ok = view.findViewById(R.id.tv_dialog_select_ok);
                loop_day = view.findViewById(R.id.lv_dialog_select_day);
                loop_hour = view.findViewById(R.id.lv_dialog_select_hour);
                loop_minute = view.findViewById(R.id.lv_dialog_select_minute);

                loop_day.setTextSize(15f);
                loop_hour.setTextSize(15f);
                loop_minute.setTextSize(15f);
                loop_day.setNotLoop();
                loop_hour.setNotLoop();
                loop_minute.setNotLoop();

                tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });

                tv_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();

                        String day = loop_day.getSelectedItem() == 0 ? "今天" : "明天";
                        int hour = loop_hour.getSelectedItem();
                        int minute = loop_minute.getSelectedItem();

                        String date_now = TimeHelper.getInstance().getStringDateShort();
                        if (loop_day.getSelectedItem() == 1)
                            date_now = TimeHelper.getInstance().getNextDay(date_now, 1);

                        String hour_hint = " " + (hour < 10 ? ("0" + hour) : hour);
                        String minute_hint = ":" + (minute < 10 ? ("0" + minute) : minute);

                        callback.doWork(
                                day + hour_hint + minute_hint,
                                date_now + hour_hint + minute_hint);
                    }
                });

                return view;
            }

            @Override
            public void setUiBeforShow() {
                String[] day_arr = {"今天", "明天"};
                final List<String> list_day = Arrays.asList(day_arr);
                loop_day.setItems(list_day);
                loop_hour.setItems(dateToList(0, 23, "%d时"));
                loop_minute.setItems(dateToList(0, 59, "%d分"));
            }

        };

        dialog.show();
    }

    private static List<String> dateToList(int minValue, int maxValue, String format) {
        List<String> items = new ArrayList<>();

        for (int i = 0; i < maxValue - minValue + 1; i++) {
            int value = minValue + i;
            items.add(format != null ? String.format(format, value) : Integer.toString(value));
        }

        return items;
    }

    public interface ClickCallBack {
        void onClick(String hint);
    }

    public interface ItemCallBack {
        void doWork(int position, String name);
    }

    public interface DateItemCallBack {
        void doWork(String hint, String date);
    }
}
