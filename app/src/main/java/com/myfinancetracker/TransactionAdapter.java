package com.myfinancetracker;

import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.VH> {

    public interface OnItemClickListener { void onItemClick(Transaction t, int position); }

    private final List<Transaction> list;
    private final Context context;
    private OnItemClickListener clickListener;

    public TransactionAdapter(Context context, List<Transaction> list) {
        this.context = context; this.list = list;
    }

    public void setOnItemClickListener(OnItemClickListener l) { this.clickListener = l; }
    public List<Transaction> getList() { return list; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Transaction t = list.get(position);
        h.tvIcon.setText(FormatUtils.getCategoryEmoji(t.getCategory()));
        h.tvDescription.setText(t.getDescription());
        h.tvCategory.setText(t.getCategory());
        h.tvDate.setText(FormatUtils.formatDate(t.getDate()));
        if (h.tvPayment != null)
            h.tvPayment.setText(t.getPaymentMethod() != null ? t.getPaymentMethod() : "");

        boolean isIncome = t.getIncome() > 0;
        double amount = isIncome ? t.getIncome() : t.getExpense();
        h.tvAmount.setText((isIncome ? "+" : "-") + FormatUtils.formatNaira(amount));
        h.tvAmount.setTextColor(isIncome
            ? Color.parseColor("#1B8A4E") : Color.parseColor("#C0392B"));

        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(t, position);
        });
    }

    public void removeItem(int position) {
        list.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, list.size());
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvIcon, tvDescription, tvCategory, tvDate, tvAmount, tvPayment;
        VH(@NonNull View v) {
            super(v);
            tvIcon        = v.findViewById(R.id.tv_category_icon);
            tvDescription = v.findViewById(R.id.tv_description);
            tvCategory    = v.findViewById(R.id.tv_category);
            tvDate        = v.findViewById(R.id.tv_date);
            tvAmount      = v.findViewById(R.id.tv_amount);
            tvPayment     = v.findViewById(R.id.tv_payment);
        }
    }
}
