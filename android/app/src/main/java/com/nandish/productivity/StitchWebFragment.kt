package com.nandish.productivity

import android.app.Activity
import android.content.pm.PackageManager
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.gson.Gson
import com.nandish.productivity.databinding.FragmentStitchBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class StitchWebFragment : Fragment(), ProDashBridge.Host {

    private var _binding: FragmentStitchBinding? = null
    private val binding get() = _binding!!

    private val gson = Gson()

    private val assetHtml: String
        get() = requireArguments().getString(ARG_ASSET_HTML)
            ?: error("Missing assetHtml argument")

    private var backCallback: OnBackPressedCallback? = null

    private var pendingDriveAfterSignIn: ((GoogleSignInAccount) -> Unit)? = null

    private val googleDriveSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (!isAdded) return@registerForActivityResult
        if (result.resultCode != Activity.RESULT_OK) {
            pendingDriveAfterSignIn = null
            return@registerForActivityResult
        }
        val data = result.data ?: run {
            pendingDriveAfterSignIn = null
            return@registerForActivityResult
        }
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val pending = pendingDriveAfterSignIn
            pendingDriveAfterSignIn = null
            if (pending != null) {
                pending.invoke(account)
            } else {
                Toast.makeText(requireContext(), "Signed in with Google", Toast.LENGTH_SHORT).show()
            }
        } catch (_: ApiException) {
            pendingDriveAfterSignIn = null
            Toast.makeText(requireContext(), "Google sign-in was cancelled or failed.", Toast.LENGTH_LONG).show()
        }
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
        applyObsidianChrome(wv)
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
                    "Google Drive…",
                    "Settings",
                    "Refresh this screen",
                    "About"
                )
            ) { _, which ->
                when (which) {
                    0 -> ItemEditors.showAddPicker(this) { refreshWeb() }
                    1 -> showGoogleDriveMenu()
                    2 -> onNavigate("settings")
                    3 -> injectHydration(wv)
                    4 -> showAbout()
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

    private fun showGoogleDriveMenu() {
        if (!isAdded) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Google Drive")
            .setItems(
                arrayOf(
                    "Back up now",
                    "Restore from Drive",
                    "Sign out of Google"
                )
            ) { _, which ->
                when (which) {
                    0 -> backupToGoogleDrive()
                    1 -> restoreFromGoogleDrive()
                    2 -> signOutGoogleDrive()
                }
            }
            .show()
    }

    private fun withGoogleAccount(block: (GoogleSignInAccount) -> Unit) {
        if (!isAdded) return
        val existing = GoogleDriveSync.lastSignedInAccount(requireContext())
        if (existing != null) {
            block(existing)
        } else {
            pendingDriveAfterSignIn = { block(it) }
            val intent = GoogleDriveSync.googleSignInClient(requireContext()).signInIntent
            googleDriveSignInLauncher.launch(intent)
        }
    }

    private fun backupToGoogleDrive() {
        if (!isAdded) return
        withGoogleAccount { account ->
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        GoogleDriveSync.uploadStateJson(requireContext(), account, StateRepository.exportJson())
                    }
                    Toast.makeText(requireContext(), "Backed up to Google Drive", Toast.LENGTH_SHORT).show()
                } catch (e: UserRecoverableAuthIOException) {
                    requireActivity().startActivity(e.intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Drive backup failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun restoreFromGoogleDrive() {
        if (!isAdded) return
        withGoogleAccount { account ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Restore from Google Drive")
                .setMessage("Replace all data on this device with the copy on Drive? This cannot be undone.")
                .setPositiveButton("Replace") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            val json = withContext(Dispatchers.IO) {
                                GoogleDriveSync.downloadStateJson(requireContext(), account)
                            }
                            if (json.isNullOrBlank()) {
                                Toast.makeText(
                                    requireContext(),
                                    "No backup found on Drive yet. Use Back up first.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@launch
                            }
                            val ok = StateRepository.importReplace(json)
                            if (ok) {
                                ReminderScheduler.schedule(requireContext().applicationContext)
                                refreshWeb()
                                Toast.makeText(requireContext(), "Restored from Drive", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Invalid backup on Drive", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: UserRecoverableAuthIOException) {
                            requireActivity().startActivity(e.intent)
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Drive restore failed: ${e.message}", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun signOutGoogleDrive() {
        if (!isAdded) return
        GoogleDriveSync.googleSignInClient(requireContext()).signOut().addOnCompleteListener {
            if (!isAdded) return@addOnCompleteListener
            Toast.makeText(requireContext(), "Signed out of Google", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSetSetting(key: String, value: String) {
        if (!isAdded) return
        when (key.trim().lowercase()) {
            "obsidianmode" -> StateRepository.update { settings.obsidianMode = value == "true" }
            "hapticsenabled" -> StateRepository.update { settings.hapticsEnabled = value == "true" }
            else -> return
        }
        refreshWeb()
    }

    private fun showAbout() {
        if (!isAdded) return
        val vn = readVersionName()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("About")
            .setMessage(
                "Silent Order (ProDash)\nVersion $vn\n\n" +
                    "Data stays on this device. Use Menu → Google Drive to back up or restore your JSON to your Google account (app data folder)."
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
        applyObsidianChrome(wv)
        val obsidian = StateRepository.get().settings.obsidianMode
        wv.evaluateJavascript(
            "(function(){ if(window.ProDashApplyAppearance) ProDashApplyAppearance(" +
                (if (obsidian) "true" else "false") +
                "); })();",
            null
        )
    }

    private fun applyObsidianChrome(wv: WebView) {
        val on = StateRepository.get().settings.obsidianMode
        val color = if (on) 0xFF131313.toInt() else 0xFF26262B.toInt()
        wv.setBackgroundColor(color)
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
