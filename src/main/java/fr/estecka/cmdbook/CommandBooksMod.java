package fr.estecka.cmdbook;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;
import fr.estecka.cmdbook.config.Config;
import fr.estecka.cmdbook.config.ConfigIO;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandBooksMod
implements ModInitializer
{
	static public final String MOD_ID = "cmdbook";
	static public final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	static public final ConfigIO io = new ConfigIO(MOD_ID+".properties");
	static public final Config config = new Config();

	@Override
	public void onInitialize() {
		try {
			io.GetOrCreate(config);
		}
		catch(IOException e){
			LOGGER.error("{}", e);
		}

		UseItemCallback.EVENT.register(CommandBooksMod::OnItemUse);
		UseEntityCallback.EVENT.register(CommandBooksMod::OnEntityUse);
		UseBlockCallback.EVENT.register(CommandBooksMod::OnBlockUse);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(CommandBooksMod::CreativeInventory);
	}

	static private void CreativeInventory(FabricItemGroupEntries entries){
		final ItemStack book = new ItemStack(Items.WRITABLE_BOOK);
		final NbtCompound custom_data = new NbtCompound();

		custom_data.putBoolean(MOD_ID, true);

		book.set(DataComponentTypes.ITEM_NAME, Text.translatable("item.cmdbook.command_book"));
		book.set(DataComponentTypes.RARITY, Rarity.EPIC);
		book.set(DataComponentTypes.ITEM_MODEL, Identifier.of(MOD_ID, "command_book"));
		book.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(custom_data));

		entries.addBefore(Items.COMMAND_BLOCK_MINECART, book);
	}

	static private ActionResult OnBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult){
		return OnItemUse(player, world, hand);
	}


	static private ActionResult OnEntityUse(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult){
		return OnItemUse(player, world, hand);
	}

	static private ActionResult	OnItemUse(PlayerEntity player, World world, Hand hand){
		final MinecraftServer server = player.getEntityWorld().getServer();

		final ItemStack book = player.getOffHandStack();
		if((!book.isOf(Items.WRITABLE_BOOK))
		|| (!player.getMainHandStack().isOf(Items.DEBUG_STICK))
		|| (!book.contains(DataComponentTypes.CUSTOM_DATA))
		|| (!book.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean(MOD_ID, false))
		){
			return ActionResult.PASS;
		}
		else if(world.isClient()){
			return ActionResult.SUCCESS;
		}
		else if(server.getPermissionLevel(player.getPlayerConfigEntry()).getLevel().isAtLeast(PermissionLevel.fromLevel(config.permissionLevel))
		    && ((ServerWorld)player.getEntityWorld()).getGameRules().getValue(GameRules.COMMAND_BLOCKS_WORK)
		){
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
			player.getEntityWorld().getServer().getCommandManager().parseAndExecute(player.getCommandSource(), command);
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
