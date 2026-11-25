package com.willard.avisoscampus

import android.app.Application
import com.willard.avisoscampus.notificacion.Notificador

class AvisosApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notificador.crearCanal(this)
    }
}