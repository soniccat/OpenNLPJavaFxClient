package com.aglushkov.di

import com.aglushkov.nlp.NLPCore
import com.aglushkov.nlphelper.MainApp
import dagger.Component

@AppComp
@Component(modules = [AppModule::class] )
interface AppComponent {
    fun getConfigService(): NLPCore

    fun inject(app: MainApp)
}