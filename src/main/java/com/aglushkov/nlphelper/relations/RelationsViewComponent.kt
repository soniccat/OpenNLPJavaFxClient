package com.aglushkov.nlphelper.relations

import com.aglushkov.db.SentenceRepository
import com.aglushkov.di.ViewComp
import com.aglushkov.nlphelper.app.ViewModule
import com.aglushkov.word_relation.WordRelationEngine
import dagger.Component

@ViewComp
@Component(
        dependencies = [RelationsViewComponent.Dependencies::class],
        modules = [RelationsViewModule::class, ViewModule::class])
interface RelationsViewComponent {
    fun inject(app: RelationsView)

    @Component.Factory
    interface Builder {
        fun create(dependencies: Dependencies): RelationsViewComponent
    }

    interface Dependencies {
        fun wordRelationEngine(): WordRelationEngine
        fun sentenceRepository(): SentenceRepository
    }
}