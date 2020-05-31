package com.aglushkov.nlphelper.relations

import com.aglushkov.di.ViewComp

import dagger.Binds
import dagger.Module

@Module
abstract class RelationsViewModule {
    @ViewComp
    @Binds
    abstract fun viewModel(model: RelationsVMImp): RelationsVM
}