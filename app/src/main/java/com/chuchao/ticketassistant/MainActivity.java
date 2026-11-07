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
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private EditText saleTime;
    private EditText ticketUrl;
    private TextView countdown;
    private TextView selectedInfo;
    private Spinner quantitySpinner;
    private Spinner tierSpinner;
    private WebView webView;

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean reminded = false;

    private LinearLayout homeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homeLayout = new LinearLayout(this);
        homeLayout.setOrientation(LinearLayout.VERTICAL);
        homeLayout.setPadding(30, 25, 30, 25);
        homeLayout.setBackgroundColor(Color.WHITE);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(homeLayout);

        TextView title = new TextView(this);
        title.setText("猫眼抢票助手 2.0");
        title.setTextSize(27);
        title.setTextColor(Color.BLACK);
        homeLayout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("购票辅助工具 | 小米 Android 版");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.GRAY);
        homeLayout.addView(subtitle);

        addSpace();

        addLabel("一、设置购买数量");

        quantitySpinner = new Spinner(this);
        String[] quantities = {
            "1张", "2张", "3张", "4张"
        };

        ArrayAdapter<String> quantityAdapter =
            new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, quantities);

        quantityAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item);

        quantitySpinner.setAdapter(quantityAdapter);
        quantitySpinner.setSelection(2);
        homeLayout.addView(quantitySpinner);

        addSpace();

        addLabel("二、设置票档");

        tierSpinner = new Spinner(this);
        String[] tiers = {
            "不限票档", "VIP", "普通票"
        };

        ArrayAdapter<String> tierAdapter =
            new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, tiers);

        tierAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item);

        tierSpinner.setAdapter(tierAdapter);
        homeLayout.addView(tierSpinner);

        selectedInfo = new TextView(this);
        selectedInfo.setTextSize(15);
        selectedInfo.setTextColor(Color.BLUE);
        homeLayout.addView(selectedInfo);

        Button updateSettings = new Button(this);
        updateSettings.setText("确认购票设置");
        homeLayout.addView(updateSettings);

        updateSettings.setOnClickListener(v -> {
            String quantity = quantitySpinner.getSelectedItem().toString();
            String tier = tierSpinner.getSelectedItem().toString();

            selectedInfo.setText(
                "当前设置：" + quantity + "，" + tier
            );

            Toast.makeText(this,
                "设置已确认，请在猫眼页面核对购票选项",
                Toast.LENGTH_SHORT).show();
        });

        addSpace();

        addLabel("三、设置开售时间");

        saleTime = new EditText(this);
        saleTime.setSingleLine(true);
        saleTime.setHint("例如：2026-11-08 12:00:00");
        homeLayout.addView(saleTime);

        Button save = new Button(this);
        save.setText("保存开售时间");
        homeLayout.addView(save);

        countdown = new TextView(this);
        countdown.setTextSize(21);
        countdown.setTextColor(Color.RED);
        countdown.setPadding(0, 15, 0, 15);
        countdown.setText("请先设置开售时间");
        homeLayout.addView(countdown);

        save.setOnClickListener(v -> {
            try {
                parseSaleTime();
                reminded = false;
                updateCountdown();
                Toast.makeText(this,
                    "开售时间设置成功",
                    Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                new AlertDialog.Builder(this)
                    .setMessage("时间格式错误，请使用：yyyy-MM-dd HH:mm:ss")
                    .setPositiveButton("确定", null)
                    .show();
            }
        });

        addSpace();

        addLabel("四、猫眼购票链接");

        ticketUrl = new EditText(this);
ticketUrl.setSingleLine(false);
ticketUrl.setMinLines(2);
ticketUrl.setHint("粘贴猫眼HTTPS链接或微信#小程序://分享内容");
homeLayout.addView(ticketUrl);

Button recognize = new Button(this);
recognize.setText("识别微信小程序链接");
homeLayout.addView(recognize);

recognize.setOnClickListener(v -> {
    String value = ticketUrl.getText().toString().trim();

    if (value.startsWith("#小程序://")) {
        new AlertDialog.Builder(this)
            .setTitle("已识别微信小程序分享格式")
            .setMessage(
                "已识别猫眼小程序分享内容：\n\n" +
                value +
                "\n\n请通过微信打开该分享内容，进入猫眼对应购票页面后，再获取官方HTTPS购票链接。"
            )
            .setPositiveButton("知道了", null)
            .show();
    } else if (value.startsWith("https://") &&
               value.contains("maoyan.com")) {
        Toast.makeText(this,
            "识别到猫眼HTTPS链接",
            Toast.LENGTH_SHORT).show();
    } else {
        Toast.makeText(this,
            "暂不支持该链接格式，请检查复制内容",
            Toast.LENGTH_SHORT).show();
    }
});

        Button start = new Button(this);
        start.setText("开始抢票 / 进入猫眼");
        start.setTextSize(18);
        homeLayout.addView(start);

        start.setOnClickListener(v -> {
            String url = ticketUrl.getText().toString().trim();

            if (url.isEmpty()) {
                url = "https://m.maoyan.com/";
            }

            if (url.startsWith("#小程序://")) {
    new AlertDialog.Builder(this)
        .setTitle("微信小程序链接")
        .setMessage(
            "已识别猫眼小程序分享内容。\n\n" +
            "请复制这段内容，通过微信打开小程序，" +
            "进入对应购票页面后获取官方HTTPS链接。"
        )
        .setPositiveButton("知道了", null)
        .show();
    return;
}

if (!url.startsWith("https://")) {
    Toast.makeText(this,
        "请输入HTTPS链接或识别微信小程序链接",
        Toast.LENGTH_SHORT).show();
    return;
}

            if (!url.contains("maoyan.com")) {
                Toast.makeText(this,
                    "请使用猫眼官方购票链接",
                    Toast.LENGTH_SHORT).show();
                return;
            }
final String targetUrl = url;
            new AlertDialog.Builder(this)
                .setTitle("准备进入猫眼")
                .setMessage(
                    "购买数量：" +
                    quantitySpinner.getSelectedItem().toString() +
                    "\n票档：" +
                    tierSpinner.getSelectedItem().toString() +
                    "\n\n请在猫眼页面核对场次、数量和票档。"
                )
                .setNegativeButton("取消", null)
                .setPositiveButton("进入购票页面",
                    (dialog, which) -> openMaoyan(targetUrl))
                .show();
        });

        Button refresh = new Button(this);
        refresh.setText("刷新购票页面");
        homeLayout.addView(refresh);

        refresh.setOnClickListener(v -> {
            if (webView != null) {
                webView.reload();
            } else {
                Toast.makeText(this,
                    "请先进入猫眼页面",
                    Toast.LENGTH_SHORT).show();
            }
        });

        Button back = new Button(this);
        back.setText("返回助手首页");
        homeLayout.addView(back);

        back.setOnClickListener(v -> {
            setContentView(scrollView);
        });

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        setContentView(scrollView);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateCountdown();
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void openMaoyan(String url) {
        webView.loadUrl(url);
        setContentView(webView);
    }

    private void addLabel(String value) {
        TextView label = new TextView(this);
        label.setText(value);
        label.setTextSize(18);
        label.setTextColor(Color.BLACK);
        label.setPadding(0, 8, 0, 8);
        homeLayout.addView(label);
    }

    private void addSpace() {
        TextView space = new TextView(this);
        space.setHeight(15);
        homeLayout.addView(space);
    }

    private Date parseSaleTime() throws ParseException {
        SimpleDateFormat format =
            new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.CHINA);

        format.setLenient(false);

        return format.parse(
            saleTime.getText().toString().trim());
    }

    private void updateCountdown() {
        if (saleTime == null || countdown == null) {
            return;
        }

        String value = saleTime.getText().toString().trim();

        if (value.isEmpty()) {
            countdown.setText("请先设置开售时间");
            return;
        }

        try {
            Date date = parseSaleTime();

            long diff =
                date.getTime() - System.currentTimeMillis();

            if (diff <= 0) {
                countdown.setText("开售时间已到！");

                if (!reminded) {
                    reminded = true;

                    new AlertDialog.Builder(this)
                        .setTitle("猫眼开售提醒")
                        .setMessage(
                            "预计开售时间已到，请检查猫眼购票页面。"
                        )
                        .setPositiveButton("确定", null)
                        .show();
                }

            } else {
                long seconds = diff / 1000;
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long secs = seconds % 60;

                countdown.setText(
                    "距离开售：" + hours + "小时 " +
                    minutes + "分 " + secs + "秒"
                );
            }

        } catch (Exception e) {
            countdown.setText("时间格式错误");
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);

        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }

        super.onDestroy();
    }
}
