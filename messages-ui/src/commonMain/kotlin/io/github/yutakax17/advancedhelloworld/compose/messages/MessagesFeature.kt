package io.github.yutakax17.advancedhelloworld.compose.messages

import io.github.yutakax17.advancedhelloworld.compose.core.FeatureDestination
import io.github.yutakax17.advancedhelloworld.compose.core.FeatureUi
import io.github.yutakax17.advancedhelloworld.compose.core.FeatureUiFactory

public object MessagesFeatureFactory : FeatureUiFactory<MessagesUiDependencies> {
    override fun create(dependencies: MessagesUiDependencies): FeatureUi =
        FeatureUi(
            id = "messages",
            destinations = listOf(
                FeatureDestination(route = "/", title = "Messages") {
                    MessagesScreen(dependencies.state, dependencies.actions)
                },
            ),
        )
}
