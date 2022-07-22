package io.github.fededri.fusor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class FusorState(
    val temperature: Float = 0f,
    val errorSignal: Float = 0f,
    val cumulativeError: Float= 0f
)
class FusorViewModel: ViewModel() {
    var uiState by mutableStateOf(FusorState())
}