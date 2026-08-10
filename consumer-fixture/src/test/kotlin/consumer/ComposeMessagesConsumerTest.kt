package consumer

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.yutakax17.advancedhelloworld.compose.messages.MessagesFeatureFactory
import io.github.yutakax17.advancedhelloworld.compose.messages.MessagesInteractor
import io.github.yutakax17.advancedhelloworld.compose.messages.MessagesStateHolder
import io.github.yutakax17.advancedhelloworld.compose.messages.MessagesUiDependencies
import io.github.yutakax17.advancedhelloworld.core.SyncResult
import io.github.yutakax17.advancedhelloworld.messages.CreateMessageResult
import io.github.yutakax17.advancedhelloworld.messages.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ComposeMessagesConsumerTest {
    @Test
    fun rendersPublishedFeatureArtifact() = runComposeUiTest {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val feature = MessagesFeatureFactory.create(
            MessagesUiDependencies(MessagesStateHolder(EmptyInteractor, scope)),
        )

        try {
            setContent { feature.destinations.single().content() }
            onNodeWithText("No messages yet").assertIsDisplayed()
            onNodeWithText("Refresh").assertIsDisplayed()
        } finally {
            scope.cancel()
        }
    }
}

private object EmptyInteractor : MessagesInteractor {
    override fun observeMessages(): Flow<List<Message>> = flowOf(emptyList())

    override suspend fun createMessage(text: String): CreateMessageResult = error("Not used")

    override suspend fun refresh(): SyncResult = SyncResult.Success

    override suspend fun retry(localId: String): SyncResult = SyncResult.Success
}
