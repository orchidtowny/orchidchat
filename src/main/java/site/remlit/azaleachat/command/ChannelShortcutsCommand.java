package site.remlit.azaleachat.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;
import site.remlit.azaleachat.service.ChannelService;

import java.util.ArrayList;
import java.util.Objects;

import static site.remlit.azaleachat.service.ComponentService.*;

public final class ChannelShortcutsCommand {
	public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("channelShortcuts")
				.executes(ChannelShortcutsCommand::execute));
	}

	public static int execute(@NotNull CommandContext<CommandSourceStack> command) {
		if (!command.getSource().isPlayer()) {
			command.getSource().sendSystemMessage(
					c2mc(mm("<red>Only players can use this command"))
			);
			return 0;
		}

		ArrayList<String> channels = new ArrayList<>(ChannelService.channels.keySet());
		channels.removeIf(channel ->
				!ChannelService.canPlayerSee(channel, Objects.requireNonNull(command.getSource().getPlayer()))
		);

		command.getSource().sendSystemMessage(
				c2mc(mm("<dark_green>There are " + channels.size() + " available channel(s):"))
		);

		for (String channel : channels) {
			String shortcuts = String.join(" ", ChannelService.channelShortcuts.get(channel));
			command.getSource().sendSystemMessage(
					c2mc(mm("<green> " + channel + "<gray> " + shortcuts))
			);
		}

		return Command.SINGLE_SUCCESS;
	}
}
