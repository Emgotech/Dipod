package com.example.ui.auth

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyBlueDark
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.SlateBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChurchRegistrationScreen(
  viewModel: AuthViewModel,
  modifier: Modifier = Modifier
) {
  var churchName by remember { mutableStateOf("") }
  var denomination by remember { mutableStateOf("") }
  var pastorName by remember { mutableStateOf("") }
  var country by remember { mutableStateOf("") }
  var state by remember { mutableStateOf("") }
  var city by remember { mutableStateOf("") }
  var address by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }

  val sizeOptions = listOf("Small (Under 100)", "Medium (100 - 500)", "Large (500 - 2,000)", "Megachurch (2,000+)")
  var selectedSize by remember { mutableStateOf(sizeOptions[1]) }
  var sizeExpanded by remember { mutableStateOf(false) }

  var validationError by remember { mutableStateOf<String?>(null) }
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(scrollState)
      .padding(horizontal = 20.dp, vertical = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Header
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(NavyBluePrimary),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Church,
          contentDescription = null,
          tint = GoldAccent,
          modifier = Modifier.size(28.dp)
        )
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column {
        Text(
          text = "Register Your Church",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Set up your independent church database",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Organization Profile",
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold,
          color = NavyBluePrimary
        )

        OutlinedTextField(
          value = churchName,
          onValueChange = { churchName = it; validationError = null },
          label = { Text("Church / Ministry Name *") },
          placeholder = { Text("e.g. Grace Tabernacle International") },
          leadingIcon = { Icon(Icons.Default.Church, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("church_name_input")
        )

        OutlinedTextField(
          value = pastorName,
          onValueChange = { pastorName = it; validationError = null },
          label = { Text("Senior Pastor / Head Minister *") },
          placeholder = { Text("e.g. Rev. Dr. David Johnson") },
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("pastor_name_input")
        )

        OutlinedTextField(
          value = denomination,
          onValueChange = { denomination = it },
          label = { Text("Denomination / Affiliation (Optional)") },
          placeholder = { Text("e.g. Pentecostal, Baptist, Independent") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Church Size Dropdown
        ExposedDropdownMenuBox(
          expanded = sizeExpanded,
          onExpandedChange = { sizeExpanded = it },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = selectedSize,
            onValueChange = {},
            readOnly = true,
            label = { Text("Congregation Size Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sizeExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = sizeExpanded,
            onDismissRequest = { sizeExpanded = false }
          ) {
            sizeOptions.forEach { option ->
              DropdownMenuItem(
                text = { Text(option) },
                onClick = {
                  selectedSize = option
                  sizeExpanded = false
                }
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Location & Contact Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Location & Official Contact",
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold,
          color = NavyBluePrimary
        )

        OutlinedTextField(
          value = country,
          onValueChange = { country = it; validationError = null },
          label = { Text("Country *") },
          placeholder = { Text("e.g. United States, Nigeria, Ghana") },
          leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("church_country_input")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = state,
            onValueChange = { state = it },
            label = { Text("State / Province") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("City") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }

        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text("Physical Street Address (Optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Official Church Phone (Optional)") },
          leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Official Church Email (Optional)") },
          leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    if (validationError != null) {
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = validationError ?: "",
        color = MaterialTheme.colorScheme.error,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Submit Button
    Button(
      onClick = {
        if (churchName.isBlank()) {
          validationError = "Please enter your church name"
          return@Button
        }
        if (pastorName.isBlank()) {
          validationError = "Please enter the senior pastor/leader name"
          return@Button
        }
        if (country.isBlank()) {
          validationError = "Please enter the country of operation"
          return@Button
        }

        viewModel.registerChurch(
          name = churchName.trim(),
          denomination = denomination.trim(),
          country = country.trim(),
          state = state.trim(),
          city = city.trim(),
          address = address.trim(),
          phone = phone.trim(),
          email = email.trim(),
          pastor = pastorName.trim(),
          sizeCategory = selectedSize
        )
      },
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = NavyBluePrimary,
        contentColor = Color.White
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .testTag("complete_church_registration_button")
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Complete Church Setup", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.ArrowForward, contentDescription = null)
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}
