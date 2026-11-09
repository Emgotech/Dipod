package com.example.ui.attendance

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.ChurchMember
import com.example.ui.ChurchUiState
import com.example.ui.ChurchViewModel
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyBlueDark
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.RoyalPurpleSecondary
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusArchived
import com.example.ui.theme.StatusVisiting
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceScreen(
  uiState: ChurchUiState,
  viewModel: ChurchViewModel,
  onOpenRecordAttendance: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedSessionForDetail by remember { mutableStateOf<AttendanceSession?>(null) }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = onOpenRecordAttendance,
        containerColor = RoyalPurpleSecondary,
        contentColor = Color.White,
        modifier = Modifier.testTag("fab_record_attendance")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Record Attendance")
      }
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
        .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
      Text(
        text = "Attendance Sessions",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = "Track service, meeting, and program participation",
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(14.dp))

      if (uiState.attendanceSessions.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.EventNote,
              contentDescription = null,
              modifier = Modifier.size(64.dp),
              tint = Color.Gray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No attendance sessions recorded yet",
              fontSize = 15.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Tap + to create a session and mark attendance",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(uiState.attendanceSessions, key = { it.id }) { session ->
            AttendanceSessionCard(
              session = session,
              onClick = { selectedSessionForDetail = session }
            )
          }
          item { Spacer(modifier = Modifier.height(80.dp)) }
        }
      }
    }
  }

  // Session Detail Dialog
  if (selectedSessionForDetail != null) {
    val session = selectedSessionForDetail!!
    Dialog(onDismissRequest = { selectedSessionForDetail = null }) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(session.serviceType, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyBluePrimary)
            IconButton(onClick = { selectedSessionForDetail = null }) {
              Icon(Icons.Default.Close, contentDescription = "Close")
            }
          }
          Text("Date: ${session.date} • ${session.title.ifBlank { "Regular Service" }}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(14.dp))

          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              StatColumn(count = session.totalPresent.toString(), label = "Present", color = StatusActive)
              StatColumn(count = session.totalAbsent.toString(), label = "Absent", color = StatusArchived)
              StatColumn(count = session.totalVisitors.toString(), label = "Visitors", color = StatusVisiting)
            }
          }

          if (session.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Notes: ${session.notes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = { selectedSessionForDetail = null },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Close")
          }
        }
      }
    }
  }
}

@Composable
fun AttendanceSessionCard(
  session: AttendanceSession,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(RoyalPurpleSecondary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.EventNote, contentDescription = null, tint = RoyalPurpleSecondary, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(session.serviceType, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(session.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = StatusActive.copy(alpha = 0.15f)
        ) {
          Text(
            text = "${session.totalPresent} Present",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = StatusActive,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }

      if (session.title.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Theme: ${session.title}", fontSize = 12.sp, color = NavyBluePrimary, fontWeight = FontWeight.Medium)
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text("Absent: ${session.totalAbsent}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Visitors: ${session.totalVisitors}", fontSize = 11.sp, color = StatusVisiting)
        if (session.departmentName.isNotBlank()) {
          Text("Dept: ${session.departmentName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
  }
}

@Composable
private fun StatColumn(count: String, label: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(count, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
    Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordAttendanceDialog(
  members: List<ChurchMember>,
  onDismiss: () -> Unit,
  onSave: (AttendanceSession, List<AttendanceRecord>) -> Unit
) {
  val serviceTypes = listOf(
    "Sunday service", "Midweek service", "Prayer meeting", "Bible study",
    "Special program", "Department meeting", "Custom service"
  )
  var serviceType by remember { mutableStateOf(serviceTypes[0]) }
  var typeExpanded by remember { mutableStateOf(false) }

  var date by remember {
    mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
  }
  var title by remember { mutableStateOf("") }
  var visitorsCountStr by remember { mutableStateOf("0") }
  var sessionNotes by remember { mutableStateOf("") }
  var searchQuery by remember { mutableStateOf("") }

  // Member attendance map: memberId -> status ("Present", "Absent", "Excused")
  val attendanceMap = remember {
    mutableStateMapOf<String, String>().apply {
      members.forEach { m -> put(m.id, "Present") } // Default to Present for quick scanning
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth(0.96f)
        .padding(vertical = 16.dp)
        .testTag("record_attendance_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Record Service Attendance", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyBluePrimary)
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Session Metadata Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ExposedDropdownMenuBox(
              expanded = typeExpanded,
              onExpandedChange = { typeExpanded = it },
              modifier = Modifier.fillMaxWidth()
            ) {
              OutlinedTextField(
                value = serviceType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Service Type *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
              )
              ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
              ) {
                serviceTypes.forEach { opt ->
                  DropdownMenuItem(text = { Text(opt) }, onClick = { serviceType = opt; typeExpanded = false })
                }
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
              )
              OutlinedTextField(
                value = visitorsCountStr,
                onValueChange = { visitorsCountStr = it.filter { ch -> ch.isDigit() } },
                label = { Text("Visitors Count") },
                singleLine = true,
                modifier = Modifier.weight(1f)
              )
            }

            OutlinedTextField(
              value = title,
              onValueChange = { title = it },
              label = { Text("Sermon / Program Title (Optional)") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Member Checklist Header & Search
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Mark Members", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
          val presentCount = attendanceMap.values.count { it == "Present" }
          Text("$presentCount / ${members.size} Present", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusActive)
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Filter members list...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Members List with Present / Absent toggles
        val visibleMembers = if (searchQuery.isBlank()) members else members.filter {
          it.fullName.lowercase().contains(searchQuery.lowercase()) || it.memberId.lowercase().contains(searchQuery.lowercase())
        }

        LazyColumn(
          modifier = Modifier.weight(1f, fill = false).height(240.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(visibleMembers, key = { it.id }) { member ->
            val status = attendanceMap[member.id] ?: "Present"
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(member.fullName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("${member.category} • ${member.memberId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Present Button
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (status == "Present") StatusActive else Color.Transparent,
                  border = if (status != "Present") androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) else null,
                  modifier = Modifier.clickable { attendanceMap[member.id] = "Present" }
                ) {
                  Text(
                    text = "Present",
                    color = if (status == "Present") Color.White else MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }

                // Absent Button
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (status == "Absent") StatusArchived else Color.Transparent,
                  border = if (status != "Absent") androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) else null,
                  modifier = Modifier.clickable { attendanceMap[member.id] = "Absent" }
                ) {
                  Text(
                    text = "Absent",
                    color = if (status == "Absent") Color.White else MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Save & Cancel Actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
            Text("Cancel")
          }

          Button(
            onClick = {
              val visitorsCount = visitorsCountStr.toLongOrNull() ?: 0L
              val session = AttendanceSession(
                serviceType = serviceType,
                date = date,
                title = title.trim(),
                totalVisitors = visitorsCount,
                notes = sessionNotes.trim()
              )

              val records = members.map { m ->
                AttendanceRecord(
                  memberId = m.id,
                  memberName = m.fullName,
                  status = attendanceMap[m.id] ?: "Present"
                )
              }

              onSave(session, records)
              onDismiss()
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RoyalPurpleSecondary),
            modifier = Modifier.weight(1f).testTag("save_attendance_button")
          ) {
            Text("Save Session")
          }
        }
      }
    }
  }
}
