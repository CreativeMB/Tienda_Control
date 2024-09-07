package com.example.tiendacontrol.dialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.tiendacontrol.R;
import com.example.tiendacontrol.helper.PuntoMil;
import com.example.tiendacontrol.model.ControlCalculadora;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.math.BigDecimal;
import java.util.Stack;

public class CalculadoraDialogFragment extends BottomSheetDialogFragment {

    private EditText display;
    private StringBuilder operationString = new StringBuilder();
    private boolean isNewOperation = true;
    private boolean isResultDisplayed = false; // Para rastrear si se muestra el resultado
    private CalculadoraListener calculadoraListener;

    public interface CalculadoraListener {
        void onResult(String result);
    }

    public void setCalculadoraListener(CalculadoraListener listener) {
        this.calculadoraListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calculadora, container, false);

        display = view.findViewById(R.id.display);
        display.setText("0");

        setUpButtonListeners(view);

        return view;
    }
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // Actualizar el estado global cuando el diálogo se cierra
        ControlCalculadora.getInstance().setCalculadoraDialogVisible(false);
    }
    private void setUpButtonListeners(View view) {
        // Botón de limpiar pantalla
        Button btnClear = view.findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(v -> clear());

        // Botones de operaciones
        Button btnPercent = view.findViewById(R.id.btn_percent);
        btnPercent.setOnClickListener(v -> applyOperator("%"));

        Button btnDivide = view.findViewById(R.id.btn_divide);
        btnDivide.setOnClickListener(v -> applyOperator("/"));

        Button btnMultiply = view.findViewById(R.id.btn_multiply);
        btnMultiply.setOnClickListener(v -> applyOperator("*"));

        Button btnSubtract = view.findViewById(R.id.btn_subtract);
        btnSubtract.setOnClickListener(v -> applyOperator("-"));

        Button btnAdd = view.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(v -> applyOperator("+"));

        // Botón para enviar el resultado
        Button btnSendResult = view.findViewById(R.id.btn_send_result);
        btnSendResult.setOnClickListener(v -> {
            calculateResult();  // Primero calcula el resultado
            sendResult();       // Luego envía el resultado
        });

        // Botón para eliminar el último dígito
        Button btnBackspace = view.findViewById(R.id.btn_backspace);
        btnBackspace.setOnClickListener(v -> deleteLastDigit());

        // Listener para los botones de números
        View.OnClickListener numberClickListener = v -> {
            Button button = (Button) v;
            onNumberClick(button.getText().toString());
        };

        view.findViewById(R.id.btn0).setOnClickListener(numberClickListener);
        view.findViewById(R.id.btn1).setOnClickListener(numberClickListener);
        view.findViewById(R.id.btn2).setOnClickListener(numberClickListener);
        view.findViewById(R.id.btn3).setOnClickListener(numberClickListener);
        view.findViewById(R.id.btn4).setOnClickListener(numberClickListener);
        view.findViewById(R.id.btn5).setOnClickListener(numberClickListener);
        view.findViewById(R.id.btn6).setOnClickListener(numberClickListener);
        view.findViewById(R.id.btn7).setOnClickListener(numberClickListener);
        view.findViewById(R.id.btn8).setOnClickListener(numberClickListener);
        view.findViewById(R.id.btn9).setOnClickListener(numberClickListener);

        // Botón del punto decimal
        Button btnDot = view.findViewById(R.id.btn_dot);
        btnDot.setOnClickListener(v -> onNumberClick("."));
    }

    // Método para limpiar la pantalla y restablecer los valores
    private void clear() {
        display.setText("0");
        operationString.setLength(0); // Limpiar la cadena de la operación
        isNewOperation = true;
        isResultDisplayed = false; // Reiniciar el indicador de resultado
    }

    // Método para gestionar la entrada de números
    private void onNumberClick(String number) {
        if (isResultDisplayed) {
            clear(); // Iniciar un nuevo cálculo si ya se mostró un resultado
            isResultDisplayed = false;
        }

        if (isNewOperation) {
            display.setText(""); // Limpiar la pantalla para nuevo número
            isNewOperation = false;
        }

        // Añadir el número a la pantalla y a la cadena de operación
        display.append(number);
        operationString.append(number);
        updateDisplay();
    }

    // Método para aplicar operadores matemáticos
    private void applyOperator(String operator) {
        if (isResultDisplayed) {
            clear(); // Iniciar un nuevo cálculo si ya se mostró un resultado
            isResultDisplayed = false;
        }

        if (!TextUtils.isEmpty(display.getText())) {
            operationString.append(" ").append(operator).append(" "); // Añadir el operador a la cadena
            display.setText("");
            isNewOperation = true;
            updateDisplay();
        }
    }

    // Método para calcular el resultado de la operación
    private void calculateResult() {
        try {
            String operationText = operationString.toString().trim();
            if (TextUtils.isEmpty(operationText)) {
                display.setText("Error");
                return;
            }

            BigDecimal result = evaluateExpression(operationText); // Evaluar la expresión
            display.setText(PuntoMil.getFormattedNumber(result.longValue()));

            operationString.setLength(0); // Limpiar la cadena y añadir el resultado
            operationString.append(operationText).append(" = ").append(result.toString());

            isNewOperation = true;
            isResultDisplayed = true; // Marcar que se ha mostrado el resultado
        } catch (Exception e) {
            display.setText("Error");
        }
        updateDisplay();
    }

    // Método para eliminar el último dígito ingresado
    private void deleteLastDigit() {
        String currentText = display.getText().toString();
        if (currentText.length() > 0 && !isResultDisplayed) {
            currentText = currentText.substring(0, currentText.length() - 1);
            display.setText(currentText.isEmpty() ? "0" : currentText);

            // Eliminar también de operationString
            if (operationString.length() > 0) {
                operationString.deleteCharAt(operationString.length() - 1);
            }
        }
    }
    // Método para evaluar la expresión matemática
    private BigDecimal evaluateExpression(String expression) throws Exception {
        Stack<BigDecimal> values = new Stack<>();
        Stack<Character> operators = new Stack<>();
        StringBuilder number = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            if (Character.isDigit(ch) || ch == '.') {
                number.append(ch);
            } else if (ch == ' ') {
                if (number.length() > 0) {
                    values.push(new BigDecimal(number.toString()));
                    number.setLength(0);
                }
            } else {
                while (!operators.isEmpty() && precedence(ch) <= precedence(operators.peek())) {
                    BigDecimal val2 = values.pop();
                    BigDecimal val1 = values.pop();
                    char op = operators.pop();
                    values.push(applyOperation(val1, val2, op));
                }
                operators.push(ch);
            }
        }
        if (number.length() > 0) {
            values.push(new BigDecimal(number.toString()));
        }
        while (!operators.isEmpty()) {
            BigDecimal val2 = values.pop();
            BigDecimal val1 = values.pop();
            char op = operators.pop();
            values.push(applyOperation(val1, val2, op));
        }
        return values.pop();
    }

    // Método para definir la precedencia de los operadores
    private int precedence(char op) {
        switch (op) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
            case '%':
                return 3;
        }
        return -1;
    }

    // Método para aplicar la operación matemática
    private BigDecimal applyOperation(BigDecimal val1, BigDecimal val2, char op) {
        switch (op) {
            case '+':
                return val1.add(val2);
            case '-':
                return val1.subtract(val2);
            case '*':
                return val1.multiply(val2);
            case '/':
                if (val2.compareTo(BigDecimal.ZERO) != 0) {
                    return val1.divide(val2, 2, BigDecimal.ROUND_HALF_UP);
                }
                throw new ArithmeticException("División por cero");
            case '%':
                return val1.multiply(val2).divide(BigDecimal.valueOf(100));
            default:
                throw new UnsupportedOperationException("Operador no soportado");
        }
    }

    // Método para actualizar la pantalla con la operación actual
    private void updateDisplay() {
        String displayText = operationString.toString();
        if (isResultDisplayed) {
            displayText = display.getText().toString(); // Mostrar solo el resultado
        }
        display.setText(displayText);
    }

    // Método para enviar el resultado final al listener
    private void sendResult() {
        if (calculadoraListener != null) {
            calculadoraListener.onResult(display.getText().toString());
        }
        dismiss(); // Cerrar el diálogo después de enviar el resultado
    }
}