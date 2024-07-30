package com.jri.emisigas.prediction

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jri.emisigas.databinding.ActivityPredictionBinding
import com.jri.emisigas.result.Result
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PredictionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPredictionBinding
    private lateinit var gruTflite: Interpreter
    private lateinit var cnnTflite: Interpreter
    private lateinit var lstmTflite: Interpreter
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var predictionAdapter: PredictionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPredictionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load TFLite models
        gruTflite = Interpreter(loadModelFile("gru_model_1_nonseasonal.tflite"), Interpreter.Options().addDelegate(FlexDelegate()))
        cnnTflite = Interpreter(loadModelFile("cnn_model_1_nonseasonal.tflite"), Interpreter.Options().addDelegate(FlexDelegate()))
        lstmTflite = Interpreter(loadModelFile("lstm_model_1_nonseasonal.tflite"), Interpreter.Options().addDelegate(FlexDelegate()))

        binding.rvSeasonal.layoutManager = LinearLayoutManager(this)
        predictionAdapter = PredictionAdapter(emptyList())
        binding.rvSeasonal.adapter = predictionAdapter

        binding.predictionButton.setOnClickListener {
            prediction()
        }

        fetchData()
    }

    private fun loadModelFile(modelFileName: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelFileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun runInference(model: Interpreter, inputData: Array<Array<FloatArray>>): Array<FloatArray> {
        val output = Array(1) { FloatArray(1) }
        model.run(inputData, output)
        return output
    }

    @SuppressLint("SetTextI18n")
    private fun prediction() {
        val distance = binding.prediction.text.toString().toFloatOrNull() ?: return

        // Prepare input for the model
        val input = arrayOf(arrayOf(floatArrayOf(distance)))

        // Run inference for the GRU model
        val gruResult = runInference(gruTflite, input)
        val predictedGruEmissionCO2 = gruResult[0][0]
        binding.modelGRU.text = "$predictedGruEmissionCO2 KG CO2"

        // Run inference for the CNN model
        val cnnResult = runInference(cnnTflite, input)
        val predictedCnnEmissionCO2 = cnnResult[0][0]
        binding.modelCNN.text = "$predictedCnnEmissionCO2 KG CO2"

        // Run inference for the LSTM model
        val lstmResult = runInference(lstmTflite, input)
        val predictedLstmEmissionCO2 = lstmResult[0][0]
        binding.modelLSTM.text = "$predictedLstmEmissionCO2 KG CO2"
    }

    private fun fetchData() {
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()

        dbRef = database.reference.child("result")

        dbRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val distanceData = mutableListOf<Float>()
                    for (resultSnapshot in snapshot.children) {
                        val result = resultSnapshot.getValue(Result::class.java)
                        if (result != null) {
                            if (result.user_id == user?.uid) {
                                val distance = result.distance.replace(",", ".").toFloatOrNull()
                                if (distance != null) {
                                    distanceData.add(distance)
                                } else {
                                    Log.e("PredictionActivity", "Invalid distance value: ${result.distance}")
                                }
                            }
                        }
                    }
                    predictNextMonth(distanceData)
                    Log.d("data distance", "$distanceData")
                } else {
                    Toast.makeText(this@PredictionActivity, "Data is Null", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PredictionActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadModelFileSeasonal(modelName: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun predictNextMonth(distanceData: List<Float>) {
        val tfliteModel = loadModelFileSeasonal("gru_model_1.tflite")
        val tflite = Interpreter(tfliteModel)

        // Preprocess the distance data
        val input = preprocessData(distanceData)

        // Generate predictions for the next month
        val predictions = mutableListOf<Float>()
        for (i in 0 until 30) { // Assuming daily predictions for a month
            // Run the model
            val output = runInference(tflite, input)

            // Extract the prediction result
            val prediction = output[0][0]
            predictions.add(prediction)

            // Update input with the latest prediction
            updateInput(input, prediction)
        }

        // Display the predictions
        displayPredictions(predictions)
    }

    private fun preprocessData(distanceData: List<Float>): Array<Array<FloatArray>> {
        // Convert the list to the required format
        val input = Array(1) { Array(25) { FloatArray(1) } }
        distanceData.takeLast(25).forEachIndexed { index, distance ->
            input[0][index][0] = distance
        }
        return input
    }

    private fun updateInput(input: Array<Array<FloatArray>>, newDistance: Float) {
        // Shift the input data to the left and add the new distance value at the end
        for (i in 0 until 24) {
            input[0][i][0] = input[0][i + 1][0]
        }
        input[0][24][0] = newDistance
    }

    private fun displayPredictions(predictions: List<Float>) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val predictionItems = predictions.mapIndexed { index, prediction ->
            val date = dateFormat.format(Date().apply { time += index * 24 * 60 * 60 * 1000L }) // Adjust for each day
            PredictionItem(date, prediction)
        }
        binding.rvSeasonal.adapter = PredictionAdapter(predictionItems)
    }

}
