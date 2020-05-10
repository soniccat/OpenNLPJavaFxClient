package com.aglushkov.nlphelper.main

import com.aglushkov.di.ViewComp

import dagger.Binds
import dagger.Module

@Module
abstract class MainViewModule {
    @ViewComp
    @Binds
    abstract fun viewModel(model: MainVMImp): MainVM
}