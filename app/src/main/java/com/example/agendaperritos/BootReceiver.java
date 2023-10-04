package com.example.agendaperritos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Aquí puedes realizar cualquier tarea necesaria después del reinicio del dispositivo.
            // Por ejemplo, puedes configurar tus alarmas o recordatorios nuevamente.
        }
    }
}
