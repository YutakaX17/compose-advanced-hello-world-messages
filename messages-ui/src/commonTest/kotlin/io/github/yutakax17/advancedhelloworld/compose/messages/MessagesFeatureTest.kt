package io.github.yutakax17.advancedhelloworld.compose.messages

import io.github.yutakax17.advancedhelloworld.core.SyncResult
import io.github.yutakax17.advancedhelloworld.messages.CreateMessageResult
import io.github.yutakax17.advancedhelloworld.messages.Message
import io.github.yutakax17.advancedhelloworld.messages.MessageSyncState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MessagesFeatureTest {
    @Test
    fun exportsMessagesRootDestination() = runTest {
        val feature = MessagesFeatureFactory.create(
            MessagesUiDependencies(MessagesStateHolder(FakeInteractor(), backgroundScope)),
        )

        assertEquals("messages", feature.id)
        assertEquals("/", feature.destinations.single().route)
    }

    @Test
    fun observesRepositoryAndSubmitsNormalizedDraft() = runTest {
        val interactor = FakeInteractor()
        val holder = MessagesStateHolder(interactor, backgroundScope)
        runCurrent()

        interactor.messages.value = listOf(message("local-1", MessageSyncState.PENDING))
        holder.onEvent(MessagesEvent.DraftChanged("  hello offline  "))
        holder.onEvent(MessagesEvent.Submit)
        runCurrent()

        assertEquals("hello offline", interactor.createdText)
        assertEquals("", holder.state.value.draftText)
        assertEquals("Saved offline. Waiting to sync.", holder.state.value.notice?.text)
        assertEquals("local-1", holder.state.value.messages.single().localId)
    }

    @Test
    fun validatesDraftAndSerializesRefreshEvents() = runTest {
        val interactor = FakeInteractor()
        val holder = MessagesStateHolder(interactor, backgroundScope)
        runCurrent()

        holder.onEvent(MessagesEvent.Submit)
        assertEquals("Enter a message before saving.", holder.state.value.validationMessage)

        holder.onEvent(MessagesEvent.DraftChanged("x".repeat(501)))
        assertEquals("Message must be 500 characters or fewer.", holder.state.value.validationMessage)

        holder.onEvent(MessagesEvent.Refresh)
        runCurrent()
        assertEquals(1, interactor.refreshCount)
        assertFalse(holder.state.value.isRefreshing)
        assertEquals("Messages are up to date.", holder.state.value.notice?.text)
    }

    @Test
    fun exposesRetryFailureAsErrorFeedback() = runTest {
        val interactor = FakeInteractor(retryResult = SyncResult.Retry("Connect and try again."))
        val holder = MessagesStateHolder(interactor, backgroundScope)
        runCurrent()

        holder.onEvent(MessagesEvent.Retry("local-2"))
        runCurrent()

        assertEquals("local-2", interactor.retriedId)
        assertIs<MessagesNoticeKind>(holder.state.value.notice?.kind)
        assertEquals(MessagesNoticeKind.ERROR, holder.state.value.notice?.kind)
    }

    @Test
    fun successfulRefreshRecoversFailedObservationWithoutDuplicateCollectors() = runTest {
        val interactor = RecoveringInteractor()
        val holder = MessagesStateHolder(interactor, backgroundScope)
        runCurrent()

        assertEquals("Unable to load messages.", holder.state.value.loadError)
        assertFalse(holder.state.value.isLoading)
        assertEquals(1, interactor.subscriptionCount)

        holder.onEvent(MessagesEvent.Refresh)
        assertTrue(holder.state.value.isRefreshing)
        runCurrent()

        assertEquals(2, interactor.subscriptionCount)
        assertEquals(null, holder.state.value.loadError)
        assertFalse(holder.state.value.isLoading)
        assertEquals("recovered", holder.state.value.messages.single().localId)

        holder.onEvent(MessagesEvent.Refresh)
        runCurrent()
        assertEquals(2, interactor.subscriptionCount)
    }
}

internal class FakeInteractor(
    private val retryResult: SyncResult = SyncResult.Success,
) : MessagesInteractor {
    val messages = MutableStateFlow<List<Message>>(emptyList())
    var createdText: String? = null
    var refreshCount: Int = 0
    var retriedId: String? = null

    override fun observeMessages(): Flow<List<Message>> = messages

    override suspend fun createMessage(text: String): CreateMessageResult {
        createdText = text
        return CreateMessageResult.Created(message("created", MessageSyncState.PENDING))
    }

    override suspend fun refresh(): SyncResult {
        refreshCount += 1
        return SyncResult.Success
    }

    override suspend fun retry(localId: String): SyncResult {
        retriedId = localId
        return retryResult
    }
}

private class RecoveringInteractor : MessagesInteractor {
    var subscriptionCount: Int = 0

    override fun observeMessages(): Flow<List<Message>> = flow {
        subscriptionCount += 1
        if (subscriptionCount == 1) {
            error("First subscription failed")
        }
        emit(listOf(message("recovered", MessageSyncState.SYNCED)))
    }

    override suspend fun createMessage(text: String): CreateMessageResult = error("Not used")

    override suspend fun refresh(): SyncResult = SyncResult.Success

    override suspend fun retry(localId: String): SyncResult = error("Not used")
}

internal fun message(localId: String, syncState: MessageSyncState): Message = Message(
    localId = localId,
    remoteId = null,
    text = "Message $localId",
    createdAtLocal = 1L,
    createdAtServer = null,
    syncState = syncState,
)
