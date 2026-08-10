package io.github.yutakax17.advancedhelloworld.compose.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.yutakax17.advancedhelloworld.messages.Message
import io.github.yutakax17.advancedhelloworld.messages.MessageSyncState

@Composable
public fun MessagesScreen(
    state: MessagesState,
    actions: MessagesActions,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = state.draftText,
            onValueChange = actions::updateDraft,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Message") },
            supportingText = { Text("${state.draftText.trim().length}/500") },
            singleLine = false,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = actions::submit,
                enabled = state.draftText.isNotBlank() && state.draftText.trim().length <= 500,
            ) {
                Text("Save")
            }
            Button(onClick = actions::refresh, enabled = !state.isRefreshing) {
                if (state.isRefreshing) {
                    CircularProgressIndicator()
                } else {
                    Text("Refresh")
                }
            }
        }
        state.userMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.messages, key = Message::localId) { message ->
                MessageRow(message = message, retry = actions::retry)
            }
        }
    }
}

@Composable
private fun MessageRow(message: Message, retry: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(message.text)
        when (message.syncState) {
            MessageSyncState.PENDING -> Text("Waiting to sync")

            MessageSyncState.SYNCING -> Text("Syncing")

            MessageSyncState.SYNCED -> Text("Synced")

            MessageSyncState.FAILED_PERMANENT -> {
                Button(onClick = { retry(message.localId) }) {
                    Text("Retry")
                }
            }
        }
    }
}
