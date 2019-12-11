package com.badoo.ribs.example.rib.switcher

import com.badoo.ribs.android.CanProvideActivityStarter
import com.badoo.ribs.android.CanProvidePermissionRequester
import com.badoo.ribs.core.Rib
import com.badoo.ribs.customisation.CanProvidePortal
import com.badoo.ribs.customisation.CanProvideRibCustomisation
import com.badoo.ribs.customisation.RibCustomisation
import com.badoo.ribs.dialog.CanProvideDialogLauncher
import com.badoo.ribs.example.rib.main_hello_world.HelloWorld
import com.badoo.ribs.example.util.CoffeeMachine
import io.reactivex.Single

interface Switcher : Rib {

    interface Dependency :
        CanProvideActivityStarter,
        CanProvidePermissionRequester,
        CanProvideDialogLauncher,
        CanProvideRibCustomisation,
        CanProvidePortal {

        fun coffeeMachine(): CoffeeMachine
    }

    class Customisation(
        val viewFactory: SwitcherView.Factory = SwitcherViewImpl.Factory()
    ) : RibCustomisation

    interface Workflow {
        fun attachHelloWorld(): Single<HelloWorld.Workflow>
        fun testCrash(): Single<HelloWorld.Workflow>
        fun waitForHelloWorld(): Single<HelloWorld.Workflow>
        fun doSomethingAndStayOnThisNode(): Single<Switcher.Workflow>
    }

}
