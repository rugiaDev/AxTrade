package com.artillexstudios.axtrade.utils;

import com.artillexstudios.axtrade.hooks.currency.CurrencyHook;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class TaxUtils {

    /** 등급별 수수료 권한 — giraffe.trade.tax.<퍼센트> 중 가장 낮은 값 (10-27 기린월드: VIP 2 · MVP 1, 기본은 화폐 설정 tax) */
    public static final String RANK_TAX_PREFIX = "giraffe.trade.tax.";

    public static double getTotalAfterTax(double original, CurrencyHook currencyHook) {
        return getTotalAfterTax(original, currencyHook, null);
    }

    /** giver = 화폐를 내는 사람 — 그 사람의 등급 수수료를 쓴다 (없으면 화폐 설정 tax) */
    public static double getTotalAfterTax(double original, CurrencyHook currencyHook, Player giver) {
        Number tax = getTaxPercent(currencyHook, giver);
        double taxMulti = getMultiplierFormat(tax.doubleValue());
        if (taxMulti == 1D) return original;
        return floorIfUnsupported(original * taxMulti, currencyHook);
    }

    public static double getTotalTax(double original, CurrencyHook currencyHook) {
        return getTotalTax(original, currencyHook, null);
    }

    public static double getTotalTax(double original, CurrencyHook currencyHook, Player giver) {
        Number tax = getTaxPercent(currencyHook, giver);
        double taxMulti = getMultiplierFormat(tax.doubleValue());
        if (taxMulti == 1D) return 0;
        return ceilIfUnsupported(original - original * taxMulti, currencyHook);
    }

    public static Number getTaxPercent(CurrencyHook currencyHook) {
        return getTaxPercent(currencyHook, null);
    }

    /**
     * 수수료(%) — 화폐 설정 tax 와 내는 사람의 등급 권한(giraffe.trade.tax.N) 중 낮은 값. 수수료가 없는 화폐(tax 0)는 그대로 0
     */
    public static Number getTaxPercent(CurrencyHook currencyHook, Player giver) {
        Number value = (Number) currencyHook.getSettings().getOrDefault("tax", 0);
        double base = Math.min(value.doubleValue(), 99D); // at most 99% tax is possible
        if (base <= 0D || giver == null) return base;
        double rank = findRankTax(giver);
        return rank < 0D ? base : Math.min(base, rank);
    }

    /* 등급 수수료 권한 중 가장 낮은 값 · 없으면 -1 */
    private static double findRankTax(Player player) {
        double best = -1D;
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (!info.getValue()) continue;
            String permission = info.getPermission();
            if (!permission.startsWith(RANK_TAX_PREFIX)) continue;
            try {
                double percent = Double.parseDouble(permission.substring(RANK_TAX_PREFIX.length()));
                if (percent >= 0D && (best < 0D || percent < best)) best = percent;
            } catch (NumberFormatException ignored) {
            }
        }
        return best;
    }

    public static double getMultiplierFormat(double original) {
        return (100D - original) / 100D;
    }

    private static double floorIfUnsupported(double amount, CurrencyHook currencyHook) {
        if (currencyHook.usesDouble()) return amount;
        return Math.floor(amount);
    }

    private static double ceilIfUnsupported(double amount, CurrencyHook currencyHook) {
        if (currencyHook.usesDouble()) return amount;
        return Math.ceil(amount);
    }
}
