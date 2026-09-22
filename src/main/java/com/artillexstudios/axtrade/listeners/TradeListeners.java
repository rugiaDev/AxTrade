package com.artillexstudios.axtrade.listeners;

import com.artillexstudios.axtrade.request.Request;
import com.artillexstudios.axtrade.request.Requests;
import com.artillexstudios.axtrade.trade.Trade;
import com.artillexstudios.axtrade.trade.Trades;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

import static com.artillexstudios.axtrade.AxTrade.CONFIG;
import static com.artillexstudios.axtrade.AxTrade.MESSAGEUTILS;

public class TradeListeners implements Listener {

    // 기린월드: GCore 는 나가기 알림의 LOWEST 에서 인벤을 찍어 DB 에 저장하고, 다음 접속 때 그 저장으로 인벤을 덮는다.
    // 거래 취소로 돌려받는 아이템이 그 뒤에 인벤에 들어가면 저장에 빠져 사라진다 (창이 닫힌 금액 입력 중에 나갈 때).
    // 그래서 LOWEST + plugin.yml loadbefore: [GCore] (같은 순위에서는 먼저 등록한 쪽이 먼저 돈다) 로 GCore 보다 앞에 둔다 (09-21)
    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(@NotNull PlayerQuitEvent event) {
        handleQuitTrade(event);
        handleQuitRequest(event);
    }

    public void handleQuitTrade(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Trade trade = Trades.getTrade(player);
        if (trade == null) return;
        trade.abort();
    }

    public void handleQuitRequest(@NotNull PlayerQuitEvent event) {
        Iterator<Request> iterator = Requests.getRequests().iterator();
        while (iterator.hasNext()) {
            Request request = iterator.next();
            if (request.getSender().equals(event.getPlayer())) {
                iterator.remove();
                continue;
            }
            if (request.getReceiver().equals(event.getPlayer())) {
                iterator.remove();
                if (!request.isActive()) continue;
                MESSAGEUTILS.sendLang(request.getSender(), "request.expired", Map.of("%player%", request.getReceiver().getName()));
            }
        }
    }

    @EventHandler
    public void onDrop(@NotNull PlayerDropItemEvent event) {
        cancelIfTrading(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickup(@NotNull EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        cancelIfTrading(player, event);
    }

    @EventHandler
    public void onArrowPickup(@NotNull PlayerPickupArrowEvent event) {
        cancelIfTrading(event.getPlayer(), event);
    }

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        Trade trade = Trades.getTrade(event.getPlayer());
        if (trade == null) return;
        if (!CONFIG.getBoolean("abort.move", true)) return;
        if (System.currentTimeMillis() - trade.getPrepTime() < 1_000L) return;
        if (event.getFrom().distanceSquared(event.getTo()) == 0) return;
        trade.abort();
    }

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent event) {
        Trade trade = Trades.getTrade(event.getPlayer());
        if (trade == null) return;
        if (!CONFIG.getBoolean("abort.interact", true)) return;
        if (System.currentTimeMillis() - trade.getPrepTime() < 1_000L) return;
        event.setCancelled(true);
        trade.abort();
    }

    @EventHandler
    public void onCommand(@NotNull PlayerCommandPreprocessEvent event) {
        Trade trade = Trades.getTrade(event.getPlayer());
        if (trade == null) return;
        if (!CONFIG.getBoolean("abort.command", true)) return;
        event.setCancelled(true);
        trade.abort();
    }

    private void cancelIfTrading(Player player, Cancellable event) {
        Trade trade = Trades.getTrade(player);
        if (trade == null) return;
        event.setCancelled(true);
    }
}
