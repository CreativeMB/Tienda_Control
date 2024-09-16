package com.creativem.tiendacontrol.helper;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class PuntoMil {
    public static String getFormattedNumber(long number) {
        // Formatea el número con separadores de mil
        return String.format("%,d", Math.abs(number));
    }

    public static void formatNumberWithThousandSeparator(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se necesita implementar
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    editText.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[^\\d]", "");
                    long parsed;
                    try {
                        parsed = Long.parseLong(cleanString);
                    } catch (NumberFormatException e) {
                        parsed = 0;
                    }

                    String formatted = getFormattedNumber(parsed);

                    // Mantener el signo negativo si es necesario
                    current = (s.toString().startsWith("-") ? "-" : "") + formatted;
                    editText.setText(current);
                    editText.setSelection(current.length());
                    editText.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No se necesita implementar
            }
        });
    }
}