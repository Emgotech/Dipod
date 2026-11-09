package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.ChurchRepository
import com.example.ui.ChurchViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.ChurchRegistrationScreen
import com.example.ui.navigation.ChurchBaseAppContent
import com.example.ui.theme.ChurchBaseTheme
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyBlueDark
import com.example.ui.theme.NavyBluePrimary

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val repository = ChurchRepository(this)

    setContent {
      ChurchBaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          val authViewModel = remember { AuthViewModel(repository) }
          AppRoot(
            repository = repository,
            authViewModel = authViewModel
          )
        }
      }
    }
  }
}

@Composable
fun AppRoot(
  repository: ChurchRepository,
  authViewModel: AuthViewModel
) {
  val context = LocalContext.current
  val credentialManager = remember { CredentialManager.create(context) }
  val authState by authViewModel.uiState.collectAsState()

  // Attempt silent auto-sign-in on app start
  LaunchedEffect(Unit) {
    authViewModel.attemptAutoSignIn(context, credentialManager)
  }

  when (val state = authState) {
    is AuthUiState.Idle, is AuthUiState.Error -> {
      AuthScreen(viewModel = authViewModel)
    }

    is AuthUiState.Loading -> {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(NavyBlueDark),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(
            modifier = Modifier
              .size(80.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = painterResource(id = R.drawable.churchbase_app_icon_1794229924174),
              contentDescription = "ChurchBase Emblem",
              modifier = Modifier.size(68.dp).clip(CircleShape)
            )
          }
          Spacer(modifier = Modifier.height(18.dp))
          Text(
            text = "ChurchBase",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(16.dp))
          CircularProgressIndicator(color = GoldAccent, strokeWidth = 3.dp)
        }
      }
    }

    is AuthUiState.NeedsChurchRegistration -> {
      ChurchRegistrationScreen(viewModel = authViewModel)
    }

    is AuthUiState.Authenticated -> {
      val uid = state.user.uid
      val churchId = state.platformUser?.churchId.orEmpty()

      // Reset ViewModel and Compose tree if user switches
      key(uid) {
        val churchViewModel = remember(churchId) {
          ChurchViewModel(repository, churchId)
        }

        ChurchBaseAppContent(
          church = state.church,
          platformUser = state.platformUser,
          viewModel = churchViewModel,
          onSignOut = {
            authViewModel.signOut(credentialManager)
          }
        )
      }
    }
  }
}
