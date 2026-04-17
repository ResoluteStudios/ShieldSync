package resolutestudios.shieldsync.config;

import net.fabricmc.loader.api.FabricLoader;
import resolutestudios.shieldsync.Shieldsync;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigManager {
    public static boolean ENABLED = true;
    public static boolean AXE_FIX_ENABLED = true;
    public static boolean DETECTION_FIX_ENABLED = true;
    public static int MAX_PING_COMPENSATION_MS = 250;

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("shieldsync.properties");

    public static void load() {
        Properties properties = new Properties();

        if (Files.exists(CONFIG_PATH)) {
            try (FileInputStream in = new FileInputStream(CONFIG_PATH.toFile())) {
                properties.load(in);

                ENABLED = Boolean.parseBoolean(properties.getProperty("enabled", "true"));
                AXE_FIX_ENABLED = Boolean.parseBoolean(properties.getProperty("axe_fix_enabled", "true"));
                DETECTION_FIX_ENABLED = Boolean.parseBoolean(properties.getProperty("detection_fix_enabled", "true"));
                MAX_PING_COMPENSATION_MS = Integer.parseInt(properties.getProperty("max_ping_compensation_ms", "250"));
            } catch (IOException | NumberFormatException e) {
                Shieldsync.LOGGER.error("Failed to load shieldsync.properties", e);
            }
        } else {
            save(); // Create with defaults
        }
    }

    public static void save() {
        Properties properties = new Properties();
        properties.setProperty("enabled", String.valueOf(ENABLED));
        properties.setProperty("axe_fix_enabled", String.valueOf(AXE_FIX_ENABLED));
        properties.setProperty("detection_fix_enabled", String.valueOf(DETECTION_FIX_ENABLED));
        properties.setProperty("max_ping_compensation_ms", String.valueOf(MAX_PING_COMPENSATION_MS));

        try (FileOutputStream out = new FileOutputStream(CONFIG_PATH.toFile())) {
            properties.store(out, "ShieldSync Configuration");
        } catch (IOException e) {
            Shieldsync.LOGGER.error("Failed to save shieldsync.properties", e);
        }
    }
}
