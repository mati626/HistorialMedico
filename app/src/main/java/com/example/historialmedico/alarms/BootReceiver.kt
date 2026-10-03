package com.example.historialmedico.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.historialmedico.data.AppDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action!= Intent.ACTION_BOOT_COMPLETED)return

        val pendingResult=goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataBase= AppDataBase.getInstance(context)

                val medicamentos=dataBase.medicamentoDao().getConRecordatorio()
                medicamentos.forEach { medicamento ->
                    val hora=medicamento.horaRecordatorio
                    if(hora!=null){
                        AlarmScheduler.programarRecordatorioMedicamento(
                            context, medicamento.id, medicamento.nombre, hora
                        )
                    }
                }

                val horasMedicas= dataBase.horaMedicaDao().getConRecordatorio()
                horasMedicas.forEach { hora ->
                    val millis= hora.recordatorioMillis
                    if(millis != null && millis> System.currentTimeMillis()){
                        AlarmScheduler.programarRecordatorioHoraMedica(
                            context, hora.id, hora.especialidad, hora.lugar, millis
                        )
                    }
                }
            }finally {
                pendingResult.finish()
            }
        }
    }
}