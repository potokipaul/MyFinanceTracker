package com.myfinancetracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.List;

public class TransactionsFragment extends Fragment {

    private static final int ADD_REQUEST  = 101;
    private static final int EDIT_REQUEST = 102;

    private DatabaseHelper db;
    private RecyclerView rv;
    private LinearLayout emptyState, monthFilterContainer;
    private TransactionAdapter adapter;
    private List<Transaction> transactions;
    private TextView tvSummary, tvPeriod;
    private int filterMonth, currentYear;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_transactions, container, false);
        db = new DatabaseHelper(requireContext());

        rv                   = v.findViewById(R.id.rv_transactions);
        emptyState           = v.findViewById(R.id.empty_state);
        tvSummary            = v.findViewById(R.id.tv_transactions_summary);
        tvPeriod             = v.findViewById(R.id.tv_transactions_period);
        monthFilterContainer = v.findViewById(R.id.month_filter_container);

        currentYear = db.getSelectedYear();
        filterMonth = db.getSelectedMonth();

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        buildMonthFilter();
        loadTransactions();

        // Swipe left = delete, swipe right = edit
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override public boolean onMove(@NonNull RecyclerView rv,
                    @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) { return false; }

            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                int pos = vh.getAdapterPosition();
                Transaction t = adapter.getList().get(pos);
                if (direction == ItemTouchHelper.LEFT) {
                    new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Transaction")
                        .setMessage("Delete \"" + t.getDescription() + "\"?")
                        .setPositiveButton("Delete", (d, w) -> {
                            db.deleteTransaction(t.getId());
                            adapter.removeItem(pos);
                            updateSummary(); checkEmpty();
                        })
                        .setNegativeButton("Cancel", (d, w) -> adapter.notifyItemChanged(pos))
                        .setOnCancelListener(d -> adapter.notifyItemChanged(pos))
                        .show();
                } else {
                    adapter.notifyItemChanged(pos);
                    openEditSheet(t, pos);
                }
            }

            @Override public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                    @NonNull RecyclerView.ViewHolder vh, float dX, float dY,
                    int actionState, boolean isActive) {
                View item = vh.itemView;
                Paint paint = new Paint();
                Paint textPaint = new Paint();
                textPaint.setTextSize(44);
                textPaint.setTextAlign(Paint.Align.CENTER);
                if (dX < 0) {
                    paint.setColor(Color.parseColor("#C0392B"));
                    c.drawRect(item.getRight() + dX, item.getTop(), item.getRight(), item.getBottom(), paint);
                    c.drawText("🗑", item.getRight() - 70, item.getTop() + item.getHeight() / 2f + 16, textPaint);
                } else if (dX > 0) {
                    paint.setColor(Color.parseColor("#1565C0"));
                    c.drawRect(item.getLeft(), item.getTop(), item.getLeft() + dX, item.getBottom(), paint);
                    c.drawText("✏", item.getLeft() + 70, item.getTop() + item.getHeight() / 2f + 16, textPaint);
                }
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isActive);
            }
        }).attachToRecyclerView(rv);

        adapter = new TransactionAdapter(requireContext(), transactions);
        adapter.setOnItemClickListener((t, pos) -> openDetailSheet(t, pos));
        rv.setAdapter(adapter);

        v.findViewById(R.id.fab_add).setOnClickListener(vv ->
            startActivityForResult(new Intent(requireContext(), AddTransactionActivity.class), ADD_REQUEST));

        return v;
    }

    private void buildMonthFilter() {
        monthFilterContainer.removeAllViews();
        for (int m = 1; m <= 12; m++) {
            final int month = m;
            TextView chip = new TextView(requireContext());
            chip.setText(FormatUtils.MONTH_SHORT[m - 1]);
            chip.setTextSize(13);
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 8, 0);
            chip.setLayoutParams(lp);
            chip.setPadding(24, 12, 24, 12);
            chip.setBackground(requireContext().getDrawable(
                m == filterMonth ? R.drawable.chip_filter_selected : R.drawable.chip_filter_unselected));
            chip.setTextColor(m == filterMonth ? Color.WHITE : Color.parseColor("#6B7280"));
            chip.setOnClickListener(vv -> { filterMonth = month; buildMonthFilter(); loadTransactions(); });
            monthFilterContainer.addView(chip);
        }
    }

    private void loadTransactions() {
        transactions = db.getTransactionsByMonthYear(filterMonth, currentYear);
        tvPeriod.setText(FormatUtils.getMonthName(filterMonth) + " " + currentYear);

        if (adapter == null) {
            adapter = new TransactionAdapter(requireContext(), transactions);
            adapter.setOnItemClickListener((t, pos) -> openDetailSheet(t, pos));
            rv.setAdapter(adapter);
        } else {
            adapter.getList().clear();
            adapter.getList().addAll(transactions);
            adapter.notifyDataSetChanged();
        }
        updateSummary(); checkEmpty();
    }

    private void updateSummary() {
        double inc = 0, exp = 0;
        for (Transaction t : adapter.getList()) { inc += t.getIncome(); exp += t.getExpense(); }
        tvSummary.setText(adapter.getItemCount() + " entries  ·  In: " +
            FormatUtils.formatNaira(inc) + "  Out: " + FormatUtils.formatNaira(exp));
    }

    private void checkEmpty() {
        rv.setVisibility(adapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void openDetailSheet(Transaction t, int pos) {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View sv = LayoutInflater.from(requireContext()).inflate(R.layout.bottomsheet_transaction, null);
        sheet.setContentView(sv);

        ((TextView) sv.findViewById(R.id.bs_icon)).setText(FormatUtils.getCategoryEmoji(t.getCategory()));
        ((TextView) sv.findViewById(R.id.bs_description)).setText(t.getDescription());
        ((TextView) sv.findViewById(R.id.bs_category)).setText(t.getCategory());
        ((TextView) sv.findViewById(R.id.bs_date)).setText(FormatUtils.formatDate(t.getDate()));
        ((TextView) sv.findViewById(R.id.bs_payment)).setText(t.getPaymentMethod() != null ? t.getPaymentMethod() : "-");
        ((TextView) sv.findViewById(R.id.bs_notes)).setText(
            (t.getNotes() != null && !t.getNotes().isEmpty()) ? t.getNotes() : "-");

        boolean isIncome = t.getIncome() > 0;
        TextView tvAmt = sv.findViewById(R.id.bs_amount);
        tvAmt.setText((isIncome ? "+" : "-") + FormatUtils.formatNaira(isIncome ? t.getIncome() : t.getExpense()));
        tvAmt.setTextColor(isIncome ? Color.parseColor("#1B8A4E") : Color.parseColor("#C0392B"));

        sv.findViewById(R.id.bs_btn_edit).setOnClickListener(v -> { sheet.dismiss(); openEditSheet(t, pos); });
        sv.findViewById(R.id.bs_btn_delete).setOnClickListener(v -> {
            sheet.dismiss();
            new AlertDialog.Builder(requireContext())
                .setTitle("Delete Transaction")
                .setMessage("Delete \"" + t.getDescription() + "\"?")
                .setPositiveButton("Delete", (d, w) -> { db.deleteTransaction(t.getId()); adapter.removeItem(pos); updateSummary(); checkEmpty(); })
                .setNegativeButton("Cancel", null).show();
        });
        sheet.show();
    }

    private void openEditSheet(Transaction t, int pos) {
        Intent i = new Intent(requireContext(), AddTransactionActivity.class);
        i.putExtra("edit_id", t.getId());
        i.putExtra("edit_description", t.getDescription());
        i.putExtra("edit_amount", t.getIncome() > 0 ? t.getIncome() : t.getExpense());
        i.putExtra("edit_is_income", t.getIncome() > 0);
        i.putExtra("edit_date", t.getDate());
        i.putExtra("edit_category", t.getCategory());
        i.putExtra("edit_payment", t.getPaymentMethod());
        i.putExtra("edit_notes", t.getNotes());
        i.putExtra("edit_month", t.getMonth());
        i.putExtra("edit_year", t.getYear());
        i.putExtra("edit_pos", pos);
        startActivityForResult(i, EDIT_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_REQUEST || requestCode == EDIT_REQUEST) loadTransactions();
    }
}
