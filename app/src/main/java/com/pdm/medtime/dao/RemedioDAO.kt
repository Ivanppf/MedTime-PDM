package com.pdm.medtime.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pdm.medtime.entities.Remedio

@Dao
interface RemedioDAO {

    @Insert
    suspend fun save(remedio: Remedio): Long

    @Query("SELECT * FROM remedios")
    suspend fun findAll(): List<Remedio>

    @Query("SELECT nome FROM remedios WHERE remedioId = :remedioId ")
    suspend fun findRemedioNomeById(remedioId: Long): String

    @Query("SELECT * FROM remedios WHERE remedioId = :remedioId")
    suspend fun findById(remedioId: Int): Remedio

}