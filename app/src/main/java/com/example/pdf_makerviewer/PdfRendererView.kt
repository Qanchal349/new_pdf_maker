package com.example.pdf_makerviewer

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class PdfRendererView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var pdfRendererCore: PdfRendererCore
    private lateinit var pdfViewAdapter: PdfViewAdapter
    private var quality = PdfQuality.NORMAL
    private var engine = PdfEngine.INTERNAL
    private var showDivider = true
    private var divider: Drawable? = null
    private var runnable = Runnable {}
    private var pdfRendererCoreInitialised = false

    var statusListener: StatusCallBack? = null
    val totalPageCount: Int
        get() {
            return pdfRendererCore.getPageCount()
        }

    interface StatusCallBack {
        fun onDownloadStart() {}
        fun onDownloadProgress(progress: Int, downloadedBytes: Long, totalBytes: Long?) {}
        fun onDownloadSuccess() {}
        fun onError(error: Throwable) {}
        fun onPageChanged(currentPage: Int, totalPage: Int) {}
    }


    fun initWithPath(path: String, pdfQuality: PdfQuality = this.quality) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            throw UnsupportedOperationException("should be over API 21")
        initWithFile(File(path), pdfQuality)
    }

    fun initWithFile(file: File, pdfQuality: PdfQuality = this.quality) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            throw UnsupportedOperationException("should be over API 21")
        init(file, pdfQuality)
    }

    private fun init(file: File, pdfQuality: PdfQuality) {

        println("----------------------------------- calling")

        pdfRendererCore = PdfRendererCore(context, file, pdfQuality)
        pdfRendererCoreInitialised = true
        pdfViewAdapter = PdfViewAdapter(pdfRendererCore)
        val v = LayoutInflater.from(context).inflate(R.layout.pdf_rendererview, this, false)
        addView(v)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.apply {
            adapter = pdfViewAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            if (showDivider) {
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    divider?.let { setDrawable(it) }
                }.let { addItemDecoration(it) }
            }
            addOnScrollListener(scrollListener)
        }

        runnable = Runnable {
            findViewById<TextView>(R.id.pageNo).visibility = View.GONE
        }

    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            (recyclerView.layoutManager as LinearLayoutManager).run {
                var foundPosition = findFirstCompletelyVisibleItemPosition()

                findViewById<TextView>(R.id.pageNo).run {
                    if (foundPosition != RecyclerView.NO_POSITION)
                        text = "${(foundPosition + 1)} of $totalPageCount"
                    findViewById<TextView>(R.id.pageNo).visibility = View.VISIBLE
                }

                if (foundPosition == 0)
                    findViewById<TextView>(R.id.pageNo).postDelayed({
                        findViewById<TextView>(R.id.pageNo).visibility = GONE
                    }, 3000)

                if (foundPosition != RecyclerView.NO_POSITION) {
                    statusListener?.onPageChanged(foundPosition, totalPageCount)
                    return@run
                }
                foundPosition = findFirstVisibleItemPosition()
                if (foundPosition != RecyclerView.NO_POSITION) {
                    statusListener?.onPageChanged(foundPosition, totalPageCount)
                    return@run
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                findViewById<TextView>(R.id.pageNo).postDelayed(runnable, 100)
            } else {
                findViewById<TextView>(R.id.pageNo).removeCallbacks(runnable)
            }
        }

    }

//    @SuppressLint("SetJavaScriptEnabled")
//    private fun initUnderKitkat(url: String) {
//        val v = LayoutInflater.from(context).inflate(R.layout.pdf_rendererview, this, false)
//        addView(v)
//        recyclerView = findViewById(R.id.recyclerView)
//        recyclerView.visibility = View.GONE
//        findViewById<WebView>(R.id.webView).visibility = View.VISIBLE
//        findViewById<WebView>(R.id.webView).settings.javaScriptEnabled = true
//        findViewById<WebView>(R.id.webView).webViewClient = PdfWebViewClient(statusListener)
//        findViewById<WebView>(R.id.webView).loadUrl(
//            "https://drive.google.com/viewer/viewer?hl=en&embedded=true&url=${URLEncoder.encode(
//                url,
//                "UTF-8"
//            )}"
//        )
//    }



    internal class PdfWebViewClient(private val statusListener: StatusCallBack?) : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            statusListener?.onDownloadSuccess()
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            statusListener?.onError(Throwable("Web resource error"))
        }

        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?
        ) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            statusListener?.onError(Throwable("Web resource error"))
        }
    }

    init {
        getAttrs(attrs, defStyleAttr)
    }

    private fun getAttrs(attrs: AttributeSet?, defStyle: Int) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.PdfRendererView, defStyle, 0)
        setTypeArray(typedArray)
    }

    private fun setTypeArray(typedArray: TypedArray) {
        val ratio =
            typedArray.getInt(R.styleable.PdfRendererView_pdfView_quality, PdfQuality.NORMAL.ratio)
        quality = PdfQuality.values().first { it.ratio == ratio }
        val engineValue =
            typedArray.getInt(R.styleable.PdfRendererView_pdfView_engine, PdfEngine.INTERNAL.value)
        engine = PdfEngine.values().first { it.value == engineValue }
        showDivider = typedArray.getBoolean(R.styleable.PdfRendererView_pdfView_showDivider, true)
        divider = typedArray.getDrawable(R.styleable.PdfRendererView_pdfView_divider)

        typedArray.recycle()
    }

    fun closePdfRender() {
        if (pdfRendererCoreInitialised)
            pdfRendererCore.closePdfRender()
    }


}