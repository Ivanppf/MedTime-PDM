package com.pdm.medtime.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remedios")
data class Remedio(
    @PrimaryKey(autoGenerate = true) val remedioId: Long = 0,
    val tomado: Boolean,
    val nome: String,
    val hora: String
)
