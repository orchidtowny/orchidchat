package site.remlit.orchidchat.service;

import com.mojang.logging.LogUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LuckPermsService {

	public boolean enabled = false;
	private static final @NotNull Logger LOGGER = LogUtils.getLogger();

	public @Nullable LuckPerms api;

	public void register() {
		api = LuckPermsProvider.get();
		enabled = true;

		LOGGER.info("LuckPerms hook loaded");
	}

}
