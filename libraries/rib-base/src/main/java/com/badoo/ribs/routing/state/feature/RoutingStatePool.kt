package com.badoo.ribs.routing.state.feature

import android.os.Parcelable
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.element.TimeCapsule
import com.badoo.mvicore.feature.ActorReducerFeature
import com.badoo.ribs.annotation.OutdatedDocumentation
import com.badoo.ribs.core.Node
import com.badoo.ribs.routing.activator.RoutingActivator
import com.badoo.ribs.routing.state.changeset.RoutingCommand
import com.badoo.ribs.routing.state.changeset.RoutingCommand.Add
import com.badoo.ribs.routing.state.RoutingContext
import com.badoo.ribs.routing.state.RoutingContext.ActivationState.ACTIVE
import com.badoo.ribs.routing.state.RoutingContext.ActivationState.INACTIVE
import com.badoo.ribs.routing.state.RoutingContext.ActivationState.SLEEPING
import com.badoo.ribs.routing.state.RoutingContext.Resolved
import com.badoo.ribs.routing.resolver.RoutingResolver
import com.badoo.ribs.routing.state.feature.RoutingStatePool.Effect
import com.badoo.ribs.routing.Routing
import com.badoo.ribs.routing.state.feature.state.SavedState
import com.badoo.ribs.routing.state.feature.state.WorkingState
import com.badoo.ribs.routing.transition.handler.TransitionHandler
import com.badoo.ribs.routing.state.changeset.TransitionDescriptor
import io.reactivex.Observable

private val timeCapsuleKey = RoutingStatePool::class.java.name
private fun <C : Parcelable> TimeCapsule<SavedState<C>>.initialState(): WorkingState<C> =
    (get<SavedState<C>>(timeCapsuleKey)
        ?.let { it.toWorkingState() }
        ?: WorkingState())

/**
 * FIXME rewrite
 *
 * State store responsible for executing [RoutingCommand]s it takes as inputs.
 *
 * The [WorkingState] contains a pool of [RoutingContext] elements referenced
 * by [Routing] objects. Practically, these keep reference to all configurations
 * currently associated with the RIB: all initial configurations (typically permanent parts
 * and one content type) + the ones coming from back stack changes.
 *
 * Any given [RoutingContext] in the pool can be typically in [ACTIVE] or [INACTIVE] state,
 * respective to whether it is active on the screen.
 * Last elements in the back stack are activated, others are deactivated.
 * Permanent parts are added and activated on initialisation and never deactivated as long as
 * the view is available.
 */
