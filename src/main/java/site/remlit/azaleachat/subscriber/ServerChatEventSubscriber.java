package site.remlit.azaleachat.subscriber;

import com.mojang.logging.LogUtils;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import site.remlit.azaleachat.model.DeterminedChannel;
import site.remlit.azaleachat.service.ChannelService;
import site.remlit.azaleachat.service.ChatService;
import site.remlit.azaleachat.service.LuckPermsService;

import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public final class ServerChatEventSubscriber {
	public static void register() {
		MinecraftForge.EVENT_BUS.register(new ServerChatEventSubscriber());
	}

	private static final @NotNull Logger LOGGER = LogUtils.getLogger();
	private static final @NotNull Pattern LUCKPERMS_META_PATTERN = Pattern.compile("%luckperms_meta_([a-zA-Z0-9]*)%");

	@SubscribeEvent
	public void onEvent(@NonNull ServerChatEvent event) {
		try {
			Player player = event.getPlayer();
			String name = player.getDisplayName().getString();
			String message = event.getRawText();

			DeterminedChannel determinedChannel = ChannelService.determineChannel(player, message);
			String formatted = ChannelService.getFormat(determinedChannel.channel);

			if (!Objects.isNull(determinedChannel.usedChannelShortcut))
				message = message.replaceFirst(determinedChannel.usedChannelShortcut, "");

			formatted = formatted
					.replace("%name%", name)
					.replace("%msg%", message);


			if (LuckPermsService.enabled && !Objects.isNull(LuckPermsService.api)) {
				User lpUser = LuckPermsService.api.getUserManager()
						.getUser(event.getPlayer().getUUID());
				if (Objects.isNull(lpUser)) return;

				CachedMetaData cachedMetaData = lpUser.getCachedData().getMetaData();

				// Prefix and Suffix
				String prefix = cachedMetaData.getPrefix();
				if (Objects.isNull(prefix)) prefix = "";

				String suffix = cachedMetaData.getSuffix();
				if (Objects.isNull(suffix)) suffix = "";

				formatted = formatted
						.replace("%luckperms_prefix%", prefix)
						.replace("%luckperms_suffix%", suffix);

				// Meta
				List<String> metaMatches = LUCKPERMS_META_PATTERN.matcher(formatted)
						.results()
						.map(MatchResult::group)
						.toList();

				for (String metaMatch : metaMatches) {
					String meta = metaMatch
							.replace("%luckperms_meta_", "")
							.replace("%", "");

					String metaValue = cachedMetaData.getMetaValue(meta);
					if (Objects.isNull(metaValue)) metaValue = "";

					formatted = formatted.replace(metaMatch, metaValue);
				}
			}

			MinecraftServer server = event.getPlayer().getServer();
			if (Objects.isNull(server)) return;

			event.setCanceled(true);

			ChatService.sendMessage(
					determinedChannel.channel,
					player,
					server.getPlayerList().getPlayers().stream()
							.map(it -> (Player)it).toList(),
					formatted
			);
		} catch (Throwable e) {
			LOGGER.error("Failed to modify chat! " + e.getLocalizedMessage(), e);
		}
	}
}
