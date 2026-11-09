package com.example.ui.members

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChurchMember
import com.example.ui.ChurchUiState
import com.example.ui.ChurchViewModel
import com.example.ui.MemberSortOption
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyBlueDark
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.RoyalPurpleSecondary
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusArchived
import com.example.ui.theme.StatusConverts
import com.example.ui.theme.StatusInactive
import com.example.ui.theme.StatusVisiting

@Composable
fun MembersScreen(
  uiState: ChurchUiState,
  viewModel: ChurchViewModel,
  onOpenAddMember: () -> Unit,
  onSelectMember: (ChurchMember) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var isCardView by remember { mutableStateOf(false) }
  var sortMenuExpanded by remember { mutableStateOf(false) }

  // Filter & Search Logic
  val filteredMembers by remember(uiState) {
    derivedStateOf {
      var list = uiState.members

      // Search Query
      if (uiState.searchQuery.isNotBlank()) {
        val q = uiState.searchQuery.trim().lowercase()
        list = list.filter { member ->
          member.fullName.lowercase().contains(q) ||
            member.memberId.lowercase().contains(q) ||
            member.phone.lowercase().contains(q) ||
            member.email.lowercase().contains(q)
        }
      }

      // Status Filter
      if (uiState.selectedStatusFilter != "All") {
        list = list.filter { it.status.equals(uiState.selectedStatusFilter, ignoreCase = true) }
      }

      // Category Filter
      if (uiState.selectedCategoryFilter != "All") {
        list = list.filter { it.category.equals(uiState.selectedCategoryFilter, ignoreCase = true) }
      }

      // Department Filter
      if (uiState.selectedDepartmentFilter != "All") {
        list = list.filter { it.departments.contains(uiState.selectedDepartmentFilter) }
      }

      // Sorting
      when (uiState.sortOption) {
        MemberSortOption.NAME_ASC -> list.sortedBy { it.firstName.lowercase() }
        MemberSortOption.NAME_DESC -> list.sortedByDescending { it.firstName.lowercase() }
        MemberSortOption.NEWEST_JOINED -> list.sortedByDescending { it.dateJoined }
        MemberSortOption.OLDEST_JOINED -> list.sortedBy { it.dateJoined }
      }
    }
  }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = onOpenAddMember,
        containerColor = NavyBluePrimary,
        contentColor = Color.White,
        modifier = Modifier.testTag("fab_add_member")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Member")
      }
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
    ) {
      // Top Search and View Switcher Bar
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search by name, ID, phone, email...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
              if (uiState.searchQuery.isNotBlank()) {
                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("member_search_input")
          )

          Spacer(modifier = Modifier.width(8.dp))

          // Sort Menu Button
          Box {
            IconButton(
              onClick = { sortMenuExpanded = true },
              modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            ) {
              Icon(Icons.Default.FilterList, contentDescription = "Sort Options", tint = NavyBluePrimary)
            }
            DropdownMenu(
              expanded = sortMenuExpanded,
              onDismissRequest = { sortMenuExpanded = false }
            ) {
              MemberSortOption.values().forEach { option ->
                DropdownMenuItem(
                  text = { Text(option.label) },
                  onClick = {
                    viewModel.setSortOption(option)
                    sortMenuExpanded = false
                  }
                )
              }
            }
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Grid vs List view toggle
          IconButton(
            onClick = { isCardView = !isCardView },
            modifier = Modifier
              .size(44.dp)
              .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
          ) {
            Icon(
              imageVector = if (isCardView) Icons.Default.ViewList else Icons.Default.GridView,
              contentDescription = "Toggle View",
              tint = NavyBluePrimary
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Status Filter Chips
        val statusFilters = listOf("All", "Active", "Inactive", "Visiting", "Archived")
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          statusFilters.forEach { status ->
            val isSelected = uiState.selectedStatusFilter == status
            FilterChip(
              selected = isSelected,
              onClick = { viewModel.setStatusFilter(status) },
              label = { Text(status, fontSize = 12.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NavyBluePrimary,
                selectedLabelColor = Color.White
              )
            )
          }
        }
      }

      // Member Count Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${filteredMembers.size} Member${if (filteredMembers.size == 1) "" else "s"}",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = uiState.sortOption.label,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Members List or Empty State
      if (filteredMembers.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              modifier = Modifier.size(64.dp),
              tint = Color.Gray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = if (uiState.searchQuery.isNotBlank()) "No members found for '${uiState.searchQuery}'" else "No members in this category yet",
              fontSize = 15.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Tap the + button below to register a church member",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredMembers, key = { it.id.ifBlank { it.memberId } }) { member ->
            if (isCardView) {
              MemberGridCard(
                member = member,
                onClick = { onSelectMember(member) },
                onCall = { initiatePhoneCall(context, member.phone) },
                onEmail = { initiateEmail(context, member.email) }
              )
            } else {
              MemberListItem(
                member = member,
                onClick = { onSelectMember(member) },
                onCall = { initiatePhoneCall(context, member.phone) },
                onEmail = { initiateEmail(context, member.email) }
              )
            }
          }
          item { Spacer(modifier = Modifier.height(80.dp)) }
        }
      }
    }
  }
}

