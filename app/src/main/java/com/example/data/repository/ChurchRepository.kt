package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.AuditLog
import com.example.data.model.Church
import com.example.data.model.ChurchMember
import com.example.data.model.Department
import com.example.data.model.PlatformUser
import com.example.data.model.Visitor
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ChurchRepository(val db: FirebaseFirestore) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private val auth = Firebase.auth

  fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing ChurchBase.")
  }

  // --- Platform User Management ---
  suspend fun getPlatformUser(uid: String): Result<PlatformUser?> {
    val path = "platformUsers/$uid"
    return try {
      val doc = db.collection("platformUsers").document(uid).get().await()
      Result.success(doc.toObject(PlatformUser::class.java))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, path)
      Result.failure(e)
    }
  }

  suspend fun savePlatformUser(user: PlatformUser): Result<Unit> {
    val uid = requireUserId()
    val docRef = db.collection("platformUsers").document(uid)
    return try {
      val payload = mutableMapOf<String, Any?>(
        "userId" to uid,
        "email" to user.email,
        "displayName" to user.displayName,
        "churchId" to user.churchId,
        "role" to user.role,
        "updatedAt" to FieldValue.serverTimestamp()
      )
      if (user.createdAt == null) {
        payload["createdAt"] = FieldValue.serverTimestamp()
      }
      docRef.set(payload.filterValues { it != null }).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      Result.failure(e)
    }
  }

  // --- Church Tenant Operations ---
  suspend fun getChurch(churchId: String): Result<Church?> {
    val path = "churches/$churchId"
    return try {
      val doc = db.collection("churches").document(churchId).get().await()
      Result.success(doc.toObject(Church::class.java))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, path)
      Result.failure(e)
    }
  }

  suspend fun createChurch(church: Church): Result<String> {
    val uid = requireUserId()
    val docRef = if (church.id.isNotBlank()) db.collection("churches").document(church.id) else db.collection("churches").document()
    return try {
      val churchId = docRef.id
      val payload = mapOf(
        "name" to church.name,
        "ownerUid" to uid,
        "denomination" to church.denomination,
        "country" to church.country,
        "state" to church.state,
        "city" to church.city,
        "address" to church.address,
        "email" to church.email,
        "phone" to church.phone,
        "pastor" to church.pastor,
        "sizeCategory" to church.sizeCategory,
        "subscriptionTier" to church.subscriptionTier,
        "subscriptionStatus" to church.subscriptionStatus,
        "dateFormat" to church.dateFormat,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      ).filterValues { it != null }

      docRef.set(payload).await()

      // Seed initial default departments
      val defaultDepts = listOf(
        "Pastoral & Leadership",
        "Choir & Music Ministry",
        "Ushering & Protocol",
        "Media & Technical",
        "Children & Youth",
        "Evangelism & Missions",
        "Prayer & Intercession",
        "Women's Fellowship",
        "Men's Fellowship"
      )
      for (deptName in defaultDepts) {
        val deptRef = db.collection("churches").document(churchId).collection("departments").document()
        deptRef.set(
          mapOf(
            "churchId" to churchId,
            "name" to deptName,
            "description" to "Official $deptName department",
            "active" to true,
            "memberCount" to 0L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()
      }

      // Log audit
      logAuditEvent(churchId, "CHURCH_REGISTERED", "Church", churchId, "Church '${church.name}' registered by $uid")
      Result.success(churchId)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateChurch(church: Church): Result<Unit> {
    requireUserId()
    require(church.id.isNotBlank()) { "Church ID must not be blank" }
    val docRef = db.collection("churches").document(church.id)
    return try {
      val payload = mapOf(
        "name" to church.name,
        "ownerUid" to church.ownerUid,
        "denomination" to church.denomination,
        "country" to church.country,
        "state" to church.state,
        "city" to church.city,
        "address" to church.address,
        "email" to church.email,
        "phone" to church.phone,
        "pastor" to church.pastor,
        "sizeCategory" to church.sizeCategory,
        "subscriptionTier" to church.subscriptionTier,
        "subscriptionStatus" to church.subscriptionStatus,
        "dateFormat" to church.dateFormat,
        "createdAt" to church.createdAt,
        "updatedAt" to FieldValue.serverTimestamp()
      ).filterValues { it != null }

      docRef.set(payload).await()
      logAuditEvent(church.id, "CHURCH_SETTINGS_UPDATED", "Church", church.id, "Church profile details updated")
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  // --- Members Operations ---
  fun observeMembers(churchId: String): Flow<List<ChurchMember>> = flow {
    requireUserId()
    val path = "churches/$churchId/members"
    emitAll(
      db.collection("churches").document(churchId).collection("members")
        .snapshots()
        .map { snapshot -> snapshot.toObjects(ChurchMember::class.java) }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  suspend fun addMember(churchId: String, member: ChurchMember): Result<String> {
    val uid = requireUserId()
    val docRef = if (member.id.isNotBlank()) db.collection("churches").document(churchId).collection("members").document(member.id)
    else db.collection("churches").document(churchId).collection("members").document()
    return try {
      val payload = mapOf(
        "churchId" to churchId,
        "memberId" to member.memberId.ifBlank { "CHB-${System.currentTimeMillis() % 10000}" },
        "firstName" to member.firstName,
        "middleName" to member.middleName,
        "lastName" to member.lastName,
        "photoUrl" to member.photoUrl,
        "gender" to member.gender,
        "dateOfBirth" to member.dateOfBirth,
        "maritalStatus" to member.maritalStatus,
        "occupation" to member.occupation,
        "residentialAddress" to member.residentialAddress,
        "country" to member.country,
        "state" to member.state,
        "city" to member.city,
        "nationality" to member.nationality,
        "phone" to member.phone,
        "alternatePhone" to member.alternatePhone,
        "email" to member.email,
        "category" to member.category,
        "status" to member.status,
        "dateFirstVisited" to member.dateFirstVisited,
        "dateJoined" to member.dateJoined,
        "baptismStatus" to member.baptismStatus,
        "baptismDate" to member.baptismDate,
        "previousChurch" to member.previousChurch,
        "invitedBy" to member.invitedBy,
        "assignedLeader" to member.assignedLeader,
        "departments" to member.departments,
        "ministries" to member.ministries,
        "smallGroup" to member.smallGroup,
        "notes" to member.notes,
        "createdBy" to uid,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      ).filterValues { it != null }

      docRef.set(payload).await()
      logAuditEvent(churchId, "MEMBER_CREATED", "ChurchMember", docRef.id, "Registered member ${member.fullName}")
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateMember(churchId: String, member: ChurchMember): Result<Unit> {
    requireUserId()
    require(member.id.isNotBlank()) { "Member ID must not be blank" }
    val docRef = db.collection("churches").document(churchId).collection("members").document(member.id)
    return try {
      val payload = mapOf(
        "churchId" to churchId,
        "memberId" to member.memberId,
        "firstName" to member.firstName,
        "middleName" to member.middleName,
        "lastName" to member.lastName,
        "photoUrl" to member.photoUrl,
        "gender" to member.gender,
        "dateOfBirth" to member.dateOfBirth,
        "maritalStatus" to member.maritalStatus,
        "occupation" to member.occupation,
        "residentialAddress" to member.residentialAddress,
        "country" to member.country,
        "state" to member.state,
        "city" to member.city,
        "nationality" to member.nationality,
        "phone" to member.phone,
        "alternatePhone" to member.alternatePhone,
        "email" to member.email,
        "category" to member.category,
        "status" to member.status,
        "dateFirstVisited" to member.dateFirstVisited,
        "dateJoined" to member.dateJoined,
        "baptismStatus" to member.baptismStatus,
        "baptismDate" to member.baptismDate,
        "previousChurch" to member.previousChurch,
        "invitedBy" to member.invitedBy,
        "assignedLeader" to member.assignedLeader,
        "departments" to member.departments,
        "ministries" to member.ministries,
        "smallGroup" to member.smallGroup,
        "notes" to member.notes,
        "createdBy" to member.createdBy,
        "createdAt" to member.createdAt,
        "updatedAt" to FieldValue.serverTimestamp()
      ).filterValues { it != null }

      docRef.set(payload).await()
      logAuditEvent(churchId, "MEMBER_UPDATED", "ChurchMember", member.id, "Updated details for ${member.fullName}")
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun deleteMember(churchId: String, memberId: String, memberName: String): Result<Unit> {
    requireUserId()
    val docRef = db.collection("churches").document(churchId).collection("members").document(memberId)
    return try {
      docRef.delete().await()
      logAuditEvent(churchId, "MEMBER_DELETED", "ChurchMember", memberId, "Deleted member record $memberName")
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      Result.failure(e)
    }
  }

  // --- Visitors Operations ---
  fun observeVisitors(churchId: String): Flow<List<Visitor>> = flow {
    requireUserId()
    val path = "churches/$churchId/visitors"
    emitAll(
      db.collection("churches").document(churchId).collection("visitors")
        .snapshots()
        .map { snapshot -> snapshot.toObjects(Visitor::class.java) }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  suspend fun addVisitor(churchId: String, visitor: Visitor): Result<String> {
    val uid = requireUserId()
    val docRef = if (visitor.id.isNotBlank()) db.collection("churches").document(churchId).collection("visitors").document(visitor.id)
    else db.collection("churches").document(churchId).collection("visitors").document()
    return try {
      val payload = mapOf(
        "churchId" to churchId,
        "name" to visitor.name,
        "phone" to visitor.phone,
        "email" to visitor.email,
        "dateVisited" to visitor.dateVisited,
        "serviceAttended" to visitor.serviceAttended,
        "invitedBy" to visitor.invitedBy,
        "followUpStatus" to visitor.followUpStatus,
        "followUpNotes" to visitor.followUpNotes,
        "assignedWorker" to visitor.assignedWorker,
        "convertedToMemberId" to visitor.convertedToMemberId,
        "createdBy" to uid,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      ).filterValues { it != null }

      docRef.set(payload).await()
      logAuditEvent(churchId, "VISITOR_RECORDED", "Visitor", docRef.id, "Recorded visitor ${visitor.name}")
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateVisitor(churchId: String, visitor: Visitor): Result<Unit> {
    requireUserId()
    require(visitor.id.isNotBlank()) { "Visitor ID must not be blank" }
    val docRef = db.collection("churches").document(churchId).collection("visitors").document(visitor.id)
    return try {
      val payload = mapOf(
        "churchId" to churchId,
        "name" to visitor.name,
        "phone" to visitor.phone,
        "email" to visitor.email,
        "dateVisited" to visitor.dateVisited,
        "serviceAttended" to visitor.serviceAttended,
        "invitedBy" to visitor.invitedBy,
        "followUpStatus" to visitor.followUpStatus,
        "followUpNotes" to visitor.followUpNotes,
        "assignedWorker" to visitor.assignedWorker,
        "convertedToMemberId" to visitor.convertedToMemberId,
        "createdBy" to visitor.createdBy,
        "createdAt" to visitor.createdAt,
        "updatedAt" to FieldValue.serverTimestamp()
      ).filterValues { it != null }

      docRef.set(payload).await()
      logAuditEvent(churchId, "VISITOR_UPDATED", "Visitor", visitor.id, "Updated follow-up for visitor ${visitor.name}")
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun convertVisitorToMember(churchId: String, visitor: Visitor, memberCategory: String = "New member"): Result<String> {
    // 1. Create church member record
    val nameParts = visitor.name.trim().split(" ")
    val firstName = nameParts.firstOrNull() ?: visitor.name
    val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else ""

    val member = ChurchMember(
      churchId = churchId,
      firstName = firstName,
      lastName = lastName,
      phone = visitor.phone,
      email = visitor.email,
      dateFirstVisited = visitor.dateVisited,
      invitedBy = visitor.invitedBy,
      category = memberCategory,
      status = "Active"
    )

    val addMemberResult = addMember(churchId, member)
    if (addMemberResult.isFailure) {
      return addMemberResult
    }
    val newMemberId = addMemberResult.getOrThrow()

    // 2. Update visitor followUpStatus to "Became a member" and link convertedToMemberId
    updateVisitor(
      churchId,
      visitor.copy(
        followUpStatus = "Became a member",
        convertedToMemberId = newMemberId
      )
    )

    logAuditEvent(churchId, "VISITOR_CONVERTED", "Visitor", visitor.id, "Converted visitor ${visitor.name} to registered member ID $newMemberId")
    return Result.success(newMemberId)
  }

  suspend fun deleteVisitor(churchId: String, visitorId: String): Result<Unit> {
    requireUserId()
    val docRef = db.collection("churches").document(churchId).collection("visitors").document(visitorId)
    return try {
      docRef.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      Result.failure(e)
    }
  }

  // --- Departments Operations ---
  fun observeDepartments(churchId: String): Flow<List<Department>> = flow {
    requireUserId()
    val path = "churches/$churchId/departments"
    emitAll(
      db.collection("churches").document(churchId).collection("departments")
        .snapshots()
        .map { snapshot -> snapshot.toObjects(Department::class.java) }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  suspend fun addDepartment(churchId: String, department: Department): Result<String> {
    requireUserId()
    val docRef = if (department.id.isNotBlank()) db.collection("churches").document(churchId).collection("departments").document(department.id)
    else db.collection("churches").document(churchId).collection("departments").document()
    return try {
      val payload = mapOf(
        "churchId" to churchId,
        "name" to department.name,
        "description" to department.description,
        "leaderId" to department.leaderId,
        "leaderName" to department.leaderName,
        "assistantLeaderName" to department.assistantLeaderName,
        "active" to department.active,
        "memberCount" to department.memberCount,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      ).filterValues { it != null }

      docRef.set(payload).await()
      logAuditEvent(churchId, "DEPARTMENT_CREATED", "Department", docRef.id, "Created department ${department.name}")
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateDepartment(churchId: String, department: Department): Result<Unit> {
    requireUserId()
    require(department.id.isNotBlank()) { "Department ID must not be blank" }
    val docRef = db.collection("churches").document(churchId).collection("departments").document(department.id)
    return try {
      val payload = mapOf(
        "churchId" to churchId,
        "name" to department.name,
        "description" to department.description,
        "leaderId" to department.leaderId,
        "leaderName" to department.leaderName,
        "assistantLeaderName" to department.assistantLeaderName,
        "active" to department.active,
        "memberCount" to department.memberCount,
        "createdAt" to department.createdAt,
        "updatedAt" to FieldValue.serverTimestamp()
      ).filterValues { it != null }

      docRef.set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun deleteDepartment(churchId: String, departmentId: String): Result<Unit> {
    requireUserId()
    val docRef = db.collection("churches").document(churchId).collection("departments").document(departmentId)
    return try {
      docRef.delete().await()
      logAuditEvent(churchId, "DEPARTMENT_DELETED", "Department", departmentId, "Deleted department record")
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      Result.failure(e)
    }
  }

  // --- Attendance Operations ---
  fun observeAttendanceSessions(churchId: String): Flow<List<AttendanceSession>> = flow {
    requireUserId()
    val path = "churches/$churchId/attendanceSessions"
    emitAll(
      db.collection("churches").document(churchId).collection("attendanceSessions")
        .snapshots()
        .map { snapshot -> snapshot.toObjects(AttendanceSession::class.java).sortedByDescending { it.date } }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  suspend fun recordAttendanceSession(
    churchId: String,
    session: AttendanceSession,
    records: List<AttendanceRecord>
  ): Result<String> {
    val uid = requireUserId()
    val sessionRef = if (session.id.isNotBlank()) db.collection("churches").document(churchId).collection("attendanceSessions").document(session.id)
    else db.collection("churches").document(churchId).collection("attendanceSessions").document()

    return try {
      val presentCount = records.count { it.status == "Present" }.toLong()
      val absentCount = records.count { it.status == "Absent" }.toLong()

      val sessionPayload = mapOf(
        "churchId" to churchId,
        "serviceType" to session.serviceType,
        "date" to session.date,
        "title" to session.title,
        "departmentId" to session.departmentId,
        "departmentName" to session.departmentName,
        "recordedBy" to uid,
        "totalPresent" to presentCount,
        "totalAbsent" to absentCount,
        "totalVisitors" to session.totalVisitors,
        "notes" to session.notes,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      ).filterValues { it != null }

      sessionRef.set(sessionPayload).await()

      // Save each member attendance record
      for (record in records) {
        val recRef = db.collection("churches").document(churchId).collection("attendanceRecords").document()
        recRef.set(
          mapOf(
            "churchId" to churchId,
            "sessionId" to sessionRef.id,
            "memberId" to record.memberId,
            "memberName" to record.memberName,
            "status" to record.status,
            "notes" to record.notes,
            "recordedAt" to FieldValue.serverTimestamp()
          ).filterValues { it != null }
        ).await()
      }

      logAuditEvent(churchId, "ATTENDANCE_RECORDED", "AttendanceSession", sessionRef.id, "Recorded attendance for ${session.serviceType} on ${session.date} ($presentCount present)")
      Result.success(sessionRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, sessionRef.path)
      Result.failure(e)
    }
  }

  fun observeAttendanceRecords(churchId: String, sessionId: String): Flow<List<AttendanceRecord>> = flow {
    requireUserId()
    val path = "churches/$churchId/attendanceRecords"
    emitAll(
      db.collection("churches").document(churchId).collection("attendanceRecords")
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(AttendanceRecord::class.java).filter { it.sessionId == sessionId }
        }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  // --- Audit Logs ---
  suspend fun logAuditEvent(
    churchId: String,
    action: String,
    entityType: String,
    entityId: String,
    details: String
  ) {
    try {
      val uid = auth.currentUser?.uid ?: "system"
      val email = auth.currentUser?.displayName ?: auth.currentUser?.email ?: "Administrator"
      val docRef = db.collection("churches").document(churchId).collection("auditLogs").document()
      val payload = mapOf(
        "churchId" to churchId,
        "action" to action,
        "entityType" to entityType,
        "entityId" to entityId,
        "performedBy" to uid,
        "performedByName" to email,
        "details" to details,
        "timestamp" to FieldValue.serverTimestamp()
      )
      docRef.set(payload).await()
    } catch (e: Exception) {
      // Non-blocking log catch
    }
  }

  fun observeAuditLogs(churchId: String): Flow<List<AuditLog>> = flow {
    requireUserId()
    val path = "churches/$churchId/auditLogs"
    emitAll(
      db.collection("churches").document(churchId).collection("auditLogs")
        .snapshots()
        .map { snapshot -> snapshot.toObjects(AuditLog::class.java) }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }
}
