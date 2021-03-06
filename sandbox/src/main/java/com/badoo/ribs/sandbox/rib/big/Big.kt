package com.badoo.ribs.sandbox.rib.big

import com.badoo.ribs.core.Rib
import com.badoo.ribs.core.customisation.RibCustomisation
import com.badoo.ribs.portal.CanProvidePortal

interface Big : Rib {

    interface Dependency : CanProvidePortal

    class Customisation(
        val viewFactory: BigView.Factory = BigViewImpl.Factory()
    ) : RibCustomisation
}
