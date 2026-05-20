package site.remlit.azaleachat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import site.remlit.azaleachat.command.ChannelCommand;
import site.remlit.azaleachat.command.ChannelShortcutsCommand;
import site.remlit.azaleachat.service.ChannelService;
import site.remlit.azaleachat.service.LuckPermsService;
import site.remlit.azaleachat.subscriber.ModConfigEventSubscriber;
import site.remlit.azaleachat.subscriber.ServerChatEventSubscriber;

@Mod(AzaleaChat.MODID)
public final class AzaleaChat {

	public static final @NotNull String MODID = "azaleachat";
	private static final @NotNull Logger LOGGER = LogUtils.getLogger();


	public AzaleaChat() {
		MinecraftForge.EVENT_BUS.register(this);
	}


	@SubscribeEvent
	public void onServerStart(ServerStartedEvent event) {
		Config.loadConfig();

		try {
			LuckPermsService.register();
		} catch (NoClassDefFoundError e) {
			LOGGER.warn("LuckPerms not found, related features will be disabled.");
		}

		ChannelService.setupChannels();

		ModConfigEventSubscriber.register();
		ServerChatEventSubscriber.register();

		LOGGER.info("Finished startup!");
	}

	@SubscribeEvent
	public void onRegisterCommands(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

		ChannelCommand.register(dispatcher);
		ChannelShortcutsCommand.register(dispatcher);

		LOGGER.info("Registered commands");
	}

	@SubscribeEvent
	public void onServerStopping(ServerStoppingEvent event) {
		LOGGER.info("Writing config before shutdown...");
		Config.loadConfig();
		Config.writeMemoryConfig();
	}
}
