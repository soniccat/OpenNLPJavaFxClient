package com.aglushkov.nlphelper.sentences

import com.aglushkov.db.AppDatabase
import com.aglushkov.di.ViewComp
import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlphelper.app.ViewModule
import dagger.Component

@ViewComp
@Component(
        dependencies = [SentencesViewComponent.Dependencies::class],
        modules = [SentencesViewModule::class, ViewModule::class])
interface SentencesViewComponent {
    fun inject(app: SentencesView)

    @Component.Factory
    interface Builder {
        fun create(dependencies: Dependencies): SentencesViewComponent
    }

    interface Dependencies {
        fun nlpCore(): NLPCore
        fun database(): AppDatabase
    }
}