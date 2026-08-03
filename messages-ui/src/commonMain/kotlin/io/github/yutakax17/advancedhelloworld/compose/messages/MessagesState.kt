package io.github.yutakax17.advancedhelloworld.compose.messages

import io.github.yutakax17.advancedhelloworld.messages.Message

public data class MessagesState(
    public val messages: List<Message> = emptyList(),
    public val draftText: String = "",
    public val isRefreshing: Boolean = false,
    public val userMessage: String? = null,
)

public interface MessagesActions {
    public fun updateDraft(text: String)

    public fun submit()

    public fun refresh()

    public fun retry(localId: String)
}

public data class MessagesUiDependencies(
    public val state: MessagesState,
    public val actions: MessagesActions,
)
