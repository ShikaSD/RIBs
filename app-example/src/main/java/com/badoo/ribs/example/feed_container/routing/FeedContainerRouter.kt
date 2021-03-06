package com.badoo.ribs.example.feed_container.routing

import android.os.Parcelable
import com.badoo.ribs.core.modality.BuildParams
import com.badoo.ribs.example.feed_container.routing.FeedContainerRouter.Configuration
import com.badoo.ribs.example.feed_container.routing.FeedContainerRouter.Configuration.Permanent
import com.badoo.ribs.routing.Routing
import com.badoo.ribs.routing.action.AttachRibRoutingAction.Companion.attach
import com.badoo.ribs.routing.action.RoutingAction
import com.badoo.ribs.routing.action.RoutingAction.Companion.noop
import com.badoo.ribs.routing.router.Router
import com.badoo.ribs.routing.source.RoutingSource.Companion.permanent
import com.badoo.ribs.routing.transition.handler.TransitionHandler
import kotlinx.android.parcel.Parcelize

class FeedContainerRouter internal constructor(
    buildParams: BuildParams<*>,
    private val builders: FeedContainerChildBuilders,
    transitionHandler: TransitionHandler<Configuration>? = null
) : Router<Configuration>(
    buildParams = buildParams,
    routingSource = permanent(Permanent.AppBar, Permanent.PhotoFeed),
    transitionHandler = transitionHandler
) {
    sealed class Configuration : Parcelable {
        sealed class Permanent : Configuration() {
            @Parcelize
            object AppBar : Configuration()

            @Parcelize
            object PhotoFeed : Configuration()
        }
    }

    override fun resolve(routing: Routing<Configuration>): RoutingAction =
        with(builders) {
            when (routing.configuration) {
                is Permanent.AppBar -> noop()
                is Permanent.PhotoFeed -> attach { photoFeedBuilder.build(it) }
            }
        }
}

