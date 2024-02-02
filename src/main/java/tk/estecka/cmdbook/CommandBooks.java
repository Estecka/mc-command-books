package tk.estecka.cmdbook;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.World;
import tk.estecka.cmdbook.config.Config;
import tk.estecka.cmdbook.config.ConfigIO;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandBooks implements ModInitializer 
{
	public static final Logger LOGGER = LoggerFactory.getLogger("CmdBooks");

	static public final ConfigIO io = new ConfigIO("cmdbook.properties");
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

	static private TypedActionResult<ItemStack>	OnItemUse(PlayerEntity player, World world, Hand hand){
		if((!player.getOffHandStack().isOf(Items.WRITABLE_BOOK)) || (!player.getMainHandStack().isOf(Items.STICK))){
			return TypedActionResult.pass(ItemStack.EMPTY);
		}
		else if(world.isClient()){
			return TypedActionResult.success(ItemStack.EMPTY);
		}
		else if((player.hasPermissionLevel(config.permissionLevel)) && (player.getServer().areCommandBlocksEnabled())){
			RunBook(player);
			return TypedActionResult.success(ItemStack.EMPTY);
		}
		else
			return TypedActionResult.pass(ItemStack.EMPTY);
	}

	static private void	RunBook(PlayerEntity player){
		NbtCompound nbt = player.getOffHandStack().getNbt();
		if (nbt != null){
			var pages = nbt.getList("pages", NbtList.STRING_TYPE);
			for (NbtElement p : pages) {
				p.asString().lines().forEach((line)->{
					// LOGGER.warn("Run: {}", line);
					RunCommand(player, line);
				});
			}
		}
	}

	static private void	RunCommand(PlayerEntity player, String command) {
		if (!StringUtils.isEmpty(command))
		try
		{
			player.getServer().getCommandManager().executeWithPrefix(player.getCommandSource(), command);
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
	}
}
