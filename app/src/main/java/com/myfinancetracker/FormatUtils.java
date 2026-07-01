package com.myfinancetracker;

import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtils {

    public static String formatNaira(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(0);
        return "₦" + nf.format(amount);
    }

    public static String formatDate(String isoDate) {
        if (isoDate == null || isoDate.length() < 10) return isoDate != null ? isoDate : "";
        try {
            String[] p = isoDate.split("-");
            return p[2] + "-" + p[1] + "-" + p[0];
        } catch (Exception e) { return isoDate; }
    }

    public static String getCategoryEmoji(String category) {
        if (category == null) return "📦";
        switch (category) {
            case "Food & Groceries": return "🛒";
            case "Transportation":   return "🚗";
            case "Utilities":        return "💡";
            case "Clothing":         return "👕";
            case "Gym":              return "💪";
            case "Education":        return "📚";
            case "Savings":          return "🏦";
            case "Personal Care":    return "💄";
            case "Gifts/Borrows":    return "🎁";
            case "Outings":          return "🎉";
            case "Miscellaneous":    return "📦";
            case "Income":           return "💰";
            default:                 return "📦";
        }
    }

    public static final String[] MONTH_NAMES = {
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    public static final String[] MONTH_SHORT = {
        "Jan","Feb","Mar","Apr","May","Jun",
        "Jul","Aug","Sep","Oct","Nov","Dec"
    };

    public static String getMonthName(int month) {
        if (month >= 1 && month <= 12) return MONTH_NAMES[month - 1];
        return "";
    }
}
