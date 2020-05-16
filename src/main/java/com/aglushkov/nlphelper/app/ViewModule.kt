package com.aglushkov.nlphelper.app

import com.aglushkov.di.ViewComp
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Named

@Module
class ViewModule {
    @ViewComp
    @Provides
    @Named("main")
    fun mainScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.Main + SupervisorJob())
    }

    @ViewComp
    @Provides
    @Named("io")
    fun ioScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    @ViewComp
    @Provides
    @Named("default")
    fun defaultScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.Default + SupervisorJob())
    }
}