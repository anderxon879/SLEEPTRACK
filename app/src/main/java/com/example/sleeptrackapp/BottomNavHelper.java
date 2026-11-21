package com.example.sleeptrackapp;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

// HELPER: Administrador de la barra de navegación inferior (BottomNavigationView) para transiciones fluidas sin parpadeos.
public class BottomNavHelper {

    // ESTO SIRVE PARA: Vincular los eventos de clic en cada icono del menú inferior y navegar a la pantalla correspondiente
    public static void setup(Activity activity, int selectedItemId) {
        // ESTO SIRVE PARA: Obtener la barra inferior definida en el layout de la pantalla actual
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) return;

        // ESTO SIRVE PARA: Resaltar visualmente el icono de la pestaña activa en pantalla
        bottomNavigationView.setSelectedItemId(selectedItemId);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            // ESTO SIRVE PARA: Si el usuario pulsa el icono donde ya está ubicado, no recargar la pantalla innecesariamente
            if (itemId == selectedItemId) {
                return true;
            }

            // ESTO SIRVE PARA: Determinar el destino de navegación según la opción presionada
            Intent intent = null;
            if (itemId == R.id.nav_inicio) {
                // Navega al Dashboard principal de descanso
                intent = new Intent(activity, ResumenDescansoActivity.class);
            } else if (itemId == R.id.nav_historial) {
                // Navega al Calendario mensual interactivo
                intent = new Intent(activity, HistorialMensualActivity.class);
            } else if (itemId == R.id.nav_registrar) {
                // Navega a la Encuesta matutina de factores de sueño
                intent = new Intent(activity, RegistroDiarioActivity.class);
            } else if (itemId == R.id.nav_consejos) {
                // Navega a las Recomendaciones inteligentes de higiene del sueño
                intent = new Intent(activity, RecomendacionesActivity.class);
            } else if (itemId == R.id.nav_perfil) {
                // Navega al Perfil del estudiante universitario
                intent = new Intent(activity, PerfilCuentaActivity.class);
            }

            // GRACIAS A ESTO: Se limpian actividades intermedias en la pila para ahorrar memoria RAM y evitar lag
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
                // ESTO SIRVE PARA: Desactivar animación pesada y lograr una transición instantánea entre pestañas
                activity.overridePendingTransition(0, 0);
                activity.finish();
                return true;
            }
            return false;
        });
    }
}
