package io.github.fededri.fusor.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import io.github.fededri.fusor.FusorState
import io.github.fededri.fusor.FusorViewModel
import io.github.fededri.fusor.ui.theme.FusorTheme
import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


@Composable
fun MainScreen(viewModel: FusorViewModel) {
    var text by remember { mutableStateOf("180") }
    val scope = rememberCoroutineScope()
    val state = viewModel.uiState

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Valor de entrada (ºC):")

            TextField(
                value = text, onValueChange = {
                    text = it
                })

            Spacer(Modifier.size(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { startSimulation(text.toIntOrNull(), scope, viewModel) }) {
                Text(text = "Iniciar simulación")
            }

            Button(
                onClick = { scope.cancel() }) {
                Text(text = "Parar simulación")
            }

            Button(
                onClick = { viewModel.uiState = FusorState() }) {
                Text(text = "Reset")
            }
        }

        DataView(state)
    }
}

@Composable
fun DataView(state: FusorState) {
    Column() {
        Row() {
            Text(text = "Temperatura:")
            Text(
                text = state.temperature.toString(),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Row() {
            Text(text = "Señal de error:")
            Text(
                text = state.errorSignal.toString(),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Row() {
            Text(text = "Error acumulado:")
            Text(
                text = state.cumulativeError.toString(),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }

}

private fun startSimulation(reference: Int?, scope: CoroutineScope, viewModel: FusorViewModel) {
    check(reference != null)

    // Ganancia arduino (Amper)
    var current: Float

    //Calentador
    var t = 0L // seconds

    val ambientTemperature = 20f
    val speedMultiplier = 1
    var errorSignal = 0f
    var plaTemperature = ambientTemperature
    var cumError = 0f
    val ki = 0.003f

    scope.launch {
        while (true) {
            val start = System.currentTimeMillis()
            current = cumError

            advanceTime(1)
            t += 1

            // Read temperature
            plaTemperature = getTemperature(current, t, speedMultiplier)
            errorSignal = getErrorSignal(reference, plaTemperature)
            val ellapsedTime = System.currentTimeMillis() - start
            cumError += ki * errorSignal * (ellapsedTime / 1000f)
            viewModel.uiState = FusorState(
                temperature = plaTemperature,
                errorSignal = errorSignal,
                cumulativeError = cumError
            )
            Log.i("Temperature:", plaTemperature.toString())
            Log.i("Error Signal:", errorSignal.toString())
            Log.i("cumError:", cumError.toString())
            Log.i("---------------------", "")
        }
    }
}

fun getTemperature(
    current: Float,
    currentTime: Long,
    speedMultiplier: Int
): Float {
    val resistance = 200 // Ohms
    val heat = getHeat(current, resistance, currentTime, speedMultiplier)
    val mass = 20// gr
    val C = 500
    val ambientTemperature = 20
    return (heat / (mass * C)) + ambientTemperature
}

@OptIn(ExperimentalTime::class)
suspend fun advanceTime(seconds: Int) {
    delay(Duration.Companion.seconds(seconds))
}

private fun getHeat(
    current: Float,
    resistance: Int,
    currentTime: Long,
    speedMultiplier: Int
): Float {
    return current.pow(2) * resistance * (currentTime * speedMultiplier)
}


private fun getErrorSignal(reference: Int, sensorValue: Float): Float {
    return reference - sensorValue
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FusorTheme {
        MainScreen(FusorViewModel())
    }
}