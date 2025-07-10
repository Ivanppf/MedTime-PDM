package com.pdm.medtime

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.pdm.medtime.databinding.ActivityCreateLembreteRemedioBinding
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class CreateLembreteRemedio : AppCompatActivity() {

    private lateinit var binding: ActivityCreateLembreteRemedioBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateLembreteRemedioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etHora.setIs24HourView(true)

        binding.btnsave.setOnClickListener {

            if (binding.etNomeMedicamento.text.toString().isBlank()
            ) {
                Toast.makeText(
                    applicationContext,
                    "Por favor, insira o nome do medicamento",
                    Toast.LENGTH_SHORT
                ).show()
            } else {

                val intent = Intent()

                intent.putExtra(
                    "nomeMedicamento",
                    binding.etNomeMedicamento.text.toString()
                )

                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val hora = LocalTime.of(binding.etHora.hour, binding.etHora.minute)
                hora.format(formatter)

                intent.putExtra(
                    "hora",
                    hora.toString()
                )

                setResult(1, intent)

                finish()
            }
        }

    }
}