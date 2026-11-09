const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_pastor";
const BOB_UID = "bob_visitor";
const CHURCH_ID = "church_grace_tabernacle";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- PLATFORM USERS TESTS ---

test("Unauthenticated user: cannot read platform users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("platformUsers").doc("alice").get());
});

test("Authenticated user: can create and read their own platform profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("platformUsers").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice@gracechurch.org",
      displayName: "Pastor Alice",
      churchId: CHURCH_ID,
      role: "OWNER",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("platformUsers").doc(ALICE_UID).get()
  );
});

test("Authenticated user: cannot read another user's platform profile directly", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("platformUsers").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice@gracechurch.org",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("platformUsers").doc(ALICE_UID).get()
  );
});

// --- CHURCHES & MEMBERS TESTS ---

test("Church creation: signed-in user can create church when ownerUid matches", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("churches").doc(CHURCH_ID).set({
      name: "Grace Tabernacle International",
      ownerUid: ALICE_UID,
      country: "United States",
      pastor: "Pastor Alice",
      createdAt: new Date(),
    })
  );
});

test("Member creation: signed-in user can register member in church", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("churches").doc(CHURCH_ID).collection("members").doc("mem_001").set({
      churchId: CHURCH_ID,
      memberId: "CHB-1001",
      firstName: "John",
      lastName: "Doe",
      category: "Regular member",
      status: "Active",
      phone: "+1234567890",
      createdAt: new Date(),
    })
  );
});

test("Unauthenticated user: cannot read church members", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("churches").doc(CHURCH_ID).collection("members").get()
  );
});

test("Audit logs: append-only enforcement (delete fails)", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("churches").doc(CHURCH_ID).collection("auditLogs").doc("log_1").set({
      churchId: CHURCH_ID,
      action: "MEMBER_CREATED",
      performedBy: ALICE_UID,
      timestamp: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("churches").doc(CHURCH_ID).collection("auditLogs").doc("log_1").delete()
  );
});
