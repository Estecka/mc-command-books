package tk.estecka.cmdbook.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import tk.estecka.cmdbook.config.ConfigIO.Property;

public class Config
extends ConfigIO.AFixedCoded
{
	public @Nullable Identifier wandItemId = new Identifier("stick");
	public int permissionLevel = 2;

	@Override
	public Map<String, Property<?>> GetProperties(){
		Map<String, Property<?>> props = new HashMap<>();
		// props.put("wandItem", Property.String ( ()->wandItemId.toString(), s->wandItemId=Identifier.tryParse(s) ));
		props.put("permissionLevel", Property.Integer( ()->permissionLevel, i->permissionLevel=i ));
		return props;
	}

	public Optional<Item> GetWandItem(){
		return Registries.ITEM.getOrEmpty(this.wandItemId);
	}
}
