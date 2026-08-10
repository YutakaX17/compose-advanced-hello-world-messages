package io.github.yutakax17.advancedhelloworld.compose.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import io.github.yutakax17.advancedhelloworld.compose.core.EmptyState
import io.github.yutakax17.advancedhelloworld.compose.core.ErrorState
import io.github.yutakax17.advancedhelloworld.compose.core.LoadingState
import io.github.yutakax17.advancedhelloworld.compose.core.PendingState
import io.github.yutakax17.advancedhelloworld.messages.Message
import io.github.yutakax17.advancedhelloworld.messages.MessageSyncState

private val minimumTouchTarget = 48.dp

@Composable
public fun MessagesScreen(
    state: MessagesState,
    onEvent: (MessagesEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MessageComposer(state = state, onEvent = onEvent)
        state.notice?.let { notice -> NoticeFeedback(notice) }
        MessagesContent(state = state, onEvent = onEvent)
    }
}

@Composable
private fun MessageComposer(
    state: MessagesState,
    onEvent: (MessagesEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = state.draftText,
            onValueChange = { onEvent(MessagesEvent.DraftChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Message composer" },
            label = { Text("Message") },
            supportingText = {
                Text(state.validationMessage ?: "${state.draftText.trim().length}/500")
            },
            isError = state.validationMessage != null,
            enabled = !state.isSubmitting,
            singleLine = false,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onEvent(MessagesEvent.Submit) },
                modifier = Modifier
                    .sizeIn(minWidth = minimumTouchTarget, minHeight = minimumTouchTarget)
                    .semantics {
                        contentDescription = "Save message"
                        role = Role.Button
                    },
                enabled = state.draftText.isNotBlank() &&
                    state.draftText.trim().length <= 500 &&
                    !state.isSubmitting,
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator()
                } else {
                    Text("Save")
                }
            }
            Button(
                onClick = { onEvent(MessagesEvent.Refresh) },
                modifier = Modifier
                    .sizeIn(minWidth = minimumTouchTarget, minHeight = minimumTouchTarget)
                    .semantics {
                        contentDescription = "Refresh messages"
                        role = Role.Button
                        stateDescription = if (state.isRefreshing) "Refreshing" else "Ready"
                    },
                enabled = !state.isRefreshing,
            ) {
                if (state.isRefreshing) {
                    CircularProgressIndicator()
                } else {
                    Text("Refresh")
                }
            }
        }
    }
}

@Composable
private fun NoticeFeedback(notice: MessagesNotice) {
    Text(
        notice.text,
        color = if (notice.kind == MessagesNoticeKind.ERROR) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        modifier = Modifier.semantics {
            liveRegion = if (notice.kind == MessagesNoticeKind.ERROR) {
                LiveRegionMode.Assertive
            } else {
                LiveRegionMode.Polite
            }
        },
    )
}

@Composable
private fun MessagesContent(
    state: MessagesState,
    onEvent: (MessagesEvent) -> Unit,
) {
    when {
        state.isLoading -> LoadingState(message = "Loading messages")

        state.loadError != null -> ErrorState(
            message = state.loadError,
            onRetry = { onEvent(MessagesEvent.Refresh) },
        )

        state.messages.isEmpty() -> EmptyState(
            title = "No messages yet",
            message = "Saved messages will appear here, including messages waiting to sync.",
        )

        else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.messages, key = Message::localId) { message ->
                MessageRow(message = message, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun MessageRow(
    message: Message,
    onEvent: (MessagesEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(message.text)
        when (message.syncState) {
            MessageSyncState.PENDING -> PendingState(message = "Waiting to sync")

            MessageSyncState.SYNCING -> LoadingState(message = "Syncing")

            MessageSyncState.SYNCED -> Text(
                "Synced",
                modifier = Modifier.semantics { stateDescription = "Message synced" },
            )

            MessageSyncState.FAILED_PERMANENT -> ErrorState(
                message = "Message failed to sync.",
                onRetry = { onEvent(MessagesEvent.Retry(message.localId)) },
            )
        }
    }
}
