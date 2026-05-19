package site.remlit.orchidchat;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import site.remlit.orchidchat.service.ChannelService;
import site.remlit.orchidchat.service.ChatService;
import site.remlit.orchidchat.service.LuckPermsService;

import java.util.Objects;

@Mod(OrchidChat.MODID)
public final class OrchidChat {

	public static final @NotNull String MODID = "orchidchat";
	private static final @NotNull Logger LOGGER = LogUtils.getLogger();


	private @Nullable LuckPermsService luckPermsService;
	private @Nullable ChannelService channelService;
	private @Nullable ChatService chatService;


	public OrchidChat() {
		MinecraftForge.EVENT_BUS.register(this);
	}


	@SubscribeEvent
	public void onServerStart(ServerStartedEvent event) {
		Config.loadConfig();

		try {
			luckPermsService = new LuckPermsService();
			luckPermsService.register();
		} catch (NoClassDefFoundError e) {
			LOGGER.warn("LuckPerms not found, related features will be disabled.");
		}

		channelService = new ChannelService(luckPermsService);
		channelService.setupChannels();
		channelService.register();

		chatService = new ChatService(luckPermsService, channelService);
		chatService.register();

		LOGGER.info("Finished startup!");
	}

	@SubscribeEvent
	public void onServerStopping(ServerStoppingEvent event) {
		LOGGER.info("Writing config before shutdown...");
		Config.writeMemoryConfig();
	}
}
