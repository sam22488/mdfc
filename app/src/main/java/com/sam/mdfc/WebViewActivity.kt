package com.sam.mdfc

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

class WebViewActivity : AppCompatActivity() {
    
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var downloadId: Long = -1
    private var pendingDownloadUrl: String? = null
    private var pendingDownloadUserAgent: String? = null
    private var pendingDownloadContentDisposition: String? = null
    private var pendingDownloadMimetype: String? = null
    
    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
        private const val PERMISSION_REQUEST_CODE = 100
    }
    
    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                openDownloadedFile(id)
            }
        }
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        
        val url = intent.getStringExtra(EXTRA_URL) ?: ""
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "MDFC"
        
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        
        // Register download complete receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                downloadCompleteReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(
                downloadCompleteReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
        
        setupWebView()
        
        if (url.isNotEmpty()) {
            webView.loadUrl(url)
        }
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            
            // Set desktop user agent for desktop view
            userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            
            // Enable mixed content
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            
            // Cache settings
            cacheMode = WebSettings.LOAD_DEFAULT
            setAppCacheEnabled(true)
        }
        
        // Set download listener
        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            handleDownload(url, userAgent, contentDisposition, mimetype)
        }
        
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return false
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }
        }
        
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.VISIBLE
                }
            }
        }
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    override fun onDestroy() {
        try {
            unregisterReceiver(downloadCompleteReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        webView.destroy()
        super.onDestroy()
    }
    
    private fun handleDownload(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimetype: String
    ) {
        // Check if it's a PDF or Excel file
        val isPdf = mimetype.contains("pdf", ignoreCase = true) || 
                    url.contains(".pdf", ignoreCase = true)
        val isExcel = mimetype.contains("excel", ignoreCase = true) ||
                      mimetype.contains("spreadsheet", ignoreCase = true) ||
                      url.contains(".xls", ignoreCase = true) ||
                      url.contains(".xlsx", ignoreCase = true)
        
        if (!isPdf && !isExcel) {
            // Not a PDF or Excel file, proceed with normal download
            startDownload(url, userAgent, contentDisposition, mimetype)
            return
        }
        
        // Check permissions for Android 6.0 to Android 9
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Store download info for later
                pendingDownloadUrl = url
                pendingDownloadUserAgent = userAgent
                pendingDownloadContentDisposition = contentDisposition
                pendingDownloadMimetype = mimetype
                
                // Request permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
                return
            }
        }
        
        // Permission granted or not needed, start download
        startDownload(url, userAgent, contentDisposition, mimetype)
    }
    
    private fun startDownload(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimetype: String
    ) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
            
            // Get filename from content disposition or URL
            val filename = URLUtil.guessFileName(url, contentDisposition, mimetype)
            
            // Set request properties
            request.apply {
                setMimeType(mimetype)
                addRequestHeader("User-Agent", userAgent)
                addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url))
                setDescription("Downloading file...")
                setTitle(filename)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            }
            
            // Enqueue download
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadId = downloadManager.enqueue(request)
            
            Toast.makeText(this, "Downloading $filename", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openDownloadedFile(downloadId: Long) {
        try {
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            
            if (cursor.moveToFirst()) {
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = cursor.getInt(statusIndex)
                
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                    val localUri = cursor.getString(uriIndex)
                    val mimeTypeIndex = cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)
                    val mimeType = cursor.getString(mimeTypeIndex)
                    
                    if (localUri != null) {
                        openFile(Uri.parse(localUri), mimeType)
                    }
                }
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to open file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openFile(uri: Uri, mimeType: String?) {
        try {
            // Convert file:// URI to content:// URI using FileProvider
            val file = File(uri.path ?: return)
            val contentUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                file
            )
            
            // Determine MIME type if not provided
            val finalMimeType = mimeType ?: when {
                file.name.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                file.name.endsWith(".xls", ignoreCase = true) -> "application/vnd.ms-excel"
                file.name.endsWith(".xlsx", ignoreCase = true) -> 
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                else -> "*/*"
            }
            
            // Create intent to open file
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, finalMimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            // Check if there's an app to handle this file type
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(intent, "Open with"))
            } else {
                Toast.makeText(
                    this,
                    "No app found to open this file type",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to open file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start pending download
                pendingDownloadUrl?.let { url ->
                    startDownload(
                        url,
                        pendingDownloadUserAgent ?: "",
                        pendingDownloadContentDisposition ?: "",
                        pendingDownloadMimetype ?: ""
                    )
                }
            } else {
                Toast.makeText(
                    this,
                    "Storage permission is required to download files",
                    Toast.LENGTH_LONG
                ).show()
            }
            
            // Clear pending download info
            pendingDownloadUrl = null
            pendingDownloadUserAgent = null
            pendingDownloadContentDisposition = null
            pendingDownloadMimetype = null
        }
    }
}
