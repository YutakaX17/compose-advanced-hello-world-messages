package io.github.yutakax17.advancedhelloworld.compose.messages

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.yutakax17.advancedhelloworld.compose.core.FeatureDestination
import io.github.yutakax17.advancedhelloworld.compose.core.FeatureUi
import io.github.yutakax17.advancedhelloworld.compose.core.FeatureUiFactory

public object MessagesFeatureFactory : FeatureUiFactory<MessagesUiDependencies> {
    override fun create(dependencies: MessagesUiDependencies): FeatureUi = FeatureUi(
        id = "messages",
        destinations = listOf(
            FeatureDestination(route = "/", title = "Messages") {
                val state by dependencies.stateHolder.state.collectAsState()
                MessagesScreen(state, dependencies.stateHolder::onEvent)
            },
        ),
    )
}
