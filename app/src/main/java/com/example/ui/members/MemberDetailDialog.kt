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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ChurchMember
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyBlueDark
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusArchived

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemberDetailDialog(
  member: ChurchMember,
  onDismiss: () -> Unit,
  onEdit: (ChurchMember) -> Unit,
  onArchive: (ChurchMember) -> Unit,
  onDelete: (ChurchMember) -> Unit
) {
  val context = LocalContext.current
  var showDeleteConfirmation by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .padding(vertical = 24.dp)
        .testTag("member_profile_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Member Profile",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBluePrimary
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Profile Identity Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = NavyBluePrimary),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              val initials = "${member.firstName.firstOrNull() ?: ""}${member.lastName.firstOrNull() ?: ""}".uppercase()
              Text(
                text = initials.ifBlank { "M" },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Text(
                text = member.fullName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "${member.category} • ID: ${member.memberId.ifBlank { "Unassigned" }}",
                fontSize = 12.sp,
                color = GoldAccent
              )
              Spacer(modifier = Modifier.height(6.dp))
              StatusBadge(status = member.status)
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Contact Section
        Text(
          text = "Contact Details",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (member.phone.isNotBlank()) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { initiatePhoneCall(context, member.phone) },
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = StatusActive, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text("Phone", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(member.phone, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = StatusActive.copy(alpha = 0.15f)
                ) {
                  Text("Call", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusActive, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
              }
            } else {
              DetailRow(icon = Icons.Default.Phone, label = "Phone", value = "Not provided")
            }

            if (member.email.isNotBlank()) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { initiateEmail(context, member.email) },
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Email, contentDescription = null, tint = NavyBluePrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text("Email", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(member.email, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = NavyBluePrimary.copy(alpha = 0.1f)
                ) {
                  Text("Send", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyBluePrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
              }
            }

            if (member.residentialAddress.isNotBlank()) {
              DetailRow(
                icon = Icons.Default.LocationOn,
                label = "Address",
                value = listOf(member.residentialAddress, member.city, member.state, member.country).filter { it.isNotBlank() }.joinToString(", ")
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Church & Discipleship Info
        Text(
          text = "Church & Ministry Details",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DetailRow(icon = Icons.Default.Church, label = "Date Joined", value = member.dateJoined.ifBlank { "Not specified" })
            if (member.dateFirstVisited.isNotBlank()) {
              DetailRow(icon = Icons.Default.Church, label = "First Visited", value = member.dateFirstVisited)
            }
            DetailRow(
              icon = Icons.Default.WaterDrop,
              label = "Baptism Status",
              value = if (member.baptismDate.isNotBlank()) "${member.baptismStatus} (${member.baptismDate})" else member.baptismStatus
            )
            if (member.assignedLeader.isNotBlank()) {
              DetailRow(icon = Icons.Default.Church, label = "Assigned Leader", value = member.assignedLeader)
            }
            if (member.departments.isNotEmpty()) {
              Column {
                Text("Departments", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  member.departments.forEach { dept ->
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = NavyBluePrimary.copy(alpha = 0.08f)
                    ) {
                      Text(dept, fontSize = 11.sp, color = NavyBluePrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Personal Information
        Text(
          text = "Personal Information",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DetailRow(icon = Icons.Default.Cake, label = "Birthday", value = member.dateOfBirth.ifBlank { "Not specified" })
            DetailRow(icon = Icons.Default.Cake, label = "Gender & Marital Status", value = "${member.gender} • ${member.maritalStatus}")
            if (member.occupation.isNotBlank()) {
              DetailRow(icon = Icons.Default.Cake, label = "Occupation", value = member.occupation)
            }
            if (member.notes.isNotBlank()) {
              DetailRow(icon = Icons.Default.Notes, label = "Pastoral Notes", value = member.notes)
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons: Edit, Archive, Delete
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = { onEdit(member); onDismiss() },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
            modifier = Modifier.weight(1f).testTag("button_edit_member")
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Edit Profile")
          }

          OutlinedButton(
            onClick = { onArchive(member); onDismiss() },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Archive")
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
          onClick = { showDeleteConfirmation = true },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Delete Member Record")
        }
      }
    }
  }

  // Delete Confirmation Dialog
  if (showDeleteConfirmation) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirmation = false },
      title = { Text("Delete Member Record?") },
      text = { Text("Are you sure you want to permanently delete '${member.fullName}'? This action will be recorded in the church audit log and cannot be undone.") },
      confirmButton = {
        Button(
          onClick = {
            showDeleteConfirmation = false
            onDelete(member)
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirmation = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun DetailRow(
  icon: ImageVector,
  label: String,
  value: String
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(icon, contentDescription = null, tint = NavyBluePrimary, modifier = Modifier.size(18.dp))
    Spacer(modifier = Modifier.width(10.dp))
    Column {
      Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
  }
}
