package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class PlatformUser(
  @DocumentId val userId: String = "",
  val email: String = "",
  val displayName: String = "",
  val churchId: String = "",
  val role: String = "OWNER", // OWNER, ADMIN, PASTOR, DEPT_LEADER, MEMBERSHIP_OFFICER, ATTENDANCE_OFFICER, READ_ONLY
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Church(
  @DocumentId val id: String = "",
  val name: String = "",
  val ownerUid: String = "",
  val denomination: String = "",
  val country: String = "",
  val state: String = "",
  val city: String = "",
  val address: String = "",
  val email: String = "",
  val phone: String = "",
  val pastor: String = "",
  val sizeCategory: String = "Medium (100-500)",
  val subscriptionTier: String = "STANDARD", // FREE, STANDARD, PREMIUM
  val subscriptionStatus: String = "ACTIVE", // ACTIVE, TRIAL, EXPIRED, SUSPENDED
  val dateFormat: String = "YYYY-MM-DD",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChurchMember(
  @DocumentId val id: String = "",
  val churchId: String = "",
  val memberId: String = "",
  val firstName: String = "",
  val middleName: String = "",
  val lastName: String = "",
  val photoUrl: String = "",
  val gender: String = "Male",
  val dateOfBirth: String = "",
  val maritalStatus: String = "Single",
  val occupation: String = "",
  val residentialAddress: String = "",
  val country: String = "",
  val state: String = "",
  val city: String = "",
  val nationality: String = "",
  val phone: String = "",
  val alternatePhone: String = "",
  val email: String = "",
  val category: String = "Regular member",
  val status: String = "Active", // Active, Inactive, Visiting, Transferred, Deceased, Archived
  val dateFirstVisited: String = "",
  val dateJoined: String = "",
  val baptismStatus: String = "Baptized",
  val baptismDate: String = "",
  val previousChurch: String = "",
  val invitedBy: String = "",
  val assignedLeader: String = "",
  val departments: List<String> = emptyList(),
  val ministries: List<String> = emptyList(),
  val smallGroup: String = "",
  val notes: String = "",
  val createdBy: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  val fullName: String
    get() = if (middleName.isBlank()) "$firstName $lastName" else "$firstName $middleName $lastName"
}

@IgnoreExtraProperties
data class Visitor(
  @DocumentId val id: String = "",
  val churchId: String = "",
  val name: String = "",
  val phone: String = "",
  val email: String = "",
  val dateVisited: String = "",
  val serviceAttended: String = "Sunday service",
  val invitedBy: String = "",
  val followUpStatus: String = "Not contacted", // Not contacted, Contacted, Invited again, Became a member, Not interested
  val followUpNotes: String = "",
  val assignedWorker: String = "",
  val convertedToMemberId: String = "",
  val createdBy: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Department(
  @DocumentId val id: String = "",
  val churchId: String = "",
  val name: String = "",
  val description: String = "",
  val leaderId: String = "",
  val leaderName: String = "",
  val assistantLeaderName: String = "",
  val active: Boolean = true,
  val memberCount: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AttendanceSession(
  @DocumentId val id: String = "",
  val churchId: String = "",
  val serviceType: String = "Sunday service",
  val date: String = "",
  val title: String = "",
  val departmentId: String = "",
  val departmentName: String = "",
  val recordedBy: String = "",
  val totalPresent: Long = 0L,
  val totalAbsent: Long = 0L,
  val totalVisitors: Long = 0L,
  val notes: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AttendanceRecord(
  @DocumentId val id: String = "",
  val churchId: String = "",
  val sessionId: String = "",
  val memberId: String = "",
  val memberName: String = "",
  val status: String = "Present", // Present, Absent, Excused
  val notes: String = "",
  @ServerTimestamp val recordedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AuditLog(
  @DocumentId val id: String = "",
  val churchId: String = "",
  val action: String = "",
  val entityType: String = "",
  val entityId: String = "",
  val performedBy: String = "",
  val performedByName: String = "",
  val details: String = "",
  @ServerTimestamp val timestamp: Timestamp? = null
)
