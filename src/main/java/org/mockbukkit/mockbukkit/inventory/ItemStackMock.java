package org.mockbukkit.mockbukkit.inventory;

import com.google.common.base.Preconditions;
import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mockbukkit.mockbukkit.exception.ItemMetaInitException;
import org.mockbukkit.mockbukkit.exception.UnimplementedOperationException;
import org.mockbukkit.mockbukkit.inventory.meta.ItemMetaMock;
import org.mockbukkit.mockbukkit.persistence.PersistentDataContainerViewMock;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ItemStackMock extends ItemStack
{

	private ItemType type = ItemTypeMock.AIR;
	private int amount = 1;
	private ItemMeta itemMeta;
	private short durability = -1;

	private static final ItemStackMock EMPTY = new ItemStackMock((Void) null);
	private static final String ITEM_META_INITIALIZATION_ERROR = "Failed to instanciate item meta class ";

	//Utility
	protected ItemStackMock()
	{
	}

	public ItemStackMock(@NotNull Material type)
	{
		this(type, 1);
	}

	public ItemStackMock(@NotNull ItemStack stack) throws IllegalArgumentException
	{
		this.type = stack.getType().asItemType();
		this.amount = stack.getAmount();
		this.durability = initDurability(this.type);
		setItemMeta(stack.getItemMeta());
	}

	public ItemStackMock(@NotNull Material type, int amount)
	{
		this.type = type.asItemType();
		this.amount = amount;
		this.durability = initDurability(this.type);
		this.itemMeta = findItemMeta(type);
	}

	private ItemStackMock(@Nullable Void v)
	{
		this.type = ItemTypeMock.AIR;
		this.durability = initDurability(type);
		this.amount = 0;
		this.itemMeta = null;
	}

	private ItemStackMock(@NotNull ItemType type)
	{
		this.type = type;
		this.durability = initDurability(type);
		this.itemMeta = findItemMeta(type.asMaterial());
	}

	/**
	 * By some reason paper differentiates between an item with durability set and one without durability set
	 */
	private short initDurability(ItemType type)
	{
		if (type == null || type.getMaxDurability() == 0)
		{
			return -1;
		}
		return 0;
	}

	@Override
	public void setType(@NotNull Material type)
	{
		if (!type.isItem() || type.isAir())
		{
			this.type = ItemType.AIR;
			this.itemMeta = null;
			this.durability = initDurability(this.type);
			return;
		}
		if (type != this.type.asMaterial())
		{
			this.type = type.asItemType();
			if (this.itemMeta == null)
			{
				this.itemMeta = findItemMeta(type);
			}
			else
			{
				this.itemMeta = Bukkit.getItemFactory().asMetaFor(this.itemMeta, type);
			}
			if (this.durability == 0)
			{
				this.durability = initDurability(this.type);
				((Damageable) this.itemMeta).resetDamage();
			}
			else
			{
				setDurability(this.durability);
			}
		}
	}

	@NotNull
	public Material getType()
	{
		return this.type.asMaterial();
	}

	@Override
	public int getAmount()
	{
		return this.amount;
	}

	@Override
	public void setAmount(int amount)
	{
		this.amount = amount;
	}

	@Override
	public boolean isEmpty()
	{
		return this == EMPTY || this.type == ItemTypeMock.AIR || this.amount <= 0;
	}

	@Override
	public boolean setItemMeta(@Nullable ItemMeta itemMeta)
	{
		if (itemMeta == null || ItemType.AIR.equals(this.type))
		{
			this.itemMeta = findItemMeta(getType());
			this.durability = initDurability(this.type);
			return true;
		}
		if (!Bukkit.getItemFactory().isApplicable(itemMeta, this))
		{
			return false;
		}

		itemMeta = Bukkit.getItemFactory().asMetaFor(itemMeta, this);
		if (itemMeta == null) return true;
		this.itemMeta = itemMeta;

		if (this.itemMeta instanceof Damageable damageable)
		{
			short defaultDurability = initDurability(this.type);
			if (!damageable.hasDamageValue())
			{
				durability = defaultDurability;
			}
			else
			{
				short value = (short) Math.min(Short.MAX_VALUE, damageable.getDamage());
				setDurability(value);
				if (durability == defaultDurability)
				{
					damageable.resetDamage();
				}
			}
		}
		return true;
	}

	@Override
	public ItemMeta getItemMeta()
	{
		return this.itemMeta != null ? this.itemMeta.clone() : null;
	}

	@Override
	public boolean hasItemMeta()
	{
		return this.itemMeta != null && !Bukkit.getItemFactory().equals(itemMeta, null);
	}

	@Override
	public int getMaxStackSize()
	{
		if (this.itemMeta == null) return 0;

		return this.itemMeta.hasMaxStackSize() ? this.itemMeta.getMaxStackSize() : this.type.getMaxStackSize();
	}

	@Override
	public short getDurability()
	{
		if (this.type == ItemType.AIR) return -1;

		return (short) Math.max(this.durability, 0);
	}

	@Override
	public void setDurability(short durability)
	{
		short oldDurability = this.durability;
		this.durability = (short) Math.min(Math.max(durability, 0), this.type.getMaxDurability());
		if ((this.itemMeta instanceof Damageable damageable) && this.durability != oldDurability)
		{
			damageable.setDamage(this.durability);
		}
	}

	@Override
	public void addUnsafeEnchantment(@NotNull Enchantment ench, int level)
	{
		Preconditions.checkArgument(ench != null, "Enchantment cannot be null");

		if (this.itemMeta != null)
		{
			this.itemMeta.addEnchant(ench, level, true);
		}
	}

	@Override
	public int getEnchantmentLevel(Enchantment ench)
	{
		Preconditions.checkArgument(ench != null, "Enchantment cannot be null");

		final ItemMeta meta = this.itemMeta;
		Preconditions.checkNotNull(meta, "Meta must not be null");

		return meta.getEnchantLevel(ench);
	}

	@Override
	public @NotNull Map<Enchantment, Integer> getEnchantments()
	{
		return this.hasItemMeta() ? itemMeta.getEnchants() : Map.of();
	}

	@Override
	public boolean isSimilar(@org.jetbrains.annotations.Nullable ItemStack stack)
	{
		if (stack == null) return false;
		if (!(stack instanceof final ItemStackMock bukkit)) return stack.isSimilar(this);
		if (this == bukkit) return true;
		return this.type == bukkit.type;
	}

	private final PersistentDataContainerView pdcView = new PersistentDataContainerViewMock()
	{
		@Override
		public <P, C> @Nullable C get(@NotNull NamespacedKey key, @NotNull PersistentDataType<P, C> type)
		{
			if (itemMeta == null) return null;

			return itemMeta.getPersistentDataContainer().get(key, type);
		}

		@Override
		public @NotNull Set<NamespacedKey> getKeys()
		{
			if (itemMeta == null) return Set.of();

			return itemMeta.getPersistentDataContainer().getKeys();
		}
	};

	@Override
	public @NotNull PersistentDataContainerView getPersistentDataContainer()
	{
		return pdcView;
	}

	@Override
	public @NotNull ItemStack withType(@NotNull Material type)
	{
		ItemStackMock item = new ItemStackMock(type, getAmount());
		if (this.durability != -1) item.setDurability(this.durability);
		item.setItemMeta(this.itemMeta);

		return item;
	}

	@Override
	public boolean containsEnchantment(@NotNull Enchantment ench)
	{
		if (this.itemMeta == null) return false;

		return this.itemMeta.getEnchants().containsKey(ench);
	}

	@Override
	public int removeEnchantment(@NotNull Enchantment ench)
	{
		if (this.itemMeta == null) return 0;

		int level = this.itemMeta.getEnchantLevel(ench);
		this.itemMeta.removeEnchant(ench);
		return level;
	}

	@Override
	public void removeEnchantments()
	{
		if (this.itemMeta == null) return;

		this.itemMeta.removeEnchantments();
	}

	@Override
	public int getMaxItemUseDuration(@NotNull LivingEntity entity)
	{
		//TODO
		throw new UnimplementedOperationException();
	}

	public static ItemStack empty()
	{
		return EMPTY.clone();
	}

	@Override
	public @NotNull ItemStack clone()
	{
		ItemStackMock clone = new ItemStackMock(this.type);

		clone.setAmount(this.amount);
		clone.setItemMeta(this.itemMeta == null ? null : this.itemMeta.clone());
		clone.durability = this.durability;
		return clone;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) return false;
		if (!(obj instanceof ItemStack stack))
		{
			return false;
		}
		if (stack instanceof ItemStackMock bukkit)
		{
			return isSimilar(bukkit) && this.amount == bukkit.getAmount() && this.durability == bukkit.durability && Objects.equals(this.itemMeta, bukkit.getItemMeta());
		}
		else
		{
			// will delegate back to this method / no stack overflow as obj then will be item stack mock instance
			return stack.equals(this);
		}
	}

	@Override
	public int hashCode()
	{
		if (type == ItemType.AIR && this != EMPTY)
		{
			return EMPTY.hashCode();
		}
		else
		{
			int hash = Objects.hash(type, durability, lore(), getEnchantments());
			hash = hash * 31 + this.getAmount();
			return hash;
		}
	}

	private static @Nullable ItemMeta findItemMeta(Material material)
	{
		if (!material.isItem() || material == Material.AIR)
		{
			return null;
		}
		final Class<? extends ItemMeta> itemMetaClass = material.asItemType().getItemMetaClass();
		if (ItemMetaMock.class.isAssignableFrom(itemMetaClass))
		{
			try
			{
				for (var ctor : itemMetaClass.getDeclaredConstructors())
				{
					if (ctor.getParameterCount() == 1 && ctor.getParameters()[0].getType() == Material.class)
					{
						return (ItemMeta) ctor.newInstance(material);
					}
				}
				return itemMetaClass.getConstructor().newInstance();
			}
			catch (InstantiationException | IllegalAccessException | InvocationTargetException |
				   NoSuchMethodException e)
			{
				throw new ItemMetaInitException(ITEM_META_INITIALIZATION_ERROR + itemMetaClass, e);
			}
		}
		return new ItemMetaMock();
	}

	@NotNull
	public static ItemStack deserialize(@NotNull Map<String, Object> args)
	{
		int version = (args.containsKey("v")) ? ((Number) args.get("v")).intValue() : -1;
		short damage = 0;
		String damageKey = "damage";

		if (args.containsKey(damageKey))
		{
			damage = ((Number) args.get(damageKey)).shortValue();
		}

		Material type = Bukkit.getUnsafe().getMaterial((String) args.get("type"), version);

		ItemStack result = new ItemStackMock(type);

		if (args.containsKey("enchantments"))
		{
			handleLegacyEnchantmentsForDeserialization(args, result);
		}
		else if (args.containsKey("meta"))
		{
			handleMetaForDeserialization(args, version, result);
		}

		if (version < 0 && args.containsKey(damageKey))
		{
			// Set damage again incase meta overwrote it
			result.setDurability(damage);
		}
		return result;
	}

	private static void handleMetaForDeserialization(@NotNull Map<String, Object> args, int version, ItemStack result)
	{
		// We cannot and will not have meta when enchantments (pre-ItemMeta) exist
		Object raw = args.get("meta");
		if (raw instanceof ItemMeta)
		{
			//((ItemMeta) raw).setVersion(version); //TODO uncomment when setVersion is implemented
			// Paper start - for pre 1.20.5 itemstacks, add HIDE_STORED_ENCHANTS flag if HIDE_ADDITIONAL_TOOLTIP is set
			if (version < 3837 && ((ItemMeta) raw).hasItemFlag(ItemFlag.HIDE_ADDITIONAL_TOOLTIP))
			{ // 1.20.5
				((ItemMeta) raw).addItemFlags(ItemFlag.HIDE_STORED_ENCHANTS);
			}
			// Paper end
			result.setItemMeta((ItemMeta) raw);
		}
	}

	private static void handleLegacyEnchantmentsForDeserialization(@NotNull Map<String, Object> args, ItemStack result)
	{
		// Backward compatiblity, @deprecated
		Object raw = args.get("enchantments");

		if (raw instanceof Map<?, ?> map)
		{

			for (Map.Entry<?, ?> entry : map.entrySet())
			{
				String stringKey = entry.getKey().toString();
				stringKey = Bukkit.getUnsafe().get(Enchantment.class, stringKey);
				NamespacedKey key = NamespacedKey.fromString(stringKey.toLowerCase(Locale.ROOT));

				Enchantment enchantment = Bukkit.getUnsafe().get(RegistryKey.ENCHANTMENT, key);

				if ((enchantment != null) && (entry.getValue() instanceof Integer))
				{
					result.addUnsafeEnchantment(enchantment, (Integer) entry.getValue());
				}
			}
		}
	}

}
