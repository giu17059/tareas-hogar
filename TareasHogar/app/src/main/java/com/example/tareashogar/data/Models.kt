package com.example.tareashogar.data

data class AppUser(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val familyId: String? = null
)

data class Family(
    val id: String = "",
    val name: String = "",
    val inviteCode: String = "",
    val members: List<String> = emptyList(),
    val createdBy: String = ""
)

enum class TaskCategory(val label: String, val emoji: String) {
    LIMPIEZA("Limpieza", "🧹"),
    COCINA("Cocina", "🍳"),
    COMPRAS("Compras", "🛒"),
    ROPA("Ropa", "👕"),
    MASCOTAS("Mascotas", "🐶"),
    OTROS("Otros", "📌");

    companion object {
        fun from(value: String?): TaskCategory = entries.firstOrNull { it.name == value } ?: OTROS
    }
}

data class HouseTask(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: TaskCategory = TaskCategory.OTROS,
    val assignedTo: String? = null,
    val assignedName: String? = null,
    val done: Boolean = false,
    val createdBy: String = "",
    val createdAt: Long = 0L,
    val completedBy: String? = null,
    val completedAt: Long? = null
)
