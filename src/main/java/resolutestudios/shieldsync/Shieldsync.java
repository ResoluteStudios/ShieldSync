package resolutestudios.shieldsync;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resolutestudios.shieldsync.command.ShieldSyncCommand;
import resolutestudios.shieldsync.config.ConfigManager;

public class Shieldsync implements ModInitializer {
	public static final String MOD_ID = "shieldsync";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ConfigManager.load();
		
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			ShieldSyncCommand.register(dispatcher);
		});

		LOGGER.info("ShieldSync initialized successfully!");
	}
}