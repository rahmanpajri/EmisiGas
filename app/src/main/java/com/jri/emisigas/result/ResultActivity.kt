package com.jri.emisigas.result

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.jri.emisigas.MainActivity
import com.jri.emisigas.R
import com.jri.emisigas.databinding.ActivityResultBinding
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var gruTflite: Interpreter
    private lateinit var cnnTflite: Interpreter
    private lateinit var lstmTflite: Interpreter
    private lateinit var predictionsGRU: List<Triple<String, Float, Float>>
    private lateinit var predictionsLSTM: List<Triple<String, Float, Float>>
    private lateinit var predictionsCNN: List<Triple<String, Float, Float>>
    private lateinit var weeklyPredictionsGRU: List<Triple<String, Float, Float>>
    private lateinit var weeklyPredictionsLSTM: List<Triple<String, Float, Float>>
    private lateinit var weeklyPredictionsCNN: List<Triple<String, Float, Float>>
    private lateinit var monthlyPredictionsGRU: List<Triple<String, Float, Float>>
    private lateinit var monthlyPredictionsLSTM: List<Triple<String, Float, Float>>
    private lateinit var monthlyPredictionsCNN: List<Triple<String, Float, Float>>
    private val SAVE_PDF_REQUEST_CODE = 2

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

        binding.exportPrediction.setOnClickListener {
            exportPdf()
        }

        val predictionOptions = listOf(
            "LSTM 24 Jam", "GRU 24 Jam", "CNN 24 Jam",
            "LSTM Weekly", "GRU Weekly", "CNN Weekly",
            "LSTM Monthly", "GRU Monthly", "CNN Monthly"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, predictionOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.predictionSelector.adapter = adapter

        binding.predictionSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedPrediction = parent.getItemAtPosition(position) as String
                when (selectedPrediction) {
                    "LSTM 24 Jam" -> populatePredictionTable(binding.tablePrediction, predictionsLSTM)
                    "GRU 24 Jam" -> populatePredictionTable(binding.tablePrediction, predictionsGRU)
                    "CNN 24 Jam" -> populatePredictionTable(binding.tablePrediction, predictionsCNN)
                    "LSTM Weekly" -> populatePredictionTable(binding.tablePrediction, weeklyPredictionsLSTM)
                    "GRU Weekly" -> populatePredictionTable(binding.tablePrediction, weeklyPredictionsGRU)
                    "CNN Weekly" -> populatePredictionTable(binding.tablePrediction, weeklyPredictionsCNN)
                    "LSTM Monthly" -> populatePredictionTable(binding.tablePrediction, monthlyPredictionsLSTM)
                    "GRU Monthly" -> populatePredictionTable(binding.tablePrediction, monthlyPredictionsGRU)
                    "CNN Monthly" -> populatePredictionTable(binding.tablePrediction, monthlyPredictionsCNN)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun exportPdf() {
        val directory = getExternalFilesDir(null)
        val pdfFilePath = "$directory/prediction_result.pdf"

        try {
            val document = Document()
            val outputStream = FileOutputStream(pdfFilePath)
            PdfWriter.getInstance(document, outputStream)
            document.open()

            document.add(Paragraph("Prediction Results"))
            document.add(Paragraph("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}"))

            val selectedPrediction = binding.predictionSelector.selectedItem as String
            document.add(Paragraph(selectedPrediction))
            val predictions = when (selectedPrediction) {
                "LSTM 24 Jam" -> predictionsLSTM
                "GRU 24 Jam" -> predictionsGRU
                "CNN 24 Jam" -> predictionsCNN
                "LSTM Weekly" -> weeklyPredictionsLSTM
                "GRU Weekly" -> weeklyPredictionsGRU
                "CNN Weekly" -> weeklyPredictionsCNN
                "LSTM Monthly" -> monthlyPredictionsLSTM
                "GRU Monthly" -> monthlyPredictionsGRU
                "CNN Monthly" -> monthlyPredictionsCNN
                else -> emptyList()
            }
            addPredictionsTable(document, predictions, selectedPrediction)

            document.close()

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
                putExtra(Intent.EXTRA_TITLE, "prediction_result.pdf")
            }

            startActivityForResult(intent, SAVE_PDF_REQUEST_CODE)

            Toast.makeText(this, "Activity converted to PDF", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addPredictionsTable(document: Document, predictions: List<Triple<String, Float, Float>>, modelName: String) {

        val columnWidths = floatArrayOf(2f, 1f, 1f)

        val table = PdfPTable(3)
        table.setWidths(columnWidths)
        table.addCell("Date")
        table.addCell("Distance")
        table.addCell("Prediction")

        var totalEmission = 0f
        predictions.forEach { prediction ->
            totalEmission += prediction.third

            table.addCell(prediction.first)
            table.addCell(prediction.second.toString())
            table.addCell(prediction.third.toString())
        }

        // Add total emission row
        table.addCell("Total")
        table.addCell("")
        table.addCell(totalEmission.toString())

        document.add(table)
    }

    private fun generateWeeklyPredictions(model: Interpreter, initialDistance: Float): List<Triple<String, Float, Float>> {
        val predictions = mutableListOf<Triple<String, Float, Float>>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy HH:mm", Locale.getDefault())
        var currentDistance = initialDistance

        for (i in 1..168) {
            val inputCurrent = arrayOf(arrayOf(floatArrayOf(currentDistance)))
            val formattedTime = dateFormat.format(calendar.time)
            val prediction = runInference(model, inputCurrent)[0][0]
            predictions.add(Triple(formattedTime, currentDistance, prediction))
            currentDistance += 0.5f
            calendar.add(Calendar.HOUR_OF_DAY, 1)
        }

        return predictions
    }

    private fun generateMonthlyPredictions(model: Interpreter, initialDistance: Float): List<Triple<String, Float, Float>> {
        val predictions = mutableListOf<Triple<String, Float, Float>>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy HH:mm", Locale.getDefault())
        var currentDistance = initialDistance

        for (i in 1..720) {
            val inputCurrent = arrayOf(arrayOf(floatArrayOf(currentDistance)))
            val formattedTime = dateFormat.format(calendar.time)
            val prediction = runInference(model, inputCurrent)[0][0]
            predictions.add(Triple(formattedTime, currentDistance, prediction))
            currentDistance += 0.5f
            calendar.add(Calendar.HOUR_OF_DAY, 1)
        }

        return predictions
    }


    @SuppressLint("SetTextI18n")
    private fun displayResult(totalEmissionCO2String: String?, totalEmissionCH4: Double, totalEmissionN2O: Double, totalDistanceString: String?) {
        binding.totalEmissionCo2.text = "${totalEmissionCO2String?.toDoubleOrNull() ?: 0.0} kg CO2"
        binding.totalEmissionCh4.text = "${formatEmission(totalEmissionCH4)} kg CH4"
        binding.totalEmissionN2o.text = "${formatEmission(totalEmissionN2O)} kg N2O"
        binding.totalDistance.text = totalDistanceString ?: "N/A"
    }

    private fun makePredictions(totalDistanceString: String?) {
        totalDistanceString?.toFloatOrNull()?.let { totalDistance ->
            val initialDistance = totalDistance + 0.5f

            predictionsGRU = generatePredictions(gruTflite, initialDistance)
            predictionsCNN = generatePredictions(cnnTflite, initialDistance)
            predictionsLSTM = generatePredictions(lstmTflite, initialDistance)

            weeklyPredictionsGRU = generateWeeklyPredictions(gruTflite, initialDistance)
            weeklyPredictionsCNN = generateWeeklyPredictions(cnnTflite, initialDistance)
            weeklyPredictionsLSTM = generateWeeklyPredictions(lstmTflite, initialDistance)

            monthlyPredictionsGRU = generateMonthlyPredictions(gruTflite, initialDistance)
            monthlyPredictionsCNN = generateMonthlyPredictions(cnnTflite, initialDistance)
            monthlyPredictionsLSTM = generateMonthlyPredictions(lstmTflite, initialDistance)
        }
    }

    private fun populatePredictionTable(tableLayout: TableLayout, predictions: List<Triple<String, Float, Float>>) {
        tableLayout.removeAllViews()

        val headerRow = TableRow(this).apply {
            setBackgroundColor(ContextCompat.getColor(this@ResultActivity, R.color.md_theme_primary))
            addView(createTextView("Date"))
            addView(createTextView("Distance"))
            addView(createTextView("Prediction"))
        }
        tableLayout.addView(headerRow)

        var totalEmission = 0f
        predictions.forEach { prediction ->
            totalEmission += prediction.third

            val tableRow = TableRow(this).apply {
                setBackgroundColor(ContextCompat.getColor(this@ResultActivity, R.color.md_theme_primary))
                addView(createTextView(prediction.first))
                addView(createTextView(prediction.second.toString()))
                addView(createTextView(prediction.third.toString()))
            }
            tableLayout.addView(tableRow)
        }

        val totalRow = TableRow(this).apply {
            setBackgroundColor(ContextCompat.getColor(this@ResultActivity, R.color.md_theme_primary))
            addView(createTextView("Total"))
            addView(createTextView(""))
            addView(createTextView(totalEmission.toString()))
        }
        tableLayout.addView(totalRow)
    }

    private fun createTextView(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_onPrimary))
            setTextColor(ContextCompat.getColor(context, R.color.md_theme_primary))
            setPadding(16, 16, 16, 16)
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 80).apply {
                setMargins(4, 4, 4, 4)
            }
        }
    }

    private fun formatEmission(emission: Double): String {
        return String.format("%.3f", emission)
    }

    private fun runInference(model: Interpreter, input: Array<Array<FloatArray>>): Array<FloatArray> {
        val output = Array(1) { FloatArray(1) }
        model.run(input, output)
        return output
    }

    private fun loadModel(modelPath: String): Interpreter {
        val assetFileDescriptor = assets.openFd(modelPath)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        val buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        return Interpreter(buffer)
    }

    private fun generatePredictions(model: Interpreter, initialDistance: Float): List<Triple<String, Float, Float>> {
        val predictions = mutableListOf<Triple<String, Float, Float>>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy HH:mm", Locale.getDefault())
        var currentDistance = initialDistance

        for (i in 1..24) {
            val inputCurrent = arrayOf(arrayOf(floatArrayOf(currentDistance)))
            val formattedTime = dateFormat.format(calendar.time)
            val prediction = runInference(model, inputCurrent)[0][0]
            predictions.add(Triple(formattedTime, currentDistance, prediction))
            currentDistance += 0.5f
            calendar.add(Calendar.HOUR_OF_DAY, 1)
        }

        return predictions
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SAVE_PDF_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.also { uri ->
                val directory = getExternalFilesDir(null)
                val pdfFilePath = "$directory/prediction_result.pdf"
                val inputStream = FileInputStream(pdfFilePath)
                contentResolver.openOutputStream(uri).use { outputStream ->
                    inputStream.copyTo(outputStream!!)
                }
                Toast.makeText(this, "Success Export PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

