package com.badoo.ribs.example.rib.main_dialog_example

import com.nhaarman.mockitokotlin2.mock
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class MainDialogExampleRouterTest {

    private var router: MainDialogExampleRouter? = null

    @Before
    fun setup() {
        router = MainDialogExampleRouter(
            savedInstanceState = null,
            dialogLauncher = mock(),
            simpleDialog = mock(),
            lazyDialog = mock(),
            ribDialog = mock()
        )
    }

    @After
    fun tearDown() {
    }

    /**
     * TODO: Add real tests.
     */
    @Test
    fun `an example test with some conditions should pass`() {
    }
}
