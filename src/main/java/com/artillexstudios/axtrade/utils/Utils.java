package com.artillexstudios.axtrade.utils;

import com.artillexstudios.axtrade.hooks.currency.CurrencyHook;
import com.artillexstudios.axtrade.lang.LanguageManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class Utils {

    @NotNull
    public static String getFormattedItemName(@NotNull ItemStack itemStack) {
        // 이름 성분은 둘 — custom_name(모루 개명)이 우선, 없으면 item_name(넥소 itemname · GCore setItemName, 09-17). 둘 다 없으면 바닐라 번역명
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return LanguageManager.getTranslated(itemStack.getType());
        String name = meta.hasDisplayName() ? meta.getDisplayName() : (meta.hasItemName() ? meta.getItemName() : "");
        return name.isBlank() ? LanguageManager.getTranslated(itemStack.getType()) : name.replace("§", "&");
    }

    @NotNull
    public static String getFormattedCurrency(@NotNull CurrencyHook currencyHook) {
        return currencyHook.getSettings().getOrDefault("name", currencyHook.getName()).toString();
    }
}
