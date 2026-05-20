package site.remlit.azaleachat.model.config;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class Configuration {

	/**
	 * Chat formats. Map key is channel, map value is format for that channel.
	 * */
	public Map<@NotNull String, @NotNull String> formats = Map.of(
			"global", "<gray>%luckperms_prefix%%name%%luckperms_suffix% <dark_gray>» <white>%luckperms_meta_chatcolor%%msg%"
	);

	/**
	 * Channels. Map key is channel name, map value is permission required, blank if none.
	 * */
	public Map<@NotNull String, @NotNull String> channels = Map.of(
			"global", "",
			"staff", "azaleachat.channel.staff"
	);

	public Map<@NotNull String, @NotNull List<String>> channelShortcuts = Map.of(
			"global", List.of("g;", "gl;", "global;", "gen;", "general;"),
			"staff", List.of("s;", "st;", "staff;")
	);

}
