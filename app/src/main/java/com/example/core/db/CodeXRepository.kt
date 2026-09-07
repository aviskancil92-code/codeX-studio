package com.example.core.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CodeXRepository(private val dao: CodeXDao) {

    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()
    val recentCommands: Flow<List<TerminalHistoryEntity>> = dao.getRecentCommands()
    val allSettings: Flow<Map<String, String>> = dao.getAllSettings().map { list ->
        list.associate { it.key to it.value }
    }

    suspend fun saveProject(project: ProjectEntity) = dao.insertProject(project)

    suspend fun deleteProject(id: String) = dao.deleteProjectById(id)

    suspend fun getProject(id: String) = dao.getProjectById(id)

    suspend fun getSetting(key: String, defaultValue: String): String {
        return dao.getSetting(key) ?: defaultValue
    }

    suspend fun setSetting(key: String, value: String) {
        dao.setSetting(SettingsEntity(key, value))
    }

    suspend fun recordCommand(cmd: String) {
        if (cmd.isNotBlank()) {
            dao.insertCommand(TerminalHistoryEntity(command = cmd.trim()))
        }
    }

    suspend fun clearHistory() = dao.clearTerminalHistory()
}
