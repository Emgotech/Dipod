package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Church
import com.example.ui.ChurchUiState
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyBlueDark
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.RoyalPurpleSecondary
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusConverts
import com.example.ui.theme.StatusVisiting
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
  church: Church?,
  uiState: ChurchUiState,
  onNavigateToTab: (Int) -> Unit,
  onOpenAddMember: () -> Unit,
  onOpenAddVisitor: () -> Unit,
  onOpenRecordAttendance: () -> Unit,
  onOpenSubscreen: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()
  val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
  val todayFormatted = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())

  // Dynamic Metrics from real church database
  val totalMembers = uiState.members.size
  val activeMembers = uiState.members.count { it.status.equals("Active", ignoreCase = true) }
  val inactiveMembers = uiState.members.count { it.status.equals("Inactive", ignoreCase = true) }
  val newConverts = uiState.members.count { it.category.equals("New convert", ignoreCase = true) }
  val leadersCount = uiState.members.count {
    val cat = it.category.lowercase()
    cat.contains("pastor") || cat.contains("elder") || cat.contains("deacon") || cat.contains("leader") || cat.contains("minister")
  }
  val firstTimeVisitors = uiState.visitors.size
  val totalDepartments = uiState.departments.size

  // Today's attendance
  val todaySession = uiState.attendanceSessions.firstOrNull { it.date == todayDate }
  val todayAttendanceCount = todaySession?.totalPresent ?: 0L

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Church Welcome Header
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = NavyBluePrimary),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
      modifier = Modifier.fillMaxWidth().testTag("dashboard_header_card")
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Church, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = church?.name ?: "ChurchBase Assembly",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = if (!church?.pastor.isNullOrBlank()) "Pastor: ${church?.pastor}" else "Church Administration",
              fontSize = 12.sp,
              color = GoldAccent
            )
          }
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.15f)
          ) {
            Text(
              text = church?.subscriptionTier ?: "STANDARD",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = todayFormatted,
          fontSize = 12.sp,
          color = Color.White.copy(alpha = 0.8f)
        )
      }
    }

    // Quick Action Section
    Text(
      text = "Quick Actions",
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      QuickActionButton(
        icon = Icons.Default.PersonAdd,
        label = "Add Member",
        color = NavyBluePrimary,
        onClick = onOpenAddMember,
        modifier = Modifier.weight(1f).testTag("quick_action_add_member")
      )
      QuickActionButton(
        icon = Icons.Default.HowToReg,
        label = "Record Attendance",
        color = RoyalPurpleSecondary,
        onClick = onOpenRecordAttendance,
        modifier = Modifier.weight(1f).testTag("quick_action_record_attendance")
      )
      QuickActionButton(
        icon = Icons.Default.PersonOutline,
        label = "Add Visitor",
        color = Color(0xFF2563EB),
        onClick = onOpenAddVisitor,
        modifier = Modifier.weight(1f)
      )
      QuickActionButton(
        icon = Icons.Default.Assessment,
        label = "Reports",
        color = Color(0xFF0D9488),
        onClick = { onOpenSubscreen("reports") },
        modifier = Modifier.weight(1f)
      )
    }

    // Key Performance Metrics Grid
    Text(
      text = "Membership Statistics",
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      MetricCard(
        title = "Total Members",
        count = totalMembers.toString(),
        subtitle = "Registered database",
        icon = Icons.Default.Group,
        iconTint = NavyBluePrimary,
        modifier = Modifier.weight(1f).clickable { onNavigateToTab(1) }
      )
      MetricCard(
        title = "Active Members",
        count = activeMembers.toString(),
        subtitle = if (totalMembers > 0) "${(activeMembers * 100) / totalMembers}% of total" else "0%",
        icon = Icons.Default.CheckCircle,
        iconTint = StatusActive,
        modifier = Modifier.weight(1f).clickable { onNavigateToTab(1) }
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      MetricCard(
        title = "New Converts",
        count = newConverts.toString(),
        subtitle = "Discipleship pipeline",
        icon = Icons.Default.MilitaryTech,
        iconTint = StatusConverts,
        modifier = Modifier.weight(1f).clickable { onOpenSubscreen("converts") }
      )
      MetricCard(
        title = "First-time Visitors",
        count = firstTimeVisitors.toString(),
        subtitle = "Follow-up tracking",
        icon = Icons.Default.PersonOutline,
        iconTint = StatusVisiting,
        modifier = Modifier.weight(1f).clickable { onOpenSubscreen("visitors") }
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      MetricCard(
        title = "Pastors & Leaders",
        count = leadersCount.toString(),
        subtitle = "Active ministry staff",
        icon = Icons.Default.SupervisorAccount,
        iconTint = RoyalPurpleSecondary,
        modifier = Modifier.weight(1f).clickable { onOpenSubscreen("leadership") }
      )
      MetricCard(
        title = "Departments",
        count = totalDepartments.toString(),
        subtitle = "Active ministries",
        icon = Icons.Default.Church,
        iconTint = GoldAccent,
        modifier = Modifier.weight(1f).clickable { onNavigateToTab(3) }
      )
    }

    // Today's Attendance Banner Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(if (todayAttendanceCount > 0) StatusActive.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.HowToReg,
            contentDescription = null,
            tint = if (todayAttendanceCount > 0) StatusActive else Color.Gray
          )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Today's Service Attendance",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (todayAttendanceCount > 0) "$todayAttendanceCount members attended today" else "No attendance recorded yet for today",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = NavyBluePrimary.copy(alpha = 0.08f),
          modifier = Modifier.clickable { onNavigateToTab(2) }
        ) {
          Text(
            text = if (todayAttendanceCount > 0) "View" else "Record",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavyBluePrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }
      }
    }

    // Additional Highlights
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.weight(1f).clickable { onOpenSubscreen("birthdays") }
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Cake, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(22.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text("Birthdays", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("View calendar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }

      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.weight(1f).clickable { onOpenSubscreen("audit") }
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Assessment, contentDescription = null, tint = NavyBluePrimary, modifier = Modifier.size(22.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text("Audit Log", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("${uiState.auditLogs.size} events", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
  }
}

@Composable
private fun QuickActionButton(
  icon: ImageVector,
  label: String,
  color: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = color.copy(alpha = 0.1f),
    modifier = modifier.clickable(onClick = onClick)
  ) {
    Column(
      modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(color),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        maxLines = 1
      )
    }
  }
}

@Composable
private fun MetricCard(
  title: String,
  count: String,
  subtitle: String,
  icon: ImageVector,
  iconTint: Color,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(iconTint.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = count,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
