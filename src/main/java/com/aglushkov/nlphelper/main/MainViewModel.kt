package com.aglushkov.nlphelper.main

import com.aglushkov.model.Resource
import com.aglushkov.nlp.NLPCore
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

interface MainViewModel {
    val tokens: MutableStateFlow<Resource<Array<String>>>
}

class MainViewModelImp @Inject constructor(val core: NLPCore): MainViewModel {
    override val tokens = MutableStateFlow<Resource<Array<String>>>(Resource.Uninitialized())

    init {

    }

    fun onReady() {

    }
}