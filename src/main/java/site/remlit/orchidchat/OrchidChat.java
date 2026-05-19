package site.remlit.orchidchat;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import site.remlit.orchidchat.service.ChatService;
import site.remlit.orchidchat.service.LuckPermsService;

import java.util.Objects;

@Mod(OrchidChat.MODID)
public final class OrchidChat {

	public static final @NotNull String MODID = "orchidchat";
	private static final @NotNull Logger LOGGER = LogUtils.getLogger();


	private @Nullable LuckPermsService luckPermsService;
	private @Nullable ChatService chatService;


	public OrchidChat() {
		MinecraftForge.EVENT_BUS.register(this);

		// This warning can be ignored, it's for a future version of Forge, not this one
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
	}


	@SubscribeEvent
	public void onServerStart(@Nullable ServerStartedEvent event) {
		if (Objects.isNull(event)) return;

		try {
			luckPermsService = new LuckPermsService();
			luckPermsService.register();
		} catch (NoClassDefFoundError e) {
			LOGGER.warn("LuckPerms not found, related features will be disabled.");
		}

		chatService = new ChatService(luckPermsService);
		chatService.register();

		LOGGER.info("Finished startup!");
	}

}
