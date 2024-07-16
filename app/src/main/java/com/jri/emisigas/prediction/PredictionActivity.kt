package com.jri.emisigas.prediction

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jri.emisigas.databinding.ActivityPredictionBinding
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class PredictionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPredictionBinding
    private lateinit var gruTflite: Interpreter
    private lateinit var cnnTflite: Interpreter
    private lateinit var lstmTflite: Interpreter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPredictionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Memuat model TFLite
        gruTflite = Interpreter(loadModelFile("gru_model_1_nonseasonal.tflite"), Interpreter.Options().addDelegate(FlexDelegate()))
        cnnTflite = Interpreter(loadModelFile("cnn_model_1_nonseasonal.tflite"), Interpreter.Options().addDelegate(FlexDelegate()))
        lstmTflite = Interpreter(loadModelFile("lstm_model_1_nonseasonal.tflite"), Interpreter.Options().addDelegate(FlexDelegate()))

        binding.predictionButton.setOnClickListener {
            prediction()
        }
    }

    // Fungsi untuk memuat file model TFLite
    private fun loadModelFile(modelFileName: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelFileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Fungsi untuk melakukan inferensi menggunakan model TFLite
    private fun runInference(model: Interpreter, inputData: Array<Array<FloatArray>>): Array<FloatArray> {
        val output = Array(1) { FloatArray(1) }
        model.run(inputData, output)
        return output
    }


    @SuppressLint("SetTextI18n")
    private fun prediction(){
        val distance = binding.prediction.text.toString()

        // Menyiapkan input untuk model
        val input = arrayOf(arrayOf(floatArrayOf(distance.toFloat())))

        // Menjalankan inferensi untuk model GRU
        val gruResult = runInference(gruTflite, input)
        val predictedGruEmissionCO2 = gruResult[0][0]
        binding.modelGRU.text = "$predictedGruEmissionCO2 KG CO2"

        // Menjalankan inferensi untuk model CNN
        val cnnResult = runInference(cnnTflite, input)
        val predictedCnnEmissionCO2 = cnnResult[0][0]
        binding.modelCNN.text = "$predictedCnnEmissionCO2 KG CO2"

        // Menjalankan inferensi untuk model LSTM
        val lstmResult = runInference(lstmTflite, input)
        val predictedLstmEmissionCO2 = lstmResult[0][0]
        binding.modelLSTM.text = "$predictedLstmEmissionCO2 KG CO2"
    }
}