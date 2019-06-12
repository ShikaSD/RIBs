package com.badoo.ribs.example.rib.dialog_example

import com.badoo.ribs.core.Rib
import com.badoo.ribs.core.view.ViewFactory
import com.badoo.ribs.customisation.CanProvideRibCustomisation
import com.badoo.ribs.customisation.RibCustomisation
import com.badoo.ribs.dialog.CanProvideDialogLauncher

interface DialogExample : Rib {

    interface Dependency : CanProvideDialogLauncher, CanProvideRibCustomisation

    class Customisation(
        val viewFactory: ViewFactory<Dependency, DialogExampleView> = DialogExampleViewImpl.Factory()
    ) : RibCustomisation
}
