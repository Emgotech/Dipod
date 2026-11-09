package com.example.ui.departments

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Department
import com.example.ui.ChurchUiState
import com.example.ui.ChurchViewModel
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyBlueDark
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.RoyalPurpleSecondary
import com.example.ui.theme.StatusActive

@Composable
fun DepartmentsScreen(
  uiState: ChurchUiState,
  viewModel: ChurchViewModel,
  modifier: Modifier = Modifier
) {
  var showAddDialog by remember { mutableStateOf(false) }
  var departmentToEdit by remember { mutableStateOf<Department?>(null) }
  var departmentToDelete by remember { mutableStateOf<Department?>(null) }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = NavyBluePrimary,
        contentColor = Color.White,
        modifier = Modifier.testTag("fab_add_department")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Department")
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
        text = "Departments & Ministries",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = "Organize church ministry teams, leadership, and workers",
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(14.dp))

      if (uiState.departments.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Church,
              contentDescription = null,
              modifier = Modifier.size(64.dp),
              tint = Color.Gray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No departments created yet",
              fontSize = 15.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Tap + to add a church ministry or department",
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
          items(uiState.departments, key = { it.id }) { dept ->
            // Count actual members belonging to this department
            val actualCount = uiState.members.count { it.departments.contains(dept.name) }
            DepartmentCard(
              department = dept,
              actualMemberCount = actualCount,
              onEdit = { departmentToEdit = dept },
              onDelete = { departmentToDelete = dept }
            )
          }
          item { Spacer(modifier = Modifier.height(80.dp)) }
        }
      }
    }
  }

  // Add / Edit Department Dialog
  if (showAddDialog || departmentToEdit != null) {
    val editing = departmentToEdit
    AddEditDepartmentDialog(
      departmentToEdit = editing,
      onDismiss = {
        showAddDialog = false
        departmentToEdit = null
      },
      onSave = { savedDept ->
        if (editing == null) {
          viewModel.addDepartment(savedDept)
        } else {
          viewModel.updateDepartment(savedDept)
        }
        showAddDialog = false
        departmentToEdit = null
      }
    )
  }

  // Delete Department Confirmation Dialog
  if (departmentToDelete != null) {
    val dept = departmentToDelete!!
    AlertDialog(
      onDismissRequest = { departmentToDelete = null },
      title = { Text("Delete Department?") },
      text = { Text("Are you sure you want to delete '${dept.name}'? Existing member records will retain their history.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteDepartment(dept.id)
            departmentToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { departmentToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun DepartmentCard(
  department: Department,
  actualMemberCount: Int,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth().testTag("department_card_${department.id}")
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
              .size(42.dp)
              .clip(CircleShape)
              .background(NavyBluePrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Church, contentDescription = null, tint = NavyBluePrimary, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(department.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (department.active) StatusActive.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
            ) {
              Text(
                text = if (department.active) "Active" else "Inactive",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (department.active) StatusActive else Color.Gray,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = RoyalPurpleSecondary.copy(alpha = 0.1f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Group, contentDescription = null, tint = RoyalPurpleSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "$actualMemberCount Members",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = RoyalPurpleSecondary
            )
          }
        }
      }

      if (department.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(department.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      if (department.leaderName.isNotBlank() || department.assistantLeaderName.isNotBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Person, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Leader: ${department.leaderName}${if (department.assistantLeaderName.isNotBlank()) " • Asst: ${department.assistantLeaderName}" else ""}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
          Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NavyBluePrimary, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}

@Composable
fun AddEditDepartmentDialog(
  departmentToEdit: Department?,
  onDismiss: () -> Unit,
  onSave: (Department) -> Unit
) {
  var name by remember { mutableStateOf(departmentToEdit?.name ?: "") }
  var description by remember { mutableStateOf(departmentToEdit?.description ?: "") }
  var leaderName by remember { mutableStateOf(departmentToEdit?.leaderName ?: "") }
  var assistantLeaderName by remember { mutableStateOf(departmentToEdit?.assistantLeaderName ?: "") }
  var active by remember { mutableStateOf(departmentToEdit?.active ?: true) }
  var validationError by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (departmentToEdit == null) "Create Department" else "Edit Department",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBluePrimary
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        OutlinedTextField(
          value = name,
          onValueChange = { name = it; validationError = null },
          label = { Text("Department Name *") },
          placeholder = { Text("e.g. Media & Technical Ministry") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("department_name_input")
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description") },
          minLines = 2,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = leaderName,
          onValueChange = { leaderName = it },
          label = { Text("Department Leader (Optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = assistantLeaderName,
          onValueChange = { assistantLeaderName = it },
          label = { Text("Assistant Leader (Optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Department Active Status", fontSize = 13.sp, fontWeight = FontWeight.Medium)
          Switch(
            checked = active,
            onCheckedChange = { active = it },
            colors = SwitchDefaults.colors(checkedThumbColor = NavyBluePrimary)
          )
        }

        if (validationError != null) {
          Text(validationError ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
            Text("Cancel")
          }
          Button(
            onClick = {
              if (name.isBlank()) {
                validationError = "Please enter department name"
                return@Button
              }
              val updated = (departmentToEdit ?: Department()).copy(
                name = name.trim(),
                description = description.trim(),
                leaderName = leaderName.trim(),
                assistantLeaderName = assistantLeaderName.trim(),
                active = active
              )
              onSave(updated)
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
            modifier = Modifier.weight(1f).testTag("save_department_button")
          ) {
            Text(if (departmentToEdit == null) "Create" else "Save")
          }
        }
      }
    }
  }
}
