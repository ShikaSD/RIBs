package com.badoo.ribs.example.rib.main_dialog_example

import com.nhaarman.mockitokotlin2.mock
import org.junit.After
import org.junit.Before
import org.junit.Test

class DialogExampleInteractorTest {

    private val router: DialogExampleRouter = mock()
    private lateinit var interactor: DialogExampleInteractor

    @Before
    fun setup() {
        interactor = DialogExampleInteractor(
            savedInstanceState = null,
            router = router,
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
