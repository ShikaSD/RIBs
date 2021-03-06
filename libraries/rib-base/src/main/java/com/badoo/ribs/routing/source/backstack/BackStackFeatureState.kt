package com.badoo.ribs.routing.source.backstack

import android.os.Parcelable
import com.badoo.ribs.routing.history.RoutingHistory
import com.badoo.ribs.routing.history.RoutingHistoryElement
import kotlinx.android.parcel.Parcelize


@Parcelize
data class BackStackFeatureState<C : Parcelable>(
    val backStack: BackStack<C> = emptyList()
) : Parcelable, RoutingHistory<C> {

    val current: RoutingHistoryElement<C>?
        get() = backStack.lastOrNull()

    override fun iterator(): Iterator<RoutingHistoryElement<C>> =
        backStack.iterator()
}


