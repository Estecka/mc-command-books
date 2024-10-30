package tk.estecka.cmdbook;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
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

	static private ActionResult	OnItemUse(PlayerEntity player, World world, Hand hand){
		if((!player.getOffHandStack().isOf(Items.WRITABLE_BOOK)) || (!player.getMainHandStack().isOf(Items.STICK))){
			return ActionResult.PASS;
		}
		else if(world.isClient()){
			return ActionResult.SUCCESS;
		}
		else if((player.hasPermissionLevel(config.permissionLevel)) && (player.getServer().areCommandBlocksEnabled())){
			RunBook((ServerPlayerEntity)player);
			return ActionResult.SUCCESS;
		}
		else
			return ActionResult.PASS;
	}

	static private void	RunBook(ServerPlayerEntity player){
		WritableBookContentComponent component = player.getOffHandStack().get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
		if (component != null){
			for (RawFilteredPair<String> p : component.pages()) {
				p.raw().lines().forEach((line)->{
					RunCommand(player, line);
				});
			}
		}
	}

	static private void	RunCommand(ServerPlayerEntity player, String command) {
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
