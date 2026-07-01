package com.myfinancetracker;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.*;

public class YearlyFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_yearly, container, false);
        DatabaseHelper db = new DatabaseHelper(requireContext());

        int year = db.getSelectedYear();
        ((TextView) v.findViewById(R.id.tv_yearly_year)).setText("Year " + year);

        List<Transaction> all = db.getTransactionsByYear(year);
        double yearIncome = 0, yearExpenses = 0;
        for (Transaction t : all) { yearIncome += t.getIncome(); yearExpenses += t.getExpense(); }

        ((TextView) v.findViewById(R.id.tv_year_income)).setText(FormatUtils.formatNaira(yearIncome));
        ((TextView) v.findViewById(R.id.tv_year_expenses)).setText(FormatUtils.formatNaira(yearExpenses));
        ((TextView) v.findViewById(R.id.tv_year_savings)).setText(FormatUtils.formatNaira(yearIncome - yearExpenses));

        LinearLayout monthlyContainer = v.findViewById(R.id.monthly_container);
        for (int m = 1; m <= 12; m++) {
            double inc = 0, exp = 0;
            for (Transaction t : all) {
                if (t.getMonth() == m) { inc += t.getIncome(); exp += t.getExpense(); }
            }
            if (inc == 0 && exp == 0) continue;
            addMonthRow(monthlyContainer, FormatUtils.getMonthName(m), inc, exp);
        }

        LinearLayout catContainer = v.findViewById(R.id.category_container);
        Map<String, Double> catMap = new LinkedHashMap<>();
        for (String cat : db.getAllExpenseCategories()) catMap.put(cat, 0.0);
        for (Transaction t : all) {
            if (catMap.containsKey(t.getCategory()))
                catMap.put(t.getCategory(), catMap.get(t.getCategory()) + t.getExpense());
        }
        for (Map.Entry<String, Double> entry : catMap.entrySet()) {
            if (entry.getValue() > 0) addCategoryRow(catContainer, db, entry.getKey(), entry.getValue());
        }

        return v;
    }

    private void addMonthRow(LinearLayout container, String month, double income, double expense) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.card_elevated);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(8));
        card.setLayoutParams(lp);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setElevation(dp(2));

        TextView tvMonth = new TextView(requireContext());
        tvMonth.setText(month);
        tvMonth.setTextColor(Color.parseColor("#1A1F36"));
        tvMonth.setTextSize(14);
        tvMonth.setTypeface(null, Typeface.BOLD);
        card.addView(tvMonth);

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(8), 0, 0);
        addAmtCol(row, "Income",   income,              Color.parseColor("#1B8A4E"), 1);
        addAmtCol(row, "Expenses", expense,             Color.parseColor("#C0392B"), 1);
        addAmtCol(row, "Net",      income - expense,
            income >= expense ? Color.parseColor("#1565C0") : Color.parseColor("#C0392B"), 1);
        card.addView(row);
        container.addView(card);
    }

    private void addAmtCol(LinearLayout parent, String label, double value, int color, int weight) {
        LinearLayout col = new LinearLayout(requireContext());
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));

        TextView tvLabel = new TextView(requireContext());
        tvLabel.setText(label);
        tvLabel.setTextColor(Color.parseColor("#9CA3AF"));
        tvLabel.setTextSize(11);

        TextView tvVal = new TextView(requireContext());
        tvVal.setText(FormatUtils.formatNaira(value));
        tvVal.setTextColor(color);
        tvVal.setTextSize(13);
        tvVal.setTypeface(null, Typeface.BOLD);
        tvVal.setPadding(0, dp(3), 0, 0);

        col.addView(tvLabel);
        col.addView(tvVal);
        parent.addView(col);
    }

    private void addCategoryRow(LinearLayout container, DatabaseHelper db, String category, double amount) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.card_elevated);
        card.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(8));
        card.setLayoutParams(lp);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setElevation(dp(2));

        TextView tvEmoji = new TextView(requireContext());
        tvEmoji.setText(db.getCategoryEmoji(category));
        tvEmoji.setTextSize(20);

        LinearLayout details = new LinearLayout(requireContext());
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(dp(14), 0, 0, 0);
        details.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvCat = new TextView(requireContext());
        tvCat.setText(category);
        tvCat.setTextColor(Color.parseColor("#1A1F36"));
        tvCat.setTextSize(14);
        details.addView(tvCat);

        TextView tvAmt = new TextView(requireContext());
        tvAmt.setText(FormatUtils.formatNaira(amount));
        tvAmt.setTextColor(Color.parseColor("#C0392B"));
        tvAmt.setTextSize(14);
        tvAmt.setTypeface(null, Typeface.BOLD);

        card.addView(tvEmoji);
        card.addView(details);
        card.addView(tvAmt);
        container.addView(card);
    }

    private int dp(int val) {
        return Math.round(val * requireContext().getResources().getDisplayMetrics().density);
    }
}
