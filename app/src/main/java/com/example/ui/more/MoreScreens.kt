package com.example.ui.more

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Church
import com.example.data.model.ChurchMember
import com.example.data.model.PlatformUser
import com.example.data.model.Visitor
import com.example.ui.ChurchUiState
import com.example.ui.ChurchViewModel
import com.example.ui.members.initiateEmail
import com.example.ui.members.initiatePhoneCall
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyBlueDark
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.RoyalPurpleSecondary
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusArchived
import com.example.ui.theme.StatusConverts
import com.example.ui.theme.StatusVisiting
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MoreHubScreen(
  church: Church?,
  platformUser: PlatformUser?,
  uiState: ChurchUiState,
  onOpenSubscreen: (String) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Church Profile Card
    Card(
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth().testTag("more_profile_card")
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(NavyBluePrimary),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Person, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = platformUser?.displayName.orEmpty().ifBlank { "Church Administrator" },
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Role: ${platformUser?.role ?: "OWNER"} • ${church?.name ?: "Church"}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = platformUser?.email.orEmpty(),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
          )
        }
      }
    }

    // Ministry Modules Section
    Text("Ministry Modules", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column {
        MoreItemRow(
          icon = Icons.Default.PersonOutline,
          iconTint = StatusVisiting,
          title = "Visitors & Follow-up",
          badge = "${uiState.visitors.size}",
          onClick = { onOpenSubscreen("visitors") }
        )
        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
        MoreItemRow(
          icon = Icons.Default.MilitaryTech,
          iconTint = StatusConverts,
          title = "New Converts & Discipleship",
          badge = "${uiState.members.count { it.category.equals("New convert", ignoreCase = true) }}",
          onClick = { onOpenSubscreen("converts") }
        )
        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
        MoreItemRow(
          icon = Icons.Default.SupervisorAccount,
          iconTint = RoyalPurpleSecondary,
          title = "Leadership & Staff Directory",
          onClick = { onOpenSubscreen("leadership") }
        )
        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
        MoreItemRow(
          icon = Icons.Default.Cake,
          iconTint = Color(0xFFE11D48),
          title = "Birthdays & Anniversaries",
          onClick = { onOpenSubscreen("birthdays") }
        )
      }
    }

    // Reports & Analytics Section
    Text("Analytics & Operations", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column {
        MoreItemRow(
          icon = Icons.Default.Assessment,
          iconTint = Color(0xFF0D9488),
          title = "Reports & CSV Data Export",
          onClick = { onOpenSubscreen("reports") }
        )
        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
        MoreItemRow(
          icon = Icons.Default.History,
          iconTint = NavyBluePrimary,
          title = "Security & Activity Audit Log",
          badge = "${uiState.auditLogs.size}",
          onClick = { onOpenSubscreen("audit") }
        )
      }
    }

    // Administration & Settings Section
    Text("Administration", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column {
        MoreItemRow(
          icon = Icons.Default.Security,
          iconTint = NavyBluePrimary,
          title = "Roles & Permissions (RBAC)",
          onClick = { onOpenSubscreen("roles") }
        )
        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
        MoreItemRow(
          icon = Icons.Default.CardMembership,
          iconTint = GoldAccent,
          title = "Subscription & SaaS Plans",
          badge = church?.subscriptionTier ?: "STANDARD",
          onClick = { onOpenSubscreen("subscription") }
        )
        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
        MoreItemRow(
          icon = Icons.Default.Settings,
          iconTint = NavyBlueDark,
          title = "Church Profile Settings",
          onClick = { onOpenSubscreen("settings") }
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Sign Out Button
    OutlinedButton(
      onClick = onSignOut,
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
      modifier = Modifier.fillMaxWidth().height(50.dp).testTag("sign_out_button")
    ) {
      Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("Sign Out", fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}

@Composable
private fun MoreItemRow(
  icon: ImageVector,
  iconTint: Color,
  title: String,
  badge: String? = null,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(iconTint.copy(alpha = 0.12f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
    }
    Spacer(modifier = Modifier.width(14.dp))
    Text(
      text = title,
      fontSize = 14.sp,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f)
    )
    if (badge != null) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
      ) {
        Text(
          text = badge,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = NavyBluePrimary,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
    }
    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
  }
}

// ==========================================
// 1. VISITORS & FOLLOW-UP MODULE
// ==========================================
@Composable
fun VisitorsScreen(
  uiState: ChurchUiState,
  viewModel: ChurchViewModel,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  var showAddVisitorDialog by remember { mutableStateOf(false) }
  var visitorToConvert by remember { mutableStateOf<Visitor?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("Visitors & Follow-up", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBluePrimary)
        Text("Track guest attendance and pastoral outreach", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Button(
        onClick = { showAddVisitorDialog = true },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary)
      ) {
        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Add")
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (uiState.visitors.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No visitors recorded yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(uiState.visitors, key = { it.id }) { visitor ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(visitor.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (visitor.followUpStatus == "Became a member") StatusActive.copy(alpha = 0.15f) else StatusVisiting.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = visitor.followUpStatus,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (visitor.followUpStatus == "Became a member") StatusActive else StatusVisiting,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text("Visited: ${visitor.dateVisited} • ${visitor.serviceAttended}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

              if (visitor.invitedBy.isNotBlank()) {
                Text("Invited by: ${visitor.invitedBy}", fontSize = 11.sp, color = NavyBluePrimary)
              }

              if (visitor.followUpNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Notes: ${visitor.followUpNotes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
              }

              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  if (visitor.phone.isNotBlank()) {
                    IconButton(onClick = { initiatePhoneCall(context, visitor.phone) }, modifier = Modifier.size(32.dp)) {
                      Icon(Icons.Default.Phone, contentDescription = "Call", tint = StatusActive, modifier = Modifier.size(16.dp))
                    }
                  }
                  if (visitor.email.isNotBlank()) {
                    IconButton(onClick = { initiateEmail(context, visitor.email) }, modifier = Modifier.size(32.dp)) {
                      Icon(Icons.Default.Email, contentDescription = "Email", tint = NavyBluePrimary, modifier = Modifier.size(16.dp))
                    }
                  }
                }

                if (visitor.followUpStatus != "Became a member") {
                  Button(
                    onClick = { visitorToConvert = visitor },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPurpleSecondary)
                  ) {
                    Text("Convert to Member", fontSize = 11.sp)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Add Visitor Dialog
  if (showAddVisitorDialog) {
    AddVisitorDialog(
      onDismiss = { showAddVisitorDialog = false },
      onSave = { newVisitor ->
        viewModel.addVisitor(newVisitor)
        showAddVisitorDialog = false
      }
    )
  }

  // Convert Visitor Confirmation Dialog
  if (visitorToConvert != null) {
    val visitor = visitorToConvert!!
    AlertDialog(
      onDismissRequest = { visitorToConvert = null },
      title = { Text("Convert Visitor to Member") },
      text = { Text("Register '${visitor.name}' as a church member? Their contact details, visit date, and invited-by information will automatically transfer.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.convertVisitorToMember(visitor)
            visitorToConvert = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary)
        ) {
          Text("Convert")
        }
      },
      dismissButton = {
        TextButton(onClick = { visitorToConvert = null }) { Text("Cancel") }
      }
    )
  }
}

@Composable
fun AddVisitorDialog(
  onDismiss: () -> Unit,
  onSave: (Visitor) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var dateVisited by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
  var serviceAttended by remember { mutableStateOf("Sunday service") }
  var invitedBy by remember { mutableStateOf("") }
  var followUpNotes by remember { mutableStateOf("") }
  var assignedWorker by remember { mutableStateOf("") }
  var error by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Record First-Time Visitor", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = NavyBluePrimary)

        OutlinedTextField(
          value = name,
          onValueChange = { name = it; error = null },
          label = { Text("Visitor Full Name *") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Phone Number (Optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Email (Optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = dateVisited,
            onValueChange = { dateVisited = it },
            label = { Text("Visit Date") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = serviceAttended,
            onValueChange = { serviceAttended = it },
            label = { Text("Service Attended") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }

        OutlinedTextField(
          value = invitedBy,
          onValueChange = { invitedBy = it },
          label = { Text("Invited By") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = followUpNotes,
          onValueChange = { followUpNotes = it },
          label = { Text("Outreach / Follow-up Notes") },
          minLines = 2,
          modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
          Text(error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
          Button(
            onClick = {
              if (name.isBlank()) {
                error = "Name is required"
                return@Button
              }
              onSave(
                Visitor(
                  name = name.trim(),
                  phone = phone.trim(),
                  email = email.trim(),
                  dateVisited = dateVisited.trim(),
                  serviceAttended = serviceAttended.trim(),
                  invitedBy = invitedBy.trim(),
                  followUpNotes = followUpNotes.trim(),
                  assignedWorker = assignedWorker.trim()
                )
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
            modifier = Modifier.weight(1f)
          ) {
            Text("Save Visitor")
          }
        }
      }
    }
  }
}

// ==========================================
// 2. NEW CONVERTS & DISCIPLESHIP MODULE
// ==========================================
@Composable
fun NewConvertsScreen(
  uiState: ChurchUiState,
  onBack: () -> Unit
) {
  val converts = uiState.members.filter { it.category.equals("New convert", ignoreCase = true) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
  ) {
    Text("New Converts & Discipleship", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBluePrimary)
    Text("Track spiritual growth, discipleship, and baptism milestones", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.height(14.dp))

    if (converts.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(54.dp))
          Spacer(modifier = Modifier.height(8.dp))
          Text("No registered new converts", color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("Register converts from member registration or convert a visitor", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
      }
    } else {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(converts, key = { it.id }) { convert ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(convert.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = StatusConverts.copy(alpha = 0.15f)
                ) {
                  Text("New Convert", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusConverts, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text("Joined: ${convert.dateJoined.ifBlank { "Recently" }} • Baptism: ${convert.baptismStatus}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

              if (convert.assignedLeader.isNotBlank()) {
                Text("Assigned Discipleship Leader: ${convert.assignedLeader}", fontSize = 12.sp, color = NavyBluePrimary)
              }

              if (convert.phone.isNotBlank()) {
                Text("Contact: ${convert.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      }
    }
  }
}

// ==========================================
// 3. LEADERSHIP & STAFF DIRECTORY
// ==========================================
@Composable
fun LeadershipScreen(
  uiState: ChurchUiState,
  onBack: () -> Unit
) {
  val leaders = uiState.members.filter {
    val cat = it.category.lowercase()
    cat.contains("pastor") || cat.contains("elder") || cat.contains("deacon") || cat.contains("leader") || cat.contains("minister")
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
  ) {
    Text("Church Leadership Directory", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBluePrimary)
    Text("Pastors, elders, deacons, ministers, and department heads", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.height(14.dp))

    if (leaders.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No leaders assigned yet in membership categories", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(leaders, key = { it.id }) { leader ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .background(RoyalPurpleSecondary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = RoyalPurpleSecondary, modifier = Modifier.size(24.dp))
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(leader.fullName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(leader.category, fontSize = 12.sp, color = RoyalPurpleSecondary, fontWeight = FontWeight.SemiBold)
                if (leader.departments.isNotEmpty()) {
                  Text("Departments: ${leader.departments.joinToString(", ")}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }
          }
        }
      }
    }
  }
}

// ==========================================
// 4. BIRTHDAYS & ANNIVERSARIES MODULE
// ==========================================
@Composable
fun BirthdaysScreen(
  uiState: ChurchUiState,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val membersWithBirthdays = uiState.members.filter { it.dateOfBirth.isNotBlank() }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
  ) {
    Text("Birthdays & Anniversaries", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBluePrimary)
    Text("Celebrate milestones and stay connected with congregants", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.height(14.dp))

    if (membersWithBirthdays.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.Cake, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
          Spacer(modifier = Modifier.height(8.dp))
          Text("No birthdays recorded in member profiles", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    } else {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(membersWithBirthdays, key = { it.id }) { member ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE11D48).copy(alpha = 0.12f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Cake, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(member.fullName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                  Text("Birthday: ${member.dateOfBirth}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }

              if (member.phone.isNotBlank()) {
                IconButton(onClick = { initiatePhoneCall(context, member.phone) }) {
                  Icon(Icons.Default.Phone, contentDescription = "Call", tint = StatusActive)
                }
              }
            }
          }
        }
      }
    }
  }
}

// ==========================================
// 5. REPORTS & CSV DATA EXPORT
// ==========================================
@Composable
fun ReportsScreen(
  church: Church?,
  uiState: ChurchUiState,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val totalMembers = uiState.members.size
  val activeCount = uiState.members.count { it.status.equals("Active", ignoreCase = true) }
  val inactiveCount = uiState.members.count { it.status.equals("Inactive", ignoreCase = true) }
  val newConvertsCount = uiState.members.count { it.category.equals("New convert", ignoreCase = true) }
  val baptizedCount = uiState.members.count { it.baptismStatus.equals("Baptized", ignoreCase = true) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text("Reports & Data Export", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBluePrimary)
    Text("Executive summary reports and CSV data export", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

    // Summary Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Demographics & Status Breakdown", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyBluePrimary)
        ReportRow(label = "Total Registered Members", value = "$totalMembers")
        ReportRow(label = "Active Members", value = "$activeCount (${if (totalMembers > 0) (activeCount * 100) / totalMembers else 0}%)")
        ReportRow(label = "Inactive / Transferred Members", value = "$inactiveCount")
        ReportRow(label = "New Converts in Pipeline", value = "$newConvertsCount")
        ReportRow(label = "Water Baptized Members", value = "$baptizedCount")
        ReportRow(label = "Total Ministry Departments", value = "${uiState.departments.size}")
        ReportRow(label = "Logged Attendance Sessions", value = "${uiState.attendanceSessions.size}")
      }
    }

    // Export Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = NavyBluePrimary),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Text("Export Church Member Directory", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Generate a CSV export containing authorized member records for offline backups or spreadsheet analysis.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = {
            exportMembersToCsv(context, church?.name ?: "Church", uiState.members)
          },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyBlueDark),
          modifier = Modifier.fillMaxWidth().testTag("export_csv_button")
        ) {
          Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Export Member Directory (CSV)", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun ReportRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
  }
}

fun exportMembersToCsv(context: Context, churchName: String, members: List<ChurchMember>) {
  val sb = StringBuilder()
  sb.append("Member ID,First Name,Last Name,Gender,Category,Status,Phone,Email,Date Joined,Baptism Status,Departments\n")
  members.forEach { m ->
    val depts = m.departments.joinToString(";")
    sb.append("\"${m.memberId}\",\"${m.firstName}\",\"${m.lastName}\",\"${m.gender}\",\"${m.category}\",\"${m.status}\",\"${m.phone}\",\"${m.email}\",\"${m.dateJoined}\",\"${m.baptismStatus}\",\"$depts\"\n")
  }

  val sendIntent = Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_TEXT, sb.toString())
    putExtra(Intent.EXTRA_SUBJECT, "$churchName - Membership Directory Export.csv")
    type = "text/plain"
  }
  val shareIntent = Intent.createChooser(sendIntent, "Export Membership CSV")
  context.startActivity(shareIntent)
}

// ==========================================
// 6. ROLES & PERMISSIONS GUIDE (RBAC)
// ==========================================
@Composable
fun RolesScreen(onBack: () -> Unit) {
  val roles = listOf(
    "CHURCH OWNER" to "Full access to all church settings, administrator delegation, subscriptions, and financial/audit records.",
    "ADMINISTRATOR" to "Manage members, departments, visitors, attendance sessions, and generate church reports.",
    "PASTOR" to "Full pastoral view of member directory, confidential notes, leadership structures, and church analytics.",
    "DEPARTMENT LEADER" to "View members in assigned department and record department service attendance.",
    "MEMBERSHIP OFFICER" to "Register and update member profiles, follow up visitors, and convert new converts.",
    "ATTENDANCE OFFICER" to "Create and record service attendance sessions.",
    "READ-ONLY USER" to "View authorized member directories and reports without edit capabilities."
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text("Role-Based Access Control (RBAC)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBluePrimary)
    Text("ChurchBase enforces strict permission tiers at the database security level.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.height(4.dp))

    roles.forEach { (role, desc) ->
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(role, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyBluePrimary)
          Spacer(modifier = Modifier.height(4.dp))
          Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
  }
}

// ==========================================
// 7. SUBSCRIPTIONS & COMMERCIAL SAAS
// ==========================================
@Composable
fun SubscriptionScreen(
  church: Church?,
  onBack: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text("Subscription & SaaS Tier", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBluePrimary)
    Text("ChurchBase commercial multi-tenant license status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

    // Current Tier Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = NavyBluePrimary),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Text("Active Tier: ${church?.subscriptionTier ?: "STANDARD"}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Account Status: ${church?.subscriptionStatus ?: "ACTIVE"}", fontSize = 13.sp, color = GoldAccent)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Includes unlimited member records, automated attendance tracking, visitor pipeline, and cloud synchronization.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
      }
    }

    Text("Available ChurchBase Plans", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

    PlanCard(
      title = "FREE TIER",
      features = listOf("Up to 100 members", "Basic member registration", "Standard dashboard", "Community support"),
      isCurrent = church?.subscriptionTier == "FREE"
    )
    PlanCard(
      title = "STANDARD TIER (Current)",
      features = listOf("Unlimited members", "Attendance tracking & session management", "Visitor & new converts pipeline", "Departments & ministry management", "CSV data export"),
      isCurrent = true
    )
    PlanCard(
      title = "PREMIUM ENTERPRISE",
      features = listOf("All Standard features", "Multi-campus church branching", "Dedicated cloud priority", "Granular custom security rules", "24/7 dedicated support"),
      isCurrent = church?.subscriptionTier == "PREMIUM"
    )
  }
}

@Composable
private fun PlanCard(
  title: String,
  features: List<String>,
  isCurrent: Boolean
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isCurrent) RoyalPurpleSecondary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    ),
    border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, RoyalPurpleSecondary) else null,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isCurrent) RoyalPurpleSecondary else MaterialTheme.colorScheme.onSurface)
        if (isCurrent) {
          Surface(shape = RoundedCornerShape(8.dp), color = RoyalPurpleSecondary) {
            Text("Active", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
          }
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      features.forEach { feat ->
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusActive, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(feat, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
  }
}

// ==========================================
// 8. CHURCH PROFILE SETTINGS
// ==========================================
@Composable
fun ChurchSettingsScreen(
  church: Church?,
  viewModel: ChurchViewModel,
  onBack: () -> Unit
) {
  var name by remember { mutableStateOf(church?.name ?: "") }
  var pastor by remember { mutableStateOf(church?.pastor ?: "") }
  var denomination by remember { mutableStateOf(church?.denomination ?: "") }
  var address by remember { mutableStateOf(church?.address ?: "") }
  var phone by remember { mutableStateOf(church?.phone ?: "") }
  var email by remember { mutableStateOf(church?.email ?: "") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text("Church Profile Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBluePrimary)
    Text("Update organization contact details and leadership information", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

    OutlinedTextField(
      value = name,
      onValueChange = { name = it },
      label = { Text("Church Name") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
      value = pastor,
      onValueChange = { pastor = it },
      label = { Text("Senior Pastor") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
      value = denomination,
      onValueChange = { denomination = it },
      label = { Text("Denomination") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
      value = phone,
      onValueChange = { phone = it },
      label = { Text("Official Phone") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
      value = email,
      onValueChange = { email = it },
      label = { Text("Official Email") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
      value = address,
      onValueChange = { address = it },
      label = { Text("Physical Address") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(10.dp))

    Button(
      onClick = {
        if (church != null) {
          val updated = church.copy(
            name = name.trim(),
            pastor = pastor.trim(),
            denomination = denomination.trim(),
            phone = phone.trim(),
            email = email.trim(),
            address = address.trim()
          )
          viewModel.updateChurchSettings(updated) { onBack() }
        }
      },
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
      modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
      Text("Save Changes")
    }
  }
}

// ==========================================
// 9. SECURITY & ACTIVITY AUDIT LOG
// ==========================================
@Composable
fun AuditLogScreen(
  uiState: ChurchUiState,
  onBack: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
  ) {
    Text("Security & Activity Audit Log", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBluePrimary)
    Text("Immutable audit trail of church database actions", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.height(14.dp))

    if (uiState.auditLogs.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No audit events recorded yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(uiState.auditLogs, key = { it.id }) { log ->
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(log.action, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBluePrimary)
                Text(
                  text = log.timestamp?.toDate()?.let { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(it) } ?: "Just now",
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Spacer(modifier = Modifier.height(3.dp))
              Text(log.details, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
              Text("Actor: ${log.performedByName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f))
            }
          }
        }
      }
    }
  }
}
