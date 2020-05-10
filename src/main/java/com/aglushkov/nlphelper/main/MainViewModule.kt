package com.aglushkov.nlphelper.main

import com.aglushkov.di.ViewComp
import com.aglushkov.nlphelper.main.MainViewModel
import com.aglushkov.nlphelper.main.MainViewModelImp
import dagger.Binds
import dagger.Module

@Module
abstract class MainViewModule {
    @ViewComp
    @Binds
    abstract fun viewModel(model: MainViewModelImp): MainViewModel
}