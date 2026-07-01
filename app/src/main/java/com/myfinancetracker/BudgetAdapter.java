package com.myfinancetracker;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.VH> {

    private final List<BudgetItem> list;

    public BudgetAdapter(List<BudgetItem> list) { this.list = list; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        BudgetItem item = list.get(position);
        h.tvIcon.setText(FormatUtils.getCategoryEmoji(item.getCategory()));
        h.tvCategory.setText(item.getCategory());

        boolean over = item.isOverBudget();
        h.tvStatus.setText(item.getStatus());
        h.tvStatus.setTextColor(over ? Color.parseColor("#C0392B") : Color.parseColor("#1B8A4E"));
        h.tvStatus.setBackgroundTintList(ColorStateList.valueOf(
            over ? Color.parseColor("#FDECEA") : Color.parseColor("#E8F5EE")));

        h.progress.setProgress(item.getPercentUsed());
        h.progress.setProgressTintList(ColorStateList.valueOf(
            over ? Color.parseColor("#C0392B") : Color.parseColor("#3949AB")));
        h.progress.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8ECF4")));

        h.tvBudgeted.setText(FormatUtils.formatNaira(item.getBudgeted()));
        h.tvActual.setText(FormatUtils.formatNaira(item.getActual()));
        h.tvRemaining.setText(FormatUtils.formatNaira(item.getRemaining()));
        h.tvRemaining.setTextColor(over ? Color.parseColor("#C0392B") : Color.parseColor("#1B8A4E"));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvIcon, tvCategory, tvStatus, tvBudgeted, tvActual, tvRemaining;
        ProgressBar progress;
        VH(@NonNull View v) {
            super(v);
            tvIcon      = v.findViewById(R.id.tv_budget_icon);
            tvCategory  = v.findViewById(R.id.tv_budget_category);
            tvStatus    = v.findViewById(R.id.tv_budget_status);
            progress    = v.findViewById(R.id.progress_budget);
            tvBudgeted  = v.findViewById(R.id.tv_budgeted);
            tvActual    = v.findViewById(R.id.tv_actual);
            tvRemaining = v.findViewById(R.id.tv_remaining);
        }
    }
}
