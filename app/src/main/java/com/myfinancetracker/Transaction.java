package com.myfinancetracker;

public class Transaction {
    private long id;
    private String date, description, category, paymentMethod, notes;
    private double income, expense;
    private int month, year;

    public Transaction() {}
    public Transaction(long id, String date, String description, String category,
                       double income, double expense, String paymentMethod,
                       String notes, int month, int year) {
        this.id = id; this.date = date; this.description = description;
        this.category = category; this.income = income; this.expense = expense;
        this.paymentMethod = paymentMethod; this.notes = notes;
        this.month = month; this.year = year;
    }

    public long getId() { return id; } public void setId(long id) { this.id = id; }
    public String getDate() { return date; } public void setDate(String d) { this.date = d; }
    public String getDescription() { return description; } public void setDescription(String d) { this.description = d; }
    public String getCategory() { return category; } public void setCategory(String c) { this.category = c; }
    public double getIncome() { return income; } public void setIncome(double i) { this.income = i; }
    public double getExpense() { return expense; } public void setExpense(double e) { this.expense = e; }
    public String getPaymentMethod() { return paymentMethod; } public void setPaymentMethod(String p) { this.paymentMethod = p; }
    public String getNotes() { return notes; } public void setNotes(String n) { this.notes = n; }
    public int getMonth() { return month; } public void setMonth(int m) { this.month = m; }
    public int getYear() { return year; } public void setYear(int y) { this.year = y; }
    public boolean isIncome() { return income > 0; }
}
