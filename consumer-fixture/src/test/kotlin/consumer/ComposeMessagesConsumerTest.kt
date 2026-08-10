package consumer

import io.github.yutakax17.advancedhelloworld.compose.messages.MessagesActions
import io.github.yutakax17.advancedhelloworld.compose.messages.MessagesFeatureFactory
import io.github.yutakax17.advancedhelloworld.compose.messages.MessagesState
import io.github.yutakax17.advancedhelloworld.compose.messages.MessagesUiDependencies
import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeMessagesConsumerTest {
    @Test
    fun resolvesPublishedFeatureContract() {
        val feature = MessagesFeatureFactory.create(
            MessagesUiDependencies(MessagesState(), NoOpActions),
        )

        assertEquals("messages", feature.id)
    }
}

private object NoOpActions : MessagesActions {
    override fun updateDraft(text: String) = Unit

    override fun submit() = Unit

    override fun refresh() = Unit

    override fun retry(localId: String) = Unit
}
