package site.remlit.azaleachat.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ComponentService {

	private static final @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();
	private static final @NotNull JSONComponentSerializer jsonComponentSerializer = GsonComponentSerializer.gson();


	/**
	 * Convert raw text to Adventure Component using MiniMessage.
	 *
	 * @param rawText Raw text to convert
	 *
	 * @return Adventure Component using MiniMessage
	 * */
	public static @NotNull Component mm(
			@NotNull String rawText
	) {
		return miniMessage.deserialize(rawText);
	}

	/**
	 * Create an Adventure Component to a Minecraft Component.
	 *
	 * @param component Adventure Component
	 *
	 * @return Minecraft Component
	 * */
	public static @NotNull net.minecraft.network.chat.Component c2mc(
			@NotNull Component component
	) {
		String rawJson = jsonComponentSerializer.serialize(component);
		return Objects.requireNonNull(
				net.minecraft.network.chat.Component.Serializer.fromJson(rawJson)
		);
	}

	/**
	 * Create a Minecraft Component to an Adventure Component.
	 *
	 * @param component Minecraft Component
	 *
	 * @return Adventure Component
	 * */
	public static @NotNull Component c2adv(
			@NotNull net.minecraft.network.chat.Component component
	) {
		String rawJson = net.minecraft.network.chat.Component.Serializer.toJson(component);
		return jsonComponentSerializer.deserialize(rawJson);
	}

}
