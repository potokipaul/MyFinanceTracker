package com.myfinancetracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.List;

public class AddTransactionActivity extends AppCompatActivity {

    private static final String[] PAYMENT_METHODS = {
        "Bank Transfer","Debit Card","Cash","Credit Card","POS"
    };

    private EditText etDescription, etAmount, etNotes;
    private Button btnDate, btnSave, btnTypeExpense, btnTypeIncome;
    private Spinner spinnerCategory, spinnerPayment;
    private DatabaseHelper db;
    private List<String> allCategories;

    private String selectedDate = "";
    private boolean isExpense = true;
    private int selectedMonth, selectedYear;
    private boolean isEditMode = false;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);
        db = new DatabaseHelper(this);

        selectedMonth = db.getSelectedMonth();
        selectedYear  = db.getSelectedYear();

        etDescription  = findViewById(R.id.et_description);
        etAmount       = findViewById(R.id.et_amount);
        etNotes        = findViewById(R.id.et_notes);
        btnDate        = findViewById(R.id.btn_date);
        btnSave        = findViewById(R.id.btn_save);
        btnTypeExpense = findViewById(R.id.btn_type_expense);
        btnTypeIncome  = findViewById(R.id.btn_type_income);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerPayment  = findViewById(R.id.spinner_payment);

        allCategories = db.getAllTransactionCategories();
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, allCategories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        ArrayAdapter<String> payAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, PAYMENT_METHODS);
        payAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPayment.setAdapter(payAdapter);

        if (getIntent().hasExtra("edit_id")) {
            isEditMode = true;
            editId = getIntent().getLongExtra("edit_id", -1);
            prefillEdit();
        } else {
            Calendar cal = Calendar.getInstance();
            selectedDate  = String.format("%04d-%02d-%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH));
            selectedMonth = cal.get(Calendar.MONTH) + 1;
            selectedYear  = cal.get(Calendar.YEAR);
            btnDate.setText(FormatUtils.formatDate(selectedDate));
        }

        btnDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (dp, y, m, d) -> {
                selectedDate  = String.format("%04d-%02d-%02d", y, m+1, d);
                selectedMonth = m+1; selectedYear = y;
                btnDate.setText(FormatUtils.formatDate(selectedDate));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        updateTypeUI();
        btnTypeExpense.setOnClickListener(v -> { isExpense = true;  updateTypeUI(); });
        btnTypeIncome.setOnClickListener(v ->  { isExpense = false; updateTypeUI(); });
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void prefillEdit() {
        isExpense = !getIntent().getBooleanExtra("edit_is_income", false);
        etDescription.setText(getIntent().getStringExtra("edit_description"));
        double amt = getIntent().getDoubleExtra("edit_amount", 0);
        etAmount.setText(amt > 0 ? String.valueOf((long) amt) : "");
        etNotes.setText(getIntent().getStringExtra("edit_notes"));
        selectedDate  = getIntent().getStringExtra("edit_date");
        selectedMonth = getIntent().getIntExtra("edit_month", db.getSelectedMonth());
        selectedYear  = getIntent().getIntExtra("edit_year", db.getSelectedYear());
        btnDate.setText(FormatUtils.formatDate(selectedDate));

        String cat = getIntent().getStringExtra("edit_category");
        for (int i = 0; i < allCategories.size(); i++) {
            if (allCategories.get(i).equals(cat)) { spinnerCategory.setSelection(i); break; }
        }
        String pay = getIntent().getStringExtra("edit_payment");
        for (int i = 0; i < PAYMENT_METHODS.length; i++) {
            if (PAYMENT_METHODS[i].equals(pay)) { spinnerPayment.setSelection(i); break; }
        }
        btnSave.setText("Update Transaction");
        updateTypeUI();
    }

    private void updateTypeUI() {
        int red      = android.graphics.Color.parseColor("#C0392B");
        int blue     = android.graphics.Color.parseColor("#1565C0");
        int grey     = android.graphics.Color.parseColor("#E8ECF4");
        int greyText = android.graphics.Color.parseColor("#6B7280");
        if (isExpense) {
            btnTypeExpense.setBackgroundTintList(android.content.res.ColorStateList.valueOf(red));
            btnTypeExpense.setTextColor(android.graphics.Color.WHITE);
            btnTypeIncome.setBackgroundTintList(android.content.res.ColorStateList.valueOf(grey));
            btnTypeIncome.setTextColor(greyText);
        } else {
            btnTypeIncome.setBackgroundTintList(android.content.res.ColorStateList.valueOf(blue));
            btnTypeIncome.setTextColor(android.graphics.Color.WHITE);
            btnTypeExpense.setBackgroundTintList(android.content.res.ColorStateList.valueOf(grey));
            btnTypeExpense.setTextColor(greyText);
        }
    }

    private void saveTransaction() {
        String desc   = etDescription.getText().toString().trim();
        String amtStr = etAmount.getText().toString().trim();
        if (desc.isEmpty())   { etDescription.setError("Required"); return; }
        if (amtStr.isEmpty()) { etAmount.setError("Required"); return; }
        if (selectedDate.isEmpty()) { Toast.makeText(this,"Select a date",Toast.LENGTH_SHORT).show(); return; }
        double amount;
        try { amount = Double.parseDouble(amtStr); }
        catch (NumberFormatException e) { etAmount.setError("Invalid"); return; }

        Transaction t = new Transaction();
        t.setDate(selectedDate);
        t.setDescription(desc);
        t.setCategory(spinnerCategory.getSelectedItem().toString());
        t.setIncome(isExpense ? 0 : amount);
        t.setExpense(isExpense ? amount : 0);
        t.setPaymentMethod(spinnerPayment.getSelectedItem().toString());
        t.setNotes(etNotes.getText().toString().trim());
        t.setMonth(selectedMonth);
        t.setYear(selectedYear);

        if (isEditMode && editId != -1) {
            t.setId(editId);
            db.updateTransaction(t);
            Toast.makeText(this, "✅ Updated!", Toast.LENGTH_SHORT).show();
        } else {
            db.insertTransaction(t);
            Toast.makeText(this, "✅ Saved!", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
