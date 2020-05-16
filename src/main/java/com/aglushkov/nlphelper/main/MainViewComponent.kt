package com.aglushkov.nlphelper.main

import com.aglushkov.db.AppDatabase
import com.aglushkov.di.ViewComp
import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlphelper.app.ViewModule
import dagger.Component

@ViewComp
@Component(
        dependencies = [MainViewComponent.Dependencies::class],
        modules = [MainViewModule::class, ViewModule::class])
interface MainViewComponent {
    fun inject(app: MainView)

    @Component.Factory
    interface Builder {
        fun create(dependencies: Dependencies): MainViewComponent
    }

    interface Dependencies {
        fun nlpCore(): NLPCore
        fun database(): AppDatabase
    }
}