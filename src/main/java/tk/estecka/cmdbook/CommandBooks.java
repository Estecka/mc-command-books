package tk.estecka.cmdbook;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import net.minecraft.util.StringHelper;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.World;
import tk.estecka.cmdbook.config.Config;
import tk.estecka.cmdbook.config.ConfigIO;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandBooks implements ModInitializer 
{
	public static final Logger LOGGER = LoggerFactory.getLogger("CmdBooks");

	static private final ConfigIO io = new ConfigIO("cmdbook.properties");
	static public final Config config = new Config();

	@Override
	public void onInitialize() {
		try {
			io.GetOrCreate(config);
		}
		catch(IOException e){
			LOGGER.error("{}", e);
		}

		UseItemCallback.EVENT.register(CommandBooks::OnItemUse);
	}

	static private	TypedActionResult<ItemStack>	OnItemUse(PlayerEntity player, World world, Hand hand){
		Optional<Item> wand = Registries.ITEM.getOrEmpty(config.wandItem);

		if((wand.isPresent())
		&& (player.hasPermissionLevel(config.permissionLevel))
		&& (player.getMainHandStack().isOf(wand.get()))
		&& (player.getOffHandStack().isOf(Items.WRITABLE_BOOK))
		){
			NbtCompound nbt = player.getOffHandStack().getNbt();
			if (nbt != null && !world.isClient()){
				var pages = nbt.getList("pages", NbtList.STRING_TYPE);
				for (NbtElement p : pages) {
					p.asString().lines().forEach((line)->{
						// LOGGER.warn("Run: {}", line);
						execute(player, line);
					});
				}
			}
			return TypedActionResult.success(ItemStack.EMPTY);
		}
		else
			return TypedActionResult.pass(ItemStack.EMPTY);
	}

	static private	boolean execute(PlayerEntity player, String command) {
		MinecraftServer server = player.getServer();
		if (server.areCommandBlocksEnabled() && !StringHelper.isEmpty(command))
		try
		{
			server.getCommandManager().executeWithPrefix(player.getCommandSource(), command);
		}
		catch(Throwable err)
		{
			LOGGER.error("Command Book threw an exception:\n", err);
			CrashReport report = CrashReport.create(err, "Executing Command Book");
			CrashReportSection section = report.addElement("Command to be executed");
			section.add("Command", command);
			section.add("Name", player.getName().getString());
			throw new CrashException(report);
		}

		return true;
	}
}
