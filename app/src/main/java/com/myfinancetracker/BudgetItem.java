package com.myfinancetracker;

public class BudgetItem {
    private String category;
    private double budgeted, actual;

    public BudgetItem(String category, double budgeted, double actual) {
        this.category = category; this.budgeted = budgeted; this.actual = actual;
    }
    public String getCategory() { return category; }
    public double getBudgeted() { return budgeted; }
    public double getActual() { return actual; }
    public double getRemaining() { return budgeted - actual; }
    public int getPercentUsed() {
        if (budgeted <= 0) return 0;
        return (int) Math.min((actual / budgeted) * 100, 100);
    }
    public boolean isOverBudget() { return actual > budgeted && budgeted > 0; }
    public String getStatus() { return isOverBudget() ? "⚠ Over Budget" : "✔ On Track"; }
}
