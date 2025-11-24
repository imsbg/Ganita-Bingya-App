package com.sandeep.ganitabigyan

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    // A private, mutable state to hold whether the animation has played.
    // It starts as false.
    private val _hasAnimationPlayed = MutableStateFlow(false)

    // A public, read-only version of the state for the UI to observe.
    val hasAnimationPlayed = _hasAnimationPlayed.asStateFlow()

    /**
     * Call this function once the animation is complete to prevent it
     * from running again in this app session.
     */
    fun onAnimationCompleted() {
        _hasAnimationPlayed.value = true
    }
}