package com.badoo.ribs.example.rib.test

import android.app.Activity.RESULT_OK
import android.app.Instrumentation
import android.content.Intent
import android.view.ViewGroup
import com.badoo.ribs.android.CanProvideActivityStarter
import com.badoo.ribs.android.CanProvidePermissionRequester
import com.badoo.ribs.core.Node
import com.badoo.ribs.customisation.CanProvidePortal
import com.badoo.ribs.customisation.RibCustomisationDirectory
import com.badoo.ribs.customisation.RibCustomisationDirectoryImpl
import com.badoo.ribs.dialog.CanProvideDialogLauncher
import com.badoo.ribs.example.app.OtherActivity
import com.badoo.ribs.example.rib.main_dialog_example.MainDialogExample
import com.badoo.ribs.example.rib.main_dialog_example.MainDialogExampleView
import com.badoo.ribs.example.rib.main_foo_bar.MainFooBar
import com.badoo.ribs.example.rib.main_foo_bar.MainFooBarView
import com.badoo.ribs.example.rib.main_hello_world.MainHelloWorld
import com.badoo.ribs.example.rib.main_hello_world.MainHelloWorldView
import com.badoo.ribs.example.rib.menu.Menu
import com.badoo.ribs.example.rib.menu.MenuView
import com.badoo.ribs.example.rib.switcher.Switcher
import com.badoo.ribs.example.rib.switcher.SwitcherView
import com.badoo.ribs.example.rib.switcher.builder.SwitcherBuilder
import com.badoo.ribs.example.rib.util.TestDefaultDependencies
import com.badoo.ribs.example.rib.util.TestView
import com.badoo.ribs.example.rib.util.component
import com.badoo.ribs.example.rib.util.subscribeOnTestObserver
import com.badoo.ribs.example.util.CoffeeMachine
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Integration test of multiple RIBs that doesn't include real UI
 * It is similar to single RIB integration test without real view implementation but includes RIB subtree.
 * It may be helpful if you want to test behaviour of multiple RIBs in composition.
 */
@RunWith(RobolectricTestRunner::class)
class MainScreenTest {

    private val dependencies = TestDefaultDependencies()

    private val menuView = TestMenuView()
    private val switcherView = TestSwitcherView()
    private val dialogExampleView = TestDialogExampleView()
    private val fooBarView = TestFooBarView()
    private val helloWorldView = TestHelloWorldView()

    private val rootRib: Node<SwitcherView> = buildRootRib()

    @Before
    fun setUp() {
        rootRib.onAttach()
        rootRib.attachToView(mock())
        rootRib.onStart()
        rootRib.onResume()

        dependencies
            .activityStarter
            .stubResponse(component<OtherActivity>(), Instrumentation.ActivityResult(RESULT_OK, null))
    }

    @Test
    fun openHelloSectionAndClickButton_launchesActivity() {
        menuView.uiEvents.accept(MenuView.Event.Select(Menu.MenuItem.HelloWorld))
        helloWorldView.uiEvents.accept(MainHelloWorldView.Event.ButtonClicked)

        dependencies.activityStarter.assertIntents {
            last().has(component<OtherActivity>())
        }
    }

    @Test
    fun openHelloSectionAndClickButton_displaysReturnedDataFromActivity() {
        val helloWorldViewModelObserver = helloWorldView.viewModel.subscribeOnTestObserver()
        dependencies.activityStarter.stubResponse(component<OtherActivity>(), Instrumentation.ActivityResult(
            RESULT_OK,
            Intent().apply { putExtra("foo", 1234) }
        ))

        menuView.uiEvents.accept(MenuView.Event.Select(Menu.MenuItem.HelloWorld))
        helloWorldView.uiEvents.accept(MainHelloWorldView.Event.ButtonClicked)

        assertThat(helloWorldViewModelObserver.values()).last().isEqualTo(MainHelloWorldView.ViewModel("Data returned: 1234"))
    }

    private fun buildRootRib() =
        SwitcherBuilder(object : Switcher.Dependency,
            CanProvideActivityStarter by dependencies,
            CanProvidePermissionRequester by dependencies,
            CanProvideDialogLauncher by dependencies,
            CanProvidePortal by dependencies {

            override fun coffeeMachine(): CoffeeMachine =
                mock()

            override fun ribCustomisation(): RibCustomisationDirectory = RibCustomisationDirectoryImpl().apply {
                put(Menu.Customisation::class, mock {
                    on { viewFactory } doReturn object : MenuView.Factory {
                        override fun invoke(deps: Nothing?): (ViewGroup) -> MenuView = {
                            menuView
                        }
                    }
                })
                put(Switcher.Customisation::class, mock {
                    on { viewFactory } doReturn object : SwitcherView.Factory {
                        override fun invoke(deps: SwitcherView.Dependency): (ViewGroup) -> SwitcherView = {
                            switcherView
                        }
                    }
                })
                put(MainDialogExample.Customisation::class, mock {
                    on { viewFactory } doReturn object : MainDialogExampleView.Factory {
                        override fun invoke(deps: Nothing?): (ViewGroup) -> MainDialogExampleView = {
                            dialogExampleView
                        }
                    }
                })
                put(MainFooBar.Customisation::class, mock {
                    on { viewFactory } doReturn object : MainFooBarView.Factory {
                        override fun invoke(deps: Nothing?): (ViewGroup) -> MainFooBarView = {
                            fooBarView
                        }
                    }
                })
                put(MainHelloWorld.Customisation::class, mock {
                    on { viewFactory } doReturn object : MainHelloWorldView.Factory {
                        override fun invoke(deps: Nothing?): (ViewGroup) -> MainHelloWorldView = {
                            helloWorldView
                        }
                    }
                })
            }
        }).build(null)

    class TestMenuView : TestView<MenuView.ViewModel, MenuView.Event>(), MenuView

    class TestFooBarView : TestView<MainFooBarView.ViewModel, MainFooBarView.Event>(), MainFooBarView

    class TestDialogExampleView : TestView<MainDialogExampleView.ViewModel, MainDialogExampleView.Event>(), MainDialogExampleView

    class TestHelloWorldView : TestView<MainHelloWorldView.ViewModel, MainHelloWorldView.Event>(), MainHelloWorldView

    class TestSwitcherView : TestView<SwitcherView.ViewModel, SwitcherView.Event>(),
        SwitcherView
}
