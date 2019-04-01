package com.badoo.ribs.core

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.ViewGroup
import com.badoo.ribs.base.leaf.SingleConfigurationInteractor
import com.badoo.ribs.core.Node.Companion.KEY_INTERACTOR
import com.badoo.ribs.core.Node.Companion.KEY_ROUTER
import com.badoo.ribs.core.Node.Companion.KEY_VIEW_STATE
import com.badoo.ribs.core.helper.TestNode
import com.badoo.ribs.core.helper.TestPublicRibInterface
import com.badoo.ribs.core.helper.TestRouter
import com.badoo.ribs.core.helper.TestView
import com.badoo.ribs.core.view.ViewFactory
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NodeTest {

    interface RandomOtherNode1 : Rib
    interface RandomOtherNode2 : Rib
    interface RandomOtherNode3 : Rib

    private lateinit var node: Node<TestView>
    private lateinit var view: TestView
    private lateinit var androidView: ViewGroup
    private lateinit var parentViewGroup: ViewGroup
    private lateinit var someViewGroup1: ViewGroup
    private lateinit var someViewGroup2: ViewGroup
    private lateinit var someViewGroup3: ViewGroup
    private lateinit var viewFactory: ViewFactory<TestView>
    private lateinit var router: Router<TestRouter.Configuration, TestView>
    private lateinit var interactor: SingleConfigurationInteractor<TestView>
    private lateinit var child1: TestNode
    private lateinit var child2: TestNode
    private lateinit var child3: TestNode
    private lateinit var allChildren: List<Node<*>>

    @Before
    fun setUp() {
        parentViewGroup = mock()
        someViewGroup1 = mock()
        someViewGroup2 = mock()
        someViewGroup3 = mock()
        androidView = mock()
        view = mock { on { androidView }.thenReturn(androidView) }
        viewFactory = mock { on { invoke(parentViewGroup) }.thenReturn(view) }
        router = mock()
        interactor = mock()

        node = Node(
            identifier = object : TestPublicRibInterface {},
            viewFactory = viewFactory,
            router = router,
            interactor = interactor
        )

        addChildren()
    }

    private fun addChildren() {
        child1 = TestNode(object : RandomOtherNode1 {})
        child2 = TestNode(object : RandomOtherNode2 {})
        child3 = TestNode(object : RandomOtherNode3 {})
        allChildren = listOf(child1, child2, child3)
        node.children.addAll(allChildren)
    }


    private fun attachToViewAlongWithChildren() {
        node.attachToView(parentViewGroup)
        node.attachChildView(child1)
        node.attachChildView(child2)
        node.attachChildView(child3)
    }

    @Test
    fun `Router's node is set after init`() {
        verify(router).node = node
    }

    @Test
    fun `onAttach() notifies Router`() {
        node.onAttach(null)
        verify(router).onAttach(null)
    }

    @Test
    fun `onAttach() notifies Interactor`() {
        node.onAttach(null)
        verify(interactor).onAttach(null)
    }

    @Test
    fun `A non-null Bundle in onAttach() is passed to Router`() {
        val bundle: Bundle = mock()
        val childBundle: Bundle = mock()
        whenever(bundle.getBundle(KEY_ROUTER)).thenReturn(childBundle)
        node.onAttach(bundle)
        verify(router).onAttach(childBundle)
    }

    @Test
    fun `A non-null Bundle in onAttach() is passed to Interactor`() {
        val bundle: Bundle = mock()
        val childBundle: Bundle = mock()
        whenever(bundle.getBundle(KEY_INTERACTOR)).thenReturn(childBundle)
        node.onAttach(bundle)
        verify(interactor).onAttach(childBundle)
    }

    @Test
    fun `onDetach() notifies Router`() {
        node.onDetach()
        verify(router).onDetach()
    }

    @Test
    fun `onDetach() notifies Interactor`() {
        node.onDetach()
        verify(interactor).onDetach()
    }

    @Test
    fun `onSaveInstanceState() saves view state as well`() {
        node.view = view
        node.onSaveInstanceState(mock())
        verify(androidView).saveHierarchyState(node.savedViewState)
    }

    @Test
    fun `onSaveInstanceState() is forwarded to Router`() {
        node.onSaveInstanceState(mock())
        verify(router).onSaveInstanceState(any())
    }

    @Test
    fun `Router's bundle from onSaveInstanceState() call is put inside original bundle`() {
        val bundle: Bundle = mock()
        val captor = argumentCaptor<Bundle>()
        node.onSaveInstanceState(bundle)
        verify(router).onSaveInstanceState(captor.capture())
        verify(bundle).putBundle(KEY_ROUTER, captor.firstValue)
    }

    @Test
    fun `onSaveInstanceState() is forwarded to Interactor`() {
        node.onSaveInstanceState(mock())
        verify(interactor).onSaveInstanceState(any())
    }

    @Test
    fun `Interactor's bundle from onSaveInstanceState call is put inside original bundle`() {
        val bundle: Bundle = mock()
        val captor = argumentCaptor<Bundle>()
        node.onSaveInstanceState(bundle)
        verify(interactor).onSaveInstanceState(captor.capture())
        verify(bundle).putBundle(KEY_INTERACTOR, captor.firstValue)
    }

    @Test
    fun `onStart() is forwarded to Interactor`() {
        node.onStart()
        verify(interactor).onStart()
    }

    @Test
    fun `onStop() is forwarded to Interactor`() {
        node.onStop()
        verify(interactor).onStop()
    }

    @Test
    fun `onPause() is forwarded to Interactor`() {
        node.onPause()
        verify(interactor).onPause()
    }

    @Test
    fun `onResume()() is forwarded to Interactor`() {
        node.onResume()
        verify(interactor).onResume()
    }

    @Test
    fun `onStart() is forwarded to all children`() {
        val mocks = createAndAttachChildMocks(3)
        node.onStart()
        mocks.forEach {
            verify(it).onStart()
        }
    }

    @Test
    fun `onStop() is forwarded to all children`() {
        val mocks = createAndAttachChildMocks(3)
        node.onStop()
        mocks.forEach {
            verify(it).onStop()
        }
    }

    @Test
    fun `onPause() is forwarded to all children`() {
        val mocks = createAndAttachChildMocks(3)
        node.onPause()
        mocks.forEach {
            verify(it).onPause()
        }
    }

    @Test
    fun `onResume() is forwarded to all children`() {
        val mocks = createAndAttachChildMocks(3)
        node.onResume()
        mocks.forEach {
            verify(it).onResume()
        }
    }

    @Test
    fun `Back press handling is forwarded to all children attached to the view if none can handle it`() {
        attachToViewAlongWithChildren()
        node.detachChildView(child2) // this means child2 should not even be asked
        child1.handleBackPress = false
        child2.handleBackPress = false
        child3.handleBackPress = false

        node.handleBackPress()

        assertEquals(true, child1.handleBackPressInvoked)
        assertEquals(false, child2.handleBackPressInvoked)
        assertEquals(true, child3.handleBackPressInvoked)
    }

    @Test
    fun `Back press handling is forwarded to children only until first one handles it`() {
        attachToViewAlongWithChildren()
        child1.handleBackPress = false
        child2.handleBackPress = true
        child3.handleBackPress = false

        node.handleBackPress()

        assertEquals(true, child1.handleBackPressInvoked)
        assertEquals(true, child2.handleBackPressInvoked)
        assertEquals(false, child3.handleBackPressInvoked)
    }

    @Test
    fun `Back press handling is forwarded to Interactor if no children handled it`() {
        attachToViewAlongWithChildren()
        child1.handleBackPress = false
        child2.handleBackPress = false
        child3.handleBackPress = false

        node.handleBackPress()

        verify(interactor).handleBackPress()
    }

    @Test
    fun `Back press handling is not forwarded to Interactor if any children handled it`() {
        attachToViewAlongWithChildren()
        child1.handleBackPress = false
        child2.handleBackPress = true
        child3.handleBackPress = false

        node.handleBackPress()

        verify(interactor, never()).handleBackPress()
    }

    @Test
    fun `Router back stack popping is invoked if none of the children nor the Interactor handled back press`() {
        attachToViewAlongWithChildren()
        child1.handleBackPress = false
        child2.handleBackPress = false
        child3.handleBackPress = false
        whenever(interactor.handleBackPress()).thenReturn(false)

        node.handleBackPress()

        verify(router).popBackStack()
    }

    @Test
    fun `Router back stack popping is not invoked if any of the children handled back press`() {
        attachToViewAlongWithChildren()
        child1.handleBackPress = false
        child2.handleBackPress = true
        child3.handleBackPress = false
        whenever(interactor.handleBackPress()).thenReturn(false)

        node.handleBackPress()

        verify(router, never()).popBackStack()
    }

    @Test
    fun `Router back stack popping is not invoked if Interactor handled back press`() {
        whenever(interactor.handleBackPress()).thenReturn(true)

        node.handleBackPress()

        verify(router, never()).popBackStack()
    }

    @Test
    fun `isViewAttached flag is initially false`() {
        assertEquals(false, node.isViewAttached)
    }

    @Test
    fun `attachToView() sets isViewAttached flag to true`() {
        node.attachToView(parentViewGroup)
        assertEquals(true, node.isViewAttached)
    }

    @Test
    fun `onDetachFromView() resets isViewAttached flag to false`() {
        node.attachToView(parentViewGroup)
        node.detachFromView()
        assertEquals(false, node.isViewAttached)
    }

    @Test
    fun `attachToView() forwards call to Router`() {
        node.attachToView(parentViewGroup)
        verify(router).onAttachView()
    }

    private fun createAndAttachChildMocks(n: Int, identifiers: MutableList<Rib> = mutableListOf()): List<Node<*>> {
        if (identifiers.isEmpty()) {
            for (i in 0 until n) {
                identifiers.add(object : Rib {})
            }
        }
        val mocks = mutableListOf<Node<*>>()
        for (i in 0 until n) {
            mocks.add(mock { on { identifier }.thenReturn(identifiers[i]) })
        }
        node.children.clear()
        node.children.addAll(mocks)
        return mocks
    }

    @Test
    fun `attachChildView() results in children added to parentViewGroup given Router does not define something else `() {
        whenever(router.getParentViewForChild(any(), anyOrNull())).thenReturn(null)
        val mocks = createAndAttachChildMocks(3)
        node.attachToView(parentViewGroup)
        mocks.forEach {
            node.attachChildView(it)
            verify(it).attachToView(parentViewGroup)
        }
    }

    @Test
    fun `attachToView() results in children added to target defined by Router`() {
        val n1 = object : RandomOtherNode1 {}
        val n2 = object : RandomOtherNode2 {}
        val n3 = object : RandomOtherNode3 {}
        val mocks = createAndAttachChildMocks(3, mutableListOf(n1, n2, n3))

        whenever(router.getParentViewForChild(n1, view)).thenReturn(someViewGroup1)
        whenever(router.getParentViewForChild(n2, view)).thenReturn(someViewGroup2)
        whenever(router.getParentViewForChild(n3, view)).thenReturn(someViewGroup3)

        node.attachToView(parentViewGroup)

        mocks.forEach {
            node.attachChildView(it)
            verify(it, never()).attachToView(parentViewGroup)
        }

        verify(mocks[0]).attachToView(someViewGroup1)
        verify(mocks[1]).attachToView(someViewGroup2)
        verify(mocks[2]).attachToView(someViewGroup3)
    }

    @Test
    fun `attachChild() does not imply attachToView when Android view system is not available`() {
        val child = mock<Node<*>>()
        node.attachChildNode(child, null)
        verify(child, never()).attachToView(parentViewGroup)
    }

    @Test
    fun `attachChildView() implies attachToView() when Android view system is available`() {
        val child = mock<Node<*>>()
        node.attachToView(parentViewGroup)
        node.attachChildView(child)
        verify(child).attachToView(parentViewGroup)
    }

    @Test
    fun `View state saved to bundle`() {
        val outState = mock<Bundle>()
        node.savedViewState = mock()
        node.onSaveInstanceState(outState)
        verify(outState).putSparseParcelableArray(KEY_VIEW_STATE, node.savedViewState)
    }

    @Test
    fun `View state is restored from bundle`() {
        val savedInstanceState = mock<Bundle>()
        val savedViewState = SparseArray<Parcelable>()
        whenever(savedInstanceState.getSparseParcelableArray<Parcelable>(KEY_VIEW_STATE)).thenReturn(savedViewState)

        node.onAttach(savedInstanceState)
        assertEquals(savedViewState, node.savedViewState)
    }

    @Test
    fun `saveViewState() does its job`() {
        node.view = view
        node.saveViewState()
        verify(androidView).saveHierarchyState(node.savedViewState)
    }

    @Test
    fun `attachToView() restores view state`() {
        node.savedViewState = mock()
        node.attachToView(parentViewGroup)
        verify(view.androidView).restoreHierarchyState(node.savedViewState)
    }

    @Test
    fun `attachToView() invokes viewFactory`() {
        node.attachToView(parentViewGroup)
        verify(viewFactory).invoke(parentViewGroup)
    }

    @Test
    fun `When current Node has a view, attachToView() adds view to parentViewGroup`() {
        node.attachToView(parentViewGroup)
        verify(parentViewGroup).addView(view.androidView)
    }

    @Test
    fun `When current Node doesn't have a view, attachToView() does not add anything to parentViewGroup`() {
        whenever(viewFactory.invoke(parentViewGroup)).thenReturn(null)
        node.attachToView(parentViewGroup)
        verify(parentViewGroup, never()).addView(anyOrNull())
    }

    @Test
    fun `When current Node has a view, attachToView() notifies Interactor of view creation`() {
        node.attachToView(parentViewGroup)
        verify(interactor).onViewCreated(view)
    }

    @Test
    fun `When current Node doesn't have a view, attachToView() does not notify Interactor of view creation`() {
        whenever(viewFactory.invoke(parentViewGroup)).thenReturn(null)
        node.attachToView(parentViewGroup)
        verify(interactor, never()).onViewCreated(anyOrNull())
    }
}
