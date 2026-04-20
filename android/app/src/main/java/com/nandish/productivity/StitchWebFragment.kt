package com.nandish.productivity

import android.content.pm.PackageManager
import android.net.Uri
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
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.nandish.productivity.databinding.FragmentStitchBinding
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class StitchWebFragment : Fragment(), ProDashBridge.Host {

    private var _binding: FragmentStitchBinding? = null
    private val binding get() = _binding!!

    private val gson = Gson()

    private val assetHtml: String
        get() = requireArguments().getString(ARG_ASSET_HTML)
            ?: error("Missing assetHtml argument")

    private var backCallback: OnBackPressedCallback? = null

    private val exportBackupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null || !isAdded) return@registerForActivityResult
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { os ->
                os.write(StateRepository.exportJson().toByteArray(StandardCharsets.UTF_8))
            } ?: throw IllegalStateException("Could not open file")
            Toast.makeText(requireContext(), "Backup saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private val importBackupLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null || !isAdded) return@registerForActivityResult
        val text = try {
            requireContext().contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        } catch (_: Exception) {
            null
        }
        if (text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Could not read file", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Import backup")
            .setMessage("Replace all data on this device with this backup? This cannot be undone.")
            .setPositiveButton("Replace") { _, _ ->
                val ok = StateRepository.importReplace(text)
                if (ok) {
                    refreshWeb()
                    Toast.makeText(requireContext(), "Backup restored", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Invalid backup file", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun refreshWeb() {
        _binding?.webView?.let { injectHydration(it) }
    }

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
        wv.removeJavascriptInterface("ProDash")
        wv.addJavascriptInterface(ProDashBridge(this), "ProDash")

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

    fun showQuickAddMenu() {
        if (!isAdded) return
        ItemEditors.showAddPicker(this) { refreshWeb() }
    }

    override fun onToggleTask(id: String) {
        StateRepository.update {
            val t = tasks.find { it.id == id } ?: return@update
            t.done = !t.done
        }
        refreshWeb()
    }

    override fun onOpenMenu() {
        if (!isAdded) return
        val wv = _binding?.webView ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Menu")
            .setItems(
                arrayOf(
                    "Add…",
                    "Export backup…",
                    "Import backup…",
                    "Settings",
                    "Refresh this screen",
                    "About"
                )
            ) { _, which ->
                when (which) {
                    0 -> ItemEditors.showAddPicker(this) { refreshWeb() }
                    1 -> exportBackupLauncher.launch("prodash-backup.json")
                    2 -> importBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    3 -> onNavigate("settings")
                    4 -> injectHydration(wv)
                    5 -> showAbout()
                }
            }
            .show()
    }

    override fun onOpenSearch() {
        if (!isAdded) return
        val wv = _binding?.webView ?: return
        val input = EditText(requireContext()).apply {
            hint = "Filter items on this screen…"
            setSingleLine()
        }
        val pad = (resources.displayMetrics.density * 24).toInt()
        input.setPadding(pad, pad / 2, pad, pad / 2)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Search")
            .setView(input)
            .setPositiveButton("Filter") { _, _ ->
                val q = input.text?.toString() ?: ""
                val quoted = JSONObject.quote(q)
                wv.evaluateJavascript(
                    "(function(){ if(window.ProDashFilter) ProDashFilter($quoted); })();",
                    null
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onNavigate(tab: String) {
        if (!isAdded) return
        val dest = when (tab.lowercase()) {
            "home" -> R.id.homeFragment
            "hub" -> R.id.hubFragment
            "goals" -> R.id.goalsFragment
            "vault" -> R.id.vaultFragment
            "logs" -> R.id.logsFragment
            "settings" -> R.id.settingsFragment
            else -> return
        }
        try {
            findNavController().navigate(dest)
        } catch (_: IllegalArgumentException) {
        } catch (_: IllegalStateException) {
        }
    }

    override fun onToast(message: String) {
        if (!isAdded) return
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onOpenEditor(kind: String, id: String) {
        if (!isAdded) return
        val idOrNull = id.ifBlank { null }
        when (kind.lowercase()) {
            "task" -> ItemEditors.showTaskEditor(this, idOrNull) { refreshWeb() }
            "note" -> ItemEditors.showNoteEditor(this, idOrNull) { refreshWeb() }
            "goal" -> ItemEditors.showGoalEditor(this, idOrNull) { refreshWeb() }
            "habit" -> ItemEditors.showHabitEditor(this, idOrNull) { refreshWeb() }
            "supplement" -> ItemEditors.showSupplementEditor(this, idOrNull) { refreshWeb() }
            "account" -> ItemEditors.showAccountEditor(this, idOrNull) { refreshWeb() }
            "transaction" -> ItemEditors.showTransactionEditor(this, idOrNull) { refreshWeb() }
            "reminder" -> ItemEditors.showReminderEditor(this) { refreshWeb() }
            else -> Toast.makeText(requireContext(), "Unknown: $kind", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onToggleHabitToday(id: String) {
        if (!isAdded) return
        StateRepository.update {
            val day = SeedData.today()
            val inner = habitLogs.getOrPut(day) { HashMap() }
            val done = inner[id] == "true"
            inner[id] = if (done) "false" else "true"
        }
        refreshWeb()
    }

    override fun onToggleSupplementLog(id: String) {
        if (!isAdded) return
        StateRepository.update {
            val day = SeedData.today()
            val inner = supplementLogs.getOrPut(day) { HashMap() }
            val cur = inner[id] == true
            inner[id] = !cur
        }
        refreshWeb()
    }

    override fun onStreamCreate() {
        if (!isAdded) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create")
            .setItems(arrayOf("New task", "New note")) { _, which ->
                when (which) {
                    0 -> ItemEditors.showTaskEditor(this, null) { refreshWeb() }
                    1 -> ItemEditors.showNoteEditor(this, null) { refreshWeb() }
                }
            }
            .show()
    }

    private fun showAbout() {
        if (!isAdded) return
        val vn = readVersionName()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("About")
            .setMessage(
                "Silent Order (ProDash)\nVersion $vn\n\n" +
                    "Offline-first: data stays on this device. Use Menu → Export / Import backup to move JSON between installs."
            )
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun readVersionName(): String = try {
        val ctx = requireContext()
        val pm = ctx.packageManager
        val pn = ctx.packageName
        @Suppress("DEPRECATION")
        val pi = if (Build.VERSION.SDK_INT >= 33) {
            pm.getPackageInfo(pn, PackageManager.PackageInfoFlags.of(0))
        } else {
            pm.getPackageInfo(pn, 0)
        }
        pi.versionName ?: ""
    } catch (_: Exception) {
        ""
    }

    private fun injectHydration(wv: WebView) {
        fun evalAsset(path: String) {
            val src = requireContext().assets.open(path).bufferedReader().use { it.readText() }
            wv.evaluateJavascript("eval(" + JSONObject.quote(src) + ");", null)
        }
        evalAsset("vendor/prodash-hydrate.js")
        evalAsset("vendor/prodash-chrome.js")
        val json = PageSnapshots.jsonForAsset(assetHtml, gson)
        wv.evaluateJavascript(
            "(function(){ if(window.ProDashHydrate) ProDashHydrate(" + json + "); })();",
            null
        )
        wv.evaluateJavascript(
            "(function(){ if(window.ProDashWireChrome) ProDashWireChrome(); })();",
            null
        )
    }

    override fun onDestroyView() {
        _binding?.webView?.apply {
            stopLoading()
            loadUrl("about:blank")
            removeJavascriptInterface("ProDash")
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
