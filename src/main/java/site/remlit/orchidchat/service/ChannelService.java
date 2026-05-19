package site.remlit.orchidchat.service;

import com.mojang.logging.LogUtils;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import site.remlit.orchidchat.Config;
import site.remlit.orchidchat.model.config.DeterminedChannel;

import java.util.*;

public class ChannelService {

	public LuckPermsService luckPermsService;

	public ChannelService(LuckPermsService luckPermsService) {
		this.luckPermsService = luckPermsService;
	}


	private static final @NotNull Logger LOGGER = LogUtils.getLogger();

	public HashMap<@NotNull String, @Nullable PermissionNode> channels = new HashMap<>();
	public HashMap<@NotNull String, @NotNull List<String>> channelShortcuts = new HashMap<>();


	/**
	 * Register events and other essentials for this service
	 * */
	@ApiStatus.Internal
	public void register() {
		MinecraftForge.EVENT_BUS.register(this);
	}


	/**
	 * Register a new chat channel and the permission required (if any) to see and chat in it.
	 *
	 * @param name Name of channel
	 * @param perm Permission required for channel (optional)
	 *
	 * @throws IllegalArgumentException When a channel is already registered by that name
	 * */
	public void registerChannel(
			@NotNull String name,
			@Nullable PermissionNode perm
	) throws IllegalArgumentException {
		if (channels.containsKey(name))
			throw new IllegalArgumentException("This channel already is registered!");

		LOGGER.info("Created channel {}, permission: {}", name, (Objects.isNull(perm) ? "none" : perm.getPermission()));
		channels.put(name, perm);
	}

	/**
	 * Determine if a player can see a channel by permissions
	 *
	 * @param channel Channel being checked
	 * @param player Player viewing channel
	 *
	 * @return If that player can view that channel
	 * */
	public boolean canPlayerSee(
			@NotNull String channel,
			@NotNull Player player
	) {
		PermissionNode requiredPerm = channels.getOrDefault(channel, null);

		// No permission required if set null
		if (Objects.isNull(requiredPerm)) return true;

		if (!luckPermsService.enabled || Objects.isNull(luckPermsService.api))
			return false;

		User user = luckPermsService.api.getUserManager().getUser(player.getUUID());
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
	public @NotNull DeterminedChannel determineChannel(
			@NotNull Player sender,
			@NotNull String rawMessage
	) {
		String usedShortcut = null;
		String channel = "global";

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
	public @Nullable String getFormat(
			@NotNull String channel
	) {
		String specifiedChannelFormat = Config.formats.get(channel);

		if (!Objects.isNull(specifiedChannelFormat))
			return specifiedChannelFormat;

		return Config.formats.get("global");
	}


	@ApiStatus.Internal
	public void setupChannels() {
		for (String channel : Config.channels.keySet()) {
			String permission = Config.channels.get(channel);

			@Nullable PermissionNode node = null;
			if (!permission.isBlank()) node = PermissionNode.builder()
					.permission(permission)
					.build();

			try {
				this.registerChannel(channel, node);
			} catch (IllegalArgumentException e) {
				LOGGER.error("There was a problem registering channel {}: {}", channel, e.getMessage());
			}
		}

		for (String channel : Config.channelShortcuts.keySet()) {
			if (!channels.containsKey(channel)) {
				LOGGER.warn("Channel shortcut for channel {} configured but channel doesn't exist, ignoring", channel);
				continue;
			}

			this.channelShortcuts.put(channel, Config.channelShortcuts.get(channel));
		}

		if (!channels.containsKey("global"))
			this.registerChannel("global", null);
	}

	@ApiStatus.Internal
	public void clearChannels() {
		channels.clear();
		channelShortcuts.clear();
	}


	// Reload channel configurations whenever the mod config changes
	@SubscribeEvent
	public void onLoad(final ModConfigEvent event) {
		this.clearChannels();
		this.setupChannels();
	}

}
