package com.aglushkov.nlphelper.app

import com.aglushkov.db.AppDatabase
import com.aglushkov.db.SentenceRepository
import com.aglushkov.di.AppComp
import com.aglushkov.nlp.NLPCore
import com.aglushkov.word_relation.WordRelationEngine
import com.aglushkov.word_relation.WordRelationEngineImpl
import com.aglushkov.word_relation.WordRelationEngineImpl_Factory
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
        return CoroutineScope(Dispatchers.Main + SupervisorJob())
    }

    @AppComp
    @Provides
    @Named("io")
    fun ioScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    @AppComp
    @Provides
    @Named("default")
    fun defaultScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.Default + SupervisorJob())
    }

    @AppComp
    @Provides
    fun nlpCore(@Named("default") scope: CoroutineScope): NLPCore {
        return NLPCore(scope)
    }

    @AppComp
    @Provides
    fun wordRelationEngine(nlpCore: NLPCore): WordRelationEngine {
        return WordRelationEngineImpl(nlpCore)
    }

    @AppComp
    @Provides
    fun appDatabase(@Named("default") scope: CoroutineScope): AppDatabase {
        return AppDatabase(scope)
    }

    @AppComp
    @Provides
    fun sentenceStorage(database: AppDatabase, nlpCore: NLPCore): SentenceRepository {
        return SentenceRepository(database, nlpCore)
    }
}