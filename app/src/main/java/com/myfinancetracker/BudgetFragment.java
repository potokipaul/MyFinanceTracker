package com.myfinancetracker;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class BudgetFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_budget, container, false);
        DatabaseHelper db = new DatabaseHelper(requireContext());

        int month = db.getSelectedMonth();
        int year  = db.getSelectedYear();
        ((TextView) v.findViewById(R.id.tv_budget_period))
            .setText(FormatUtils.getMonthName(month) + " " + year);

        List<Transaction> txns = db.getTransactionsByMonthYear(month, year);
        List<String> categories = db.getAllExpenseCategories();
        List<BudgetItem> items = new ArrayList<>();
        for (String cat : categories) {
            double budget = db.getBudget(cat);
            double actual = 0;
            for (Transaction t : txns) if (t.getCategory().equals(cat)) actual += t.getExpense();
            items.add(new BudgetItem(cat, budget, actual));
        }

        RecyclerView rv = v.findViewById(R.id.rv_budget);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new BudgetAdapter(items));
        return v;
    }
}
