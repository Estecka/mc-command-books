package tk.estecka.cmdbook.config;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import net.minecraft.util.Identifier;
import tk.estecka.cmdbook.config.ConfigIO.Property;

public class Config
extends ConfigIO.AFixedCoded
{
	public @Nullable Identifier wandItem = new Identifier("stick");
	public int permissionLevel = 2;

	@Override
	public Map<String, Property<?>> GetProperties(){
		Map<String, Property<?>> props = new HashMap<>();
		props.put("wandItem", Property.String ( ()->wandItem.toString(), s->wandItem=Identifier.tryParse(s) ));
		props.put("permissionLevel", Property.Integer( ()->permissionLevel, i->permissionLevel=i ));
		return props;
	}
}
