package com.pdm.medtime.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pdm.medtime.entities.RemedioTomado

@Dao
interface RemedioTomadoDAO {
    @Insert
    suspend fun save(remedioTomado: RemedioTomado): Long

    @Query("SELECT * FROM remedio_tomado")
    suspend fun findAll(): List<RemedioTomado>
}