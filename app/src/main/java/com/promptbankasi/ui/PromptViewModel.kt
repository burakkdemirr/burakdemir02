package com.promptbankasi.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.promptbankasi.data.PromptItem
import com.promptbankasi.data.PromptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val PREFS = "prompt_bankasi_prefs"
private const val KEY_FAVORITES = "favorites"
private const val KEY_THEME_DARK = "theme_dark"
private const val KEY_CUSTOM_PROMPTS = "custom_prompts"
private const val KEY_USAGE = "usage_map"

class PromptViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS, 0)

    private val _uiState = MutableStateFlow(PromptUiState())
    val uiState: StateFlow<PromptUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val favorites = prefs.getString(KEY_FAVORITES, "")
            .orEmpty()
            .split(",")
            .filter { it.isNotBlank() }
            .toSet()

        val usageMap = prefs.getString(KEY_USAGE, "")
            .orEmpty()
            .split("|")
            .mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) parts[0] to parts[1].toIntOrNull() else null
            }
            .filter { it.second != null }
            .associate { it.first to (it.second ?: 0) }

        val customPrompts = prefs.getString(KEY_CUSTOM_PROMPTS, "")
            .orEmpty()
            .split("~~~")
            .mapNotNull { raw ->
                if (raw.isBlank()) return@mapNotNull null
                val p = raw.split(";;")
                if (p.size < 5) return@mapNotNull null
                PromptItem(
                    id = p[0],
                    categoryId = p[1],
                    title = p[2],
                    description = p[3],
                    promptText = p[4],
                    isCustom = true
                )
            }

        val merged = (PromptRepository.starterPrompts + customPrompts).map {
            it.copy(usageCount = usageMap[it.id] ?: it.usageCount)
        }

        _uiState.update {
            it.copy(
                isDarkTheme = prefs.getBoolean(KEY_THEME_DARK, false),
                prompts = merged,
                favorites = favorites
            )
        }
    }

    fun setSearch(query: String) = _uiState.update { it.copy(searchQuery = query) }

    fun toggleFavorite(promptId: String) {
        _uiState.update {
            val updated = if (promptId in it.favorites) it.favorites - promptId else it.favorites + promptId
            saveFavorites(updated)
            it.copy(favorites = updated)
        }
    }

    fun incrementUsage(promptId: String) {
        _uiState.update { state ->
            val updatedPrompts = state.prompts.map { p ->
                if (p.id == promptId) p.copy(usageCount = p.usageCount + 1) else p
            }
            saveUsage(updatedPrompts)
            state.copy(prompts = updatedPrompts)
        }
    }

    fun addCustomPrompt(categoryId: String, title: String, description: String, promptText: String) {
        val newPrompt = PromptItem(
            id = "custom_${System.currentTimeMillis()}",
            categoryId = categoryId,
            title = title,
            description = description,
            promptText = promptText,
            isCustom = true
        )
        _uiState.update {
            val updated = it.prompts + newPrompt
            saveCustomPrompts(updated.filter { p -> p.isCustom })
            it.copy(prompts = updated)
        }
    }

    fun toggleTheme() {
        _uiState.update {
            val updated = !it.isDarkTheme
            prefs.edit().putBoolean(KEY_THEME_DARK, updated).apply()
            it.copy(isDarkTheme = updated)
        }
    }

    fun generatePromptWithAi(topic: String, goal: String): String {
        return """
            Sen deneyimli bir yapay zeka danışmanısın.
            Konu: $topic
            Hedef: $goal
            
            Bu hedefe uygun, net talimatlar içeren, örnek çıktı formatı barındıran ve adım adım ilerleyen bir prompt oluştur.
            Cevap tamamen Türkçe olsun.
        """.trimIndent()
    }

    private fun saveFavorites(favorites: Set<String>) {
        prefs.edit().putString(KEY_FAVORITES, favorites.joinToString(",")).apply()
    }

    private fun saveCustomPrompts(customPrompts: List<PromptItem>) {
        val raw = customPrompts.joinToString("~~~") {
            "${it.id};;${it.categoryId};;${it.title};;${it.description};;${it.promptText}"
        }
        prefs.edit().putString(KEY_CUSTOM_PROMPTS, raw).apply()
    }

    private fun saveUsage(prompts: List<PromptItem>) {
        val usageRaw = prompts.joinToString("|") { "${it.id}:${it.usageCount}" }
        prefs.edit().putString(KEY_USAGE, usageRaw).apply()
    }
}

data class PromptUiState(
    val isDarkTheme: Boolean = false,
    val searchQuery: String = "",
    val prompts: List<PromptItem> = emptyList(),
    val favorites: Set<String> = emptySet()
)
