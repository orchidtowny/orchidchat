package site.remlit.azaleachat.service;

import com.mojang.logging.LogUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class LuckPermsService {

	public static boolean enabled = false;
	private static final @NotNull Logger LOGGER = LogUtils.getLogger();

	public static @Nullable LuckPerms api;


	public static void register() {
		api = LuckPermsProvider.get();
		enabled = true;

		LOGGER.info("LuckPerms hook loaded");
	}

}
