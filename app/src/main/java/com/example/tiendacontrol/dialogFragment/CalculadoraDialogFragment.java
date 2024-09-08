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
import java.math.MathContext;
import java.math.RoundingMode;
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
            clear();
            isResultDisplayed = false;
        }

        if (isNewOperation) {
            display.setText("");
        }

        // Esta línea SIEMPRE debe ejecutarse al ingresar un número
        isNewOperation = false;

        display.append(number);
        operationString.append(number);
        updateDisplay();
    }

    // Método para aplicar operadores matemáticos
    private void applyOperator(String operator) {
        // Obtener el texto actual de la operación
        String currentOperation = operationString.toString().trim();

        // Verificar si la pantalla muestra solo el "0" y no permitir añadir operadores en ese caso
        if (display.getText().toString().equals("0") && currentOperation.length() == 0) {
            return; // No hace nada si no hay un número antes del operador
        }

        // Verificar si el último carácter es un operador
        if (!currentOperation.isEmpty()) {
            char lastChar = currentOperation.charAt(currentOperation.length() -1);

            // Si el último carácter es un operador, lo reemplazamos por el nuevo
            if (lastChar == '+' || lastChar == '-' || lastChar == '*' || lastChar == '/' || lastChar == '%') {
                // Si el penúltimo carácter es un espacio, retrocede dos posiciones
                int replaceIndex = operationString.length() - 1;
                if (operationString.charAt(replaceIndex - 1) == ' ') {
                    replaceIndex -= 2;
                }
                operationString.setCharAt(replaceIndex, operator.charAt(0));
            } else {
                // Si no es un operador, agregar el nuevo operador
                operationString.append(operator);
            }
        } else {
            // Si es la primera vez que se agrega un operador, agregar el número actual seguido del operador
            operationString.append(display.getText().toString()).append(" ").append(operator).append(" ");
        }

        // Limpiar la pantalla para que el usuario pueda ingresar el siguiente número
        display.setText("");
        isNewOperation = true;
        updateDisplay();
    }
    // Método para calcular el resultado de la operación
    private void calculateResult() {
        try {
            BigDecimal result = evaluateExpression(operationString.toString());
            display.setText(result.stripTrailingZeros().toPlainString());
            operationString.setLength(0);
            operationString.append(result);
            isNewOperation = true;
            isResultDisplayed = true;
        } catch (Exception e) {
            display.setText("Error");
        }
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
            } else if (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%') {
                if (number.length() > 0) {
                    values.push(new BigDecimal(number.toString()));
                    number.setLength(0);
                }

                while (!operators.isEmpty() && precedence(ch) <= precedence(operators.peek())) {
                    applyLastOperation(values, operators);
                }
                operators.push(ch);
            }
        }

        if (number.length() > 0) {
            values.push(new BigDecimal(number.toString()));
        }

        while (!operators.isEmpty()) {
            applyLastOperation(values, operators);
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
                    return val1.divide(val2, MathContext.DECIMAL128);
                }
                throw new ArithmeticException("División por cero");
            case '%':
                return val1.multiply(val2).divide(BigDecimal.valueOf(100), MathContext.DECIMAL128);
            default:
                throw new UnsupportedOperationException("Operador no soportado");
        }
    }
    // Método para actualizar la pantalla con la operación actual
    private void updateDisplay() {
        display.setText(operationString.toString());
    }

    // Método para enviar el resultado final al listener
    private void sendResult() {
        if (calculadoraListener != null) {
            calculadoraListener.onResult(display.getText().toString());
        }
        dismiss(); // Cerrar el diálogo después de enviar el resultado
    }
    private void applyLastOperation(Stack<BigDecimal> values, Stack<Character> operators) {
        if (!operators.isEmpty()) {
            char op = operators.pop();
            BigDecimal val2 = values.pop();
            BigDecimal val1 = values.pop();
            values.push(applyOperation(val1, val2, op));
        }
    }
}