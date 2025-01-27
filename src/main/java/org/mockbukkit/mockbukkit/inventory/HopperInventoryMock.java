package org.mockbukkit.mockbukkit.inventory;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Mock implementation of a Hopper {@link InventoryType}.
 *
 * @see InventoryMock
 * @see InventoryType#HOPPER
 */
public class HopperInventoryMock extends InventoryMock
{

	/**
	 * Constructs a new {@link HopperInventoryMock} for the given holder.
	 *
	 * @param holder The holder of the inventory.
	 */
	public HopperInventoryMock(InventoryHolder holder)
	{
		super(holder, 5, InventoryType.HOPPER);
	}

	protected HopperInventoryMock(@NotNull HopperInventoryMock inventory)
	{
		super(inventory);
	}

	@Override
	@NotNull
	public Inventory getSnapshot()
	{
		return new HopperInventoryMock(this);
	}

}
