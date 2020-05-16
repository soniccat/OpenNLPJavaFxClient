package com.aglushkov.nlphelper

import javafx.scene.Parent
import javafx.stage.Stage
import javafx.stage.WindowEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import java.util.concurrent.CancellationException
import javax.inject.Inject
import javax.inject.Named

open class BaseView {
    @Inject
    @Named("main")
    lateinit var mainScope: CoroutineScope

    private lateinit var _stage: Stage
    var stage: Stage
        get() = _stage
        set(value) {
            _stage = value
            onStageChanged()
        }

    private lateinit var _parent: Parent
    var parent: Parent
        get() = _parent
        set(value) {
            _parent = value
            onParentChanged()
        }

    open fun onStageChanged() {
        _stage.apply {
            val oldDispatcher = eventDispatcher
            setEventDispatcher { event, tail ->
                if (event is WindowEvent) {
                    when (event.eventType) {
                        WindowEvent.WINDOW_SHOWING -> onShowing()
                        WindowEvent.WINDOW_SHOWN -> onShown()
                        WindowEvent.WINDOW_CLOSE_REQUEST -> onCloseRequest()
                    }
                }

                oldDispatcher.dispatchEvent(event, tail)
            }
        }

        triggerOnReadyIfNeeded()
    }

    open fun onParentChanged() {
        triggerOnReadyIfNeeded()
    }

    private fun triggerOnReadyIfNeeded() {
        if (this::_stage.isInitialized && this::_parent.isInitialized) {
            onReady()
        }
    }

    open fun onReady() {
    }

    open fun onShowing() {
    }

    open fun onShown() {
    }

    open fun onCloseRequest() {
        mainScope.cancel(CancellationException())
    }

    fun show() = stage.show()
}