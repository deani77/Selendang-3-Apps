package com.selendang3.app

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.selendang3.app.data.EstateDatabase
import com.selendang3.app.data.EstateField
import com.selendang3.app.data.EstateSnapshot
import com.selendang3.app.data.SyncState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseName = "room-schema-migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), EstateDatabase::class.java)

    @After fun deleteTestDatabase() {
        context.deleteDatabase(databaseName)
    }

    @Test fun version1To2PreservesFieldsAndSyncAndSupportsSnapshotWrites() = runBlocking {
        // Build the old database from Room's committed v1 schema, not handwritten DDL.
        helper.createDatabase(databaseName, 1).apply {
            execSQL("INSERT INTO estate_fields (code, alternateCode, hectares, palms, reportingYear, verified) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf("DEMO-A", null, 1.25, 100, 2026, 0))
            execSQL("INSERT INTO estate_fields (code, alternateCode, hectares, palms, reportingYear, verified) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf("DEMO-B", "OLD-B", 2.5, 250, 2025, 1))
            execSQL("INSERT INTO sync_state (id, lastSuccessEpochMillis) VALUES (?, ?)", arrayOf(1, 123456789L))
            close()
        }

        // Validate every table against the committed v2 schema, including dropped tables.
        helper.runMigrationsAndValidate(databaseName, 2, true, EstateDatabase.MIGRATION_1_2).apply {
            query("SELECT COUNT(*) FROM estate_snapshot").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
            }
            close()
        }

        // Open the migrated file through the production Room model and exercise its DAO.
        val database = Room.databaseBuilder(context, EstateDatabase::class.java, databaseName)
            .addMigrations(EstateDatabase.MIGRATION_1_2).build()
        try {
            val dao = database.estateDao()
            assertEquals(listOf(
                EstateField("DEMO-A", null, 1.25, 100, 2026, false),
                EstateField("DEMO-B", "OLD-B", 2.5, 250, 2025, true),
            ), dao.observeFields().first())
            assertEquals(SyncState(lastSuccessEpochMillis = 123456789L), dao.observeSync().first())
            assertNull(dao.currentSnapshot())

            // Room stores snapshot content as opaque text; payload parsing is tested separately.
            val snapshot = EstateSnapshot(revision = 1, content = "{\"test\":\"fictional\"}",
                updatedAt = 987654321L, source = "Migration test")
            dao.putSnapshot(snapshot)
            assertEquals(snapshot, dao.currentSnapshot())
        } finally {
            database.close()
        }
    }
}
