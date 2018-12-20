package com.xujiaji.todo.module.main;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xujiaji.todo.R;

public class DailyDialog extends Dialog {
    private final Context context;

    private final String picUrl;
    private final String content;
    private final String from;
    private final int    size;
    private final String color;

    private DailyDialog(Builder builder) {
        super(builder.context, R.style.bubble_dialog);
        context = builder.context;
        picUrl  = builder.picUrl;
        content = builder.content;
        from    = builder.from;
        size    = builder.size;
        color   = builder.color;

        Window window = getWindow();
        if (window != null)
            window.setWindowAnimations(R.style.dialogDailyWindowAnim);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_daily);
        ImageView imgBg    = findViewById(R.id.imgBg);
        TextView tvContent = findViewById(R.id.tvContent);
        TextView tvFrom    = findViewById(R.id.tvFrom);

        Glide.with(getContext()).load(picUrl).into(imgBg);
        tvContent.setText(content);
        tvContent.setTextColor(Color.parseColor(color));
        tvContent.setTextSize(size);
        tvFrom.setText(from);
    }

    public static class Builder {
        private final Context context;

        private String picUrl  = "";
        private String content = "";
        private String from    = "";
        private int    size    = 14;
        private String color   = "#FF000000";

        public Builder(Context context){
            this.context = context;
        }

        public Builder size(int val) {
            if (val != 0) this.size = val;
            return this;
        }

        public Builder color(String val) {
            if (!TextUtils.isEmpty(val)) this.color = val;
            return this;
        }

        public Builder content(String val) {
            if (!TextUtils.isEmpty(val)) this.content = val;
            return this;
        }

        public Builder from(String val) {
            if (!TextUtils.isEmpty(val)) this.from = val;
            return this;
        }

        public Builder picUrl(String val) {
            if (!TextUtils.isEmpty(val)) this.picUrl = val;
            return this;
        }

        public DailyDialog build() {
            return new DailyDialog(this);
        }
    }
}
