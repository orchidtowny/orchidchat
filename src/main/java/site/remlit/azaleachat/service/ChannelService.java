package site.remlit.azaleachat.service;

import com.mojang.logging.LogUtils;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import site.remlit.azaleachat.Config;
import site.remlit.azaleachat.model.DeterminedChannel;

import java.util.*;

public final class ChannelService {

	public LuckPermsService luckPermsService;

	public ChannelService(LuckPermsService luckPermsService) {
		this.luckPermsService = luckPermsService;
	}


	private static final @NotNull Logger LOGGER = LogUtils.getLogger();

	public static HashMap<@NotNull String, @Nullable PermissionNode> channels = new HashMap<>();
	public static HashMap<@NotNull String, @NotNull List<String>> channelShortcuts = new HashMap<>();

	public static HashMap<@NotNull UUID, @NotNull String> playerCurrentChannel = new HashMap<>();


	/**
	 * Register a new chat channel and the permission required (if any) to see and chat in it.
	 *
	 * @param name Name of channel
	 * @param perm Permission required for channel (optional)
	 *
	 * @throws IllegalArgumentException When a channel is already registered by that name
	 * */
	public static void registerChannel(
			@NotNull String name,
			@Nullable PermissionNode perm
	) throws IllegalArgumentException {
		if (channels.containsKey(name))
			throw new IllegalArgumentException("This channel already is registered!");

		LOGGER.info("Created channel {}, permission: {}", name, (Objects.isNull(perm) ? "none" : perm.getPermission()));
		channels.put(name, perm);
	}

	/**
	 * Set a persistent channel for a player to use.
	 *
	 * @param player Player to set channel for
	 * @param channel Chanel to set
	 * */
	public static void setPlayerChannel(
			@NotNull Player player,
			@NotNull String channel
	) {
		if (!channels.containsKey(channel))
			throw new IllegalArgumentException("This channel doesn't exist!");

		playerCurrentChannel.put(player.getUUID(), channel);
	}

	/**
	 * Determine if a player can see a channel by permissions
	 *
	 * @param channel Channel being checked
	 * @param player Player viewing channel
	 *
	 * @return If that player can view that channel
	 * */
	public static boolean canPlayerSee(
			@NotNull String channel,
			@NotNull Player player
	) {
		PermissionNode requiredPerm = channels.getOrDefault(channel, null);

		// No permission required if set null
		if (Objects.isNull(requiredPerm)) return true;

		if (!LuckPermsService.enabled || Objects.isNull(LuckPermsService.api))
			return false;

		User user = LuckPermsService.api.getUserManager().getUser(player.getUUID());
		if (Objects.isNull(user)) return false;

		return user.getCachedData().getPermissionData()
				.checkPermission(requiredPerm.getPermission())
				.asBoolean();
	}

	/**
	 * Determine what channel a chat message should go in.
	 *
	 * @param sender Sender of the message
	 * @param rawMessage Raw message contents
	 *
	 * @return Name of channel
	 * */
	public static @NotNull DeterminedChannel determineChannel(
			@NotNull Player sender,
			@NotNull String rawMessage
	) {
		String usedShortcut = null;
		String channel = "global";

		String overrideChannel = playerCurrentChannel.get(sender.getUUID());
		if (!Objects.isNull(overrideChannel))
			channel = overrideChannel;

		// TODO: allow setting channel to persist, then check sender against list.

		for (String c : channelShortcuts.keySet()) {
			List<String> shortcuts = channelShortcuts.get(c);

			for (String shortcut : shortcuts) {
				if (rawMessage.startsWith(shortcut)) {
					channel = c;
					usedShortcut = shortcut;
					break;
				}
			}
		}

		return new DeterminedChannel(
				channel,
				usedShortcut
		);
	}

	/**
	 * Get the chat format for a channel.
	 *
	 * @param channel Channel to get the format of
	 *
	 * @return Channel format, default if not found
	 * */
	public static @Nullable String getFormat(
			@NotNull String channel
	) {
		String specifiedChannelFormat = Config.formats.get(channel);

		if (!Objects.isNull(specifiedChannelFormat))
			return specifiedChannelFormat;

		return Config.formats.get("global");
	}


	@ApiStatus.Internal
	public static void setupChannels() {
		for (String channel : Config.channels.keySet()) {
			String permission = Config.channels.get(channel);

			@Nullable PermissionNode node = null;
			if (!permission.isBlank()) node = PermissionNode.builder()
					.permission(permission)
					.build();

			try {
				registerChannel(channel, node);
			} catch (IllegalArgumentException e) {
				LOGGER.error("There was a problem registering channel {}: {}", channel, e.getMessage());
			}
		}

		for (String channel : Config.channelShortcuts.keySet()) {
			if (!channels.containsKey(channel)) {
				LOGGER.warn("Channel shortcut for channel {} configured but channel doesn't exist, ignoring", channel);
				continue;
			}

			channelShortcuts.put(channel, Config.channelShortcuts.get(channel));
		}

		if (!channels.containsKey("global"))
			registerChannel("global", null);
	}

	@ApiStatus.Internal
	public static void clearChannels() {
		channels.clear();
		channelShortcuts.clear();
	}

}
