package com.aglushkov.nlphelper.di

import com.aglushkov.di.ViewComp
import dagger.Binds
import dagger.Module

@Module
abstract class MainViewModule {
    @ViewComp
    @Binds
    abstract fun viewModel(model: MainViewModelImp): MainViewModel
}