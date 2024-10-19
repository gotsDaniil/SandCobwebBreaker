package work.gotsDaniil.sandcobwebbreaker;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class BoatAndMinecraftBreaker implements Listener {

    private ConfigManager configManager;
    private static int MAX_ENTITIES_PER_BLOCK;

    public BoatAndMinecraftBreaker(ConfigManager configManager) {
        this.configManager = configManager;
        MAX_ENTITIES_PER_BLOCK = configManager.MAX_ENTITIES_PER_BLOCK();
    }

    public static void checkAndBreakEntities() {
        Map<Location, Integer> entityCountMap = new HashMap<>();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Minecart || entity instanceof Boat || entity instanceof ArmorStand) {
                    Location loc = entity.getLocation();
                    entityCountMap.merge(loc, 1, Integer::sum);
                }
            }
        }

        for (Map.Entry<Location, Integer> entry : entityCountMap.entrySet()) {
            if (entry.getValue() > MAX_ENTITIES_PER_BLOCK) {
                for (Entity entity : entry.getKey().getChunk().getEntities()) {
                    if ((entity instanceof Minecart || entity instanceof Boat || entity instanceof ArmorStand) && entity.getLocation().equals(entry.getKey())) {
                        entity.remove();
                    }
                }
            }
        }
    }
}