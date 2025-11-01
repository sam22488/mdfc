package com.sam.mdfc

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TERM_LOAN_URL = "https://tl.wbmdfc.org/district/login"
        private const val DLS_URL = "https://dls.wbmdfc.org/login-district"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupButtons()
    }
    
    private fun setupButtons() {
        val btnTermLoan = findViewById<MaterialButton>(R.id.btnTermLoan)
        val btnDls = findViewById<MaterialButton>(R.id.btnDls)
        
        btnTermLoan.setOnClickListener {
            openWebView(TERM_LOAN_URL, "Term Loan")
        }
        
        btnDls.setOnClickListener {
            openWebView(DLS_URL, "DLS")
        }
    }
    
    private fun openWebView(url: String, title: String) {
        val intent = Intent(this, WebViewActivity::class.java).apply {
            putExtra(WebViewActivity.EXTRA_URL, url)
            putExtra(WebViewActivity.EXTRA_TITLE, title)
        }
        startActivity(intent)
    }
}