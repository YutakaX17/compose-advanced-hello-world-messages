package io.github.yutakax17.advancedhelloworld.compose.messages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.yutakax17.advancedhelloworld.compose.core.AdvancedHelloWorldTheme
import io.github.yutakax17.advancedhelloworld.messages.MessageSyncState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class MessagesScreenTest {
    @Test
    fun composerValidationAndSubmissionAreExposed() = runComposeUiTest {
        val events = mutableListOf<MessagesEvent>()
        var state by mutableStateOf(MessagesState(isLoading = false))
        setContent {
            AdvancedHelloWorldTheme {
                MessagesScreen(state, events::add)
            }
        }

        onNodeWithContentDescription("Message composer").assertIsDisplayed()
        onNodeWithContentDescription("Save message").assertIsNotEnabled()
        onNodeWithContentDescription("Message composer").performTextInput("Ready")
        assertEquals(MessagesEvent.DraftChanged("Ready"), events.single())
        events.clear()

        state = MessagesState(draftText = "Ready", isLoading = false)
        waitForIdle()
        onNodeWithContentDescription("Save message")
            .assertIsEnabled()
            .assertHasClickAction()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        assertIs<MessagesEvent.Submit>(events.single())
    }

    @Test
    fun rendersSharedLoadingAndRetryableErrorStates() = runComposeUiTest {
        val events = mutableListOf<MessagesEvent>()
        var state by mutableStateOf(MessagesState())
        setContent {
            AdvancedHelloWorldTheme {
                MessagesScreen(state, events::add)
            }
        }
        onNodeWithText("Loading messages").assertIsDisplayed()

        state = MessagesState(isLoading = false, loadError = "Unable to load messages.")
        waitForIdle()
        onNodeWithText("Unable to load messages.").assertIsDisplayed()
        onNodeWithText("Retry").performClick()
        assertIs<MessagesEvent.Refresh>(events.single())
    }

    @Test
    fun refreshProgressIsAccessibleAndDisabled() = runComposeUiTest {
        setContent {
            AdvancedHelloWorldTheme {
                MessagesScreen(MessagesState(isLoading = false, isRefreshing = true)) { }
            }
        }

        onNodeWithContentDescription("Refresh messages")
            .assertIsDisplayed()
            .assertIsNotEnabled()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Refreshing"))
    }

    @Test
    fun rendersSharedEmptyAndAllSynchronizationStates() = runComposeUiTest {
        var state by mutableStateOf(MessagesState(isLoading = false))
        setContent {
            AdvancedHelloWorldTheme {
                MessagesScreen(state) { }
            }
        }
        onNodeWithText("No messages yet").assertIsDisplayed()

        state = MessagesState(
            isLoading = false,
            messages = listOf(
                message("pending", MessageSyncState.PENDING),
                message("syncing", MessageSyncState.SYNCING),
                message("synced", MessageSyncState.SYNCED),
                message("failed", MessageSyncState.FAILED_PERMANENT),
            ),
        )
        waitForIdle()

        onNodeWithText("Waiting to sync").assertIsDisplayed()
        onNodeWithText("Syncing").assertIsDisplayed()
        onNodeWithText("Synced").assertIsDisplayed()
        onNodeWithText("Message failed to sync.").assertIsDisplayed()
        onNodeWithText("Retry").assertHasClickAction()
    }

    @Test
    fun retryAndAssertiveErrorFeedbackEmitEvents() = runComposeUiTest {
        val events = mutableListOf<MessagesEvent>()
        setContent {
            AdvancedHelloWorldTheme {
                MessagesScreen(
                    MessagesState(
                        isLoading = false,
                        messages = listOf(message("failed", MessageSyncState.FAILED_PERMANENT)),
                        notice = MessagesNotice("Unable to synchronize.", MessagesNoticeKind.ERROR),
                    ),
                    events::add,
                )
            }
        }

        onNodeWithText("Unable to synchronize.")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Assertive))
        onNodeWithText("Retry").performClick()
        assertEquals(MessagesEvent.Retry("failed"), events.single())
    }
}
