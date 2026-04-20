package com.nandish.productivity

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.nandish.productivity.databinding.FragmentStitchBinding
import org.json.JSONObject

class StitchWebFragment : Fragment() {

    private var _binding: FragmentStitchBinding? = null
    private val binding get() = _binding!!

    private val gson = Gson()

    private val assetHtml: String
        get() = requireArguments().getString(ARG_ASSET_HTML)
            ?: error("Missing assetHtml argument")

    private var backCallback: OnBackPressedCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStitchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val wv = binding.webView
        wv.setBackgroundColor(0xFF000000.toInt())
        val settings = wv.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.builtInZoomControls = false
        settings.displayZoomControls = false
        @Suppress("DEPRECATION")
        settings.allowFileAccessFromFileURLs = true
        @Suppress("DEPRECATION")
        settings.allowUniversalAccessFromFileURLs = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        wv.webChromeClient = WebChromeClient()
        wv.addJavascriptInterface(
            ProDashBridge {
                requireActivity().runOnUiThread { injectHydration(wv) }
            },
            "ProDash"
        )

        backCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (wv.canGoBack()) wv.goBack()
            }
        }.also { cb ->
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, cb)
        }

        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                backCallback?.isEnabled = wv.canGoBack()
                wv.postDelayed({ injectHydration(wv) }, 450)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val u = request.url.toString()
                if (u.startsWith("file:///android_asset/")) return false
                view.loadUrl(u)
                return true
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("file:///android_asset/")) return false
                view.loadUrl(url)
                return true
            }
        }

        wv.loadUrl("file:///android_asset/$assetHtml")
    }

    private fun injectHydration(wv: WebView) {
        val hydrateSrc = requireContext().assets.open("vendor/prodash-hydrate.js").bufferedReader().use { it.readText() }
        wv.evaluateJavascript("eval(" + JSONObject.quote(hydrateSrc) + ");", null)
        val json = PageSnapshots.jsonForAsset(assetHtml, gson)
        wv.evaluateJavascript(
            "(function(){ if(window.ProDashHydrate) ProDashHydrate(" + json + "); })();",
            null
        )
    }

    override fun onDestroyView() {
        binding.webView.apply {
            stopLoading()
            loadUrl("about:blank")
            destroy()
        }
        backCallback?.remove()
        backCallback = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val ARG_ASSET_HTML = "assetHtml"
    }
}
