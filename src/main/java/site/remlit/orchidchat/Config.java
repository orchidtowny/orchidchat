package site.remlit.orchidchat;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(
		modid = OrchidChat.MODID,
		bus = Mod.EventBusSubscriber.Bus.MOD
)
public final class Config {

	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();


	private static final ForgeConfigSpec.ConfigValue<String> FORMAT = BUILDER
			.comment("""
					 Format to use for chat, MiniMessage allowed.
					
					 Placeholders:
					 %name% - Player's name
					 %msg% - Message
					
					 %luckperms_prefix% - LuckPerms prefix
					 %luckperms_suffix% - LuckPerms suffix
					 %luckperms_meta_<value>% - LuckPerms meta value
					""")
			.define(
					"format",
					"<gray>" +
							"<hover:show_text:\"<yellow>%luckperms_meta_rankDescription%\">%luckperms_prefix%</hover>" +
							"<click:run_command:\"msg %name%\">%name%</click>" +
							"%luckperms_suffix%" +
							" <dark_gray>» " +
							"<white>%luckperms_meta_chatcolor%%msg%"
			);


	static final ForgeConfigSpec SPEC = BUILDER.build();


	public static String format;


	@SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
		format = FORMAT.get();
	}

}
