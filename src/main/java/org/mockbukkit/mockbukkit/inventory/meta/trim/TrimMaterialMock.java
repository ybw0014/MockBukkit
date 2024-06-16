package org.mockbukkit.mockbukkit.inventory.meta.trim;

import org.mockbukkit.mockbukkit.exception.UnimplementedOperationException;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.jetbrains.annotations.NotNull;

public class TrimMaterialMock implements TrimMaterial
{

	private final NamespacedKey key;
	private final Component description;

	public TrimMaterialMock(JsonObject data)
	{
		this.key = NamespacedKey.fromString(data.get("key").getAsString());
		this.description = GsonComponentSerializer.gson().deserializeFromTree(data.get("description"));
	}

	@Override
	public @NotNull Component description()
	{
		return description;
	}

	@Override
	public @NotNull String getTranslationKey()
	{
		throw new UnimplementedOperationException();
	}

	@Override
	public @NotNull NamespacedKey getKey()
	{
		return key;
	}

}
