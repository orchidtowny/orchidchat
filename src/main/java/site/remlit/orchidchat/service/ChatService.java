package site.remlit.orchidchat.service;

import com.mojang.logging.LogUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import site.remlit.orchidchat.Config;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class ChatService {

	public @Nullable LuckPermsService luckPermsService;

	public ChatService(@Nullable LuckPermsService luckPermsService) {
		this.luckPermsService = luckPermsService;
	}


	private static final @NotNull Logger LOGGER = LogUtils.getLogger();

	private static final @NotNull MiniMessage MM = MiniMessage.miniMessage();
	private static final @NotNull JSONComponentSerializer JCS = GsonComponentSerializer.gson();
	private static final @NotNull Pattern LUCKPERMS_META_PATTERN = Pattern.compile("%luckperms_meta_([a-zA-Z0-9]*)%");


	public void register() {
		MinecraftForge.EVENT_BUS.register(this);
	}


	@SubscribeEvent
	public void onServerChatEvent(@Nullable ServerChatEvent event) {
		if (Objects.isNull(event)) return;

		try {
			String formatted = Config.format;

			String name = event.getPlayer().getDisplayName().getString();
			String message = event.getRawText();


			formatted = formatted
					.replace("%name%", name)
					.replace("%msg%", message);


			if (
					!Objects.isNull(luckPermsService) &&
					luckPermsService.enabled &&
					!Objects.isNull(luckPermsService.api)
			) {
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


			// Conversion
			Component miniMessage = MM.deserialize(formatted);
			String rawJson = JCS.serialize(miniMessage);
			net.minecraft.network.chat.Component finalMessage = net.minecraft.network.chat.Component.Serializer
					.fromJson(rawJson);


			event.setCanceled(true);

			if (Objects.isNull(finalMessage)) return;

			MinecraftServer server = event.getPlayer().getServer();
			if (Objects.isNull(server)) return;


			// Sending
			LOGGER.info(finalMessage.getString());

			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.sendSystemMessage(finalMessage);
			}
		} catch (Throwable e) {
			LOGGER.error("Failed to modify chat! " + e.getLocalizedMessage(), e);
		}
	}

}
