package com.aglushkov.nlphelper.di

import com.aglushkov.di.AppComp
import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlphelper.MainApp
import dagger.Component

@AppComp
@Component(modules = [AppModule::class] )
interface AppComponent: MainViewComponent.Dependencies {
    override fun nlpCore(): NLPCore

    fun inject(app: MainApp)
}