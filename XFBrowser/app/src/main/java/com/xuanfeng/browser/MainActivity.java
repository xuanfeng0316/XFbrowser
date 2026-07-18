/*
 * Copyright (c) 2026 xuanfeng0316
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.xuanfeng.browser;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.*;
import android.widget.*;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import java.util.Map;
import java.util.HashMap;
import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import java.io.File;
import java.io.FileOutputStream;
import android.os.Environment;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.view.ViewGroup;
import android.content.SharedPreferences;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.Environment;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.content.Context;
import android.widget.EditText;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.widget.CheckBox;
import java.net.HttpURLConnection;
import java.net.URL;
import android.webkit.CookieManager;

public class MainActivity extends Activity {

    private WebView webView;
    private EditText etUrl;
    // 常1
    private Button btnBack, btnForward, btnRefresh, btnDesktop, btnSettings, btnMenu;
    
    private ProgressBar progressBar;
    private boolean isAdBlockEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupWebView();
        setupListeners();
        
        webView.loadUrl("https://www.baidu.com");
    }

    private void initViews() {
   	    webView = findViewById(R.id.webview);
    	etUrl = findViewById(R.id.et_url);
    	btnBack = findViewById(R.id.btn_back);
   	    btnForward = findViewById(R.id.btn_forward);
   	    btnRefresh = findViewById(R.id.btn_refresh);
   	    btnDesktop = findViewById(R.id.btn_desktop); 
   	    btnSettings = findViewById(R.id.btn_settings); 
	    progressBar = findViewById(R.id.progress_bar);
	    btnMenu = findViewById(R.id.btn_menu);
	}

    private void setupWebView() {
    WebSettings webSettings = webView.getSettings();
    webSettings.setJavaScriptEnabled(true);
    webSettings.setDomStorageEnabled(true);
    webSettings.setBuiltInZoomControls(true);
    webSettings.setDisplayZoomControls(false);
    
    // 全部浏览器协议
    String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0";
    webSettings.setUserAgentString(ua);
    
    webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
    webSettings.setLoadWithOverviewMode(true);
    webSettings.setUseWideViewPort(true);
    webSettings.setDefaultTextEncodingName("utf-8");
    webSettings.setAllowFileAccess(true);
    webSettings.setAllowContentAccess(true);
    webSettings.setDatabaseEnabled(true);
    webSettings.setGeolocationEnabled(true);
    webSettings.setAppCacheEnabled(true);
    webSettings.setLoadsImagesAutomatically(true);
    webSettings.setBlockNetworkImage(false);
    webSettings.setBlockNetworkLoads(false);
    
    // 添加完整请求头
    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
    headers.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
    headers.put("Accept-Encoding", "gzip, deflate, br");
    headers.put("Connection", "keep-alive");
    headers.put("Upgrade-Insecure-Requests", "1");
    headers.put("Sec-Fetch-Dest", "document");
    headers.put("Sec-Fetch-Mode", "navigate");
    headers.put("Sec-Fetch-Site", "none");
    headers.put("Sec-Fetch-User", "?1");
    headers.put("Cache-Control", "max-age=0");
    
    webView.setWebViewClient(new WebViewClient() {
    
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url, headers);  // 携带请求头
            return true;
        }
        
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            // 拦截并添加请求头
            return super.shouldInterceptRequest(view, request);
        }
        
        @Override
		public void onPageFinished(WebView view, String url) {
   		 super.onPageFinished(view, url);
   		 etUrl.setText(url);
  		  btnBack.setEnabled(webView.canGoBack());
   		 btnForward.setEnabled(webView.canGoForward());
   		 progressBar.setVisibility(View.GONE);
    
		}
		
    });
    //占位2
    webView.setWebChromeClient(new WebChromeClient() {
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        progressBar.setProgress(newProgress);
        if (newProgress < 100) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
});

webView.addJavascriptInterface(new Object() {
    @JavascriptInterface
    public void showSource(String html) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("源码")
                    .setMessage(html)
                    .setPositiveButton("确定", null)
                    .setNegativeButton("复制", (dialog, which) -> {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("source", html);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(MainActivity.this, "已复制", Toast.LENGTH_SHORT).show();
                    })
                    .show();
            }
        });
    }
    
    @JavascriptInterface
    public void downloadSource(String domain, String html) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) 
                        + "/xfbrowser/网页源码/" + domain + "/";
                    File dir = new File(dirPath);
                    if (!dir.exists()) dir.mkdirs();
                    
                    String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".html";
                    File file = new File(dir, fileName);
                    
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(html.getBytes("UTF-8"));
                    fos.close();
                    
                    Toast.makeText(MainActivity.this, "源码已保存到: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}, "XF");
// 下载文件
webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
    String fileName = getFileName(url, contentDisposition);
    downloadFile(url, fileName);
});
}
//1111111
private String getFileName(String url, String contentDisposition) {
    if (contentDisposition != null) {
        String[] parts = contentDisposition.split("filename=");
        if (parts.length > 1) {
            String name = parts[1].replace("\"", "").split(";")[0];
            if (!name.isEmpty()) return name;
        }
    }
    try {
        String name = url.substring(url.lastIndexOf("/") + 1);
        if (name.contains("?")) name = name.substring(0, name.indexOf("?"));
        return java.net.URLDecoder.decode(name, "UTF-8");
    } catch (Exception e) {
        return "download_" + System.currentTimeMillis();
    }
}

/*private void downloadFile(String url, String fileName) {
    String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) 
        + "/xfbrowser/下载/";
    File dir = new File(dirPath);
    if (!dir.exists()) dir.mkdirs();
    
    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
    request.setDestinationUri(Uri.fromFile(new File(dir, fileName)));
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
    
    DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
    manager.enqueue(request);
    
    Toast.makeText(MainActivity.this, "开始下载: " + fileName, Toast.LENGTH_SHORT).show();
}*/
private void downloadFile(String url, String fileName) {
    // 在子线程开始前获取 WebView 相关数据
    String userAgent = webView.getSettings().getUserAgentString();
    
    // 先发一个 HEAD 请求确认是不是真的文件
    new Thread(() -> {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("HEAD");
            conn.setInstanceFollowRedirects(false);
            
            // 添加完整请求头，模拟真实浏览器
            conn.setRequestProperty("User-Agent", userAgent);  // 用提前获取的
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
            conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
            conn.setRequestProperty("Sec-Fetch-Dest", "document");
            conn.setRequestProperty("Sec-Fetch-Mode", "navigate");
            conn.setRequestProperty("Sec-Fetch-Site", "none");
            conn.setRequestProperty("Sec-Fetch-User", "?1");
            conn.setRequestProperty("Cache-Control", "max-age=0");
            
            // 如果有 Cookie，也带上
            android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
            String cookie = cookieManager.getCookie(url);
            if (cookie != null) {
                conn.setRequestProperty("Cookie", cookie);
            }
            
            conn.connect();
            
            String contentType = conn.getContentType();
            int responseCode = conn.getResponseCode();
            
            // 如果是重定向，获取新地址
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || 
                responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                String newUrl = conn.getHeaderField("Location");
                if (newUrl != null) {
                    String finalNewUrl = newUrl;
                    runOnUiThread(() -> {
                        webView.loadUrl(finalNewUrl);
                    });
                    return;
                }
            }
            
            // 如果是 HTML，说明需要执行 JS
            if (contentType != null && contentType.contains("text/html")) {
                runOnUiThread(() -> {
                    webView.loadUrl(url);
                });
                return;
            }
            
            // 不是 HTML，直接下载
            runOnUiThread(() -> {
                try {
                    String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) 
                        + "/xfbrowser/下载/";
                    File dir = new File(dirPath);
                    if (!dir.exists()) dir.mkdirs();
                    
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setDestinationUri(Uri.fromFile(new File(dir, fileName)));
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setAllowedOverMetered(true);
                    request.setAllowedOverRoaming(true);
                    
                    // 添加所有请求头
                    request.addRequestHeader("User-Agent", userAgent);
                    request.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
                    request.addRequestHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
                    request.addRequestHeader("Accept-Encoding", "gzip, deflate, br");
                    request.addRequestHeader("Connection", "keep-alive");
                    request.addRequestHeader("Upgrade-Insecure-Requests", "1");
                    request.addRequestHeader("Sec-Fetch-Dest", "document");
                    request.addRequestHeader("Sec-Fetch-Mode", "navigate");
                    request.addRequestHeader("Sec-Fetch-Site", "none");
                    request.addRequestHeader("Sec-Fetch-User", "?1");
                    request.addRequestHeader("Cache-Control", "max-age=0");
                    
                    if (cookie != null) {
                        request.addRequestHeader("Cookie", cookie);
                    }
                    
                    DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    manager.enqueue(request);
                    
                    Toast.makeText(MainActivity.this, "开始下载: " + fileName, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "下载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "下载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }).start();
}
// 1111111end
// 获取文件扩展名
private String getFileExtension(String url) {
    try {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        if (fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            return fileName.substring(dotIndex);
        }
    } catch (Exception e) {}
    return "";
}
// 追踪下载
/*private void trackDownload(final String url) {
    // 创建新的WebView来追踪
    WebView tracker = new WebView(this);
    tracker.getSettings().setJavaScriptEnabled(true);
    tracker.getSettings().setUserAgentString(webView.getSettings().getUserAgentString());
    
    tracker.setWebViewClient(new WebViewClient() {
        private String finalUrl = url;
        private boolean isDownloadStarted = false;
        
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String newUrl) {
            finalUrl = newUrl;
            return false;
        }
        
        @Override
        public void onPageFinished(WebView view, String pageUrl) {
            if (!isDownloadStarted) {
                checkAndDownload(pageUrl, view);
            }
        }
        
        private void checkAndDownload(String url, WebView view) {
            String extension = getFileExtension(url).toLowerCase();
            
            String[] executableExts = {".exe", ".bat", ".cmd", ".sh", ".bin", ".run", ".app", ".deb", ".rpm"};
            boolean isExecutable = false;
            for (String ext : executableExts) {
                if (extension.endsWith(ext)) {
                    isExecutable = true;
                    break;
                }
            }
            
            if (!extension.contains("htm") || isExecutable) {
                downloadFile(url, getFileName(url, null));
                isDownloadStarted = true;
                view.destroy();
                return;
            }
            
            view.loadUrl("javascript:(function(){" +
                "var meta=document.querySelector('meta[http-equiv=\"refresh\"]');" +
                "if(meta){var content=meta.getAttribute('content');" +
                "var match=content.match(/url=([^\\s]+)/i);" +
                "if(match){window.location.href=match[1];}}" +
                "})()");
        }
    });
    
    tracker.loadUrl(url);
}  // ←  trackDownload 的结束*/
//} // setupWebView结


// 结

    private void setupListeners() {
    // 回车加载
    etUrl.setOnKeyListener(new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                loadUrl();
                return true;
            }
            return false;
        }
    });
    
    btnBack.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (webView.canGoBack()) webView.goBack();
        }
    });
    
    btnRefresh.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            webView.reload();
        }
    });
    
    btnDesktop.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        String current = webView.getSettings().getUserAgentString();
        
        // 检查电脑特征
        if (current.contains("Windows NT") || current.contains("Mac OS") || current.contains("X11")) {
            // PC-PE
            String mobileUA = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";
            webView.getSettings().setUserAgentString(mobileUA);
            btnDesktop.setText("手机版");
            Toast.makeText(MainActivity.this, "已切换为手机版", Toast.LENGTH_SHORT).show();
        } else {
            // PE-PC
            String desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0";
            webView.getSettings().setUserAgentString(desktopUA);
            btnDesktop.setText("电脑版");
            Toast.makeText(MainActivity.this, "正在切换为电脑版，请稍等", Toast.LENGTH_SHORT).show();
        }
        webView.reload();
    }
});
    
    btnSettings.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
    });
    btnMenu.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        showMenuDialog();
    }
    });
}
// 更多
    private void showMenuDialog() {
    SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
    
    final String[] items = {
        "自动刷新: " + (prefs.getBoolean("auto_refresh", true) ? "开" : "关"),
        "下载网页源码",
        "查看网页源码", 
        "文本模式: " + (prefs.getBoolean("text_mode", false) ? "开" : "关")
    };
    
    new AlertDialog.Builder(MainActivity.this)
        .setTitle("更多设置")
        .setItems(items, (dialog, which) -> {
            switch (which) {
                case 0: // 自动刷新
                    boolean auto = !prefs.getBoolean("auto_refresh", true);
                    prefs.edit().putBoolean("auto_refresh", auto).apply();
                    Toast.makeText(MainActivity.this, "自动刷新:" + (auto ? "开" : "关"), Toast.LENGTH_SHORT).show();
                    break;
                case 1: // 下载源码
                    String url = webView.getUrl();
                    String domain = "unknown";
                    try {
                        domain = new java.net.URL(url).getHost().replace("www.", "");
                    } catch (Exception e) {}
                    webView.loadUrl("javascript:XF.downloadSource('" + domain + "', document.documentElement.outerHTML);");
                    dialog.dismiss();
                    break;
                case 2: // 查看源码
                    webView.loadUrl("javascript:XF.showSource(document.documentElement.outerHTML);");
                    dialog.dismiss();
                    break;
                case 3: // 文本模式
                    boolean text = !prefs.getBoolean("text_mode", false);
                    prefs.edit().putBoolean("text_mode", text).apply();
                    setTextMode(text);
                    Toast.makeText(MainActivity.this, "文本模式:" + (text ? "开" : "关"), Toast.LENGTH_SHORT).show();
                    break;
            }
        })
        .setNegativeButton("关闭", null)
        .show();
}
// 结束1占位
    private void loadUrl() {
        String url = etUrl.getText().toString().trim();
        if (!url.isEmpty()) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            webView.loadUrl(url);
        }
    }
private void setTextMode(boolean enable) {
    WebSettings settings = webView.getSettings();
    if (enable) {
        settings.setBlockNetworkImage(true);
        settings.setLoadsImagesAutomatically(false);
        settings.setJavaScriptEnabled(false);
        String textUA = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";
        settings.setUserAgentString(textUA);
        Toast.makeText(MainActivity.this, "文本模式已开启", Toast.LENGTH_SHORT).show();
    } else {
        settings.setBlockNetworkImage(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptEnabled(true);
        String normalUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0";
        settings.setUserAgentString(normalUA);
        Toast.makeText(MainActivity.this, "文本模式已关闭", Toast.LENGTH_SHORT).show();
    }
    //webView.reload();
}
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}