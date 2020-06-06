package com.aglushkov.nlphelper.relations

import com.aglushkov.db.SentenceRepository
import com.aglushkov.db.models.Sentence
import com.aglushkov.db.models.WordRelation
import com.aglushkov.word_relation.WordRelationEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.*
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Named


interface RelationsVM {
    enum class RelationOption {
        VB_N,
        VB_TEST
    }

    val relations: MutableStateFlow<List<WordRelation.Impl>>

    fun relationOptions(): List<RelationOption>
    fun onOptionSelected(option: RelationOption)
    fun onWordChanged(word: String)
    fun onStartSearchRequested()
}

class RelationsVMImp @Inject constructor(
        private val wordRelationEngine: WordRelationEngine,
        private val sentenceRepository: SentenceRepository,
        private @Named("main") val mainScope: CoroutineScope,
        private @Named("default") val defaultScope: CoroutineScope
): RelationsVM {

    override val relations = MutableStateFlow<List<WordRelation.Impl>>(emptyList())

    private var searchScope: CoroutineScope? = null
    private var job: Job? = null
    private var broadcastChannel: BroadcastChannel<Sentence>? = null

    private var word: String = ""
    private var relationOption: RelationsVM.RelationOption = RelationsVM.RelationOption.VB_N

    override fun relationOptions() = listOf(RelationsVM.RelationOption.VB_N, RelationsVM.RelationOption.VB_TEST)

    override fun onOptionSelected(option: RelationsVM.RelationOption) {
        relationOption = option
        findRelationsIfNeeded()
    }

    override fun onWordChanged(word: String) {
        this.word = word
        findRelationsIfNeeded()
    }

    override fun onStartSearchRequested() {
        findRelations()
    }

    private fun findRelationsIfNeeded() {
        if (word.length > 0) {
            findRelations()
        }
    }

    private fun findRelations() {
        val worker = relationEngineWorker()
        val word = this.word

        //searchScope?.cancel()
        val currentJob = job

        //job?.cancel(CancellationException())
        broadcastChannel?.cancel(CancellationException())
        broadcastChannel = null
        searchScope?.cancel(CancellationException())
        searchScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        val scope = searchScope!!
//        val scope = if (searchScope == null) {
//            CoroutineScope(Dispatchers.Default + SupervisorJob()).also {
//                searchScope = it
//            }
//        } else {
//            searchScope!!
//        }

        job = scope.launch {
            //currentJob?.cancelAndJoin()
            val relations: List<WordRelation.Impl> = withContext(scope.coroutineContext) {
                wordRelationEngine.waitUntilInitialized()
                val channel = sentenceRepository.loadSentences().broadcastIn(scope).also {
                    broadcastChannel = it
                }
                val sharedFlow = channel.asFlow()
                val availableProcessors = Runtime.getRuntime().availableProcessors().toLong()

                val deferredTasks: MutableList<Deferred<List<WordRelation.Impl>>> = mutableListOf()
                for (i in 0 until availableProcessors) {
                    val relationEngine = wordRelationEngine.clone()
                    val deferred = async {
                        val nounAfterVerbs = mutableListOf<WordRelation.Impl>()
                        val flow = sharedFlow.filter { it.id % availableProcessors == i }
                        flow.collect {
                            if (!isActive) {
                                print("omg")
                            }
                            nounAfterVerbs.addAll(worker(relationEngine, it.text, word))
                        }
                        nounAfterVerbs.toList()
                    }
                    deferredTasks.add(deferred)
                }

                return@withContext try {
                    val relations = mutableListOf<WordRelation.Impl>()
                    for (d in deferredTasks) {
                        relations.addAll(d.await())
                    }
                    println("count " + relations.size)

                    val sortedRelations = mutableListOf<WordRelation.Impl>()
                    val groupedRelations = relations
                            .groupBy { it.word1 }
                    for (relation in groupedRelations) {
                        val sortedValues = relation.value
                                .groupBy { it.word2 }
                                .toList()
                                .sortedByDescending { it.second.size }

                        for (r in sortedValues) {
                            sortedRelations.add(r.second.first())
                        }
                    }

                    sortedRelations
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        e.printStackTrace()
                    }

                    channel.cancel(CancellationException())
                    for (d in deferredTasks) {
                        d.cancel(CancellationException())
                    }

                    emptyList<WordRelation.Impl>()
                }
            }

            if (isActive) {
                this@RelationsVMImp.relations.value = relations
            }
        }

        //wordRelationEngine.findNounAfterVerb()
    }

    private fun relationEngineWorker(): (WordRelationEngine, String, String) -> List<WordRelation.Impl> {
        return when (relationOption) {
            RelationsVM.RelationOption.VB_N -> { engine: WordRelationEngine, text: String, word: String ->
                engine.findNounAfterVerb(text, word)
            }
            else -> { engine: WordRelationEngine, text: String, word: String ->
                emptyList()
            }
        }
    }
}