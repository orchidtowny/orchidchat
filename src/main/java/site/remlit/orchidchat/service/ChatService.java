package site.remlit.orchidchat.service;

import com.mojang.logging.LogUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import site.remlit.orchidchat.model.config.DeterminedChannel;

import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class ChatService {

	public @NotNull LuckPermsService luckPermsService;
	public @NotNull ChannelService channelService;

	public ChatService(
			@NotNull LuckPermsService luckPermsService,
			@NotNull ChannelService channelService
	) {
		this.luckPermsService = luckPermsService;
		this.channelService = channelService;
	}


	private static final @NotNull Logger LOGGER = LogUtils.getLogger();

	private static final @NotNull MiniMessage MM = MiniMessage.miniMessage();
	private static final @NotNull JSONComponentSerializer JCS = GsonComponentSerializer.gson();
	private static final @NotNull Pattern LUCKPERMS_META_PATTERN = Pattern.compile("%luckperms_meta_([a-zA-Z0-9]*)%");


	/**
	 * Register events and other essentials for this service
	 * */
	@ApiStatus.Internal
	public void register() {
		MinecraftForge.EVENT_BUS.register(this);
	}


	/**
	 * Send a message to a channel
	 *
	 * @param channel Channel to send to
	 * @param sender Message sender
	 * @param players List of players on the server,
	 *                or at least the targets of this message
	 * @param formatted Formatted message
	 * */
	public void sendMessage(
			@NotNull String channel,
			@NotNull Player sender,
			@NotNull List<Player> players,
			@NotNull String formatted
	) {
		if (!channelService.canPlayerSee(channel, sender)) return;

		boolean skipExpensiveLookup = channelService.channels.get(channel) == null;

		Component miniMessage = MM.deserialize(formatted.trim());
		String rawJson = JCS.serialize(miniMessage);
		net.minecraft.network.chat.Component finalMessage = net.minecraft.network.chat.Component.Serializer
				.fromJson(rawJson);

		if (Objects.isNull(finalMessage)) return;

		LOGGER.info("[{}] {}", channel, finalMessage.getString());

		for (Player player : players) {
			if (!skipExpensiveLookup && !channelService.canPlayerSee(channel, player))
				continue;

			player.sendSystemMessage(finalMessage);
		}
	}


	@SubscribeEvent
	public void onServerChatEvent(ServerChatEvent event) {
		try {
			Player player = event.getPlayer();
			String name = player.getDisplayName().getString();
			String message = event.getRawText();

			DeterminedChannel determinedChannel = channelService.determineChannel(player, message);
			String formatted = channelService.getFormat(determinedChannel.channel);

			if (!Objects.isNull(determinedChannel.usedChannelShortcut))
				message = message.replaceFirst(determinedChannel.usedChannelShortcut, "");

			formatted = formatted
					.replace("%name%", name)
					.replace("%msg%", message);


			if (luckPermsService.enabled && !Objects.isNull(luckPermsService.api)) {
				User lpUser = luckPermsService.api.getUserManager()
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

			this.sendMessage(
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
