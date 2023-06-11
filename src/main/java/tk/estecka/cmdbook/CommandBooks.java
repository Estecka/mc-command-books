package tk.estecka.cmdbook;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringHelper;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandBooks implements ModInitializer 
{
	public static final Logger LOGGER = LoggerFactory.getLogger("CmdBooks");

	@Override
	public void onInitialize() {
		UseItemCallback.EVENT.register((player, world, hand)->{
			if((player.getMainHandStack().isOf(Items.STICK))
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
		});
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
