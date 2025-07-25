package com.pdm.medtime

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pdm.medtime.dao.RemedioDAO
import com.pdm.medtime.dao.RemedioTomadoDAO
import com.pdm.medtime.databinding.ActivityMainBinding
import com.pdm.medtime.entities.Remedio
import com.pdm.medtime.entities.RemedioTomado
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var result: ActivityResultLauncher<Intent>
    private lateinit var itens: ArrayList<LinearLayout>
    private val APP_PREFERENCES_NAME = "MeuPref_DataStore"
    private lateinit var database: AppDatabase
    private lateinit var remedioDAO: RemedioDAO
    private lateinit var remedioTomadoDAO: RemedioTomadoDAO


    val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(
        name = APP_PREFERENCES_NAME
    )

    object AppPreferencesKeys {
        val LISTA_REMEDIOS = stringPreferencesKey("lista_remedios")
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

        binding.btnListaTomados.setOnClickListener {
            result.launch(Intent(this, logremedios::class.java))
        }

        database = AppDatabase.getDatabase(this)
        remedioDAO = database.remedioDao()
        remedioTomadoDAO = database.remedioTomadoDao()

        val gson = Gson()

        lifecycleScope.launch {
            val tipoListaRemedios = object : TypeToken<List<Remedio>>() {}.type

            val listaRemedios = applicationContext.appDataStore.data
                .map { preferences ->
                    val json = preferences[AppPreferencesKeys.LISTA_REMEDIOS]
                    if (json != null) {
                        gson.fromJson<List<Remedio>>(json, tipoListaRemedios)
                    } else {
                        emptyList()
                    }
                }
                .firstOrNull() ?: emptyList()
            if (!listaRemedios.isEmpty()) {
                updateComponent(listaRemedios)
            } else {
                Toast.makeText(
                    applicationContext,
                    "Aperte no botão + para começar",
                    Toast.LENGTH_LONG
                ).show()
            }
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

    @RequiresApi(Build.VERSION_CODES.O)
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

        ((layoutFields.get(0) as LinearLayout).get(0) as CheckBox).setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                lifecycleScope.launch {
                    val novoRemedio = Remedio(0, true, nomeMedicamento, hora)
                    val remedioId = remedioDAO.save(novoRemedio)

                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    val hora = LocalDateTime.now().format(formatter)
                    val remedioTomado = RemedioTomado(0, remedioId, hora.toString())
                    remedioTomadoDAO.save(remedioTomado)
                }
            }
        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateComponent(listaRemedios: List<Remedio>) {
        listaRemedios.forEach {
            addComponent(it.tomado, it.nome, it.hora)
        }
    }

    override fun onPause() {
        val lista: List<Remedio> = itens.map {
            val item = Remedio(
                0,
                ((it.get(0) as LinearLayout).get(0) as CheckBox).isChecked,
                ((it.get(0) as LinearLayout).get(1) as TextView).text.toString(),
                ((it.get(0) as LinearLayout).get(2) as TextView).text.toString()
            )
            item
        }

        saveListaRemedios(lista)

        super.onPause()
    }

    private fun saveListaRemedios(lista: List<Remedio>) {
        lifecycleScope.launch {
            val gson = Gson()
            val json = gson.toJson(lista)
            applicationContext.appDataStore.edit { preferences ->
                preferences[AppPreferencesKeys.LISTA_REMEDIOS] = json
            }
            Log.d("DataStoreMigration", "Lista de Remédios Salva: $json")
        }
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