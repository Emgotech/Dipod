package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.Church
import com.example.data.model.PlatformUser
import com.example.data.repository.ChurchRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface AuthUiState {
  data object Idle : AuthUiState
  data object Loading : AuthUiState
  data class Authenticated(val user: FirebaseUser, val platformUser: PlatformUser?, val church: Church?) : AuthUiState
  data class NeedsChurchRegistration(val user: FirebaseUser, val platformUser: PlatformUser) : AuthUiState
  data class Error(val message: String) : AuthUiState
}

class AuthViewModel(private val repository: ChurchRepository) : ViewModel() {

  private val auth = Firebase.auth
  private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
  val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

  private var authListener: FirebaseAuth.AuthStateListener? = null

  init {
    listenToAuthState()
  }

  private fun listenToAuthState() {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
      val user = firebaseAuth.currentUser
      if (user != null) {
        loadUserProfile(user)
      } else {
        _uiState.value = AuthUiState.Idle
      }
    }
    authListener = listener
    auth.addAuthStateListener(listener)
  }

  fun loadUserProfile(user: FirebaseUser) {
    viewModelScope.launch {
      _uiState.value = AuthUiState.Loading
      val result = repository.getPlatformUser(user.uid)
      if (result.isSuccess) {
        val platformUser = result.getOrThrow()
        if (platformUser != null && platformUser.churchId.isNotBlank()) {
          val churchResult = repository.getChurch(platformUser.churchId)
          val church = churchResult.getOrNull()
          _uiState.value = AuthUiState.Authenticated(user, platformUser, church)
        } else {
          val newUser = platformUser ?: PlatformUser(
            userId = user.uid,
            email = user.email ?: "",
            displayName = user.displayName ?: "Pastor / Administrator",
            role = "OWNER"
          )
          _uiState.value = AuthUiState.NeedsChurchRegistration(user, newUser)
        }
      } else {
        // Fallback for new user
        val newUser = PlatformUser(
          userId = user.uid,
          email = user.email ?: "",
          displayName = user.displayName ?: "Pastor / Administrator",
          role = "OWNER"
        )
        _uiState.value = AuthUiState.NeedsChurchRegistration(user, newUser)
      }
    }
  }

  fun signInWithGoogle(
    activity: Activity,
    credentialManager: CredentialManager,
    onCancelled: () -> Unit = {}
  ) {
    _uiState.value = AuthUiState.Loading
    val clientId = try {
      activity.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
      _uiState.value = AuthUiState.Error("Google Sign-In configuration missing: default_web_client_id")
      return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    viewModelScope.launch {
      try {
        val result = credentialManager.getCredential(activity, request)
        val credential = result.credential
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
          val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
          val authResult = auth.signInWithCredential(authCredential).await()
          val user = authResult.user
          if (user != null) {
            loadUserProfile(user)
          } else {
            _uiState.value = AuthUiState.Error("Sign in failed: User was null")
          }
        } else {
          _uiState.value = AuthUiState.Error("Unexpected credential type: ${credential.type}")
        }
      } catch (e: GetCredentialCancellationException) {
        Log.w("Auth", "Google Sign-In cancelled: ${e.message}", e)
        onCancelled()
        _uiState.value = AuthUiState.Idle
      } catch (e: Exception) {
        Log.e("Auth", "Google Sign-In failed", e)
        _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Sign in failed")
      }
    }
  }

  fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager
  ) {
    if (auth.currentUser != null) {
      loadUserProfile(auth.currentUser!!)
      return
    }

    val clientId = try {
      context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
      return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
      .setFilterByAuthorizedAccounts(true)
      .setServerClientId(clientId)
      .setAutoSelectEnabled(true)
      .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    viewModelScope.launch {
      try {
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
          val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
          auth.signInWithCredential(authCredential).await()
        }
      } catch (e: Exception) {
        // Silent sign in failed, continue to idle/login screen
      }
    }
  }

  fun registerChurch(
    name: String,
    denomination: String,
    country: String,
    state: String,
    city: String,
    address: String,
    phone: String,
    email: String,
    pastor: String,
    sizeCategory: String
  ) {
    val currentUser = auth.currentUser ?: return
    viewModelScope.launch {
      _uiState.value = AuthUiState.Loading
      val church = Church(
        name = name,
        ownerUid = currentUser.uid,
        denomination = denomination,
        country = country,
        state = state,
        city = city,
        address = address,
        phone = phone,
        email = email,
        pastor = pastor,
        sizeCategory = sizeCategory,
        subscriptionTier = "STANDARD",
        subscriptionStatus = "ACTIVE"
      )
      val createResult = repository.createChurch(church)
      if (createResult.isSuccess) {
        val churchId = createResult.getOrThrow()
        val platformUser = PlatformUser(
          userId = currentUser.uid,
          email = currentUser.email ?: "",
          displayName = currentUser.displayName ?: pastor.ifBlank { "Senior Pastor" },
          churchId = churchId,
          role = "OWNER"
        )
        repository.savePlatformUser(platformUser)
        val savedChurch = church.copy(id = churchId)
        _uiState.value = AuthUiState.Authenticated(currentUser, platformUser, savedChurch)
      } else {
        _uiState.value = AuthUiState.Error(createResult.exceptionOrNull()?.localizedMessage ?: "Failed to register church")
      }
    }
  }

  fun signOut(credentialManager: CredentialManager) {
    viewModelScope.launch {
      withContext(NonCancellable) {
        try {
          credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
          Log.w("Auth", "Failed to clear credential state: ${e.message}")
        } finally {
          auth.signOut()
          _uiState.value = AuthUiState.Idle
        }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    authListener?.let { auth.removeAuthStateListener(it) }
  }
}
