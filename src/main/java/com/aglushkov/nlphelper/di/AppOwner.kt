package com.aglushkov.nlphelper.di

import javafx.application.Application

interface AppOwner {
    companion object {
        const val Key = "app_owner"
    }

    fun getApplication(): Application
}