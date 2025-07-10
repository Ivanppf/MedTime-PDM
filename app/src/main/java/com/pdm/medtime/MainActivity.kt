package com.pdm.medtime

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pdm.medtime.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var result: ActivityResultLauncher<Intent>
    private lateinit var itens: ArrayList<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        itens = ArrayList()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
        prefs = getSharedPreferences("MeuPref", MODE_PRIVATE)

        val gson = Gson()
        val json = prefs.getString("lista_remedios", null)
        println("json: " + json)

        if (json != null) {
            val tipo = object : TypeToken<List<Remedio>>() {}.type
            val listaRecuperada: List<Remedio> = gson.fromJson(json, tipo)
            updateComponent(listaRecuperada)
        }

        if (itens.isEmpty()) {
            Toast.makeText(
                applicationContext,
                "Aperte no botão + para começar",
                Toast.LENGTH_LONG
            ).show()
        }

        binding.btnAdicionar.setOnClickListener {
            result.launch(Intent(this, CreateLembreteRemedio::class.java))
        }
        result = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data != null && it.resultCode == 1) {
                val nomeMedicamento = it.data?.extras?.getString("nomeMedicamento") + ""
                val hora = it.data?.extras?.getString("hora") + ""
                addComponent(false, nomeMedicamento, hora)
            }
        }
    }

    fun addComponent(tomado: Boolean, nomeMedicamento: String, hora: String) {
        val svValores = findViewById<LinearLayout>(R.id.lista_itens)
        val vi =
            binding.root.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layoutFields = vi.inflate(R.layout.item, null) as LinearLayout
        itens.add(layoutFields)

        val cb_tomado = (layoutFields.get(0) as LinearLayout).get(0) as CheckBox
        val tv_nome = (layoutFields.get(0) as LinearLayout).get(1) as TextView
        val tv_hora = (layoutFields.get(0) as LinearLayout).get(2) as TextView

        cb_tomado.isChecked = tomado
        tv_nome.text = nomeMedicamento
        tv_hora.text = hora

        val partes = hora.split(":")
        val hora = partes[0].toInt()
        val minuto = partes[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        agendarNotificacaoPara18h(applicationContext, nomeMedicamento, calendar)

        svValores.addView(layoutFields)
    }

    fun updateComponent(listaRemedios: List<Remedio>) {
        listaRemedios.forEach {
            addComponent(it.tomado, it.nome, it.hora)
        }
    }

    override fun onPause() {
        val lista: List<Remedio> = itens.map {
            val item = Remedio(
                ((it.get(0) as LinearLayout).get(0) as CheckBox).isChecked,
                ((it.get(0) as LinearLayout).get(1) as TextView).text.toString(),
                ((it.get(0) as LinearLayout).get(2) as TextView).text.toString()
            )
            item
        }
        val editor = prefs.edit()
        val gson = Gson()

        val json = gson.toJson(lista)

        editor.putString("lista_remedios", json)
        editor.apply()
        super.onPause()
    }

    @SuppressLint("ScheduleExactAlarm")
    fun agendarNotificacaoPara18h(context: Context, nomeMedicamento: String, calendar: Calendar) {
        val intent = Intent(context, NotificacaoReceiver::class.java)
        intent.putExtra("nomeMedicamento", nomeMedicamento)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

    }

    fun removerComponente(v: View) {
        itens.remove(v.parent.parent)
        val viewGroup = v.parent.parent as ViewGroup
        (viewGroup.parent as ViewGroup).removeView(viewGroup)
    }

}