package com.example.sleeptrackapp;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

// HELPER: Notificaciones visuales flotantes superiores (Snackbar personalizado oscuro y redondeado con iconos).
public class CustomToast {

    // ESTO SIRVE PARA: Construir y mostrar una alerta emergente moderna en el borde superior de la pantalla
    public static void show(Activity activity, String message, boolean isError) {
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView == null) return;

        View decorView = activity.getWindow().getDecorView();

        // ESTO SIRVE PARA: Instanciar el componente Snackbar nativo con duración estándar
        Snackbar snackbar = Snackbar.make(decorView, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();

        // ESTO SIRVE PARA: Aplicar el fondo con esquinas redondeadas y tono oscuro personalizado
        snackbarView.setBackgroundResource(R.drawable.bg_custom_toast);

        // ESTO SIRVE PARA: Posicionar la alerta en la parte superior y centrada horizontalmente
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.setMargins(20, 80, 20, 0);
        snackbarView.setLayoutParams(params);
        snackbarView.setElevation(24f);

        // ESTO SIRVE PARA: Configurar el icono representativo (check verde para éxito o alerta para error)
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (textView != null) {
            textView.setTextColor(Color.WHITE);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextSize(14f);

            if (isError) {
                // Icono rojo de error si falló una operación o validación
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error_outline, 0, 0, 0);
                textView.setCompoundDrawablePadding(16);
            } else {
                // Icono verde de check si la operación fue exitosa
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
                textView.setCompoundDrawablePadding(16);
            }
        }

        // ESTO SIRVE PARA: Mostrar la alerta flotante en pantalla
        snackbar.show();
    }
}
