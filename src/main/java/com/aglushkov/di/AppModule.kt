package com.aglushkov.di

import com.aglushkov.nlp.NLPCore
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Named

@Module
class AppModule {
    @AppComp
    @Provides
    @Named("main")
    fun mainScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    @AppComp
    @Provides
    @Named("io")
    fun ioScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    @AppComp
    @Provides
    fun nlpCore(@Named("io") scope: CoroutineScope): NLPCore {
        return NLPCore(scope)
    }
}