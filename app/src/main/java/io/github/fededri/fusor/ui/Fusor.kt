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
import io.github.fededri.fusor.ui.theme.FusorTheme
import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


@Composable
fun MainScreen() {
    var text by remember { mutableStateOf("180") }
    val scope = rememberCoroutineScope()

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

        Row() {
            Spacer(modifier = Modifier.fillMaxWidth(0.3f))
            Button(
                modifier = Modifier.fillMaxWidth(0.4f),
                onClick = { startSimulation(text.toIntOrNull(), scope) }) {
                Text(text = "Iniciar simulación")
            }

            Spacer(modifier = Modifier.fillMaxWidth(0.3f))
        }

    }
}

private fun startSimulation(reference: Int?, scope: CoroutineScope) {
    check(reference != null)

    // Ganancia arduino (Amper)
    val current = reference * 0.02f

    //Calentador
    heatUp(current = current, scope = scope, reference = reference)
}

@OptIn(ExperimentalTime::class)
private fun heatUp(current: Float, scope: CoroutineScope, reference: Int) {
    val resistance = 200 // Ohms

    var t = 0L // seconds
    val mass = 20// gr
    val C = 1500
    val ambientTemperature = 20f
    val speedMultiplier = 25
    var errorSignal = 0f
    var plaTemperature = ambientTemperature
    var cumError = 0f

    scope.launch {
        while (true) {
            val start = System.nanoTime()
            if (errorSignal > 0) {
                // Heat up
                val heat = current.pow(2) * resistance * (t*speedMultiplier)
                plaTemperature = (heat / (mass * C)) + ambientTemperature
                t += 1
            } else {
                // cool down
                plaTemperature -= 1
                t -= 1
            }

            delay(Duration.Companion.seconds(1))

            Log.i("Temperature:", plaTemperature.toString())

            // Read temperature
            errorSignal = getErrorSignal(reference, plaTemperature)
            val ellapsedTime = System.nanoTime() - start
            cumError += errorSignal * ellapsedTime
            Log.i("Error Signal:", errorSignal.toString())
            Log.i("cumError:", cumError.toString())
        }
    }
}

private fun getErrorSignal(reference: Int, sensorValue: Float): Float {
    return reference - sensorValue
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FusorTheme {
        MainScreen()
    }
}