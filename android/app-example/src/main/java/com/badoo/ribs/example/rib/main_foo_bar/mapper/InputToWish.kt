package com.badoo.ribs.example.rib.main_foo_bar.mapper

import com.badoo.ribs.example.rib.main_foo_bar.FooBar
import com.badoo.ribs.example.rib.main_foo_bar.feature.FooBarFeature

internal object InputToWish : (FooBar.Input) -> FooBarFeature.Wish? {

    override fun invoke(event: FooBar.Input): FooBarFeature.Wish? =
        TODO("Implement FooBarInputToWish mapping")
}
