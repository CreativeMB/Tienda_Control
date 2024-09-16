package com.creativem.tiendacontrol.monitor;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.creativem.tiendacontrol.R;
import java.util.Arrays;
import java.util.List;

public class LosLirios extends AppCompatActivity {

    private static final String URL_TO_SHARE = "https://www.floristerialoslirios.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.los_lirios);

        ImageView iconInicio = findViewById(R.id.inicio);
        iconInicio.setOnClickListener(view -> startActivity(new Intent(LosLirios.this, BaseDatos.class)));

        ImageView shareIcon = findViewById(R.id.linkCompartir);
        shareIcon.setOnClickListener(view -> shareLink(URL_TO_SHARE));

        WebView webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Habilitar JavaScript si es necesario

        // Verificar la conexión a Internet
        if (!isInternetAvailable()) {
            showNoInternetDialog();
        } else {
            // Establecer WebViewClient personalizado
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    String url = request.getUrl().toString();
                    if (isExternalAppUrl(url)) {
                        openExternalApp(url);
                        return true; // Indica que la URL fue manejada externamente
                    }

                    view.loadUrl(url);
                    return false; // Deja que el WebView cargue las URLs internas
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // Para versiones antiguas de Android
                    if (isExternalAppUrl(url)) {
                        openExternalApp(url);
                        return true;
                    }

                    view.loadUrl(url);
                    return false;
                }
            });

            // Cargar una URL inicial en el WebView
            webView.loadUrl("https://www.floristerialoslirios.com/");
        }
    }

    private void shareLink(String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(shareIntent, "Compartir enlace"));
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sin conexión a Internet")
                .setMessage("No hay conexión a Internet. ¿Deseas volver a la actividad de base de datos?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Volver a la actividad BaseDatos
                        Intent intent = new Intent(LosLirios.this, BaseDatos.class);
                        startActivity(intent);
                        finish(); // Finaliza la actividad actual si es necesario
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cerrar el diálogo sin hacer nada
                        dialog.dismiss();
                    }
                })
                .setCancelable(false) // El diálogo no puede ser cancelado tocando fuera de él
                .show();
    }

    private boolean isExternalAppUrl(String url) {
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();

        // Esquemas personalizados como fb-messenger://, instagram://
        if (scheme != null && (scheme.equals("fb-messenger") || scheme.equals("whatsapp") ||
                scheme.equals("tg") || scheme.equals("mailto") ||
                scheme.equals("tel") || scheme.equals("instagram"))) {
            return true;
        }

        String host = uri.getHost();
        if (host == null) {
            return false;
        }

        // Lista de dominios externos
        List<String> externalDomains = Arrays.asList(
                "wa.me",
                "whatsapp.com",
                "instagram.com",
                "facebook.com",
                "telegram.me",
                "messenger.com",
                "tiktok.com",
                "play.google.com"
        );

        for (String domain : externalDomains) {
            if (host.contains(domain)) {
                return true;
            }
        }

        return false;
    }

    private void openExternalApp(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        // Verifica si Instagram está instalado para manejar el esquema instagram://
        if (url.startsWith("instagram://")) {
            intent.setPackage("com.instagram.android");
        }

        try {
            startActivity(intent);
        } catch (Exception e) {
            // Si la aplicación no está instalada, abrir en el navegador web
            if (url.startsWith("instagram://")) {
                String webUrl = url.replace("instagram://user?username=", "https://www.instagram.com/");
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)));
            }
        }
    }
}