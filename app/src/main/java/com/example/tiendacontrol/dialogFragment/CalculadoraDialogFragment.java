package com.example.tiendacontrol.dialogFragment;
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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.math.BigDecimal;
import java.util.Stack;

public class CalculadoraDialogFragment extends BottomSheetDialogFragment {

    private EditText display;
    private StringBuilder operationString = new StringBuilder();
    private boolean isNewOperation = true;
    private boolean isResultDisplayed = false; // Track if result is displayed
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
        View view = inflater.inflate(R.layout.dialog_calculadora, container, false);

        display = view.findViewById(R.id.display);
        display.setText("0");

        setUpButtonListeners(view);

        return view;
    }

    private void setUpButtonListeners(View view) {
        Button btnClear = view.findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(v -> clear());

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

        Button btnEquals = view.findViewById(R.id.btn_equals);
        btnEquals.setOnClickListener(v -> calculateResult());

        Button btnSendResult = view.findViewById(R.id.btn_send_result);
        btnSendResult.setOnClickListener(v -> sendResult());

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

        Button btnDot = view.findViewById(R.id.btn_dot);
        btnDot.setOnClickListener(v -> onNumberClick("."));
    }

    private void clear() {
        display.setText("0");
        operationString.setLength(0); // Clear the operation string
        isNewOperation = true;
        isResultDisplayed = false; // Reset result display flag
    }

    private void onNumberClick(String number) {
        if (isResultDisplayed) {
            // If result is displayed, start a new calculation
            clear();
            isResultDisplayed = false;
        }

        if (isNewOperation) {
            display.setText(""); // Clear display for new number input
            isNewOperation = false;
        }
        // Append the number to display and operationString
        display.append(number);
        operationString.append(number);
        updateDisplay();
    }

    private void applyOperator(String operator) {
        if (isResultDisplayed) {
            // If result is displayed, start a new calculation
            clear();
            isResultDisplayed = false;
        }

        if (!TextUtils.isEmpty(display.getText())) {
            // Add operator to the operation string and reset display
            operationString.append(" ").append(operator).append(" ");
            display.setText("");
            isNewOperation = true;
            updateDisplay();
        }
    }

    private void calculateResult() {
        try {
            // Calculate the result of the entire operation string
            String operationText = operationString.toString().trim();
            if (TextUtils.isEmpty(operationText)) {
                display.setText("Error");
                return;
            }

            // Evaluate the expression
            BigDecimal result = evaluateExpression(operationText);
            display.setText(PuntoMil.getFormattedNumber(result.longValue()));

            // Append the result to the operation string
            operationString.setLength(0);
            operationString.append(operationText).append(" = ").append(result.toString());

            isNewOperation = true;
            isResultDisplayed = true; // Flag to indicate that result is displayed
        } catch (Exception e) {
            display.setText("Error");
        }
        updateDisplay();
    }

    private BigDecimal evaluateExpression(String expression) throws Exception {
        // Parse and evaluate the expression
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
                    return val1.divide(val2, 2, BigDecimal.ROUND_HALF_UP);
                }
                throw new ArithmeticException("Division by zero");
            case '%':
                return val1.multiply(val2).divide(BigDecimal.valueOf(100));
            default:
                throw new UnsupportedOperationException("Operator not supported");
        }
    }

    private void updateDisplay() {
        // Update display to show current operation string without the final equals
        String displayText = operationString.toString();
        if (isResultDisplayed) {
            // Only show the final result
            displayText = display.getText().toString();
        }
        display.setText(displayText);
    }

    private void sendResult() {
        // Send the current display text as the result
        if (calculadoraListener != null) {
            calculadoraListener.onResult(display.getText().toString());
        }
        dismiss(); // Close the dialog
    }
}