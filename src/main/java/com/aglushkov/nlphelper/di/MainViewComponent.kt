package com.aglushkov.nlphelper.di

import com.aglushkov.di.ViewComp
import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlphelper.MainView
import dagger.Component

@ViewComp
@Component(
        dependencies = [MainViewComponent.Dependencies::class],
        modules = [MainViewModule::class])
interface MainViewComponent {
    fun inject(app: MainView)

    @Component.Factory
    interface Builder {
        fun create(dependencies: Dependencies): MainViewComponent
    }

    interface Dependencies {
        fun nlpCore(): NLPCore
    }
}