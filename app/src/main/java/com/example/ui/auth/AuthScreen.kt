package com.example.ui.auth

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyBlueDark
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.RoyalPurpleSecondary

@Composable
fun AuthScreen(
  viewModel: AuthViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val credentialManager = CredentialManager.create(context)
  val scrollState = rememberScrollState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            NavyBlueDark,
            NavyBluePrimary,
            RoyalPurpleSecondary.copy(alpha = 0.85f)
          )
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 24.dp, vertical = 40.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(20.dp))

      // Logo Icon
      Box(
        modifier = Modifier
          .size(90.dp)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.churchbase_app_icon_1794229924174),
          contentDescription = "ChurchBase Emblem",
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // App Title
      Text(
        text = "ChurchBase",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        letterSpacing = 0.5.sp
      )

      Text(
        text = "Enterprise Church Membership & Administration",
        fontSize = 14.sp,
        color = GoldAccent,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Hero Banner Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = Modifier
          .fillMaxWidth()
          .height(170.dp)
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          Image(
            painter = painterResource(id = R.drawable.church_hero_banner_1794229940193),
            contentDescription = "Church Sanctuary",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(Color.Transparent, NavyBlueDark.copy(alpha = 0.8f))
                )
              )
          )
          Column(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(16.dp)
          ) {
            Text(
              text = "Multi-Church Cloud Platform",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Text(
              text = "Secure cloud database for congregations of all sizes",
              color = Color.White.copy(alpha = 0.85f),
              fontSize = 12.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Value Propositions
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        FeatureRow(icon = Icons.Default.Group, text = "Comprehensive Member & Leadership Records")
        FeatureRow(icon = Icons.Default.HowToReg, text = "Real-time Service Attendance Tracking")
        FeatureRow(icon = Icons.Default.Church, text = "Visitor Follow-up & Convert Discipleship")
        FeatureRow(icon = Icons.Default.Security, text = "Multi-Tenant Cloud Isolation & Security")
      }

      Spacer(modifier = Modifier.height(36.dp))

      // Google Sign-In Button
      Button(
        onClick = {
          if (context is Activity) {
            viewModel.signInWithGoogle(context, credentialManager)
          }
        },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.White,
          contentColor = NavyBlueDark
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("google_sign_in_button")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = NavyBlueDark,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = "Sign in with Google",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "By signing in, you access your church's private encrypted database.",
        color = Color.White.copy(alpha = 0.65f),
        fontSize = 11.sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun FeatureRow(icon: ImageVector, text: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = GoldAccent,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = text,
      color = Color.White.copy(alpha = 0.95f),
      fontSize = 13.sp,
      fontWeight = FontWeight.Medium
    )
  }
}
