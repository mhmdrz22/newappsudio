package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.ResistanceDao
import com.example.data.ResistanceTask
import com.example.data.TransmissionLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ResistanceDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ResistanceDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.resistanceDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveTask() = runBlocking {
        val task = ResistanceTask(
            title = "Test Mission Alpha",
            description = "Sabotage syndicate transmission lines.",
            threatLevel = "CRITICAL",
            status = "PENDING",
            targetSector = "SECTOR_XX",
            decryptionKey = "TEST_KEY"
        )
        dao.insertTask(task)

        val allTasks = dao.getAllTasks().first()
        assertEquals(1, allTasks.size)
        assertEquals("Test Mission Alpha", allTasks[0].title)
        assertEquals("PENDING", allTasks[0].status)
    }

    @Test
    fun updateTaskStatus() = runBlocking {
        val task = ResistanceTask(
            id = 1,
            title = "Test Mission Beta",
            description = "Gather encryption nodes.",
            threatLevel = "LOW",
            status = "PENDING",
            targetSector = "SECTOR_01",
            decryptionKey = "NONE"
        )
        dao.insertTask(task)

        val inserted = dao.getAllTasks().first()[0]
        dao.updateTask(inserted.copy(status = "ACTIVE"))

        val updatedTasks = dao.getAllTasks().first()
        assertEquals("ACTIVE", updatedTasks[0].status)
    }

    @Test
    fun insertAndDecryptTransmission() = runBlocking {
        val log = TransmissionLog(
            sender = "ORACLE_TEST",
            encryptedContent = "TXlTZWNyZXRNZXNzYWdlCg==",
            decryptedContent = "Unmasked tactical command details.",
            isDecrypted = false
        )
        dao.insertTransmission(log)

        val logs = dao.getAllTransmissions().first()
        assertEquals(1, logs.size)
        assertEquals(false, logs[0].isDecrypted)

        dao.updateTransmission(logs[0].copy(isDecrypted = true))
        val updatedLogs = dao.getAllTransmissions().first()
        assertEquals(true, updatedLogs[0].isDecrypted)
    }
}