@OutdatedDocumentation
internal class RoutingStatePool<C : Parcelable>(
    timeCapsule: TimeCapsule<SavedState<C>>,
    resolver: RoutingResolver<C>,
    activator: RoutingActivator<C>,
    parentNode: Node<*>,
    transitionHandler: TransitionHandler<C>?
) : ActorReducerFeature<Transaction<C>, Effect<C>, WorkingState<C>, Nothing>(
    initialState = timeCapsule.initialState<C>(),
    bootstrapper = BootStrapperImpl(timeCapsule.initialState<C>()),
    actor = Actor(
        resolver = resolver,
        activator = activator,
        parentNode = parentNode,
        transitionHandler = transitionHandler
    ),
    reducer = ReducerImpl()
) {
    init {
        timeCapsule.register(timeCapsuleKey) { state.toSavedState() }
    }

    sealed class Effect<C : Parcelable> {
        sealed class Global<C : Parcelable>: Effect<C>() {
            class WakeUp<C : Parcelable>: Global<C>()
            class Sleep<C : Parcelable>: Global<C>()
            class SaveInstanceState<C : Parcelable>(
                val updatedElements: Map<Routing<C>, Resolved<C>>
            ): Global<C>()
        }

        sealed class Individual<C : Parcelable>: Effect<C>() {
            abstract val key: Routing<C>

            class Added<C : Parcelable>(
                override val key: Routing<C>,
                val updatedElement: Resolved<C>
            ) : Individual<C>()

            class Removed<C : Parcelable>(
                override val key: Routing<C>,
                val updatedElement: Resolved<C>
            ) : Individual<C>()

            class Activated<C : Parcelable>(
                override val key: Routing<C>,
                val updatedElement: Resolved<C>
            ) : Individual<C>()

            class Deactivated<C : Parcelable>(
                override val key: Routing<C>,
                val updatedElement: Resolved<C>
            ) : Individual<C>()

            class PendingDeactivateTrue<C : Parcelable>(
                override val key: Routing<C>
            ) : Individual<C>()

            class PendingDeactivateFalse<C : Parcelable>(
                override val key: Routing<C>
            ) : Individual<C>()

            class PendingRemovalTrue<C : Parcelable>(
                override val key: Routing<C>
            ) : Individual<C>()

            class PendingRemovalFalse<C : Parcelable>(
                override val key: Routing<C>
            ) : Individual<C>()
        }

        data class TransitionStarted<C : Parcelable>(
            val transition: OngoingTransition<C>
        ) : Effect<C>()

        data class TransitionFinished<C : Parcelable>(
            val transition: OngoingTransition<C>
        ) : Effect<C>()
    }

    class BootStrapperImpl<C : Parcelable>(
        private val initialState: WorkingState<C>
    ) : Bootstrapper<Transaction<C>> {

        override fun invoke(): Observable<Transaction<C>> =
            when {
                initialState.pool.isNotEmpty() -> Observable.just(
                    Transaction.RoutingChange(
                        descriptor = TransitionDescriptor.None,
                        changeset = initialState.pool
                            .filter { it.value.activationState == SLEEPING }
                            .map { Add(it.key) }
                    )
                )
                else -> Observable.empty()
            }
    }

    /**
     * Creates a new [WorkingState] based on the old one, plus the applied [Effect].
     *
     * Involves changing [WorkingState.activationLevel] in case of [Effect.Global],
     * and replacing elements of the [WorkingState.pool] changed by actions in [ActorImpl].
     */
    class ReducerImpl<C : Parcelable> : Reducer<WorkingState<C>, Effect<C>> {
        override fun invoke(state: WorkingState<C>, effect: Effect<C>): WorkingState<C> =
            when (effect) {
                is Effect.Global -> state.global(effect)
                is Effect.Individual -> state.individual(effect)
                is Effect.TransitionStarted -> state.copy(ongoingTransitions = state.ongoingTransitions + effect.transition)
                is Effect.TransitionFinished -> state.copy(ongoingTransitions = state.ongoingTransitions - effect.transition)
            }

        private fun WorkingState<C>.global(effect: Effect.Global<C>): WorkingState<C> =
            when (effect) {
                is Effect.Global.Sleep -> copy(
                    activationLevel = SLEEPING
                )
                is Effect.Global.WakeUp -> copy(
                    activationLevel = ACTIVE
                )
                is Effect.Global.SaveInstanceState -> copy(
                    pool = pool + effect.updatedElements
                )
            }

        private fun WorkingState<C>.individual(effect: Effect.Individual<C>): WorkingState<C> {
            val key = effect.key

            return when (effect) {
                is Effect.Individual.Added -> copy(
                    pool = pool.plus(key to effect.updatedElement)
                )
                is Effect.Individual.Removed -> copy(
                    pool = pool.minus(key)
                )
                is Effect.Individual.Activated -> copy(
                    pool = pool.minus(key).plus(key to effect.updatedElement)
                )
                is Effect.Individual.Deactivated -> copy(
                    pool = pool.minus(key).plus(key to effect.updatedElement)
                )
                is Effect.Individual.PendingDeactivateTrue -> copy(
                    pendingDeactivate = pendingDeactivate + effect.key
                )
                is Effect.Individual.PendingDeactivateFalse -> copy(
                    pendingDeactivate = pendingDeactivate - effect.key
                )
                is Effect.Individual.PendingRemovalTrue -> copy(
                    pendingRemoval = pendingRemoval + effect.key
                )
                is Effect.Individual.PendingRemovalFalse -> copy(
                    pendingRemoval = pendingRemoval - effect.key
                )
            }
        }
    }

    override fun dispose() {
        super.dispose()
        state.ongoingTransitions.forEach {
            it.dispose()
        }
    }
}
