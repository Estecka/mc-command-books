package tk.estecka.cmdbook.config;

import java.io.IOException;
import java.util.stream.Collectors;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import tk.estecka.cmdbook.CommandBooks;

public class ModMenu
implements ModMenuApi
{
	static private final Config DEFAULTS = new Config();

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory(){
		return this::CreateScreen;
	}

	private Screen	CreateScreen(Screen parent){
		final var CONFIG = CommandBooks.config;
		final var builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.literal("Command Books"));
		final var entries = builder.entryBuilder();

		var MISC = builder.getOrCreateCategory(Text.literal("Misc"));

		MISC.addEntry(
			entries.startDropdownMenu(Text.literal("Wand Item"),
				DropdownMenuBuilder.TopCellElementBuilder.ofItemObject(CONFIG.GetWandItem().orElse(Items.AIR)),
				DropdownMenuBuilder.CellCreatorBuilder.ofItemObject()
			)
			.setDefaultValue(DEFAULTS.GetWandItem().orElse(Items.AIR))
			.setSelections(Registries.ITEM.stream().collect(Collectors.toSet()))
			.setSaveConsumer(item -> CONFIG.wandItemId=Registries.ITEM.getId(item))
			.build()
		);

		builder.setSavingRunnable(()->{
			try{
				CommandBooks.io.Write(CONFIG);
			}
			catch (IOException e){
				CommandBooks.LOGGER.error("Unable to save config: {}", e);
			}
		});

		return builder.build();
	}
}
