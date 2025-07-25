package com.pdm.medtime

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.pdm.medtime.dao.RemedioDAO
import com.pdm.medtime.dao.RemedioTomadoDAO
import com.pdm.medtime.databinding.ActivityLogremediosBinding
import kotlinx.coroutines.launch

class logremedios : AppCompatActivity() {
    private lateinit var itens: ArrayList<LinearLayout>
    private lateinit var database: AppDatabase
    private lateinit var remedioTomadoDAO: RemedioTomadoDAO
    private lateinit var remedioDAO: RemedioDAO
    private lateinit var binding: ActivityLogremediosBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogremediosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        itens = ArrayList()

        database = AppDatabase.getDatabase(this)
        remedioDAO = database.remedioDao()
        remedioTomadoDAO = database.remedioTomadoDao()

        lifecycleScope.launch {
            val remediosRecuperados = remedioTomadoDAO.findAll()
            remediosRecuperados.forEach {
                val nomeRemedio = remedioDAO.findRemedioNomeById(it.remedioFk)
                addComponent(nomeRemedio, it.dataHoraTomada)
            }
        }
    }

    fun addComponent(nomeMedicamento: String, hora: String) {
        val vi =
            binding.root.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layoutFields = vi.inflate(R.layout.logremediotomado, null) as LinearLayout
        itens.add(layoutFields)

        val tv_nome = (layoutFields.get(0) as LinearLayout).get(0) as TextView
        val tv_data = (layoutFields.get(0) as LinearLayout).get(1) as TextView

        tv_nome.text = nomeMedicamento
        tv_data.text = hora

        binding.listaRemediosTomados.addView(layoutFields)
    }
}