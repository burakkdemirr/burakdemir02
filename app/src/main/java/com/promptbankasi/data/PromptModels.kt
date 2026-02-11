package com.promptbankasi.data

data class PromptCategory(
    val id: String,
    val emoji: String,
    val name: String
)

data class PromptItem(
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val promptText: String,
    val usageCount: Int = 0,
    val isPopular: Boolean = false,
    val isCustom: Boolean = false
)
