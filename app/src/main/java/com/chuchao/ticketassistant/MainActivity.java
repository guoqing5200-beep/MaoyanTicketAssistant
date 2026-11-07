package com.maoyan.ticketassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private EditText saleTime;
    private EditText ticketUrl;
    private TextView countdown;
    private WebView webView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean reminded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);
        layout.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("猫眼抢票助手 1.0");
        title.setTextSize(26);
        title.setTextColor(Color.BLACK);
        layout.addView(title);

        TextView info = new TextView(this);
        info.setText("购买数量：3张\n票档：不限\n平台：猫眼\n验证码、订单及支付：本人操作");
        info.setTextSize(17);
        layout.addView(info);

        saleTime = new EditText(this);
        saleTime.setHint("输入开售时间，例如：2026-11-08 12:00:00");
        layout.addView(saleTime);

        Button save = new Button(this);
        save.setText("保存开售时间");
        layout.addView(save);

        countdown = new TextView(this);
        countdown.setTextSize(22);
        countdown.setTextColor(Color.RED);
        layout.addView(countdown);

        ticketUrl = new EditText(this);
        ticketUrl.setHint("粘贴猫眼比赛购票链接（可留空）");
        layout.addView(ticketUrl);

        Button open = new Button(this);
        open.setText("进入猫眼购票页面");
        layout.addView(open);

        Button refresh = new Button(this);
        refresh.setText("刷新购票页面");
        layout.addView(refresh);

        Button back = new Button(this);
        back.setText("返回助手首页");
        layout.addView(back);

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        save.setOnClickListener(v -> {
            reminded = false;
            updateCountdown();
        });

        open.setOnClickListener(v -> {
            String url = ticketUrl.getText().toString().trim();
            if (url.isEmpty()) {
                url = "https://m.maoyan.com/";
            }
            if (!url.startsWith("https://")) {
                new AlertDialog.Builder(this)
                    .setMessage("请使用安全的 HTTPS 猫眼购票链接")
                    .setPositiveButton("确定", null)
                    .show();
                return;
            }
            webView.loadUrl(url);
            setContentView(webView);
        });

        refresh.setOnClickListener(v -> {
            if (webView.getParent() == null) {
                setContentView(webView);
            }
            webView.reload();
        });

        back.setOnClickListener(v -> {
            if (webView.getParent() != null) {
                setContentView(layout);
            }
        });

        setContentView(layout);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateCountdown();
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void updateCountdown() {
        String value = saleTime.getText().toString().trim();
        if (value.isEmpty()) {
            countdown.setText("请先设置开售时间");
            return;
        }

        try {
            SimpleDateFormat format =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            Date date = format.parse(value);
            long diff = date.getTime() - System.currentTimeMillis();

            if (diff <= 0) {
                countdown.setText("开售时间已到！");
                if (!reminded) {
                    reminded = true;
                    new AlertDialog.Builder(this)
                        .setTitle("猫眼开售提醒")
                        .setMessage("预计开售时间已到，请检查购票页面。")
                        .setPositiveButton("确定", null)
                        .show();
                }
            } else {
                long seconds = diff / 1000;
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long secs = seconds % 60;
                countdown.setText("距离开售：" + hours + "小时 "
                    + minutes + "分 " + secs + "秒");
            }
        } catch (Exception e) {
            countdown.setText("时间格式错误，请重新输入");
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
