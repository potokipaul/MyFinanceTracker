package com.myfinancetracker;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsFragment extends Fragment {

    private DatabaseHelper db;
    private static final String[] YEARS = {"2024","2025","2026","2027","2028","2029","2030"};

    private LinearLayout customCategoriesList, budgetTargetsContainer;
    private EditText etCatEmoji, etCatName;
    private final Map<String, EditText> budgetFieldMap = new HashMap<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        db = new DatabaseHelper(requireContext());

        // Period spinners
        Spinner spinnerMonth = v.findViewById(R.id.spinner_month);
        Spinner spinnerYear  = v.findViewById(R.id.spinner_year);

        ArrayAdapter<String> mAdp = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, FormatUtils.MONTH_NAMES);
        mAdp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(mAdp);
        spinnerMonth.setSelection(db.getSelectedMonth() - 1);

        ArrayAdapter<String> yAdp = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, YEARS);
        yAdp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yAdp);
        for (int i = 0; i < YEARS.length; i++) {
            if (Integer.parseInt(YEARS[i]) == db.getSelectedYear()) { spinnerYear.setSelection(i); break; }
        }

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View vv, int pos, long id) {
                db.setSetting("selected_month", String.valueOf(pos + 1));
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View vv, int pos, long id) {
                db.setSetting("selected_year", YEARS[pos]);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        // Cash & Borrows
        EditText etCash   = v.findViewById(R.id.et_cash_at_hand);
        EditText etBorrow = v.findViewById(R.id.et_borrows);
        double cash = db.getCashAtHand(), borrow = db.getBorrows();
        if (cash > 0)   etCash.setText(String.valueOf((long) cash));
        if (borrow > 0) etBorrow.setText(String.valueOf((long) borrow));
        v.findViewById(R.id.btn_save_cash).setOnClickListener(vv -> {
            String c = etCash.getText().toString().trim();
            String b = etBorrow.getText().toString().trim();
            db.setSetting("cash_at_hand", c.isEmpty() ? "0" : c);
            db.setSetting("borrows", b.isEmpty() ? "0" : b);
            Toast.makeText(requireContext(), "✅ Saved!", Toast.LENGTH_SHORT).show();
        });

        // Categories
        customCategoriesList   = v.findViewById(R.id.custom_categories_list);
        budgetTargetsContainer = v.findViewById(R.id.budget_targets_container);
        etCatEmoji = v.findViewById(R.id.et_cat_emoji);
        etCatName  = v.findViewById(R.id.et_cat_name);

        v.findViewById(R.id.btn_add_category).setOnClickListener(vv -> addCategory());

        refreshCategoryList();
        refreshBudgetFields();

        v.findViewById(R.id.btn_save_budget).setOnClickListener(vv -> saveBudgets());

        return v;
    }

    private void addCategory() {
        String name  = etCatName.getText().toString().trim();
        String emoji = etCatEmoji.getText().toString().trim();
        if (name.isEmpty()) { etCatName.setError("Enter a name"); return; }
        try {
            db.addCustomCategory(name, emoji.isEmpty() ? "📦" : emoji);
            etCatName.setText(""); etCatEmoji.setText("");
            refreshCategoryList(); refreshBudgetFields();
            Toast.makeText(requireContext(), "✅ Category added!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Category already exists", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshCategoryList() {
        customCategoriesList.removeAllViews();
        List<String> all = db.getAllExpenseCategories();
        for (String cat : all) {
            String emoji = db.getCategoryEmoji(cat);

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dp(8));
            row.setLayoutParams(lp);
            row.setBackground(requireContext().getDrawable(R.drawable.input_background));
            row.setPadding(dp(12), dp(10), dp(12), dp(10));

            TextView tvEmoji = new TextView(requireContext());
            tvEmoji.setText(emoji);
            tvEmoji.setTextSize(18);
            tvEmoji.setPadding(0, 0, dp(10), 0);
            row.addView(tvEmoji);

            TextView tvName = new TextView(requireContext());
            tvName.setText(cat);
            tvName.setTextColor(Color.parseColor("#1A1F36"));
            tvName.setTextSize(14);
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            tvName.setLayoutParams(nlp);
            row.addView(tvName);

            // Delete button — available for ALL categories
            Button btnDel = new Button(requireContext());
            btnDel.setText("✕");
            btnDel.setTextSize(14);
            btnDel.setTextColor(Color.parseColor("#C0392B"));
            btnDel.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                Color.parseColor("#FDECEA")));
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(dp(40), dp(40));
            blp.setMargins(dp(8), 0, 0, 0);
            btnDel.setLayoutParams(blp);
            btnDel.setPadding(0, 0, 0, 0);
            final String catName = cat;
            btnDel.setOnClickListener(vv -> confirmDelete(catName));
            row.addView(btnDel);

            // Long press = rename dialog
            final String catEmoji = emoji;
            row.setOnLongClickListener(vvv -> { showRenameDialog(catName, catEmoji); return true; });

            customCategoriesList.addView(row);
        }

        // Add hint label
        TextView hint = new TextView(requireContext());
        hint.setText("Tip: long-press a row to rename it");
        hint.setTextColor(Color.parseColor("#9CA3AF"));
        hint.setTextSize(11);
        hint.setPadding(0, dp(4), 0, 0);
        customCategoriesList.addView(hint);
    }

    private void confirmDelete(String catName) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete \"" + catName + "\"?")
            .setMessage("This removes the category from budget targets. Existing transactions keep their category name.")
            .setPositiveButton("Delete", (d, w) -> {
                db.deleteCategory(catName);
                refreshCategoryList(); refreshBudgetFields();
                Toast.makeText(requireContext(), "Category removed", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showRenameDialog(String oldName, String oldEmoji) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Rename Category");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(16), dp(20), dp(8));

        EditText etEmoji = new EditText(requireContext());
        etEmoji.setText(oldEmoji);
        etEmoji.setGravity(android.view.Gravity.CENTER);
        etEmoji.setTextSize(22);
        etEmoji.setBackground(requireContext().getDrawable(R.drawable.input_background));
        etEmoji.setPadding(dp(10), dp(8), dp(10), dp(8));
        LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        elp.setMargins(0, 0, 0, dp(10));
        etEmoji.setLayoutParams(elp);

        EditText etName = new EditText(requireContext());
        etName.setText(oldName);
        etName.setBackground(requireContext().getDrawable(R.drawable.input_background));
        etName.setPadding(dp(14), dp(8), dp(14), dp(8));
        etName.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(52)));

        layout.addView(etEmoji);
        layout.addView(etName);
        builder.setView(layout);

        builder.setPositiveButton("Save", (d, w) -> {
            String newName  = etName.getText().toString().trim();
            String newEmoji = etEmoji.getText().toString().trim();
            if (!newName.isEmpty()) {
                db.renameCategory(oldName, newName, newEmoji.isEmpty() ? oldEmoji : newEmoji);
                refreshCategoryList(); refreshBudgetFields();
                Toast.makeText(requireContext(), "✅ Renamed!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void refreshBudgetFields() {
        budgetTargetsContainer.removeAllViews();
        budgetFieldMap.clear();
        for (String cat : db.getAllExpenseCategories()) {
            String emoji = db.getCategoryEmoji(cat);

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dp(12));
            row.setLayoutParams(lp);

            TextView tvLabel = new TextView(requireContext());
            tvLabel.setText(emoji + "  " + cat);
            tvLabel.setTextColor(Color.parseColor("#1A1F36"));
            tvLabel.setTextSize(14);
            tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            row.addView(tvLabel);

            EditText et = new EditText(requireContext());
            et.setBackground(requireContext().getDrawable(R.drawable.input_background));
            et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            et.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
            et.setPadding(dp(10), dp(8), dp(10), dp(8));
            et.setLayoutParams(new LinearLayout.LayoutParams(dp(120), dp(44)));
            double b = db.getBudget(cat);
            if (b > 0) et.setText(String.valueOf((long) b));

            budgetFieldMap.put(cat, et);
            row.addView(et);
            budgetTargetsContainer.addView(row);
        }
    }

    private void saveBudgets() {
        for (Map.Entry<String, EditText> entry : budgetFieldMap.entrySet()) {
            String val = entry.getValue().getText().toString().trim();
            if (!val.isEmpty()) {
                try { db.setBudget(entry.getKey(), Double.parseDouble(val)); }
                catch (NumberFormatException ignored) {}
            }
        }
        Toast.makeText(requireContext(), "✅ Budget saved!", Toast.LENGTH_SHORT).show();
    }

    private int dp(int val) {
        return Math.round(val * requireContext().getResources().getDisplayMetrics().density);
    }
}
