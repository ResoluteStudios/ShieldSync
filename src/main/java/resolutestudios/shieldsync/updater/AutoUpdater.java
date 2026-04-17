package resolutestudios.shieldsync.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import resolutestudios.shieldsync.Shieldsync;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AutoUpdater {
    // Assuming the repo is ResoluteStudios/ShieldSync based on typical github paths
    private static final String API_URL = "https://api.github.com/repos/ResoluteStudios/ShieldSync/releases/latest";

    public static void checkForUpdatesAndApply(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Checking GitHub for ShieldSync updates...").formatted(Formatting.YELLOW), false);

        CompletableFuture.runAsync(() -> {
            try {
                URL url = java.net.URI.create(API_URL).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

                if (conn.getResponseCode() != 200) {
                    source.sendFeedback(() -> Text.literal("No releases found or API limit reached.").formatted(Formatting.RED), false);
                    return;
                }

                JsonObject response = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
                String latestVersionStr = response.get("tag_name").getAsString().replace("v", "");
                
                Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(Shieldsync.MOD_ID);
                if (modContainer.isEmpty()) return;
                
                String currentVersionStr = modContainer.get().getMetadata().getVersion().getFriendlyString();

                if (latestVersionStr.equals(currentVersionStr)) {
                    source.sendFeedback(() -> Text.literal("ShieldSync is up to date! (v" + currentVersionStr + ")").formatted(Formatting.GREEN), false);
                    return;
                }

                source.sendFeedback(() -> Text.literal("Found newer version v" + latestVersionStr + " (Current is v" + currentVersionStr + "). Downloading...").formatted(Formatting.AQUA), false);

                JsonArray assets = response.getAsJsonArray("assets");
                if (assets.size() == 0) {
                    source.sendFeedback(() -> Text.literal("No jar file found in the latest release assets.").formatted(Formatting.RED), false);
                    return;
                }

                String downloadUrl = assets.get(0).getAsJsonObject().get("browser_download_url").getAsString();
                String fileName = assets.get(0).getAsJsonObject().get("name").getAsString();
                
                downloadAndInstall(downloadUrl, fileName, source);

            } catch (Exception e) {
                Shieldsync.LOGGER.error("Update check failed", e);
                source.sendFeedback(() -> Text.literal("An error occurred during the update check.").formatted(Formatting.RED), false);
            }
        });
    }

    private static void downloadAndInstall(String downloadUrl, String fileName, ServerCommandSource source) {
        try {
            URL url = java.net.URI.create(downloadUrl).toURL();
            Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
            Path targetFile = modsDir.resolve(fileName);

            try (InputStream in = url.openStream()) {
                Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }

            source.sendFeedback(() -> Text.literal("Successfully downloaded " + fileName + "!").formatted(Formatting.GREEN), false);
            source.sendFeedback(() -> Text.literal("Reloading ShieldSync configuration... (Note: Full JAR hot-swap requires a server restart)").formatted(Formatting.YELLOW), false);
            
            // Reload the config as a mock "reload" per user request since JVM hot reloading of Mixins isn't natively supported.
            resolutestudios.shieldsync.config.ConfigManager.load();
            
            source.sendFeedback(() -> Text.literal("ShieldSync reloaded! Server restart required to apply new Mixins.").formatted(Formatting.GREEN), false);
            
            // Also attempt to delete the old jar (a bit tricky as it's locked by JVM, but we can try)
            deleteOldJars(fileName);
            
        } catch (Exception e) {
            Shieldsync.LOGGER.error("Failed to download update", e);
            source.sendFeedback(() -> Text.literal("Failed to download or apply the update.").formatted(Formatting.RED), false);
        }
    }
    
    private static void deleteOldJars(String newFileName) {
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
        try {
            Files.list(modsDir).forEach(path -> {
                String name = path.getFileName().toString();
                if (name.startsWith("shieldsync") && name.endsWith(".jar") && !name.equals(newFileName)) {
                    try {
                        Files.delete(path); // Might fail on Windows due to FileLock
                    } catch (Exception ignored) {}
                }
            });
        } catch (Exception e) {
            Shieldsync.LOGGER.error("Failed to list mods directory", e);
        }
    }
}
