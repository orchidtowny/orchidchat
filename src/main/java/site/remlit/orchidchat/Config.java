package site.remlit.orchidchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import site.remlit.orchidchat.model.config.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(
		modid = OrchidChat.MODID,
		bus = Mod.EventBusSubscriber.Bus.MOD
)
public final class Config {
	// jank config setup because the default forge one wasn't what i wanted.

	private static final @NotNull Logger LOGGER = LogUtils.getLogger();


	public static Map<String, String> formats;
	public static Map<String, String> channels;
	public static Map<String, List<String>> channelShortcuts;


	private static final @NotNull Path configPath = Path.of("config/orchidchat.json");
	private static final @NotNull Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static @NotNull Configuration cfg = new Configuration();


	@ApiStatus.Internal
	public static void writeMemoryConfig() {
		try {
			Files.write(configPath, gson.toJson(cfg).getBytes());
		} catch (IOException e) {
			LOGGER.error("Writing config from memory failed. {}", e.getMessage());
		}
	}

	@ApiStatus.Internal
	public static void readFileConfig() {
		try {
			String string = Files.readString(configPath);
			cfg = gson.fromJson(string, Configuration.class);
		} catch (IOException e) {
			LOGGER.error("Reading config from file failed. {}", e.getMessage());
		}
	}


	public static void loadConfig() {
		try {
			LOGGER.info("Loading configuration at {}", configPath.toAbsolutePath());

			if (!configPath.toFile().exists()) {
				Files.createFile(configPath);
				writeMemoryConfig();
			}

			readFileConfig();
		} catch (IOException e) {
			LOGGER.error("Failed to load config: {}", e.getMessage());
		}

		formats = cfg.formats;
		channels = cfg.channels;
		channelShortcuts = cfg.channelShortcuts;
	}


	@SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
		loadConfig();
	}

}
