package work.gotsDaniil.sandcobwebbreaker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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

    private ConfigManager configManager;
    private final Set<UUID> trackedEntities = new HashSet<>();
    private double TPS_THRESHOLD;
    private double MSPT_THRESHOLD;
    private int MAX_ENTITIES_PER_BLOCK;
    private static final Set<Material> TRACKED_MATERIALS = Set.of(
            Material.SAND, Material.GRAVEL, Material.RED_SAND
    );

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.TPS_THRESHOLD = configManager.TPS_THRESHOLD();
        this.MSPT_THRESHOLD = configManager.MSPT_THRESHOLD();
        this.MAX_ENTITIES_PER_BLOCK = configManager.MAX_ENTITIES_PER_BLOCK();

        Bukkit.getPluginManager().registerEvents(this, this);
        BoatAndMinecraftBreaker boatAndMinecraftBreaker = new BoatAndMinecraftBreaker(configManager);
        Bukkit.getPluginManager().registerEvents(boatAndMinecraftBreaker, this);

        StartTPSCheckTask();
        getCommand("sandcobwebbreaker").setExecutor(this);
    }

    private void StartTPSCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                double mspt = Bukkit.getServer().getAverageTickTime();
                if (mspt >= MSPT_THRESHOLD) {
                    BoatAndMinecraftBreaker.checkAndBreakEntities();
                }
                if (Bukkit.getServer().getTPS()[0] <= TPS_THRESHOLD) {
                    trackedEntities.forEach(entityId -> {
                        Entity entity = Bukkit.getEntity(entityId);
                        if (entity != null && entity.getType() == EntityType.FALLING_BLOCK) {
                            entity.remove();
                        }
                    });
                    trackedEntities.clear();
                }
            }
        }.runTaskTimer(this, 20L, 20L);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.FALLING_BLOCK &&
                (TRACKED_MATERIALS.contains(((FallingBlock) entity).getMaterial())
                        || ((FallingBlock) entity).getMaterial().name().endsWith("_CONCRETE_POWDER"))) {
            this.trackedEntities.add(entity.getUniqueId());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("sandcobwebbreaker")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("sandcobwebbreaker.reload")) {
                    sender.sendMessage("У вас нет прав для выполнения этой команды");
                    return true;
                }
                reloadConfig();
                sender.sendMessage("Конфигурация была " + ChatColor.GREEN + "успешно " + ChatColor.WHITE + "перезагружена");
                return true;
            }
        }
        return false;
    }

    public void reloadConfig() {
        configManager.reloadConfig();
        this.TPS_THRESHOLD = configManager.TPS_THRESHOLD();
    }
}