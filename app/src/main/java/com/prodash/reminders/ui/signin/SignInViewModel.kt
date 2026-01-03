package com.prodash.reminders.ui.signin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.prodash.reminders.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignInViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState

    fun googleClient() = GoogleSignIn.getClient(
        getApplication(),
        com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN,
        )
            .requestIdToken(getApplication<Application>().getString(R.string.default_web_client_id))
            .requestEmail()
            .build(),
    )

    fun onGoogleResult(account: GoogleSignInAccount?) {
        viewModelScope.launch {
            if (account == null) {
                _uiState.value = SignInUiState(error = "Sign-in canceled")
                return@launch
            }
            try {
                _uiState.value = SignInUiState(isLoading = true)
                val idToken = account.idToken
                if (idToken.isNullOrBlank()) {
                    _uiState.value = SignInUiState(
                        isLoading = false,
                        error = "Google token missing. Check Firebase google-services.json and OAuth client setup.",
                    )
                    return@launch
                }
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                _uiState.value = SignInUiState(isLoading = false, signedIn = true)
            } catch (e: FirebaseAuthException) {
                _uiState.value = SignInUiState(
                    isLoading = false,
                    error = "Firebase auth failed (${e.errorCode}). Verify SHA-1 and web client ID in Firebase.",
                )
            } catch (e: Exception) {
                _uiState.value = SignInUiState(isLoading = false, error = e.message ?: "Sign-in failed")
            }
        }
    }

    fun onGoogleActivityResult(data: android.content.Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            onGoogleResult(account)
        } catch (e: ApiException) {
            _uiState.value = SignInUiState(error = e.message ?: "Google sign-in failed")
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun consumeSignedIn() {
        _uiState.value = _uiState.value.copy(signedIn = false)
    }
}

data class SignInUiState(
    val isLoading: Boolean = false,
    val signedIn: Boolean = false,
    val error: String? = null,
)
