package org.mockbukkit.mockbukkit.entity;

import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.UnimplementedOperationException;
import org.mockbukkit.mockbukkit.block.data.BlockDataMock;
import org.mockbukkit.mockbukkit.entity.data.EntityState;

import com.google.common.base.Preconditions;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Mock implementation of an {@link Enderman}.
 *
 * @see MonsterMock
 */
public class EndermanMock extends MonsterMock implements Enderman
{

	private @Nullable BlockData carriedBlock = null;
	private boolean isScreaming = false;
	private boolean hasBeenStaredAt = false;

	/**
	 * Constructs a new {@link EndermanMock} on the provided {@link ServerMock} with
	 * a specified {@link UUID}.
	 *
	 * @param server The server to create the entity on.
	 * @param uuid   The UUID of the entity.
	 */
	public EndermanMock(@NotNull ServerMock server, @NotNull UUID uuid)
	{
		super(server, uuid);
	}

	/**
	 * We're not implementing this as this would randomly fail tests. This is not a
	 * bug, it's a feature.
	 */
	@Override
	public boolean teleportRandomly()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	@Deprecated
	public @NotNull MaterialData getCarriedMaterial()
	{
		assertHasBlock();
		return new MaterialData(carriedBlock.getMaterial());
	}

	@Override
	@Deprecated
	public void setCarriedMaterial(@NotNull MaterialData material)
	{
		Preconditions.checkNotNull(material, "MaterialData cannot be null");
		carriedBlock = BlockDataMock.mock(material.getItemType());
	}

	@Override
	public @Nullable BlockData getCarriedBlock()
	{
		assertHasBlock();
		return this.carriedBlock;
	}

	@Override
	public void setCarriedBlock(@Nullable BlockData blockData)
	{
		Preconditions.checkNotNull(blockData, "BlockData cannot be null");
		this.carriedBlock = blockData;
	}

	@Override
	public boolean isScreaming()
	{
		return this.isScreaming;
	}

	@Override
	public void setScreaming(boolean screaming)
	{
		this.isScreaming = screaming;
	}

	@Override
	public boolean hasBeenStaredAt()
	{
		return this.hasBeenStaredAt;
	}

	@Override
	public void setHasBeenStaredAt(boolean hasBeenStaredAt)
	{
		this.hasBeenStaredAt = hasBeenStaredAt;
	}

	/**
	 * Asserts that this Enderman is holding a block.
	 */
	public void assertHasBlock()
	{
		Preconditions.checkState(this.carriedBlock != null, "Carried Block must be set before using this method");
	}

	@Override
	protected EntityState getEntityState()
	{
		if (this.isScreaming())
		{
			return EntityState.ANGRY;
		}
		return super.getEntityState();
	}

	@Override
	public EntityType getType()
	{
		return EntityType.ENDERMAN;
	}

}