@Composable
fun MemberListItem(
  member: ChurchMember,
  onClick: () -> Unit,
  onCall: () -> Unit,
  onEmail: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("member_item_${member.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Avatar with Initials
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(NavyBluePrimary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        val initials = "${member.firstName.firstOrNull() ?: ""}${member.lastName.firstOrNull() ?: ""}".uppercase()
        Text(
          text = initials.ifBlank { "M" },
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = NavyBluePrimary
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = member.fullName,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          if (member.memberId.isNotBlank()) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = NavyBluePrimary.copy(alpha = 0.08f)
            ) {
              Text(
                text = member.memberId,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = NavyBluePrimary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Status Pill
          StatusBadge(status = member.status)

          // Category
          Text(
            text = member.category,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        if (member.phone.isNotBlank() || member.email.isNotBlank()) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = if (member.phone.isNotBlank()) member.phone else member.email,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
          )
        }
      }

      // Quick contact buttons
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (member.phone.isNotBlank()) {
          IconButton(
            onClick = onCall,
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.Default.Phone, contentDescription = "Call", tint = StatusActive, modifier = Modifier.size(18.dp))
          }
        }
        if (member.email.isNotBlank()) {
          IconButton(
            onClick = onEmail,
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.Default.Email, contentDescription = "Email", tint = NavyBluePrimary, modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  }
}

@Composable
fun MemberGridCard(
  member: ChurchMember,
  onClick: () -> Unit,
  onCall: () -> Unit,
  onEmail: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(NavyBluePrimary.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          val initials = "${member.firstName.firstOrNull() ?: ""}${member.lastName.firstOrNull() ?: ""}".uppercase()
          Text(
            text = initials.ifBlank { "M" },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBluePrimary
          )
        }
        StatusBadge(status = member.status)
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = member.fullName,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "${member.category} • ${member.memberId}",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (member.departments.isNotEmpty()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Depts: ${member.departments.joinToString(", ")}",
          fontSize = 11.sp,
          color = NavyBluePrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        if (member.phone.isNotBlank()) {
          IconButton(onClick = onCall, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Phone, contentDescription = "Call", tint = StatusActive, modifier = Modifier.size(18.dp))
          }
        }
        if (member.email.isNotBlank()) {
          IconButton(onClick = onEmail, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Email, contentDescription = "Email", tint = NavyBluePrimary, modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  }
}

@Composable
fun StatusBadge(status: String) {
  val (bgColor, textColor) = when (status.lowercase()) {
    "active" -> StatusActive.copy(alpha = 0.15f) to StatusActive
    "inactive" -> StatusInactive.copy(alpha = 0.15f) to StatusInactive
    "visiting" -> StatusVisiting.copy(alpha = 0.15f) to StatusVisiting
    "archived", "deceased" -> StatusArchived.copy(alpha = 0.15f) to StatusArchived
    else -> Color.Gray.copy(alpha = 0.15f) to Color.DarkGray
  }

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = bgColor
  ) {
    Text(
      text = status,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      color = textColor,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
    )
  }
}

fun initiatePhoneCall(context: Context, phone: String) {
  if (phone.isBlank()) return
  try {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.trim()}"))
    context.startActivity(intent)
  } catch (e: Exception) {
    // Graceful catch if dialer not present
  }
}

fun initiateEmail(context: Context, email: String) {
  if (email.isBlank()) return
  try {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${email.trim()}"))
    context.startActivity(intent)
  } catch (e: Exception) {
    // Graceful catch if mail client not present
  }
}
