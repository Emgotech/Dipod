package com.example.ui.members

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ChurchMember
import com.example.data.model.Department
import com.example.ui.theme.NavyBluePrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditMemberDialog(
  memberToEdit: ChurchMember?,
  departments: List<Department>,
  onDismiss: () -> Unit,
  onSave: (ChurchMember) -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) } // 0: Personal, 1: Contact, 2: Church & Ministry

  // Personal fields
  var firstName by remember { mutableStateOf(memberToEdit?.firstName ?: "") }
  var middleName by remember { mutableStateOf(memberToEdit?.middleName ?: "") }
  var lastName by remember { mutableStateOf(memberToEdit?.lastName ?: "") }
  var gender by remember { mutableStateOf(memberToEdit?.gender ?: "Male") }
  var dateOfBirth by remember { mutableStateOf(memberToEdit?.dateOfBirth ?: "") }
  var maritalStatus by remember { mutableStateOf(memberToEdit?.maritalStatus ?: "Single") }
  var occupation by remember { mutableStateOf(memberToEdit?.occupation ?: "") }
  var nationality by remember { mutableStateOf(memberToEdit?.nationality ?: "") }
  var residentialAddress by remember { mutableStateOf(memberToEdit?.residentialAddress ?: "") }
  var city by remember { mutableStateOf(memberToEdit?.city ?: "") }
  var state by remember { mutableStateOf(memberToEdit?.state ?: "") }
  var country by remember { mutableStateOf(memberToEdit?.country ?: "") }

  // Contact fields (Explicitly OPTIONAL per requirements)
  var phone by remember { mutableStateOf(memberToEdit?.phone ?: "") }
  var alternatePhone by remember { mutableStateOf(memberToEdit?.alternatePhone ?: "") }
  var email by remember { mutableStateOf(memberToEdit?.email ?: "") }

  // Church membership fields
  var memberId by remember {
    mutableStateOf(
      memberToEdit?.memberId ?: "CHB-${SimpleDateFormat("yyMM", Locale.getDefault()).format(Date())}-${(100..999).random()}"
    )
  }
  val categoryOptions = listOf(
    "Regular member", "New convert", "New member", "Church worker", "Department leader",
    "Pastor", "Assistant pastor", "Elder", "Deacon", "Deaconess", "Minister", "First-time visitor", "Other"
  )
  var category by remember { mutableStateOf(memberToEdit?.category ?: categoryOptions[0]) }
  var categoryExpanded by remember { mutableStateOf(false) }

  val statusOptions = listOf("Active", "Inactive", "Visiting", "Transferred", "Deceased", "Archived")
  var status by remember { mutableStateOf(memberToEdit?.status ?: statusOptions[0]) }
  var statusExpanded by remember { mutableStateOf(false) }

  var dateJoined by remember {
    mutableStateOf(
      memberToEdit?.dateJoined ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
  }
  var dateFirstVisited by remember { mutableStateOf(memberToEdit?.dateFirstVisited ?: "") }

  val baptismOptions = listOf("Baptized", "Not Baptized", "Pending")
  var baptismStatus by remember { mutableStateOf(memberToEdit?.baptismStatus ?: baptismOptions[0]) }
  var baptismDate by remember { mutableStateOf(memberToEdit?.baptismDate ?: "") }
  var previousChurch by remember { mutableStateOf(memberToEdit?.previousChurch ?: "") }
  var invitedBy by remember { mutableStateOf(memberToEdit?.invitedBy ?: "") }
  var assignedLeader by remember { mutableStateOf(memberToEdit?.assignedLeader ?: "") }
  var selectedDepartments by remember { mutableStateOf(memberToEdit?.departments?.toSet() ?: emptySet()) }
  var smallGroup by remember { mutableStateOf(memberToEdit?.smallGroup ?: "") }
  var notes by remember { mutableStateOf(memberToEdit?.notes ?: "") }

  var validationError by remember { mutableStateOf<String?>(null) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth(0.96f)
        .padding(vertical = 20.dp)
        .testTag("add_edit_member_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (memberToEdit == null) "Register New Member" else "Edit Member Profile",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBluePrimary
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs for Organized Form Navigation
        TabRow(
          selectedTabIndex = selectedTab,
          contentColor = NavyBluePrimary
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Personal", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Contact", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            text = { Text("Church", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Form Content (Scrollable)
        Column(
          modifier = Modifier
            .weight(1f, fill = false)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          when (selectedTab) {
            0 -> {
              // --- Tab 0: Personal Information ---
              OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it; validationError = null },
                label = { Text("First Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("member_first_name_input")
              )

              OutlinedTextField(
                value = middleName,
                onValueChange = { middleName = it },
                label = { Text("Middle Name (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )

              OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it; validationError = null },
                label = { Text("Last Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("member_last_name_input")
              )

              // Gender selection
              Text("Gender", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Male", "Female", "Other").forEach { g ->
                  val isSelected = gender == g
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) NavyBluePrimary else MaterialTheme.colorScheme.background,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) else null,
                    modifier = Modifier.clickable { gender = g }
                  ) {
                    Text(
                      text = g,
                      color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Medium,
                      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                  }
                }
              }

              OutlinedTextField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                label = { Text("Date of Birth (YYYY-MM-DD)") },
                placeholder = { Text("e.g. 1990-05-15") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )

              // Marital Status selection
              Text("Marital Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Single", "Married", "Widowed", "Divorced").forEach { ms ->
                  val isSelected = maritalStatus == ms
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) NavyBluePrimary else MaterialTheme.colorScheme.background,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) else null,
                    modifier = Modifier.clickable { maritalStatus = ms }
                  ) {
                    Text(
                      text = ms,
                      color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                      fontSize = 12.sp,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                  }
                }
              }

              OutlinedTextField(
                value = occupation,
                onValueChange = { occupation = it },
                label = { Text("Occupation / Profession (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )

              OutlinedTextField(
                value = residentialAddress,
                onValueChange = { residentialAddress = it },
                label = { Text("Residential Address (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = city,
                  onValueChange = { city = it },
                  label = { Text("City") },
                  singleLine = true,
                  modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                  value = state,
                  onValueChange = { state = it },
                  label = { Text("State") },
                  singleLine = true,
                  modifier = Modifier.weight(1f)
                )
              }
            }

            1 -> {
              // --- Tab 1: Contact Information (OPTIONAL) ---
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyBluePrimary.copy(alpha = 0.05f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = "Note: Contact fields are optional. Members may be registered without phone or email.",
                  fontSize = 12.sp,
                  color = NavyBluePrimary,
                  modifier = Modifier.padding(12.dp)
                )
              }

              OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Primary Phone Number (Optional)") },
                placeholder = { Text("e.g. +1 555-123-4567") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("member_phone_input")
              )

              OutlinedTextField(
                value = alternatePhone,
                onValueChange = { alternatePhone = it },
                label = { Text("Alternate Phone Number (Optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )

              OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address (Optional)") },
                placeholder = { Text("e.g. member@example.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("member_email_input")
              )
            }

            2 -> {
              // --- Tab 2: Church & Ministry Information ---
              OutlinedTextField(
                value = memberId,
                onValueChange = { memberId = it },
                label = { Text("Member ID (Auto-generated / Customizable)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )

              // Category dropdown
              ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it },
                modifier = Modifier.fillMaxWidth()
              ) {
                OutlinedTextField(
                  value = category,
                  onValueChange = {},
                  readOnly = true,
                  label = { Text("Membership Category *") },
                  trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                  modifier = Modifier.menuAnchor().fillMaxWidth().testTag("member_category_dropdown")
                )
                ExposedDropdownMenu(
                  expanded = categoryExpanded,
                  onDismissRequest = { categoryExpanded = false }
                ) {
                  categoryOptions.forEach { opt ->
                    DropdownMenuItem(
                      text = { Text(opt) },
                      onClick = {
                        category = opt
                        categoryExpanded = false
                      }
                    )
                  }
                }
              }

              // Status dropdown
              ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = it },
                modifier = Modifier.fillMaxWidth()
              ) {
                OutlinedTextField(
                  value = status,
                  onValueChange = {},
                  readOnly = true,
                  label = { Text("Membership Status *") },
                  trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                  modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                  expanded = statusExpanded,
                  onDismissRequest = { statusExpanded = false }
                ) {
                  statusOptions.forEach { opt ->
                    DropdownMenuItem(
                      text = { Text(opt) },
                      onClick = {
                        status = opt
                        statusExpanded = false
                      }
                    )
                  }
                }
              }

              OutlinedTextField(
                value = dateJoined,
                onValueChange = { dateJoined = it },
                label = { Text("Date Joined Church (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )

              OutlinedTextField(
                value = dateFirstVisited,
                onValueChange = { dateFirstVisited = it },
                label = { Text("Date First Visited (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )

              // Baptism
              Text("Baptism Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                baptismOptions.forEach { bOpt ->
                  val isSelected = baptismStatus == bOpt
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) NavyBluePrimary else MaterialTheme.colorScheme.background,
                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) else null,
                    modifier = Modifier.clickable { baptismStatus = bOpt }
                  ) {
                    Text(
                      text = bOpt,
                      color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                      fontSize = 12.sp,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                  }
                }
              }

              OutlinedTextField(
                value = invitedBy,
                onValueChange = { invitedBy = it },
                label = { Text("Invited By (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )

              OutlinedTextField(
                value = assignedLeader,
                onValueChange = { assignedLeader = it },
                label = { Text("Assigned Pastor / Leader (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )

              // Department checkboxes
              if (departments.isNotEmpty()) {
                Text("Assigned Departments & Ministries", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                departments.forEach { dept ->
                  val isChecked = selectedDepartments.contains(dept.name)
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        selectedDepartments = if (isChecked) selectedDepartments - dept.name else selectedDepartments + dept.name
                      }
                      .padding(vertical = 2.dp)
                  ) {
                    Checkbox(
                      checked = isChecked,
                      onCheckedChange = {
                        selectedDepartments = if (it) selectedDepartments + dept.name else selectedDepartments - dept.name
                      },
                      colors = CheckboxDefaults.colors(checkedColor = NavyBluePrimary)
                    )
                    Text(dept.name, fontSize = 13.sp)
                  }
                }
              }

              OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Pastoral Notes / Administrative Remarks") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }

        if (validationError != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(validationError ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text("Cancel")
          }

          Button(
            onClick = {
              if (firstName.isBlank()) {
                selectedTab = 0
                validationError = "First name is required"
                return@Button
              }
              if (lastName.isBlank()) {
                selectedTab = 0
                validationError = "Last name is required"
                return@Button
              }

              val updatedMember = (memberToEdit ?: ChurchMember()).copy(
                memberId = memberId.trim(),
                firstName = firstName.trim(),
                middleName = middleName.trim(),
                lastName = lastName.trim(),
                gender = gender,
                dateOfBirth = dateOfBirth.trim(),
                maritalStatus = maritalStatus,
                occupation = occupation.trim(),
                nationality = nationality.trim(),
                residentialAddress = residentialAddress.trim(),
                city = city.trim(),
                state = state.trim(),
                country = country.trim(),
                phone = phone.trim(),
                alternatePhone = alternatePhone.trim(),
                email = email.trim(),
                category = category,
                status = status,
                dateJoined = dateJoined.trim(),
                dateFirstVisited = dateFirstVisited.trim(),
                baptismStatus = baptismStatus,
                baptismDate = baptismDate.trim(),
                previousChurch = previousChurch.trim(),
                invitedBy = invitedBy.trim(),
                assignedLeader = assignedLeader.trim(),
                departments = selectedDepartments.toList(),
                smallGroup = smallGroup.trim(),
                notes = notes.trim()
              )

              onSave(updatedMember)
              onDismiss()
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
            modifier = Modifier.weight(1f).testTag("save_member_button")
          ) {
            Text(if (memberToEdit == null) "Register" else "Save Changes")
          }
        }
      }
    }
  }
}
