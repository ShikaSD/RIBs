package com.badoo.ribs.example.rib.main_hello_world.mapper

import com.badoo.ribs.example.rib.main_hello_world.HelloWorldView
import com.badoo.ribs.example.rib.main_hello_world.feature.HelloWorldFeature

internal object ViewEventToWish : (HelloWorldView.Event) -> HelloWorldFeature.Wish? {

    override fun invoke(event: HelloWorldView.Event): HelloWorldFeature.Wish? =
        TODO("Implement HelloWorldViewEventToWish mapping")
}
