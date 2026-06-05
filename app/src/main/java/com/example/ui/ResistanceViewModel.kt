package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ResistanceRepository
import com.example.data.ResistanceTask
import com.example.data.TransmissionLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class ResistanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ResistanceRepository
    val uiTasks: StateFlow<List<ResistanceTask>>
    val uiTransmissions: StateFlow<List<TransmissionLog>>

    // Dynamic environmental network states
    private val _syndicateInterference = MutableStateFlow(24) // Dynamic load index %
    val syndicateInterference: StateFlow<Int> = _syndicateInterference.asStateFlow()

    private val _networkLatency = MutableStateFlow(42) // ms
    val networkLatency: StateFlow<Int> = _networkLatency.asStateFlow()

    private val _activeDecryptions = MutableStateFlow<Set<Int>>(emptySet())
    val activeDecryptions: StateFlow<Set<Int>> = _activeDecryptions.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).resistanceDao()
        repository = ResistanceRepository(dao)

        uiTasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        uiTransmissions = repository.allTransmissions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed data if empty
        viewModelScope.launch {
            if (repository.allTasks.first().isEmpty()) {
                seedInitialTasks()
            }
            if (repository.allTransmissions.first().isEmpty()) {
                seedInitialTransmissions()
            }
        }

        // Simulate realistic terminal fluctuation
        viewModelScope.launch {
            while (true) {
                delay(4000)
                _syndicateInterference.value = Random.nextInt(15, 38)
                _networkLatency.value = Random.nextInt(28, 65)
            }
        }
    }

    private suspend fun seedInitialTasks() {
        val initial = listOf(
            ResistanceTask(
                title = "Infiltrate Sector 4 Data Vault",
                description = "Bypass the biometric scanner of the Syndicate central server to siphon the encryption database.",
                threatLevel = "CRITICAL",
                status = "ACTIVE",
                targetSector = "SECTOR_04_CORE",
                decryptionKey = "AES_GCM_256_S4"
            ),
            ResistanceTask(
                title = "Intercept Syndicate Cargo Feed",
                description = "Reroute local logistics hub cameras to map the incoming weapon delivery route.",
                threatLevel = "HIGH",
                status = "PENDING",
                targetSector = "SECTOR_09_DISTRIB",
                decryptionKey = "SHA_512_FEED_B"
            ),
            ResistanceTask(
                title = "Node-07 Thermal Stabilization",
                description = "Deploy software patches to throttle background compilation engines and mask thermal footprints.",
                threatLevel = "LOW",
                status = "COMPLETED",
                targetSector = "UNDERGROUND_SUB",
                decryptionKey = "NONE"
            )
        )
        for (task in initial) {
            repository.insertTask(task)
        }
    }

    private suspend fun seedInitialTransmissions() {
        val initial = listOf(
            TransmissionLog(
                sender = "ORACLE",
                encryptedContent = "U2VuZCBpbmZvcm1hdGlvbiBub3c7IHN5bmRpY2F0ZSBpcyBtb3Zpbmcgb24gc2VjdG9yIDUu",
                decryptedContent = "URGENT: Syndicate strike unit is re-routing towards Sector 5. Disperse local node immediately.",
                isDecrypted = false,
                signalStrength = 92
            ),
            TransmissionLog(
                sender = "NODE_03",
                encryptedContent = "U3lzdGVtcyBzdGFibGUsIG5vIGFjdGl2aXR5IGRldGVjdGVk",
                decryptedContent = "All thermal footprints masked. Sub-grid 3 operational.",
                isDecrypted = true,
                signalStrength = 84
            ),
            TransmissionLog(
                sender = "UNKNOWN_SOURCE",
                encryptedContent = "V2UgaGF2ZSB0aGUgZW5jcnlwdGlvbiBrZXlzLiBBd2FpdCBzaWduYWwu",
                decryptedContent = "COURIER LOCATED: Encryption keys retrieved. Keep visual terminal open for downlink instructions.",
                isDecrypted = false,
                signalStrength = 41
            )
        )
        for (log in initial) {
            repository.insertTransmission(log)
        }
    }

    fun submitTask(title: String, description: String, threatLevel: String, targetSector: String, decryptionKey: String) {
        viewModelScope.launch {
            val task = ResistanceTask(
                title = title,
                description = description,
                threatLevel = threatLevel,
                status = "PENDING",
                targetSector = targetSector,
                decryptionKey = decryptionKey.ifBlank { "TACTICAL_SECURE" }
            )
            repository.insertTask(task)
        }
    }

    fun updateTaskStatus(task: ResistanceTask, newStatus: String) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = newStatus))
        }
    }

    fun removeTask(id: Int) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    fun transmitLog(sender: String, messageText: String) {
        viewModelScope.launch {
            // Emulate secure encrypt matching
            val base64Encoded = android.util.Base64.encodeToString(messageText.toByteArray(), android.util.Base64.NO_WRAP)
            val log = TransmissionLog(
                sender = sender.uppercase().ifBlank { "NODE_LOCAL" },
                encryptedContent = base64Encoded,
                decryptedContent = messageText,
                isDecrypted = true, // Local messages are composed unencrypted then locked, shown as open to sender
                signalStrength = 100
            )
            repository.insertTransmission(log)
        }
    }

    fun decryptTransmission(log: TransmissionLog) {
        if (log.isDecrypted || _activeDecryptions.value.contains(log.id)) return

        viewModelScope.launch {
            _activeDecryptions.value = _activeDecryptions.value + log.id
            // Simulate immersive crunching of decryption keys
            delay(1500)
            repository.updateTransmission(log.copy(isDecrypted = true))
            _activeDecryptions.value = _activeDecryptions.value - log.id
        }
    }

    fun purgeTransmissions() {
        viewModelScope.launch {
            repository.clearAllTransmissions()
            seedInitialTransmissions()
        }
    }
}
