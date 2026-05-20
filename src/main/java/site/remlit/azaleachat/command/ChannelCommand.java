package site.remlit.azaleachat.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;
import site.remlit.azaleachat.service.ChannelService;

import java.util.Objects;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static site.remlit.azaleachat.service.ComponentService.*;

public final class ChannelCommand {
	public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("channel")
				.then(Commands.argument("name", word())
						.suggests((context, builder) -> {
							for (String channel : ChannelService.channels.keySet())
								builder.suggest(channel);
							return builder.buildFuture();
						})
						.executes(ChannelCommand::execute));

		dispatcher.register(command);
	}

	public static int execute(@NotNull CommandContext<CommandSourceStack> command) {
		if (!command.getSource().isPlayer()) {
			command.getSource().sendSystemMessage(
					c2mc(mm("<red>Only players can use this command"))
			);
			return 0;
		}

		String name = command.getArgument("name", String.class);

		if (Objects.isNull(name)) {
			command.getSource().sendSystemMessage(
					c2mc(mm("<red>Channel name required"))
			);
			return 0;
		}

		try {
			ChannelService.setPlayerChannel(
					Objects.requireNonNull(
							command.getSource().getPlayer()
					),
					name
			);

			command.getSource().sendSystemMessage(
					c2mc(mm("<dark_green>Switched to channel "+name))
			);
		} catch (IllegalArgumentException e) {
			command.getSource().sendSystemMessage(
					c2mc(mm("<red>"+e.getMessage()))
			);
			return 0;
		}

		return Command.SINGLE_SUCCESS;
	}
}
