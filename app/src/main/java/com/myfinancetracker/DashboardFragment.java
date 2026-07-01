package com.myfinancetracker;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.List;

public class DashboardFragment extends Fragment {

    private DatabaseHelper db;
    private LinearLayout budgetContainer, recentContainer;
    private TextView tvPeriod, tvIncome, tvExpenses, tvBalance, tvCash, tvBorrow;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);
        db = new DatabaseHelper(requireContext());

        tvPeriod   = v.findViewById(R.id.tv_period);
        tvIncome   = v.findViewById(R.id.tv_total_income);
        tvExpenses = v.findViewById(R.id.tv_total_expenses);
        tvBalance  = v.findViewById(R.id.tv_balance);
        tvCash     = v.findViewById(R.id.tv_cash);
        tvBorrow   = v.findViewById(R.id.tv_borrow);
        budgetContainer = v.findViewById(R.id.budget_overview_container);
        recentContainer = v.findViewById(R.id.recent_transactions_container);

        loadData();
        return v;
    }

    private void loadData() {
        int month = db.getSelectedMonth();
        int year  = db.getSelectedYear();
        tvPeriod.setText(FormatUtils.getMonthName(month) + " " + year);

        List<Transaction> txns = db.getTransactionsByMonthYear(month, year);
        double totalIncome = 0, totalExpenses = 0;
        for (Transaction t : txns) { totalIncome += t.getIncome(); totalExpenses += t.getExpense(); }

        tvIncome.setText(FormatUtils.formatNaira(totalIncome));
        tvExpenses.setText(FormatUtils.formatNaira(totalExpenses));
        tvBalance.setText(FormatUtils.formatNaira(totalIncome - totalExpenses));
        tvCash.setText(FormatUtils.formatNaira(db.getCashAtHand()));
        tvBorrow.setText(FormatUtils.formatNaira(db.getBorrows()));

        budgetContainer.removeAllViews();
        List<String> categories = db.getAllExpenseCategories();
        int shown = 0;
        for (String cat : categories) {
            if (shown >= 4) break;
            double budget = db.getBudget(cat);
            double actual = 0;
            for (Transaction t : txns) if (t.getCategory().equals(cat)) actual += t.getExpense();
            if (budget == 0 && actual == 0) continue;
            addBudgetRow(cat, budget, actual);
            shown++;
        }

        recentContainer.removeAllViews();
        int count = Math.min(5, txns.size());
        for (int i = 0; i < count; i++) addTransactionRow(txns.get(i));
    }

    private void addBudgetRow(String category, double budget, double actual) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.card_elevated);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(lp);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setElevation(dp(2));

        LinearLayout topRow = new LinearLayout(requireContext());
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView tvIcon = new TextView(requireContext());
        tvIcon.setText(db.getCategoryEmoji(category));
        tvIcon.setTextSize(18);
        topRow.addView(tvIcon);

        TextView tvCat = new TextView(requireContext());
        tvCat.setText("  " + category);
        tvCat.setTextColor(Color.parseColor("#1A1F36"));
        tvCat.setTextSize(14);
        tvCat.setTypeface(null, Typeface.BOLD);
        tvCat.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        topRow.addView(tvCat);

        boolean over = actual > budget && budget > 0;
        TextView tvStatus = new TextView(requireContext());
        tvStatus.setText(over ? "⚠ Over" : "✔");
        tvStatus.setTextColor(over ? Color.parseColor("#C0392B") : Color.parseColor("#1B8A4E"));
        tvStatus.setTextSize(12);
        tvStatus.setTypeface(null, Typeface.BOLD);
        topRow.addView(tvStatus);
        card.addView(topRow);

        ProgressBar pb = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        pb.setProgress(budget > 0 ? (int) Math.min((actual / budget) * 100, 100) : 0);
        pb.setProgressTintList(ColorStateList.valueOf(
            over ? Color.parseColor("#C0392B") : Color.parseColor("#3949AB")));
        pb.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8ECF4")));
        LinearLayout.LayoutParams pblp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(8));
        pblp.setMargins(0, dp(10), 0, dp(8));
        pb.setLayoutParams(pblp);
        card.addView(pb);

        TextView tvAmt = new TextView(requireContext());
        tvAmt.setText(FormatUtils.formatNaira(actual) + " of " + FormatUtils.formatNaira(budget));
        tvAmt.setTextColor(Color.parseColor("#6B7280"));
        tvAmt.setTextSize(12);
        card.addView(tvAmt);

        budgetContainer.addView(card);
    }

    private void addTransactionRow(Transaction t) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.ripple_rounded);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(lp);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setGravity(android.view.Gravity.CENTER_VERTICAL);
        card.setElevation(dp(2));

        TextView tvEmoji = new TextView(requireContext());
        tvEmoji.setText(db.getCategoryEmoji(t.getCategory()));
        tvEmoji.setTextSize(22);
        card.addView(tvEmoji);

        LinearLayout details = new LinearLayout(requireContext());
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(dp(14), 0, 0, 0);
        details.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvDesc = new TextView(requireContext());
        tvDesc.setText(t.getDescription());
        tvDesc.setTextColor(Color.parseColor("#1A1F36"));
        tvDesc.setTextSize(14);
        tvDesc.setTypeface(null, Typeface.BOLD);
        tvDesc.setMaxLines(1);
        tvDesc.setEllipsize(android.text.TextUtils.TruncateAt.END);

        TextView tvSub = new TextView(requireContext());
        tvSub.setText(t.getCategory() + "  ·  " + FormatUtils.formatDate(t.getDate()));
        tvSub.setTextColor(Color.parseColor("#6B7280"));
        tvSub.setTextSize(12);
        tvSub.setPadding(0, dp(4), 0, 0);

        details.addView(tvDesc);
        details.addView(tvSub);
        card.addView(details);

        boolean isIncome = t.getIncome() > 0;
        TextView tvAmt = new TextView(requireContext());
        tvAmt.setText((isIncome ? "+" : "-") + FormatUtils.formatNaira(isIncome ? t.getIncome() : t.getExpense()));
        tvAmt.setTextColor(isIncome ? Color.parseColor("#1B8A4E") : Color.parseColor("#C0392B"));
        tvAmt.setTextSize(14);
        tvAmt.setTypeface(null, Typeface.BOLD);
        card.addView(tvAmt);

        recentContainer.addView(card);
    }

    private int dp(int val) {
        return Math.round(val * requireContext().getResources().getDisplayMetrics().density);
    }
}
