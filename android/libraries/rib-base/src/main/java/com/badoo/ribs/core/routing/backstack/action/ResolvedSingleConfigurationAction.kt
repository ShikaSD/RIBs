package com.badoo.ribs.core.routing.backstack.action

import android.os.Parcelable
import com.badoo.ribs.core.routing.backstack.ConfigurationContext
import com.badoo.ribs.core.routing.backstack.ConfigurationKey

/**
 * A convenience class to automatically call resolution on the key.
 * Child classes can act directly on the [ConfigurationContext.Resolved] element.
 */
internal abstract class ResolvedSingleConfigurationAction : SingleConfigurationAction {

    override fun <C : Parcelable> execute(
        key: ConfigurationKey,
        params: ActionExecutionParams<C>
    ): ConfigurationContext.Resolved<C> {
        val resolved = params.resolver.invoke(key)

        return execute(
            resolved,
            params
        )
    }

    abstract fun <C : Parcelable> execute(
        item: ConfigurationContext.Resolved<C>,
        params: ActionExecutionParams<C>
    ): ConfigurationContext.Resolved<C>
}
