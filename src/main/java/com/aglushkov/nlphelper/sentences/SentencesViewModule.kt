package com.aglushkov.nlphelper.sentences

import com.aglushkov.di.ViewComp

import dagger.Binds
import dagger.Module

@Module
abstract class SentencesViewModule {
    @ViewComp
    @Binds
    abstract fun viewModel(model: SentencesVMImp): SentencesVM
}