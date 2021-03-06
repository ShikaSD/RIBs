package com.badoo.ribs.routing.source.backstack.operation

import android.os.Parcelable
import com.badoo.ribs.routing.source.backstack.BackStackFeature
import com.badoo.ribs.routing.history.RoutingHistoryElement
import com.badoo.ribs.routing.source.backstack.BackStack

/**
 * Operation:
 *
 * [A, B, C] + Pop = [A, B]  // no overlays
 * [A, B, C {O1}] + Pop = [A, B, C]  // overlays are popped first
 */
class Pop<C : Parcelable> : BackStackOperation<C> {

    override fun invoke(backStack: BackStack<C>): BackStack<C> =
        when {
            backStack.canPopOverlay ->
                backStack.replaceLastWith(
                    backStack.last().copy(
                        overlays = backStack.last().overlays.dropLast(1)
                    )
                )
            backStack.canPopContent -> backStack.dropLast(1)
            else -> backStack
        }

    override fun isApplicable(backStack: BackStack<C>): Boolean =
        when {
            backStack.canPopOverlay -> true
            backStack.canPop -> true
            else -> false
        }

    private fun BackStack<C>.replaceLastWith(replacement: RoutingHistoryElement<C>): BackStack<C> =
        toMutableList().apply { set(lastIndex, replacement) }
}

private val <C : Parcelable> BackStack<C>.canPopContent: Boolean
    get() = size > 1

internal val <C : Parcelable> BackStack<C>.canPop: Boolean
    get() = canPopContent || canPopOverlay

internal val <C : Parcelable> BackStack<C>.canPopOverlay: Boolean
    get() = lastOrNull()?.overlays?.isNotEmpty() == true

fun <C : Parcelable> BackStackFeature<C>.pop() {
    accept(BackStackFeature.Operation(Pop()))
}
