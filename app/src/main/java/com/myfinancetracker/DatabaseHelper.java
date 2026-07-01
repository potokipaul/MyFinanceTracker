package com.myfinancetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "finance_tracker.db";
    private static final int DB_VERSION = 4;

    public static final String TABLE_TRANSACTIONS  = "transactions";
    public static final String COL_ID              = "id";
    public static final String COL_DATE            = "date";
    public static final String COL_DESCRIPTION     = "description";
    public static final String COL_CATEGORY        = "category";
    public static final String COL_INCOME          = "income";
    public static final String COL_EXPENSE         = "expense";
    public static final String COL_PAYMENT         = "payment_method";
    public static final String COL_NOTES           = "notes";
    public static final String COL_MONTH           = "month";
    public static final String COL_YEAR            = "year";

    public static final String TABLE_BUDGET        = "budget";
    public static final String COL_BUDGET_CATEGORY = "category";
    public static final String COL_BUDGET_AMOUNT   = "amount";

    public static final String TABLE_SETTINGS      = "settings";
    public static final String COL_KEY             = "key_name";
    public static final String COL_VALUE           = "value";

    public static final String TABLE_CATEGORIES    = "custom_categories";
    public static final String COL_CAT_NAME        = "name";
    public static final String COL_CAT_EMOJI       = "emoji";
    public static final String COL_CAT_IS_DEFAULT  = "is_default";

    private static final Object[][] DEFAULT_CATS = {
        {"Food & Groceries", "🛒", 60000},
        {"Transportation",   "🚗", 20000},
        {"Utilities",        "💡", 58800},
        {"Clothing",         "👕", 30000},
        {"Gym",              "💪", 20000},
        {"Education",        "📚", 10000},
        {"Savings",          "🏦", 30000},
        {"Personal Care",    "💄", 4500},
        {"Gifts/Borrows",    "🎁", 30000},
        {"Outings",          "🎉", 15000},
        {"Miscellaneous",    "📦", 20000},
    };

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + "(" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_DATE + " TEXT," + COL_DESCRIPTION + " TEXT," +
            COL_CATEGORY + " TEXT," + COL_INCOME + " REAL DEFAULT 0," +
            COL_EXPENSE + " REAL DEFAULT 0," + COL_PAYMENT + " TEXT," +
            COL_NOTES + " TEXT," + COL_MONTH + " INTEGER," + COL_YEAR + " INTEGER)");

        db.execSQL("CREATE TABLE " + TABLE_BUDGET + "(" +
            COL_BUDGET_CATEGORY + " TEXT PRIMARY KEY," +
            COL_BUDGET_AMOUNT + " REAL DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_SETTINGS + "(" +
            COL_KEY + " TEXT PRIMARY KEY," + COL_VALUE + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + "(" +
            COL_CAT_NAME + " TEXT PRIMARY KEY," +
            COL_CAT_EMOJI + " TEXT," +
            COL_CAT_IS_DEFAULT + " INTEGER DEFAULT 0)");

        for (Object[] cat : DEFAULT_CATS) {
            insertCategoryRow(db, (String) cat[0], (String) cat[1], true);
            ContentValues bv = new ContentValues();
            bv.put(COL_BUDGET_CATEGORY, (String) cat[0]);
            bv.put(COL_BUDGET_AMOUNT, (double)(int) cat[2]);
            db.insert(TABLE_BUDGET, null, bv);
        }

        // Use device current date
        Calendar cal = Calendar.getInstance();
        insertSetting(db, "selected_month", String.valueOf(cal.get(Calendar.MONTH) + 1));
        insertSetting(db, "selected_year",  String.valueOf(cal.get(Calendar.YEAR)));
        insertSetting(db, "cash_at_hand",   "0");
        insertSetting(db, "borrows",        "0");
    }

    private void insertCategoryRow(SQLiteDatabase db, String name, String emoji, boolean isDefault) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAT_NAME, name);
        cv.put(COL_CAT_EMOJI, emoji);
        cv.put(COL_CAT_IS_DEFAULT, isDefault ? 1 : 0);
        db.insertOrThrow(TABLE_CATEGORIES, null, cv);
    }

    private void insertSetting(SQLiteDatabase db, String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(COL_KEY, key); cv.put(COL_VALUE, value);
        db.insert(TABLE_SETTINGS, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try { db.execSQL("INSERT OR IGNORE INTO " + TABLE_SETTINGS + " VALUES('cash_at_hand','0')"); } catch (Exception ignored) {}
            try { db.execSQL("INSERT OR IGNORE INTO " + TABLE_SETTINGS + " VALUES('borrows','0')"); } catch (Exception ignored) {}
        }
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORIES + "(" +
                COL_CAT_NAME + " TEXT PRIMARY KEY," +
                COL_CAT_EMOJI + " TEXT," +
                COL_CAT_IS_DEFAULT + " INTEGER DEFAULT 0)");
            for (Object[] cat : DEFAULT_CATS) {
                try { insertCategoryRow(db, (String) cat[0], (String) cat[1], true); }
                catch (Exception ignored) {}
            }
        }
        if (oldVersion < 4) {
            // Sync to current device date
            Calendar cal = Calendar.getInstance();
            int m = cal.get(Calendar.MONTH) + 1;
            int y = cal.get(Calendar.YEAR);
            try {
                db.execSQL("INSERT OR REPLACE INTO " + TABLE_SETTINGS + " VALUES('selected_month','" + m + "')");
                db.execSQL("INSERT OR REPLACE INTO " + TABLE_SETTINGS + " VALUES('selected_year','" + y + "')");
            } catch (Exception ignored) {}
        }
    }

    // ── Categories ───────────────────────────────────────────────────────────

    public List<String> getAllExpenseCategories() {
        List<String> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT " + COL_CAT_NAME + " FROM " + TABLE_CATEGORIES +
            " ORDER BY " + COL_CAT_IS_DEFAULT + " DESC, " + COL_CAT_NAME + " ASC", null);
        while (c.moveToNext()) list.add(c.getString(0));
        c.close(); return list;
    }

    public List<String> getAllTransactionCategories() {
        List<String> list = getAllExpenseCategories();
        list.add("Income");
        return list;
    }

    public String getCategoryEmoji(String name) {
        if (name == null) return "📦";
        if (name.equals("Income")) return "💰";
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT " + COL_CAT_EMOJI + " FROM " + TABLE_CATEGORIES +
            " WHERE " + COL_CAT_NAME + "=?", new String[]{name});
        if (c.moveToFirst()) { String e = c.getString(0); c.close(); return e != null ? e : "📦"; }
        c.close();
        return FormatUtils.getCategoryEmoji(name);
    }

    public void addCustomCategory(String name, String emoji) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAT_NAME, name);
        cv.put(COL_CAT_EMOJI, emoji != null && !emoji.isEmpty() ? emoji : "📦");
        cv.put(COL_CAT_IS_DEFAULT, 0);
        getWritableDatabase().insertOrThrow(TABLE_CATEGORIES, null, cv);
        ContentValues bv = new ContentValues();
        bv.put(COL_BUDGET_CATEGORY, name);
        bv.put(COL_BUDGET_AMOUNT, 0);
        getWritableDatabase().insertOrThrow(TABLE_BUDGET, null, bv);
    }

    /** Rename any category — updates categories, budget, and all transactions */
    public void renameCategory(String oldName, String newName, String newEmoji) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(COL_CAT_NAME, newName);
            cv.put(COL_CAT_EMOJI, newEmoji);
            db.update(TABLE_CATEGORIES, cv, COL_CAT_NAME + "=?", new String[]{oldName});
            ContentValues bv = new ContentValues();
            bv.put(COL_BUDGET_CATEGORY, newName);
            db.update(TABLE_BUDGET, bv, COL_BUDGET_CATEGORY + "=?", new String[]{oldName});
            ContentValues tv = new ContentValues();
            tv.put(COL_CATEGORY, newName);
            db.update(TABLE_TRANSACTIONS, tv, COL_CATEGORY + "=?", new String[]{oldName});
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }

    /** Delete any category (default or custom) */
    public void deleteCategory(String name) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CATEGORIES, COL_CAT_NAME + "=?", new String[]{name});
        db.delete(TABLE_BUDGET, COL_BUDGET_CATEGORY + "=?", new String[]{name});
    }

    public boolean isDefaultCategory(String name) {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT " + COL_CAT_IS_DEFAULT + " FROM " + TABLE_CATEGORIES +
            " WHERE " + COL_CAT_NAME + "=?", new String[]{name});
        if (c.moveToFirst()) { int v = c.getInt(0); c.close(); return v == 1; }
        c.close(); return false;
    }

    // ── Transactions ─────────────────────────────────────────────────────────

    public long insertTransaction(Transaction t) {
        return getWritableDatabase().insert(TABLE_TRANSACTIONS, null, buildTxnCV(t));
    }

    public void updateTransaction(Transaction t) {
        getWritableDatabase().update(TABLE_TRANSACTIONS, buildTxnCV(t),
            COL_ID + "=?", new String[]{String.valueOf(t.getId())});
    }

    public void deleteTransaction(long id) {
        getWritableDatabase().delete(TABLE_TRANSACTIONS,
            COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    private ContentValues buildTxnCV(Transaction t) {
        ContentValues cv = new ContentValues();
        cv.put(COL_DATE, t.getDate()); cv.put(COL_DESCRIPTION, t.getDescription());
        cv.put(COL_CATEGORY, t.getCategory()); cv.put(COL_INCOME, t.getIncome());
        cv.put(COL_EXPENSE, t.getExpense()); cv.put(COL_PAYMENT, t.getPaymentMethod());
        cv.put(COL_NOTES, t.getNotes()); cv.put(COL_MONTH, t.getMonth());
        cv.put(COL_YEAR, t.getYear());
        return cv;
    }

    /** Ordered by ID DESC = newest entry first (as inputted) */
    public List<Transaction> getTransactionsByMonthYear(int month, int year) {
        return queryTransactions(
            "WHERE " + COL_MONTH + "=? AND " + COL_YEAR + "=? ORDER BY " + COL_ID + " DESC",
            new String[]{String.valueOf(month), String.valueOf(year)});
    }

    public List<Transaction> getTransactionsByYear(int year) {
        return queryTransactions(
            "WHERE " + COL_YEAR + "=? ORDER BY " + COL_ID + " DESC",
            new String[]{String.valueOf(year)});
    }

    private List<Transaction> queryTransactions(String where, String[] args) {
        List<Transaction> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT * FROM " + TABLE_TRANSACTIONS + " " + where, args);
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close(); return list;
    }

    private Transaction fromCursor(Cursor c) {
        return new Transaction(
            c.getLong(c.getColumnIndexOrThrow(COL_ID)),
            c.getString(c.getColumnIndexOrThrow(COL_DATE)),
            c.getString(c.getColumnIndexOrThrow(COL_DESCRIPTION)),
            c.getString(c.getColumnIndexOrThrow(COL_CATEGORY)),
            c.getDouble(c.getColumnIndexOrThrow(COL_INCOME)),
            c.getDouble(c.getColumnIndexOrThrow(COL_EXPENSE)),
            c.getString(c.getColumnIndexOrThrow(COL_PAYMENT)),
            c.getString(c.getColumnIndexOrThrow(COL_NOTES)),
            c.getInt(c.getColumnIndexOrThrow(COL_MONTH)),
            c.getInt(c.getColumnIndexOrThrow(COL_YEAR)));
    }

    // ── Budget ────────────────────────────────────────────────────────────────

    public double getBudget(String category) {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT " + COL_BUDGET_AMOUNT + " FROM " + TABLE_BUDGET +
            " WHERE " + COL_BUDGET_CATEGORY + "=?", new String[]{category});
        if (c.moveToFirst()) { double v = c.getDouble(0); c.close(); return v; }
        c.close(); return 0;
    }

    public void setBudget(String category, double amount) {
        ContentValues cv = new ContentValues();
        cv.put(COL_BUDGET_AMOUNT, amount);
        int rows = getWritableDatabase().update(TABLE_BUDGET, cv,
            COL_BUDGET_CATEGORY + "=?", new String[]{category});
        if (rows == 0) {
            cv.put(COL_BUDGET_CATEGORY, category);
            getWritableDatabase().insert(TABLE_BUDGET, null, cv);
        }
    }

    // ── Settings ──────────────────────────────────────────────────────────────

    public String getSetting(String key, String def) {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT " + COL_VALUE + " FROM " + TABLE_SETTINGS +
            " WHERE " + COL_KEY + "=?", new String[]{key});
        if (c.moveToFirst()) { String v = c.getString(0); c.close(); return v; }
        c.close(); return def;
    }

    public void setSetting(String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(COL_VALUE, value);
        int rows = getWritableDatabase().update(TABLE_SETTINGS, cv,
            COL_KEY + "=?", new String[]{key});
        if (rows == 0) {
            cv.put(COL_KEY, key);
            getWritableDatabase().insert(TABLE_SETTINGS, null, cv);
        }
    }

    public int getSelectedMonth() {
        String stored = getSetting("selected_month", "0");
        int month = 0;
        try { month = Integer.parseInt(stored); } catch (Exception ignored) {}
        if (month < 1 || month > 12) {
            month = Calendar.getInstance().get(Calendar.MONTH) + 1;
            setSetting("selected_month", String.valueOf(month));
        }
        return month;
    }

    public int getSelectedYear() {
        String stored = getSetting("selected_year", "0");
        int year = 0;
        try { year = Integer.parseInt(stored); } catch (Exception ignored) {}
        if (year < 2000) {
            year = Calendar.getInstance().get(Calendar.YEAR);
            setSetting("selected_year", String.valueOf(year));
        }
        return year;
    }

    public double getCashAtHand() {
        try { return Double.parseDouble(getSetting("cash_at_hand", "0")); }
        catch (Exception e) { return 0; }
    }

    public double getBorrows() {
        try { return Double.parseDouble(getSetting("borrows", "0")); }
        catch (Exception e) { return 0; }
    }
}
