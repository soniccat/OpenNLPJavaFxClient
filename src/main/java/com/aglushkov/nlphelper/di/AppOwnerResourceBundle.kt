package com.aglushkov.nlphelper.di

import java.util.*

class AppOwnerResourceBundle(private val appOwner: AppOwner): ResourceBundle() {
    override fun getKeys(): Enumeration<String> {
        return Collections.enumeration(Collections.singletonList(AppOwner.Key))
    }

    override fun handleGetObject(key: String): Any {
        if (key == AppOwner.Key) return appOwner
        return ""
    }
}