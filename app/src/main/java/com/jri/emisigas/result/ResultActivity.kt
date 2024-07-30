package com.jri.emisigas.result

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jri.emisigas.MainActivity
import com.jri.emisigas.R
import com.jri.emisigas.databinding.ActivityResultBinding
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var gruTflite: Interpreter
    private lateinit var cnnTflite: Interpreter
    private lateinit var lstmTflite: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val totalEmissionCO2String = intent.getStringExtra("TOTAL_EMISSION_CO2")
        val totalEmissionCH4String = intent.getDoubleExtra("TOTAL_EMISSION_CH4", 0.0)
        val totalEmissionN2OString = intent.getDoubleExtra("TOTAL_EMISSION_N2O", 0.0)
        val totalDistanceString = intent.getStringExtra("TOTAL_DISTANCE")

        gruTflite = loadModel("gru_model_1_nonseasonal.tflite")
        cnnTflite = loadModel("cnn_model_1_nonseasonal.tflite")
        lstmTflite = loadModel("lstm_model_1_nonseasonal.tflite")

        displayResult(totalEmissionCO2String, totalEmissionCH4String, totalEmissionN2OString, totalDistanceString)
        makePredictions(totalDistanceString)

        binding.toHome.setOnClickListener {
            startActivity(Intent(this@ResultActivity, MainActivity::class.java))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayResult(totalEmissionCO2String: String?, totalEmissionCH4: Double, totalEmissionN2O: Double, totalDistanceString: String?) {
        binding.totalEmissionCo2.text = "${totalEmissionCO2String?.toDoubleOrNull() ?: 0.0} kg CO2"
        binding.totalEmissionCh4.text = "${formatEmission(totalEmissionCH4)} kg CH4"
        binding.totalEmissionN2o.text = "${formatEmission(totalEmissionN2O)} kg N2O"
        binding.totalDistance.text = "${totalDistanceString?.toDoubleOrNull() ?: 0.0} km"
    }

    private fun formatEmission(emission: Double) = if (emission == 0.0) "0.0" else String.format("%.8f", emission)

    private fun loadModel(modelFileName: String): Interpreter {
        val options = Interpreter.Options().addDelegate(FlexDelegate())
        return Interpreter(loadModelFile(modelFileName), options)
    }

    private fun loadModelFile(modelFileName: String): MappedByteBuffer {
        return assets.openFd(modelFileName).run {
            FileInputStream(fileDescriptor).channel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }
    }

    private fun runInference(model: Interpreter, inputData: Array<Array<FloatArray>>): Array<FloatArray> {
        val output = Array(1) { FloatArray(1) }
        model.run(inputData, output)
        return output
    }

    @SuppressLint("SetTextI18n")
    private fun makePredictions(totalDistanceString: String?) {
        val initialDistance = totalDistanceString?.toFloatOrNull() ?: return
        val predictionsGRU = mutableListOf<Pair<String, Float>>()
        val predictionsLSTM = mutableListOf<Pair<String, Float>>()
        val predictionsCNN = mutableListOf<Pair<String, Float>>()

        var currentDistance = initialDistance
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy HH:mm", Locale.getDefault())

        for (i in 1..24) {
            val inputCurrent = arrayOf(arrayOf(floatArrayOf(currentDistance)))

            val formattedTime = dateFormat.format(calendar.time)
            predictionsGRU.add(Pair(formattedTime, runInference(gruTflite, inputCurrent)[0][0]))
            predictionsLSTM.add(Pair(formattedTime, runInference(lstmTflite, inputCurrent)[0][0]))
            predictionsCNN.add(Pair(formattedTime, runInference(cnnTflite, inputCurrent)[0][0]))

            currentDistance += 0.5f
            calendar.add(Calendar.HOUR_OF_DAY, 1)
        }

        binding.modelGRU.text = "${predictionsGRU.first().second} KG CO2"
        binding.modelLSTM.text = "${predictionsLSTM.first().second} KG CO2"
        binding.modelCNN.text = "${predictionsCNN.first().second} KG CO2"

        populatePredictionTable(binding.tableGRU, predictionsGRU)
        populatePredictionTable(binding.tableLSTM, predictionsLSTM)
        populatePredictionTable(binding.tableCNN, predictionsCNN)
    }

    private fun populatePredictionTable(table: TableLayout, predictions: List<Pair<String, Float>>) {
        predictions.forEach { prediction ->
            val tableRow = TableRow(this).apply {
                setBackgroundColor(ContextCompat.getColor(this@ResultActivity, R.color.md_theme_primary))
            }

            val dateTextView = createTextView(prediction.first)
            val predictionTextView = createTextView(prediction.second.toString())

            tableRow.addView(dateTextView)
            tableRow.addView(predictionTextView)
            table.addView(tableRow)
        }
    }

    private fun createTextView(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_onPrimary))
            setTextColor(ContextCompat.getColor(context, R.color.md_theme_primary))
            setPadding(12, 12, 12, 12)
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 120).apply {
                setMargins(4, 4, 4, 4)
            }
        }
    }
}
