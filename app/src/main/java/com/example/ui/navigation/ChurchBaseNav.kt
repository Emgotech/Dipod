package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Church
import com.example.data.model.ChurchMember
import com.example.data.model.PlatformUser
import com.example.data.model.Visitor
import com.example.ui.ChurchViewModel
import com.example.ui.attendance.AttendanceScreen
import com.example.ui.attendance.RecordAttendanceDialog
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.departments.DepartmentsScreen
import com.example.ui.members.AddEditMemberDialog
import com.example.ui.more.AddVisitorDialog
import com.example.ui.members.MemberDetailDialog
import com.example.ui.members.MembersScreen
import com.example.ui.more.AuditLogScreen
import com.example.ui.more.BirthdaysScreen
import com.example.ui.more.ChurchSettingsScreen
import com.example.ui.more.LeadershipScreen
import com.example.ui.more.MoreHubScreen
import com.example.ui.more.NewConvertsScreen
import com.example.ui.more.ReportsScreen
import com.example.ui.more.RolesScreen
import com.example.ui.more.SubscriptionScreen
import com.example.ui.more.VisitorsScreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyBlueDark
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.RoyalPurpleSecondary

sealed class NavTab(val index: Int, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
  object Dashboard : NavTab(0, "Dashboard", Icons.Default.Dashboard)
  object Members : NavTab(1, "Members", Icons.Default.Group)
  object Attendance : NavTab(2, "Attendance", Icons.Default.HowToReg)
  object Departments : NavTab(3, "Departments", Icons.Default.Church)
  object More : NavTab(4, "More", Icons.Default.MoreHoriz)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChurchBaseAppContent(
  church: Church?,
  platformUser: PlatformUser?,
  viewModel: ChurchViewModel,
  onSignOut: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()
  var currentTabIndex by remember { mutableIntStateOf(0) }
  var currentSubscreen by remember { mutableStateOf<String?>(null) }

  // Dialog states
  var showAddMemberDialog by remember { mutableStateOf(false) }
  var memberToEdit by remember { mutableStateOf<ChurchMember?>(null) }
  var selectedMemberForDetail by remember { mutableStateOf<ChurchMember?>(null) }
  var showRecordAttendanceDialog by remember { mutableStateOf(false) }
  var showAddVisitorDialog by remember { mutableStateOf(false) }

  val snackbarHostState = remember { SnackbarHostState() }

  // Display user messages via Snackbar
  LaunchedEffect(uiState.userMessage) {
    uiState.userMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearUserMessage()
    }
  }

  // Handle system back button for sub-screens
  BackHandler(enabled = currentSubscreen != null) {
    currentSubscreen = null
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      if (currentSubscreen != null) {
        TopAppBar(
          title = {
            Text(
              text = when (currentSubscreen) {
                "visitors" -> "Visitors & Follow-up"
                "converts" -> "New Converts"
                "leadership" -> "Leadership Directory"
                "birthdays" -> "Birthdays & Anniversaries"
                "reports" -> "Reports & Export"
                "roles" -> "Roles & Permissions"
                "subscription" -> "SaaS Plans"
                "settings" -> "Church Settings"
                "audit" -> "Security Audit Log"
                else -> "ChurchBase"
              },
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
          },
          navigationIcon = {
            IconButton(onClick = { currentSubscreen = null }) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = NavyBluePrimary,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
          )
        )
      }
    },
    bottomBar = {
      if (currentSubscreen == null) {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          tonalElevation = 8.dp
        ) {
          listOf(NavTab.Dashboard, NavTab.Members, NavTab.Attendance, NavTab.Departments, NavTab.More).forEach { tab ->
            val isSelected = currentTabIndex == tab.index
            NavigationBarItem(
              selected = isSelected,
              onClick = { currentTabIndex = tab.index },
              icon = {
                Icon(
                  imageVector = tab.icon,
                  contentDescription = tab.title,
                  modifier = Modifier.size(24.dp)
                )
              },
              label = {
                Text(
                  text = tab.title,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyBluePrimary,
                selectedTextColor = NavyBluePrimary,
                indicatorColor = NavyBluePrimary.copy(alpha = 0.12f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
              ),
              modifier = Modifier.testTag("nav_tab_${tab.title.lowercase()}")
            )
          }
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      if (currentSubscreen != null) {
        when (currentSubscreen) {
          "visitors" -> VisitorsScreen(uiState = uiState, viewModel = viewModel, onBack = { currentSubscreen = null })
          "converts" -> NewConvertsScreen(uiState = uiState, onBack = { currentSubscreen = null })
          "leadership" -> LeadershipScreen(uiState = uiState, onBack = { currentSubscreen = null })
          "birthdays" -> BirthdaysScreen(uiState = uiState, onBack = { currentSubscreen = null })
          "reports" -> ReportsScreen(church = church, uiState = uiState, onBack = { currentSubscreen = null })
          "roles" -> RolesScreen(onBack = { currentSubscreen = null })
          "subscription" -> SubscriptionScreen(church = church, onBack = { currentSubscreen = null })
          "settings" -> ChurchSettingsScreen(church = church, viewModel = viewModel, onBack = { currentSubscreen = null })
          "audit" -> AuditLogScreen(uiState = uiState, onBack = { currentSubscreen = null })
        }
      } else {
        when (currentTabIndex) {
          0 -> DashboardScreen(
            church = church,
            uiState = uiState,
            onNavigateToTab = { currentTabIndex = it },
            onOpenAddMember = { showAddMemberDialog = true },
            onOpenAddVisitor = { showAddVisitorDialog = true },
            onOpenRecordAttendance = { showRecordAttendanceDialog = true },
            onOpenSubscreen = { currentSubscreen = it }
          )
          1 -> MembersScreen(
            uiState = uiState,
            viewModel = viewModel,
            onOpenAddMember = { showAddMemberDialog = true },
            onSelectMember = { selectedMemberForDetail = it }
          )
          2 -> AttendanceScreen(
            uiState = uiState,
            viewModel = viewModel,
            onOpenRecordAttendance = { showRecordAttendanceDialog = true }
          )
          3 -> DepartmentsScreen(
            uiState = uiState,
            viewModel = viewModel
          )
          4 -> MoreHubScreen(
            church = church,
            platformUser = platformUser,
            uiState = uiState,
            onOpenSubscreen = { currentSubscreen = it },
            onSignOut = onSignOut
          )
        }
      }
    }
  }

  // Add / Edit Member Dialog
  if (showAddMemberDialog || memberToEdit != null) {
    val editing = memberToEdit
    AddEditMemberDialog(
      memberToEdit = editing,
      departments = uiState.departments,
      onDismiss = {
        showAddMemberDialog = false
        memberToEdit = null
      },
      onSave = { savedMember ->
        if (editing == null) {
          viewModel.addMember(savedMember)
        } else {
          viewModel.updateMember(savedMember)
        }
        showAddMemberDialog = false
        memberToEdit = null
      }
    )
  }

  // Member Detail Dialog
  if (selectedMemberForDetail != null) {
    val member = selectedMemberForDetail!!
    MemberDetailDialog(
      member = member,
      onDismiss = { selectedMemberForDetail = null },
      onEdit = {
        memberToEdit = it
        selectedMemberForDetail = null
      },
      onArchive = {
        viewModel.archiveMember(it)
        selectedMemberForDetail = null
      },
      onDelete = {
        viewModel.deleteMember(it.id, it.fullName)
        selectedMemberForDetail = null
      }
    )
  }

  // Record Attendance Dialog
  if (showRecordAttendanceDialog) {
    RecordAttendanceDialog(
      members = uiState.members,
      onDismiss = { showRecordAttendanceDialog = false },
      onSave = { session, records ->
        viewModel.recordAttendance(session, records)
        showRecordAttendanceDialog = false
      }
    )
  }

  // Add Visitor Dialog
  if (showAddVisitorDialog) {
    AddVisitorDialog(
      onDismiss = { showAddVisitorDialog = false },
      onSave = { visitor ->
        viewModel.addVisitor(visitor)
        showAddVisitorDialog = false
      }
    )
  }
}
