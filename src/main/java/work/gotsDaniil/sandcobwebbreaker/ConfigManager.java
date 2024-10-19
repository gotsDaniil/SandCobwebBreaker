package work.gotsDaniil.sandcobwebbreaker;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private final SandCobwebBreaker plugin;
    private FileConfiguration config;

    public ConfigManager(SandCobwebBreaker plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private File getDataFolder() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        return dataFolder;
    }

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        // Проверяем, существует ли файл
        if (!configFile.exists()) {
            try {
                // Копируем файл из ресурсов в папку плагина
                InputStream inputStream = plugin.getResource("config.yml");
                if (inputStream == null) {
                    plugin.getLogger().severe("Файл config.yml не найден в ресурсах плагина!");
                    return;
                }
                Path configFilePath = configFile.toPath();
                if (configFilePath == null) {
                    plugin.getLogger().severe("Не удалось получить путь к файлу config.yml!");
                    return;
                }
                Files.copy(inputStream, configFilePath);
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось скопировать файл config.yml из ресурсов!");
                e.printStackTrace();
                return;
            }
        }

        // Загружаем конфигурацию
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public double TPS_THRESHOLD() {
        return config.getDouble("TPS_THRESHOLD");
    }

    public double MSPT_THRESHOLD() {
        return config.getDouble("MSPT_THRESHOLD");
    }

    public int MAX_ENTITIES_PER_BLOCK() {
        return config.getInt("MAX_ENTITIES_PER_BLOCK");
    }

    public void reloadConfig() {
        loadConfig();
        plugin.saveDefaultConfig();
    }
}