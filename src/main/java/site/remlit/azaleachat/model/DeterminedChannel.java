package site.remlit.azaleachat.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeterminedChannel {

	public @NotNull String channel;
	public @Nullable String usedChannelShortcut;

	public DeterminedChannel(
			@NotNull String channel,
			@Nullable String usedChannelShortcut
	) {
		this.channel = channel;
		this.usedChannelShortcut = usedChannelShortcut;
	}

}
