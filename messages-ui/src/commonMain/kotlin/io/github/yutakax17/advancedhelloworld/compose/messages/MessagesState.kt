package io.github.yutakax17.advancedhelloworld.compose.messages

import io.github.yutakax17.advancedhelloworld.core.SyncResult
import io.github.yutakax17.advancedhelloworld.messages.CreateMessageResult
import io.github.yutakax17.advancedhelloworld.messages.Message
import io.github.yutakax17.advancedhelloworld.messages.MessageRepository
import io.github.yutakax17.advancedhelloworld.messages.MessageValidation
import io.github.yutakax17.advancedhelloworld.messages.validateMessageText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

public enum class MessagesNoticeKind {
    INFO,
    ERROR,
}

public data class MessagesNotice(
    public val text: String,
    public val kind: MessagesNoticeKind,
)

public data class MessagesState(
    public val messages: List<Message> = emptyList(),
    public val draftText: String = "",
    public val validationMessage: String? = null,
    public val isLoading: Boolean = true,
    public val isSubmitting: Boolean = false,
    public val isRefreshing: Boolean = false,
    public val loadError: String? = null,
    public val notice: MessagesNotice? = null,
)

public sealed interface MessagesEvent {
    public data class DraftChanged(public val text: String) : MessagesEvent

    public data object Submit : MessagesEvent

    public data object Refresh : MessagesEvent

    public data class Retry(public val localId: String) : MessagesEvent
}

public interface MessagesInteractor {
    public fun observeMessages(): Flow<List<Message>>

    public suspend fun createMessage(text: String): CreateMessageResult

    public suspend fun refresh(): SyncResult

    public suspend fun retry(localId: String): SyncResult
}

public class RepositoryMessagesInteractor(
    private val repository: MessageRepository,
    private val retryMessage: suspend (String) -> SyncResult = { repository.syncContributor.synchronize() },
) : MessagesInteractor {
    override fun observeMessages(): Flow<List<Message>> = repository.observeLocal()

    override suspend fun createMessage(text: String): CreateMessageResult = repository.createOffline(text)

    override suspend fun refresh(): SyncResult = repository.syncContributor.synchronize()

    override suspend fun retry(localId: String): SyncResult = retryMessage(localId)
}

public class MessagesStateHolder(
    private val interactor: MessagesInteractor,
    private val scope: CoroutineScope,
) {
    private val mutableState: MutableStateFlow<MessagesState> = MutableStateFlow(MessagesState())
    private var observationJob: Job? = null

    public val state: StateFlow<MessagesState> = mutableState.asStateFlow()

    init {
        observeMessages()
    }

    public fun onEvent(event: MessagesEvent) {
        when (event) {
            is MessagesEvent.DraftChanged -> updateDraft(event.text)
            MessagesEvent.Submit -> submit()
            MessagesEvent.Refresh -> refresh()
            is MessagesEvent.Retry -> retry(event.localId)
        }
    }

    private fun observeMessages() {
        observationJob?.cancel()
        mutableState.update { it.copy(isLoading = true, loadError = null) }
        observationJob = scope.launch {
            interactor.observeMessages()
                .catch {
                    mutableState.update {
                        it.copy(isLoading = false, loadError = "Unable to load messages.")
                    }
                }
                .collect { messages ->
                    mutableState.update {
                        it.copy(messages = messages, isLoading = false, loadError = null)
                    }
                }
        }
    }

    private fun updateDraft(text: String) {
        val validationMessage = when (val validation = validateMessageText(text)) {
            MessageValidation.Blank,
            is MessageValidation.Valid,
            -> null

            is MessageValidation.TooLong -> "Message must be ${validation.maximumLength} characters or fewer."
        }
        mutableState.update {
            it.copy(draftText = text, validationMessage = validationMessage, notice = null)
        }
    }

    private fun submit() {
        if (mutableState.value.isSubmitting) return
        val validation = validateMessageText(mutableState.value.draftText)
        if (validation !is MessageValidation.Valid) {
            mutableState.update {
                it.copy(validationMessage = validation.toPresentationMessage())
            }
            return
        }
        mutableState.update { it.copy(isSubmitting = true, validationMessage = null, notice = null) }
        scope.launch {
            val result = runCatching { interactor.createMessage(validation.normalizedText) }
                .getOrElse {
                    mutableState.update {
                        it.copy(
                            isSubmitting = false,
                            notice = MessagesNotice("Unable to save message.", MessagesNoticeKind.ERROR),
                        )
                    }
                    return@launch
                }
            when (result) {
                is CreateMessageResult.Created -> mutableState.update {
                    it.copy(
                        draftText = "",
                        isSubmitting = false,
                        notice = MessagesNotice("Saved offline. Waiting to sync.", MessagesNoticeKind.INFO),
                    )
                }

                is CreateMessageResult.Rejected -> mutableState.update {
                    it.copy(isSubmitting = false, validationMessage = result.validation.toPresentationMessage())
                }
            }
        }
    }

    private fun refresh() {
        if (mutableState.value.isRefreshing) return
        val shouldRecoverObservation = mutableState.value.loadError != null
        mutableState.update { it.copy(isRefreshing = true, notice = null) }
        scope.launch {
            val result = runCatching { interactor.refresh() }
                .getOrElse { SyncResult.Retry("Unable to refresh messages.") }
            mutableState.update {
                it.copy(isRefreshing = false, notice = result.toNotice("Messages are up to date."))
            }
            if (shouldRecoverObservation && result == SyncResult.Success) {
                observeMessages()
            }
        }
    }

    private fun retry(localId: String) {
        scope.launch {
            mutableState.update { it.copy(notice = null) }
            val result = runCatching { interactor.retry(localId) }
                .getOrElse { SyncResult.Retry("Unable to retry message.") }
            mutableState.update {
                it.copy(notice = result.toNotice("Retry started."))
            }
        }
    }
}

private fun MessageValidation.toPresentationMessage(): String = when (this) {
    MessageValidation.Blank -> "Enter a message before saving."
    is MessageValidation.TooLong -> "Message must be $maximumLength characters or fewer."
    is MessageValidation.Valid -> ""
}

private fun SyncResult.toNotice(successMessage: String): MessagesNotice = when (this) {
    SyncResult.Success -> MessagesNotice(successMessage, MessagesNoticeKind.INFO)
    is SyncResult.Retry -> MessagesNotice(reason, MessagesNoticeKind.ERROR)
    is SyncResult.PermanentFailure -> MessagesNotice(reason, MessagesNoticeKind.ERROR)
}

public data class MessagesUiDependencies(
    public val stateHolder: MessagesStateHolder,
)
