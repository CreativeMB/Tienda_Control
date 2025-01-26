package com.creativem.tiendacontrol.dialogFragment;

import java.text.DecimalFormat;

public class Utils {
    public static String formatValor(double valor) {
        DecimalFormat df = new DecimalFormat("#.##"); // O "0.00" si siempre quieres dos decimales
        return df.format(valor);
    }
}
