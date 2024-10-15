package com.example.sandcobwebbreaker;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SandCobwebBreaker extends JavaPlugin implements Listener {

    private final Set<UUID> trackedEntities = new HashSet<>();
    private double tpsThreshold = 18.5;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        // Запускаем задачу для проверки TPS и удаления падающего песка
        new BukkitRunnable() {
            @Override
            public void run() {
                double tps = Bukkit.getServer().getTPS()[0];
                if (tps <= tpsThreshold) {
                    for (UUID entityId : trackedEntities) {
                        Entity entity = Bukkit.getEntity(entityId);
                        if (entity != null && entity.getType() == EntityType.FALLING_BLOCK) {
                            entity.remove();
                        }
                    }
                    trackedEntities.clear();
                }
            }
        }.runTaskTimer(this, 20L, 20L); // Проверка каждую секунду
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.FALLING_BLOCK) {
            FallingBlock fallingBlock = (FallingBlock) entity;
            if (fallingBlock.getMaterial() == org.bukkit.Material.SAND) {
                trackedEntities.add(entity.getUniqueId());
            }
        }
    }
}