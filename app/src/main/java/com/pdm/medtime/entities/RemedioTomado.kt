package com.pdm.medtime.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "remedio_tomado",
    foreignKeys = [
        ForeignKey(
            entity = Remedio::class,
            parentColumns = ["remedioId"],
            childColumns = ["remedioFk"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RemedioTomado(
    @PrimaryKey(autoGenerate = true)
    val remedioTomadoId: Long = 0,
    val remedioFk: Long,
    val dataHoraTomada: String

)