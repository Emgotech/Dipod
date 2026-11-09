package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Church
import com.example.data.model.ChurchMember
import com.example.data.model.Department
import com.example.data.model.PlatformUser
import com.example.data.model.Visitor
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ChurchRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createChurch_authenticatedOwner_createsSuccessfully() = runBlocking {
    val uid = signInTestUser("pastor_${UUID.randomUUID()}@churchbase.org")
    val repository = ChurchRepository(firestore)

    val churchId = "church_${UUID.randomUUID().toString().take(8)}"
    val church = Church(
      id = churchId,
      name = "Grace Cathedral",
      country = "Nigeria",
      pastor = "Pastor Aniekan",
      ownerUid = uid
    )

    val result = withTimeout(5000L) { repository.createChurch(church) }
    assertTrue(result.isSuccess)
    assertEquals(churchId, result.getOrThrow())

    val fetched = withTimeout(5000L) { repository.getChurch(churchId) }
    assertTrue(fetched.isSuccess)
    assertNotNull(fetched.getOrThrow())
    assertEquals("Grace Cathedral", fetched.getOrThrow()?.name)
  }

  @Test
  fun addMember_and_observeMembers_returnsEmittedList() = runBlocking {
    val uid = signInTestUser("admin_${UUID.randomUUID()}@churchbase.org")
    val repository = ChurchRepository(firestore)

    val churchId = "church_${UUID.randomUUID().toString().take(8)}"
    val church = Church(
      id = churchId,
      name = "Faith Assembly",
      country = "United States",
      ownerUid = uid
    )
    repository.createChurch(church)

    val member = ChurchMember(
      churchId = churchId,
      memberId = "CHB-2001",
      firstName = "Barnabas",
      lastName = "Paul",
      category = "Church worker",
      status = "Active"
    )

    val addResult = withTimeout(5000L) { repository.addMember(churchId, member) }
    assertTrue(addResult.isSuccess)

    val emitted = withTimeout(5000L) {
      repository.observeMembers(churchId).first { it.isNotEmpty() }
    }
    assertTrue(emitted.any { it.firstName == "Barnabas" })
  }

  @Test
  fun convertVisitorToMember_createsMemberAndUpdatesVisitor() = runBlocking {
    val uid = signInTestUser("worker_${UUID.randomUUID()}@churchbase.org")
    val repository = ChurchRepository(firestore)

    val churchId = "church_${UUID.randomUUID().toString().take(8)}"
    val church = Church(
      id = churchId,
      name = "Victory Chapel",
      country = "Ghana",
      ownerUid = uid
    )
    repository.createChurch(church)

    val visitor = Visitor(
      churchId = churchId,
      name = "David Livingstone",
      dateVisited = "2026-11-08",
      phone = "+233555123456"
    )
    val visitorId = repository.addVisitor(churchId, visitor).getOrThrow()
    val savedVisitor = visitor.copy(id = visitorId)

    val memberId = withTimeout(5000L) {
      repository.convertVisitorToMember(churchId, savedVisitor, "New member").getOrThrow()
    }
    assertTrue(memberId.isNotEmpty())
  }

  @Test
  fun unauthenticatedAccess_fails() = runBlocking {
    auth.signOut()
    val repository = ChurchRepository(firestore)

    val result = runCatching { repository.requireUserId() }
    assertTrue(result.isFailure)
  }
}
