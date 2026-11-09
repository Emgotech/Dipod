package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.AuditLog
import com.example.data.model.Church
import com.example.data.model.ChurchMember
import com.example.data.model.Department
import com.example.data.model.Visitor
import com.example.data.repository.ChurchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChurchUiState(
  val members: List<ChurchMember> = emptyList(),
  val visitors: List<Visitor> = emptyList(),
  val departments: List<Department> = emptyList(),
  val attendanceSessions: List<AttendanceSession> = emptyList(),
  val auditLogs: List<AuditLog> = emptyList(),
  val isLoading: Boolean = false,
  val userMessage: String? = null,
  val searchQuery: String = "",
  val selectedCategoryFilter: String = "All",
  val selectedStatusFilter: String = "All",
  val selectedDepartmentFilter: String = "All",
  val sortOption: MemberSortOption = MemberSortOption.NAME_ASC
)

enum class MemberSortOption(val label: String) {
  NAME_ASC("Name (A-Z)"),
  NAME_DESC("Name (Z-A)"),
  NEWEST_JOINED("Newest Joined"),
  OLDEST_JOINED("Oldest Joined")
}

class ChurchViewModel(
  private val repository: ChurchRepository,
  private val churchId: String
) : ViewModel() {

  private val _uiState = MutableStateFlow(ChurchUiState(isLoading = true))
  val uiState: StateFlow<ChurchUiState> = _uiState.asStateFlow()

  init {
    loadChurchData()
  }

  fun loadChurchData() {
    viewModelScope.launch {
      // 1. Members
      launch {
        repository.observeMembers(churchId)
          .catch { e -> _uiState.value = _uiState.value.copy(userMessage = "Failed to load members: ${e.message}") }
          .collect { list ->
            _uiState.value = _uiState.value.copy(members = list, isLoading = false)
          }
      }

      // 2. Visitors
      launch {
        repository.observeVisitors(churchId)
          .catch { e -> _uiState.value = _uiState.value.copy(userMessage = "Failed to load visitors: ${e.message}") }
          .collect { list ->
            _uiState.value = _uiState.value.copy(visitors = list)
          }
      }

      // 3. Departments
      launch {
        repository.observeDepartments(churchId)
          .catch { e -> _uiState.value = _uiState.value.copy(userMessage = "Failed to load departments: ${e.message}") }
          .collect { list ->
            _uiState.value = _uiState.value.copy(departments = list)
          }
      }

      // 4. Attendance
      launch {
        repository.observeAttendanceSessions(churchId)
          .catch { e -> _uiState.value = _uiState.value.copy(userMessage = "Failed to load attendance: ${e.message}") }
          .collect { list ->
            _uiState.value = _uiState.value.copy(attendanceSessions = list)
          }
      }

      // 5. Audit Logs
      launch {
        repository.observeAuditLogs(churchId)
          .catch { /* non-critical */ }
          .collect { list ->
            _uiState.value = _uiState.value.copy(auditLogs = list.sortedByDescending { it.timestamp?.seconds ?: 0 })
          }
      }
    }
  }

  fun setSearchQuery(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
  }

  fun setCategoryFilter(category: String) {
    _uiState.value = _uiState.value.copy(selectedCategoryFilter = category)
  }

  fun setStatusFilter(status: String) {
    _uiState.value = _uiState.value.copy(selectedStatusFilter = status)
  }

  fun setDepartmentFilter(department: String) {
    _uiState.value = _uiState.value.copy(selectedDepartmentFilter = department)
  }

  fun setSortOption(sortOption: MemberSortOption) {
    _uiState.value = _uiState.value.copy(sortOption = sortOption)
  }

  fun clearUserMessage() {
    _uiState.value = _uiState.value.copy(userMessage = null)
  }

  // --- Member Actions ---
  fun addMember(member: ChurchMember, onComplete: (Boolean) -> Unit = {}) {
    viewModelScope.launch {
      val result = repository.addMember(churchId, member)
      if (result.isSuccess) {
        _uiState.value = _uiState.value.copy(userMessage = "Member added successfully")
        onComplete(true)
      } else {
        _uiState.value = _uiState.value.copy(userMessage = "Error adding member: ${result.exceptionOrNull()?.localizedMessage}")
        onComplete(false)
      }
    }
  }

  fun updateMember(member: ChurchMember, onComplete: (Boolean) -> Unit = {}) {
    viewModelScope.launch {
      val result = repository.updateMember(churchId, member)
      if (result.isSuccess) {
        _uiState.value = _uiState.value.copy(userMessage = "Member updated successfully")
        onComplete(true)
      } else {
        _uiState.value = _uiState.value.copy(userMessage = "Error updating member: ${result.exceptionOrNull()?.localizedMessage}")
        onComplete(false)
      }
    }
  }

  fun archiveMember(member: ChurchMember) {
    updateMember(member.copy(status = "Archived"))
  }

  fun deleteMember(memberId: String, memberName: String, onComplete: (Boolean) -> Unit = {}) {
    viewModelScope.launch {
      val result = repository.deleteMember(churchId, memberId, memberName)
      if (result.isSuccess) {
        _uiState.value = _uiState.value.copy(userMessage = "Member deleted")
        onComplete(true)
      } else {
        _uiState.value = _uiState.value.copy(userMessage = "Error deleting member: ${result.exceptionOrNull()?.localizedMessage}")
        onComplete(false)
      }
    }
  }

  // --- Visitor Actions ---
  fun addVisitor(visitor: Visitor, onComplete: (Boolean) -> Unit = {}) {
    viewModelScope.launch {
      val result = repository.addVisitor(churchId, visitor)
      if (result.isSuccess) {
        _uiState.value = _uiState.value.copy(userMessage = "Visitor recorded successfully")
        onComplete(true)
      } else {
        _uiState.value = _uiState.value.copy(userMessage = "Error: ${result.exceptionOrNull()?.localizedMessage}")
        onComplete(false)
      }
    }
  }

  fun updateVisitor(visitor: Visitor, onComplete: (Boolean) -> Unit = {}) {
    viewModelScope.launch {
      val result = repository.updateVisitor(churchId, visitor)
      if (result.isSuccess) {
        _uiState.value = _uiState.value.copy(userMessage = "Visitor status updated")
        onComplete(true)
      } else {
        _uiState.value = _uiState.value.copy(userMessage = "Error: ${result.exceptionOrNull()?.localizedMessage}")
        onComplete(false)
      }
    }
  }

  fun convertVisitorToMember(visitor: Visitor, category: String = "New member", onComplete: (Boolean) -> Unit = {}) {
    viewModelScope.launch {
      val result = repository.convertVisitorToMember(churchId, visitor, category)
      if (result.isSuccess) {
        _uiState.value = _uiState.value.copy(userMessage = "Converted ${visitor.name} to registered member!")
        onComplete(true)
      } else {
        _uiState.value = _uiState.value.copy(userMessage = "Conversion error: ${result.exceptionOrNull()?.localizedMessage}")
        onComplete(false)
      }
    }
  }

  fun deleteVisitor(visitorId: String) {
    viewModelScope.launch {
      repository.deleteVisitor(churchId, visitorId)
    }
  }

  // --- Department Actions ---
  fun addDepartment(department: Department, onComplete: (Boolean) -> Unit = {}) {
    viewModelScope.launch {
      val result = repository.addDepartment(churchId, department)
      if (result.isSuccess) {
        _uiState.value = _uiState.value.copy(userMessage = "Department created")
        onComplete(true)
      } else {
        _uiState.value = _uiState.value.copy(userMessage = "Error: ${result.exceptionOrNull()?.localizedMessage}")
        onComplete(false)
      }
    }
  }

  fun updateDepartment(department: Department, onComplete: (Boolean) -> Unit = {}) {
    viewModelScope.launch {
      val result = repository.updateDepartment(churchId, department)
      if (result.isSuccess) {
        _uiState.value = _uiState.value.copy(userMessage = "Department updated")
        onComplete(true)
      } else {
        _uiState.value = _uiState.value.copy(userMessage = "Error: ${result.exceptionOrNull()?.localizedMessage}")
        onComplete(false)
      }
    }
  }

  fun deleteDepartment(departmentId: String) {
    viewModelScope.launch {
      repository.deleteDepartment(churchId, departmentId)
    }
  }

  // --- Attendance Actions ---
  fun recordAttendance(
    session: AttendanceSession,
    records: List<AttendanceRecord>,
    onComplete: (Boolean) -> Unit = {}
  ) {
    viewModelScope.launch {
      val result = repository.recordAttendanceSession(churchId, session, records)
      if (result.isSuccess) {
        _uiState.value = _uiState.value.copy(userMessage = "Attendance session saved successfully")
        onComplete(true)
      } else {
        _uiState.value = _uiState.value.copy(userMessage = "Error: ${result.exceptionOrNull()?.localizedMessage}")
        onComplete(false)
      }
    }
  }

  // --- Update Church Settings ---
  fun updateChurchSettings(church: Church, onComplete: (Boolean) -> Unit = {}) {
    viewModelScope.launch {
      val result = repository.updateChurch(church)
      if (result.isSuccess) {
        _uiState.value = _uiState.value.copy(userMessage = "Church settings updated")
        onComplete(true)
      } else {
        _uiState.value = _uiState.value.copy(userMessage = "Error: ${result.exceptionOrNull()?.localizedMessage}")
        onComplete(false)
      }
    }
  }
}
