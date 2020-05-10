package com.aglushkov.nlphelper.app

import com.aglushkov.di.AppComp
import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlphelper.main.MainViewComponent
import dagger.Component

@AppComp
@Component(modules = [AppModule::class] )
interface AppComponent: MainViewComponent.Dependencies {
    override fun nlpCore(): NLPCore

    fun inject(app: MainApp)
}