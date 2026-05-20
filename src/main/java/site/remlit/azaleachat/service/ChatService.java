package site.remlit.azaleachat.service;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

public final class ChatService {

	private static final @NotNull Logger LOGGER = LogUtils.getLogger();


	/**
	 * Send a message to a channel
	 *
	 * @param channel Channel to send to
	 * @param sender Message sender
	 * @param players List of players on the server,
	 *                or at least the targets of this message
	 * @param formatted Formatted message
	 * */
	public static void sendMessage(
			@NotNull String channel,
			@NotNull Player sender,
			@NotNull List<Player> players,
			@NotNull String formatted
	) {
		if (!ChannelService.canPlayerSee(channel, sender))
			return;

		boolean skipExpensiveLookup = ChannelService.channels.get(channel) == null;

		Component finalMessage = ComponentService.c2mc(
				ComponentService.mm(formatted.trim())
		);

		LOGGER.info("[{}] {}", channel, finalMessage.getString());

		for (Player player : players) {
			if (!skipExpensiveLookup && !ChannelService.canPlayerSee(channel, player))
				continue;

			player.sendSystemMessage(finalMessage);
		}
	}

}
