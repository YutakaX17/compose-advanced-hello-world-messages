package io.github.yutakax17.advancedhelloworld.compose.messages

import kotlin.test.Test
import kotlin.test.assertEquals

class MessagesFeatureTest {
    @Test
    fun exportsMessagesRootDestination() {
        val feature = MessagesFeatureFactory.create(
            MessagesUiDependencies(
                state = MessagesState(),
                actions = NoOpActions,
            ),
        )

        assertEquals("messages", feature.id)
        assertEquals("/", feature.destinations.single().route)
    }
}

private object NoOpActions : MessagesActions {
    override fun updateDraft(text: String) = Unit
    override fun submit() = Unit
    override fun refresh() = Unit
    override fun retry(localId: String) = Unit
}
