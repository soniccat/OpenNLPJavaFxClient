package com.aglushkov.nlphelper.relations

import com.aglushkov.db.AppDatabase
import com.aglushkov.db.SentenceRepository
import com.aglushkov.db.models.Sentence
import com.aglushkov.db.models.WordRelation
import com.aglushkov.nlp.NLPSentence
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
        VB_PREP,
        VB_TEST
    }

    val relations: MutableStateFlow<List<Pair<WordRelation.Impl, Int>>>

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

    override val relations = MutableStateFlow<List<Pair<WordRelation.Impl, Int>>>(emptyList())

    private var job: Job? = null

    private var word: String = ""
    private var relationOption: RelationsVM.RelationOption = RelationsVM.RelationOption.VB_N

    override fun relationOptions() = listOf(RelationsVM.RelationOption.VB_N, RelationsVM.RelationOption.VB_PREP, RelationsVM.RelationOption.VB_TEST)

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
        if (word.isNotEmpty()) {
            findRelations()
        }
    }

    private fun findRelations() {
        val worker = relationEngineWorker()
        val word = this.word

        val currentJob = job
        job = defaultScope.launch {
            //val time = System.currentTimeMillis()
            currentJob?.cancelAndJoin()
            //println("cancel time " + (System.currentTimeMillis() - time))

            val relations: List<Pair<WordRelation.Impl, Int>> = withContext(Dispatchers.Default) {
                wordRelationEngine.waitUntilInitialized()
                val channel = sentenceRepository.loadSentencesNLP().broadcastIn(defaultScope)
                val sharedFlow = channel.asFlow()
                val availableProcessors = Runtime.getRuntime().availableProcessors().toLong() - 1

                val deferredTasks: MutableList<Deferred<List<WordRelation.Impl>>> = mutableListOf()
                for (i in 0 until availableProcessors) {
                    val relationEngine = wordRelationEngine.clone()
                    val deferred = async {
                        val nounAfterVerbs = mutableListOf<WordRelation.Impl>()
                        val flow = sharedFlow.filter { it.id % availableProcessors == i }
                        flow.collect {
                            val nlpSentence = AppDatabase.createNLPSentence(it, relationEngine.nlpCore)
                            nounAfterVerbs.addAll(worker(relationEngine, nlpSentence, word))
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

                    val sortedRelations = mutableListOf<Pair<WordRelation.Impl, Int>>()
                    val groupedRelations = relations
                            .groupBy { it.word1 }
                    for (relation in groupedRelations) {
                        val sortedValues = relation.value
                                .groupBy { it.word2 }
                                .toList()
                                .sortedByDescending { it.second.size }

                        for (r in sortedValues) {
                            sortedRelations.add(Pair(r.second.first(), r.second.size))
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

                    emptyList<Pair<WordRelation.Impl, Int>>()
                }
            }

            if (isActive) {
                this@RelationsVMImp.relations.value = relations
            }
        }
    }

    private fun relationEngineWorker(): (WordRelationEngine, NLPSentence, String) -> List<WordRelation.Impl> {
        return when (relationOption) {
            RelationsVM.RelationOption.VB_N -> { engine: WordRelationEngine, nlpSentence: NLPSentence, word: String ->
                engine.findNounAfterVerb(nlpSentence, word)
            }
            RelationsVM.RelationOption.VB_PREP -> { engine: WordRelationEngine, nlpSentence: NLPSentence, word: String ->
                engine.findPrepAfterVerb(nlpSentence, word)
            }
            else -> { engine: WordRelationEngine, nlpSentence: NLPSentence, word: String ->
                emptyList()
            }
        }
    }
}