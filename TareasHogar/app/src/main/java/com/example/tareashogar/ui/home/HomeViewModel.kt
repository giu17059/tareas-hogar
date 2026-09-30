package com.example.tareashogar.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tareashogar.analytics.AnalyticsTracker
import com.example.tareashogar.data.AppUser
import com.example.tareashogar.data.Family
import com.example.tareashogar.data.FamilyRepository
import com.example.tareashogar.data.FeedbackRepository
import com.example.tareashogar.data.HouseTask
import com.example.tareashogar.data.TaskCategory
import com.example.tareashogar.data.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter(val label: String) {
    PENDIENTES("Pendientes"),
    MIAS("Mías"),
    TODAS("Todas"),
    HECHAS("Hechas")
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel : ViewModel() {
    private val taskRepo = TaskRepository()
    private val familyRepo = FamilyRepository()
    private val feedbackRepo = FeedbackRepository()

    private val familyId = MutableStateFlow<String?>(null)
    private var currentUser = AppUser()

    val family: StateFlow<Family?> = familyId.filterNotNull()
        .flatMapLatest { familyRepo.observeFamily(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val members: StateFlow<List<AppUser>> = familyId.filterNotNull()
        .flatMapLatest { familyRepo.observeMembers(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tasks: StateFlow<List<HouseTask>> = familyId.filterNotNull()
        .flatMapLatest { taskRepo.observeTasks(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var filter by mutableStateOf(TaskFilter.PENDIENTES)
    var isSending by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    fun start(familyId: String, user: AppUser) {
        currentUser = user
        this.familyId.value = familyId
    }

    fun consumeMessage() {
        message = null
    }

    fun visibleTasks(all: List<HouseTask>): List<HouseTask> {
        val filtered = when (filter) {
            TaskFilter.PENDIENTES -> all.filter { !it.done }
            TaskFilter.MIAS -> all.filter { it.assignedTo == currentUser.uid }
            TaskFilter.TODAS -> all
            TaskFilter.HECHAS -> all.filter { it.done }
        }
        return filtered.sortedBy { it.done }
    }

    fun addTask(title: String, description: String, category: TaskCategory, assignee: AppUser?) {
        val fid = familyId.value ?: return
        if (title.isBlank()) {
            message = "La tarea necesita un título"
            return
        }
        viewModelScope.launch {
            runCatching {
                taskRepo.addTask(fid, title, description, category, assignee, currentUser.uid)
            }.onSuccess {
                AnalyticsTracker.log(
                    "task_created",
                    "category" to category.name,
                    "assigned" to (assignee != null)
                )
                message = "Tarea creada"
            }.onFailure {
                message = "No se pudo crear la tarea"
            }
        }
    }

    fun toggleDone(task: HouseTask) {
        val fid = familyId.value ?: return
        val done = !task.done
        viewModelScope.launch {
            runCatching {
                taskRepo.setDone(fid, task.id, done, currentUser.uid)
            }.onSuccess {
                if (done) {
                    AnalyticsTracker.log(
                        "task_completed",
                        "category" to task.category.name,
                        "was_assigned_to_me" to (task.assignedTo == currentUser.uid)
                    )
                }
            }.onFailure {
                message = "No se pudo actualizar la tarea"
            }
        }
    }

    fun deleteTask(task: HouseTask) {
        val fid = familyId.value ?: return
        viewModelScope.launch {
            runCatching { taskRepo.deleteTask(fid, task.id) }
                .onSuccess { AnalyticsTracker.log("task_deleted") }
                .onFailure { message = "No se pudo eliminar la tarea" }
        }
    }

    fun leaveFamily() {
        val fid = familyId.value ?: return
        viewModelScope.launch {
            runCatching { familyRepo.leaveFamily(currentUser.uid, fid) }
                .onSuccess { AnalyticsTracker.log("family_left") }
                .onFailure { message = "No se pudo salir de la familia" }
        }
    }

    fun sendFeedback(rating: Int, wouldUseWeekly: String, comment: String, onSent: () -> Unit) {
        if (isSending) return
        viewModelScope.launch {
            isSending = true
            runCatching {
                feedbackRepo.send(
                    user = currentUser,
                    familyId = familyId.value,
                    rating = rating,
                    wouldUseWeekly = wouldUseWeekly,
                    comment = comment,
                    tasksInFamily = tasks.value.size
                )
            }.onSuccess {
                AnalyticsTracker.log("feedback_sent", "rating" to rating)
                onSent()
            }.onFailure {
                message = "No se pudo enviar tu opinión"
            }
            isSending = false
        }
    }
}
