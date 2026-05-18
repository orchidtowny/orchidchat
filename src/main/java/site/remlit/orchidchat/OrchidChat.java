package site.remlit.orchidchat;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import site.remlit.orchidchat.service.ChatService;
import site.remlit.orchidchat.service.LuckpermsService;

@Mod(OrchidChat.MODID)
public class OrchidChat {

	public static final String MODID = "orchidchat";
	private static final Logger LOGGER = LogUtils.getLogger();

	private LuckpermsService luckpermsService;
	private ChatService chatService;

	public OrchidChat() {
		MinecraftForge.EVENT_BUS.register(this);

		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC);
	}

	@SubscribeEvent
	public void onServerStart(ServerStartingEvent event) {
		try {
			luckpermsService = new LuckpermsService();
			luckpermsService.register();
		} catch (NoClassDefFoundError e) {
			LOGGER.warn("Luckperms not found, related features will be disabled.");
		}

		chatService = new ChatService(luckpermsService);
		chatService.register();

		LOGGER.info("Finished startup!");
	}

}
