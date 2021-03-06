package com.badoo.ribs.sandbox.rib.dialog_example.builder

import com.badoo.ribs.core.modality.BuildParams
import com.badoo.ribs.sandbox.rib.dialog_example.DialogExample
import com.badoo.ribs.sandbox.rib.dialog_example.DialogExampleNode
import com.badoo.ribs.sandbox.rib.lorem_ipsum.LoremIpsum
import dagger.BindsInstance

@DialogExampleScope
@dagger.Component(
    modules = [DialogExampleModule::class],
    dependencies = [DialogExample.Dependency::class]
)
internal interface DialogExampleComponent : LoremIpsum.Dependency {

    @dagger.Component.Factory
    interface Factory {
        fun create(
            dependency: DialogExample.Dependency,
            @BindsInstance customisation: DialogExample.Customisation,
            @BindsInstance buildParams: BuildParams<Nothing?>
        ): DialogExampleComponent
    }

    fun dependency(): DialogExample.Dependency
    fun node(): DialogExampleNode
}
