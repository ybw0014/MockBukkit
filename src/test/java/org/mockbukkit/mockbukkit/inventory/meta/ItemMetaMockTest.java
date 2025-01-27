package org.mockbukkit.mockbukkit.inventory.meta;

import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Tag;
import org.junit.jupiter.params.provider.Arguments;
import net.kyori.adventure.text.Component;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.plugin.PluginMock;
import org.mockbukkit.mockbukkit.inventory.ItemStackMock;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockbukkit.mockbukkit.matcher.inventory.meta.ItemMetaAnyLoreMatcher.hasAnyLore;
import static org.mockbukkit.mockbukkit.matcher.inventory.meta.ItemMetaAnyLoreMatcher.hasNoLore;
import static org.mockbukkit.mockbukkit.matcher.inventory.meta.ItemMetaLoreMatcher.doesNotHaveLore;
import static org.mockbukkit.mockbukkit.matcher.inventory.meta.ItemMetaLoreMatcher.hasLore;

@ExtendWith(MockBukkitExtension.class)
class ItemMetaMockTest
{

	private ItemMetaMock meta;

	@BeforeEach
	void setUp()
	{
		meta = new ItemMetaMock();
	}

	@Test
	void new_CopyConstructor_Copied()
	{
		meta.setDisplayName("Some name");
		meta.setLore(List.of("lore"));
		meta.setUnbreakable(true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
		meta.setMaxDamage(512);
		ItemMetaMock meta2 = new ItemMetaMock(meta);
		meta2.setLore(List.of("lore"));
		assertEquals(meta2, meta);
		assertEquals(meta, meta2);
		assertEquals(meta.hashCode(), meta2.hashCode());
		assertEquals(meta.getItemFlags(), meta2.getItemFlags());
		assertEquals(meta.getMaxDamage(), meta2.getMaxDamage());
	}

	@Test
	void hasDisplayName_Default_False()
	{
		assertFalse(meta.hasDisplayName());
	}

	@Test
	void setDisplayName_NewName_NameSetExactly()
	{
		meta.setDisplayName("Some name");
		assertTrue(meta.hasDisplayName());
		assertEquals("Some name", meta.getDisplayName());
	}

	@Test
	void setDisplayName_Null_NameRemoves()
	{
		meta.setDisplayName("Some name");
		meta.setDisplayName(null);
		assertFalse(meta.hasDisplayName());
	}

	@Test
	void equals_SameWithoutDisplayName_True()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		assertEquals(meta, meta2);
		assertEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_SameWithDisplayName_True()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setDisplayName("Some name");
		meta2.setDisplayName("Some name");
		assertEquals(meta, meta2);
		assertEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_SameLore_True()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setLore(Collections.singletonList("lore"));
		meta2.setLore(Collections.singletonList("lore"));
		assertEquals(meta, meta2);
		assertEquals(meta2, meta);
		assertEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_DifferentDisplayName_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setDisplayName("Some name");
		meta2.setDisplayName("Different name");
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_OneWithDisplayNameOneWithout_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setDisplayName("Some name");
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_OneWithLoreOneWithout_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setLore(Collections.singletonList("lore"));
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_DifferentSizedLore_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setLore(Collections.singletonList("lore"));
		meta2.setLore(Arrays.asList("lore", "more lore"));
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_Null_False()
	{
		assertNotEquals(null, meta);
		assertNotEquals(null, meta);
	}

	@Test
	void equals_DamageSame_True()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setDamage(10);
		meta2.setDamage(10);
		assertEquals(meta, meta2);
		assertEquals(meta2, meta);
		assertEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_DamageDifferent_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setDamage(10);
		meta2.setDamage(20);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_DamageOneWithout_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setDamage(10);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_MaxDamageSame_True()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setMaxDamage(10);
		meta2.setMaxDamage(10);
		assertEquals(meta, meta2);
		assertEquals(meta2, meta);
		assertEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_MaxDamageDifferent_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setMaxDamage(10);
		meta2.setMaxDamage(20);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_MaxDamageOneWithout_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setMaxDamage(10);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_RepairableSame_True()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setRepairCost(10);
		meta2.setRepairCost(10);
		assertEquals(meta, meta2);
		assertEquals(meta2, meta);
		assertEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_RepairableDifferent_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setRepairCost(10);
		meta2.setRepairCost(20);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_RepairableOneWithout_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setRepairCost(10);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_MaxStackSizeSame_True()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setMaxStackSize(10);
		meta2.setMaxStackSize(10);
		assertEquals(meta, meta2);
		assertEquals(meta2, meta);
		assertEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_MaxStackSizeDifferent_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setMaxStackSize(10);
		meta2.setMaxStackSize(20);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_MaxStackSizeOneWithout_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setMaxStackSize(10);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_IsHideTooltipSame_True()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setHideTooltip(true);
		meta2.setHideTooltip(true);
		assertEquals(meta, meta2);
		assertEquals(meta2, meta);
		assertEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_IsHideTooltipDifferent_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setHideTooltip(true);
		meta2.setHideTooltip(false);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_IsFireResistantSame_True()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setFireResistant(true);
		meta2.setFireResistant(true);
		assertEquals(meta, meta2);
		assertEquals(meta2, meta);
		assertEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_IsFireResistantDifferent_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setFireResistant(true);
		meta2.setFireResistant(false);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}


	@Test
	void equals_AttributeModifiersSame_True()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		final NamespacedKey key = NamespacedKey.randomKey();
		meta.addAttributeModifier(
				Attribute.MAX_HEALTH,
				new AttributeModifier(key, 1000, AttributeModifier.Operation.ADD_NUMBER));
		meta2.addAttributeModifier(
				Attribute.MAX_HEALTH,
				new AttributeModifier(key, 1000, AttributeModifier.Operation.ADD_NUMBER));
		assertEquals(meta, meta2);
		assertEquals(meta2, meta);
		assertEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_AttributeModifiersDifferent_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		final NamespacedKey key = NamespacedKey.randomKey();
		meta.addAttributeModifier(
				Attribute.MAX_HEALTH,
				new AttributeModifier(key, 1000, AttributeModifier.Operation.ADD_NUMBER));
		meta2.addAttributeModifier(
				Attribute.MAX_HEALTH,
				new AttributeModifier(key, 100, AttributeModifier.Operation.ADD_NUMBER));
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_AttributeModifiersOneWithout_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		final NamespacedKey key = NamespacedKey.randomKey();
		meta2.addAttributeModifier(
				Attribute.MAX_HEALTH,
				new AttributeModifier(key, 100, AttributeModifier.Operation.ADD_NUMBER));
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}


	@Test
	void equals_EnchantsSame_True()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.addEnchant(Enchantment.UNBREAKING, 5, true);
		meta2.addEnchant(Enchantment.UNBREAKING, 5, true);
		assertEquals(meta, meta2);
		assertEquals(meta2, meta);
		assertEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_EnchantsDifferent_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.addEnchant(Enchantment.UNBREAKING, 5, true);
		meta2.addEnchant(Enchantment.UNBREAKING, 5, true);
		meta2.addEnchant(Enchantment.SHARPNESS, 1, true);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_EnchantsDifferentLevel_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.addEnchant(Enchantment.UNBREAKING, 5, true);
		meta2.addEnchant(Enchantment.UNBREAKING, 10, true);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_EnchantsOneEmpty_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.addEnchant(Enchantment.UNBREAKING, 5, true);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_HideFlagsSame_True()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE);
		meta2.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE);
		assertEquals(meta, meta2);
		assertEquals(meta2, meta);
		assertEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_HideFlagsDifferent_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE);
		meta2.addItemFlags(ItemFlag.HIDE_DESTROYS);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_HideFlagsOneEmpty_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_PersistentDataSame_True()
	{
		PluginMock plugin = MockBukkit.createMockPlugin();
		ItemMetaMock meta2 = new ItemMetaMock();
		NamespacedKey key = new NamespacedKey(plugin, "key");
		meta.getPersistentDataContainer().set(key, PersistentDataType.LONG, 0L);
		meta2.getPersistentDataContainer().set(key, PersistentDataType.LONG, 0L);
		assertEquals(meta, meta2);
		assertEquals(meta2, meta);
		assertEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_PersistentDataDifferent_False()
	{
		PluginMock plugin = MockBukkit.createMockPlugin();
		ItemMetaMock meta2 = new ItemMetaMock();
		NamespacedKey key = new NamespacedKey(plugin, "key");
		meta.getPersistentDataContainer().set(key, PersistentDataType.LONG, 0L);
		meta2.getPersistentDataContainer().set(key, PersistentDataType.LONG, 10L);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_PersistentDataOneEmpty_False()
	{
		PluginMock plugin = MockBukkit.createMockPlugin();
		ItemMetaMock meta2 = new ItemMetaMock();
		NamespacedKey key = new NamespacedKey(plugin, "key");
		meta.getPersistentDataContainer().set(key, PersistentDataType.LONG, 0L);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_UnbreakableSame_True()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setUnbreakable(true);
		meta2.setUnbreakable(true);
		assertEquals(meta, meta2);
		assertEquals(meta2, meta);
		meta.setUnbreakable(false);
		meta2.setUnbreakable(false);
		assertEquals(meta, meta2);
		assertEquals(meta2, meta);
		assertEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_UnbreakableDifferent_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setUnbreakable(true);
		meta2.setUnbreakable(false);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_CustomModelDataSame_True()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setCustomModelData(10);
		meta2.setCustomModelData(10);
		assertEquals(meta, meta2);
		assertEquals(meta2, meta);
		assertEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_CustomModelDataDifferent_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setCustomModelData(10);
		meta2.setCustomModelData(20);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void equals_CustomModelDataOneWithout_False()
	{
		ItemMetaMock meta2 = new ItemMetaMock();
		meta.setCustomModelData(10);
		assertNotEquals(meta, meta2);
		assertNotEquals(meta2, meta);
		assertNotEquals(meta.hashCode(), meta2.hashCode());
	}

	@Test
	void clone_WithDisplayName_ClonedExactly()
	{
		meta.setDisplayName("Some name");
		ItemMetaMock cloned = meta.clone();
		assertEquals(meta, cloned);
		assertEquals(meta.hashCode(), cloned.hashCode());
	}

	@Test
	void hasLore_NoLore_False()
	{
		assertFalse(meta.hasLore());
	}

	@Test
	void hasLore_HasLore_True()
	{
		meta.setLore(Arrays.asList("Hello", "world"));
		assertTrue(meta.hasLore());
	}

	@Test
	void getLore_LoreSet_ExactLines()
	{
		meta.setLore(Arrays.asList("Hello", "world"));
		List<String> lore = meta.getLore();
		assertEquals(2, lore.size());
		assertEquals("Hello", lore.get(0));
		assertEquals("world", lore.get(1));
	}

	@Test
	void getLore_LoreChangedAfterSet_LoreNotChanged()
	{
		List<String> originalLore = Arrays.asList("Hello", "world");
		meta.setLore(originalLore);
		originalLore.set(0, "Changed");
		List<String> lore = meta.getLore();
		lore.set(1, "Also changed");
		lore = meta.getLore();
		assertEquals(2, lore.size());
		assertEquals("Hello", lore.get(0));
		assertEquals("world", lore.get(1));
	}

	@Test
	void hasLocalizedName_NoLocalizedName_False()
	{
		assertFalse(meta.hasLocalizedName());
	}

	@Test
	void setLocalizedName_NewName_NameSetExactly()
	{
		meta.setLocalizedName("Some name");
		assertTrue(meta.hasLocalizedName());
		assertEquals("Some name", meta.getLocalizedName());
	}

	@Test
	void hasEnchants()
	{
		assertFalse(meta.hasEnchants());
		meta.addEnchant(Enchantment.UNBREAKING, 1, true);
		assertTrue(meta.hasEnchants());
	}

	@Test
	void hasEnchant()
	{
		assertFalse(meta.hasEnchant(Enchantment.MENDING));
		meta.addEnchant(Enchantment.MENDING, 1, true);
		assertTrue(meta.hasEnchant(Enchantment.MENDING));
	}

	@Test
	void getEnchantLevel()
	{
		assertEquals(0, meta.getEnchantLevel(Enchantment.UNBREAKING));
		meta.addEnchant(Enchantment.UNBREAKING, 50, true);
		assertEquals(50, meta.getEnchantLevel(Enchantment.UNBREAKING));
	}

	@Test
	void getEnchants()
	{
		meta.addEnchant(Enchantment.UNBREAKING, 3, true);

		Map<Enchantment, Integer> actual = meta.getEnchants();
		assertEquals(1, actual.size());
		assertEquals(3, actual.get(Enchantment.UNBREAKING));
	}

	@Test
	void getEnchants_IsSorted()
	{
		meta.addEnchant(Enchantment.UNBREAKING, 3, true);
		Map<Enchantment, Integer> actual = meta.getEnchants();

		assertInstanceOf(SortedMap.class, actual);
	}

	@Test
	void getEnchants_IsCopy()
	{
		Map<Enchantment, Integer> actual1 = meta.getEnchants();
		Map<Enchantment, Integer> actual2 = meta.getEnchants();

		assertNotSame(actual1, actual2);
		assertEquals(actual1, actual2);

		meta.addEnchant(Enchantment.UNBREAKING, 3, true);
		Map<Enchantment, Integer> actual3 = meta.getEnchants();

		assertNotEquals(actual1, actual3);
	}

	@Test
	void removeEnchant_NotExisting()
	{
		assertFalse(meta.removeEnchant(Enchantment.SHARPNESS));
	}

	@Test
	void removeEnchant()
	{
		meta.addEnchant(Enchantment.SHARPNESS, 5, true);
		assertTrue(meta.removeEnchant(Enchantment.SHARPNESS));
	}

	@Test
	void removeEnchants()
	{
		meta.addEnchant(Enchantment.SHARPNESS, 5, true);
		meta.addEnchant(Enchantment.UNBREAKING, 3, true);
		assertEquals(2, meta.getEnchants().size());

		meta.removeEnchantments();
		assertTrue(meta.getEnchants().isEmpty());
	}

	@Test
	void addEnchant_IgnoreLevel()
	{
		assertTrue(meta.addEnchant(Enchantment.UNBREAKING, 100, true));
		assertTrue(meta.hasEnchant(Enchantment.UNBREAKING));
	}

	@Test
	void addEnchant_AlreadyExist()
	{
		meta.addEnchant(Enchantment.UNBREAKING, 100, true);
		assertFalse(meta.addEnchant(Enchantment.UNBREAKING, 100, true));
	}

	@Test
	void setUnbreakable_True_ItemIsUnbreakable()
	{
		meta.setUnbreakable(true);
		assertTrue(meta.isUnbreakable());
	}

	@Test
	void setUnbreakable_False_ItemIsBreakable()
	{
		meta.setUnbreakable(false);
		assertFalse(meta.isUnbreakable());
	}

	@Test
	void testHasNoLore_HasNoLore_Returns()
	{
		assertThat(meta, hasNoLore());
	}

	@Test
	void testHasNoLore_HasNoLore_Asserts()
	{
		meta.setLore(Arrays.asList("Hello", "world"));
		assertThat(meta, hasAnyLore());
	}

	@Test
	void testLore_CorrectLore_Returns()
	{
		meta.setLore(Arrays.asList("Hello", "world"));
		assertThat(meta, hasLore("Hello", "world"));
	}

	@Test
	void testLore_InorrectLore_Asserts()
	{
		meta.setLore(Arrays.asList("Hello", "world"));
		assertThat(meta, doesNotHaveLore("Something", "else"));
	}

	@Test
	void testDamageCorrectlySet()
	{
		assertFalse(meta.hasDamageValue());
		assertFalse(meta.hasDamage());

		int value = 500;
		meta.setDamage(value);
		ItemStack item = new ItemStackMock(Material.DIAMOND_SWORD);
		item.setItemMeta(meta);

		Damageable itemMeta = (Damageable) item.getItemMeta();
		int damage = itemMeta.getDamage();
		assertEquals(value, damage);
		assertTrue(itemMeta.hasDamage());
		assertTrue(itemMeta.hasDamageValue());
	}

	@Test
	void testNoDamage()
	{
		meta.setDamage(0);
		assertFalse(meta.hasDamage());
		assertTrue(meta.hasDamageValue());

		ItemStack item = new ItemStackMock(Material.DIAMOND_SWORD);
		item.setItemMeta(meta);

		Damageable itemMeta = (Damageable) item.getItemMeta();
		int damage = itemMeta.getDamage();
		assertEquals(0, damage);
		assertFalse(itemMeta.hasDamage());
		assertFalse(itemMeta.hasDamageValue());
	}

	@Test
	void testNegativeDamageFail()
	{
		try
		{
			meta.setDamage(-1);
			fail("Negative damage could be set");
		} catch (IllegalStateException e)
		{
			// Good scenario
		}
	}

	@Test
	void testResetDamage()
	{
		meta.setDamage(1);

		assertTrue(meta.hasDamageValue());
		assertTrue(meta.hasDamage());

		meta.resetDamage();

		assertFalse(meta.hasDamageValue());
		assertFalse(meta.hasDamage());
	}

	@Test
	void testRepairCostSetCorrectly()
	{
		int value = 10;
		meta.setRepairCost(value);
		ItemStack item = new ItemStackMock(Material.DIAMOND_SWORD);
		item.setItemMeta(meta);

		Repairable itemMeta = (Repairable) item.getItemMeta();
		int repairCost = itemMeta.getRepairCost();
		assertEquals(value, repairCost);
		assertTrue(itemMeta.hasRepairCost());
	}

	@Test
	void testRepairCost()
	{
		meta.setRepairCost(0);
		ItemStack item = new ItemStackMock(Material.DIAMOND_SWORD);
		item.setItemMeta(meta);

		Repairable itemMeta = (Repairable) item.getItemMeta();
		int repairCost = itemMeta.getRepairCost();
		assertEquals(0, repairCost);
		assertFalse(itemMeta.hasRepairCost());
	}

	@Test
	void testCustomModelData()
	{
		meta.setCustomModelData(null);
		assertFalse(meta.hasCustomModelData());

		meta.setCustomModelData(100);
		assertTrue(meta.hasCustomModelData());
		assertEquals(100, meta.getCustomModelData());
	}

	@Test
	void testSerialization()
	{
		// Tests for displayName, Lore, enchants, unbreakable status, and damage
		meta.setDisplayName("Test name");
		meta.setLore(List.of("Test lore"));
		meta.setUnbreakable(true);
		meta.setDamage(5);
		meta.setRepairCost(3);

		Map<String, Object> actual = meta.serialize();

		// Perform tests
		assertEquals("\"Test name\"", actual.get("display-name"));
		assertEquals(List.of("\"Test lore\""), actual.get("lore"));
		assertEquals(true, actual.get("Unbreakable"));
		assertEquals(5, actual.get("Damage"));
		assertEquals(3, actual.get("repair-cost"));
	}

	@Test
	void testDeserialization()
	{
		Map<String, Object> actual = meta.serialize();
		assertEquals(meta, ItemMetaMock.deserialize(actual));
	}

	@Test
	void testBukkitSerialization() throws IOException, ClassNotFoundException
	{
		ItemMetaMock empty = new ItemMetaMock();
		ItemMetaMock modified = new ItemMetaMock();

		modified.setDisplayName("Test name");
		modified.setLore(List.of("Test lore"));
		modified.setUnbreakable(true);
		modified.setDamage(5);
		modified.setRepairCost(3);
		modified.setCustomModelData(2);

		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		BukkitObjectOutputStream bukkitOutput = new BukkitObjectOutputStream(byteOutput);

		bukkitOutput.writeObject(empty);
		bukkitOutput.writeObject(modified);

		ByteArrayInputStream byteInput = new ByteArrayInputStream(byteOutput.toByteArray());
		BukkitObjectInputStream bukkitInput = new BukkitObjectInputStream(byteInput);

		assertEquals(empty, bukkitInput.readObject());
		assertEquals(modified, bukkitInput.readObject());

		bukkitOutput.close();
		bukkitInput.close();
	}

	@Test
	void hasAttributeModifiers_Constructor_Empty()
	{
		ItemMetaMock meta = new ItemMetaMock();

		assertFalse(meta.hasAttributeModifiers());
	}

	@Test
	void getAttributeModifiers_Constructor_Null()
	{
		ItemMetaMock meta = new ItemMetaMock();

		assertNull(meta.getAttributeModifiers());
	}

	@Test
	void setAttributeModifiers_NullMap()
	{
		ItemMetaMock meta = new ItemMetaMock();

		meta.setAttributeModifiers(null);

		assertFalse(meta.hasAttributeModifiers());
	}

	@Test
	void setAttributeModifiers_AddsEntries()
	{
		ItemMetaMock meta = new ItemMetaMock();

		Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();
		AttributeModifier modifier = new AttributeModifier("test", 1, AttributeModifier.Operation.ADD_NUMBER);
		modifiers.put(Attribute.ARMOR, modifier);

		meta.setAttributeModifiers(modifiers);

		assertEquals(1, meta.getAttributeModifiers().size());
		assertEquals(1, meta.getAttributeModifiers().get(Attribute.ARMOR).size());
		assertEquals(modifier, meta.getAttributeModifiers().get(Attribute.ARMOR).stream().findFirst().orElse(null));
	}

	@Test
	void setAttributeModifiers_RemovesAndAddsEntries()
	{
		ItemMetaMock meta = new ItemMetaMock();
		Multimap<Attribute, AttributeModifier> oldModifiers = LinkedHashMultimap.create();
		oldModifiers.put(Attribute.ARMOR, new AttributeModifier("test_1", 1, AttributeModifier.Operation.ADD_NUMBER));
		meta.setAttributeModifiers(oldModifiers);
		Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();
		AttributeModifier modifier = new AttributeModifier("test_2", 1, AttributeModifier.Operation.ADD_NUMBER);
		modifiers.put(Attribute.ARMOR_TOUGHNESS, modifier);

		meta.setAttributeModifiers(modifiers);

		assertEquals(1, meta.getAttributeModifiers().size());
		assertEquals(1, meta.getAttributeModifiers().get(Attribute.ARMOR_TOUGHNESS).size());
		assertEquals(modifier, meta.getAttributeModifiers().get(Attribute.ARMOR_TOUGHNESS).stream().findFirst().orElse(null));
	}

	@Test
	void getAttributeModifiers_Slot()
	{
		ItemMetaMock meta = new ItemMetaMock();
		Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();
		AttributeModifier modifier1 = new AttributeModifier(UUID.randomUUID(), "test_1", 1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD);
		AttributeModifier modifier2 = new AttributeModifier(UUID.randomUUID(), "test_2", 1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST);
		modifiers.put(Attribute.ARMOR, modifier1);
		modifiers.put(Attribute.ARMOR, modifier2);

		meta.setAttributeModifiers(modifiers);

		assertEquals(1, meta.getAttributeModifiers(EquipmentSlot.HEAD).size());
		assertEquals(1, meta.getAttributeModifiers(EquipmentSlot.HEAD).get(Attribute.ARMOR).size());
		assertEquals(modifier1, meta.getAttributeModifiers(EquipmentSlot.HEAD).get(Attribute.ARMOR).stream().findFirst().orElse(null));
	}

	@Test
	void getAttributeModifiers_NullAttribute_ThrowException()
	{
		ItemMetaMock meta = new ItemMetaMock();

		assertThrowsExactly(NullPointerException.class, () -> meta.getAttributeModifiers((Attribute) null));
	}

	@Test
	void addAttributeModifier_AddsOne()
	{
		ItemMetaMock meta = new ItemMetaMock();
		AttributeModifier modifier = new AttributeModifier("test", 1, AttributeModifier.Operation.ADD_NUMBER);

		meta.addAttributeModifier(Attribute.ARMOR, modifier);

		assertEquals(1, meta.getAttributeModifiers().size());
		assertEquals(1, meta.getAttributeModifiers().get(Attribute.ARMOR).size());
		assertEquals(modifier, meta.getAttributeModifiers().get(Attribute.ARMOR).stream().findFirst().orElse(null));
	}

	@Test
	void addAttributeModifier_Duplicate_ThrowsException()
	{
		ItemMetaMock meta = new ItemMetaMock();
		AttributeModifier modifier = new AttributeModifier("test", 1, AttributeModifier.Operation.ADD_NUMBER);

		meta.addAttributeModifier(Attribute.ARMOR, modifier);

		assertThrowsExactly(IllegalArgumentException.class, () -> meta.addAttributeModifier(Attribute.ARMOR, modifier));
	}

	@Test
	void addAttributeModifier_NullAttribute_ThrowsException()
	{
		ItemMetaMock meta = new ItemMetaMock();
		AttributeModifier modifier = new AttributeModifier("test", 1, AttributeModifier.Operation.ADD_NUMBER);

		assertThrowsExactly(NullPointerException.class, () -> meta.addAttributeModifier(null, modifier));
	}

	@Test
	void addAttributeModifier_NullModifier_ThrowsException()
	{
		ItemMetaMock meta = new ItemMetaMock();

		assertThrowsExactly(NullPointerException.class, () -> meta.addAttributeModifier(Attribute.ARMOR, null));
	}

	@Test
	void removeAttribute_Attribute()
	{
		ItemMetaMock meta = new ItemMetaMock();
		AttributeModifier modifier = new AttributeModifier("test", 1, AttributeModifier.Operation.ADD_NUMBER);
		meta.addAttributeModifier(Attribute.ARMOR, modifier);

		assertTrue(meta.hasAttributeModifiers());

		meta.removeAttributeModifier(Attribute.ARMOR);

		assertFalse(meta.hasAttributeModifiers());
	}

	@Test
	void removeAttribute_Attribute_NullThrowsException()
	{
		ItemMetaMock meta = new ItemMetaMock();

		assertThrowsExactly(NullPointerException.class, () -> meta.removeAttributeModifier((Attribute) null));
	}

	@Test
	void removeAttribute_Slot_RemovesCorrectSlot()
	{
		ItemMetaMock meta = new ItemMetaMock();
		AttributeModifier modifier1 = new AttributeModifier(UUID.randomUUID(), "test_1", 1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD);
		AttributeModifier modifier2 = new AttributeModifier(UUID.randomUUID(), "test_2", 1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST);
		meta.addAttributeModifier(Attribute.ARMOR, modifier1);
		meta.addAttributeModifier(Attribute.ARMOR, modifier2);

		assertEquals(2, meta.getAttributeModifiers().size());
		meta.removeAttributeModifier(EquipmentSlot.HEAD);
		assertEquals(1, meta.getAttributeModifiers().size());
		assertEquals(modifier2, meta.getAttributeModifiers().get(Attribute.ARMOR).stream().findFirst().orElse(null));
	}

	@Test
		// May seem a little weird, but this is what Spigot does
		// (https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/inventory/CraftMetaItem.java#1019)
	void removeAttribute_Slot_RemovesAllNoSlots()
	{
		ItemMetaMock meta = new ItemMetaMock();
		AttributeModifier modifier1 = new AttributeModifier("test_1", 1, AttributeModifier.Operation.ADD_NUMBER);
		AttributeModifier modifier2 = new AttributeModifier("test_2", 1, AttributeModifier.Operation.ADD_NUMBER);
		meta.addAttributeModifier(Attribute.ARMOR, modifier1);
		meta.addAttributeModifier(Attribute.ARMOR, modifier2);

		meta.removeAttributeModifier(EquipmentSlot.HEAD);

		assertFalse(meta.hasAttributeModifiers());
	}

	@Test
	void removeAttribute_SpecificModifier_Removes()
	{
		ItemMetaMock meta = new ItemMetaMock();
		AttributeModifier modifier1 = new AttributeModifier(UUID.randomUUID(), "test_1", 1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD);
		AttributeModifier modifier2 = new AttributeModifier(UUID.randomUUID(), "test_2", 1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST);
		meta.addAttributeModifier(Attribute.ARMOR, modifier1);
		meta.addAttributeModifier(Attribute.ARMOR, modifier2);

		meta.removeAttributeModifier(Attribute.ARMOR, modifier1);

		assertEquals(1, meta.getAttributeModifiers().size());
		assertEquals(modifier2, meta.getAttributeModifiers().get(Attribute.ARMOR).stream().findFirst().orElse(null));
	}

	@Test
	void removeAttribute_SpecificModifier_NullAttribute_ThrowsException()
	{
		ItemMetaMock meta = new ItemMetaMock();
		AttributeModifier modifier = new AttributeModifier("test_1", 1, AttributeModifier.Operation.ADD_NUMBER);
		meta.addAttributeModifier(Attribute.ARMOR, modifier);

		assertThrowsExactly(NullPointerException.class, () -> meta.removeAttributeModifier(null, modifier));
	}

	@Test
	void removeAttribute_SpecificModifier_NullModifier_ThrowsException()
	{
		ItemMetaMock meta = new ItemMetaMock();

		assertThrowsExactly(NullPointerException.class, () -> meta.removeAttributeModifier(Attribute.ARMOR, null));
	}

	@Test
	void testHasMaxDamageDefault()
	{
		assertFalse(meta.hasMaxDamage());
	}

	@Test
	void testGetMaxDamageDefault()
	{
		assertThrows(IllegalStateException.class, () -> meta.getMaxDamage());
	}

	@Test
	void testSetMaxDamage()
	{
		meta.setMaxDamage(Integer.valueOf(1));
		assertEquals(1, meta.getMaxDamage());
	}

	@Test
	void testSetMaxDamage_Null()
	{
		assertDoesNotThrow(() -> meta.setMaxDamage(null));
		assertFalse(meta.hasMaxDamage());
		assertThrows(IllegalStateException.class, () -> meta.getMaxDamage());
	}

	@Test
	void testIsHideToolTipDefault()
	{
		assertFalse(meta.isHideTooltip());
	}

	@Test
	void testSetHideToolTip()
	{
		meta.setHideTooltip(true);
		assertTrue(meta.isHideTooltip());
	}

	@Test
	void testIsFireResistantDefault()
	{
		assertFalse(meta.isFireResistant());
	}

	@Test
	void testSetFireResistant()
	{
		meta.setFireResistant(true);
		assertTrue(meta.isFireResistant());
	}
	@Test
	void testHasEnchantable()
	{
		assertFalse(meta.hasEnchantable());

		meta.setEnchantable(10);
		assertTrue(meta.hasEnchantable());
	}

	@Test
	void testGetEnchantable()
	{
		meta.setEnchantable(10);
		assertEquals(10, meta.getEnchantable());

		meta.setEnchantable(null);
		IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, meta::getEnchantable);
		assertEquals("We don't have Enchantable! Check hasEnchantable first!",
				illegalStateException.getMessage());
	}

	@Test
	void testSetEnchantable()
	{
		meta.setEnchantable(10);
		assertEquals(10, meta.getEnchantable());

		meta.setEnchantable(null);
		assertFalse(meta.hasEnchantable());

		IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () ->
		{
			meta.setEnchantable(-1);
		});

		assertEquals("Enchantability must be positive", illegalArgumentException.getMessage());
	}

	@Test
	void testHasCustomModelData()
	{
		assertFalse(meta.hasCustomModelData());

		meta.setCustomModelData(100);
		assertTrue(meta.hasCustomModelData());
	}

	@Test
	void testGetCustomModelData()
	{
		meta.setCustomModelData(100);
		assertEquals(100, meta.getCustomModelData());
	}

	@Test
	void testSetCustomModelData()
	{
		meta.setCustomModelData(100);
		assertEquals(100, meta.getCustomModelData());

		meta.setCustomModelData(null);
		assertFalse(meta.hasCustomModelData());
	}

	@Test
	void testItemName()
	{
		Component name = Component.text("Test Name");
		meta.itemName(name);
		assertEquals(name, meta.itemName());
	}

	@Test
	void testSetItemName()
	{
		Component name = Component.text("Test Name");
		meta.itemName(name);
		assertEquals(name, meta.itemName());

		meta.itemName(null);
		assertFalse(meta.hasItemName());
	}

	@Test
	void testGetItemName()
	{
		Component name = Component.text("Test Name");
		meta.itemName(name);
		assertEquals("Test Name", meta.getItemName());
	}

	@Test
	void testSetItemNameString()
	{
		meta.setItemName("Test Name");
		assertEquals("Test Name", meta.getItemName());
	}

	@Test
	void testHasItemNameNotSet()
	{
		assertFalse(meta.hasItemName());
		meta.itemName(Component.text("Test Name"));
		assertTrue(meta.hasItemName());
	}


	@ParameterizedTest
	@MethodSource("getItemMetaTypesStream")
	void hashCode_equalsForAllExceptItemMeta(JsonElement jsonElement)
	{
		ItemType itemType = Registry.ITEM.get(NamespacedKey.fromString(jsonElement.getAsString()));
		ItemMeta itemMeta = itemType.createItemStack().getItemMeta();
		fillFieldsWithData(itemMeta);
		ItemMeta cloned = itemMeta.clone();
		assertEquals(itemMeta, cloned);
		assertEquals(itemMeta.hashCode(), cloned.hashCode());
	}

	@Test
	void hashCode_equalsForItemMeta()
	{
		ItemMeta itemMeta = new ItemMetaMock();
		fillFieldsWithData(itemMeta);
		ItemMeta cloned = itemMeta.clone();
		assertEquals(itemMeta, cloned);
		assertEquals(itemMeta.hashCode(), cloned.hashCode());
	}

	static Stream<JsonElement> getItemMetaTypesStream() throws IOException
	{
		try (InputStream inputStream = MockBukkit.class.getResourceAsStream("/itemstack/metaItemTypes.json"))
		{
			return JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonArray().asList().stream();
		}
	}

	private void fillFieldsWithData(ItemMeta object)
	{
		Class<? extends ItemMeta> itemMetaClass = object.getClass();
		for (Method method : itemMetaClass.getDeclaredMethods())
		{
			if (method.getReturnType() != void.class)
			{
				continue;
			}
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 1)
			{
				continue;
			}
			Class<?> parameterType = method.getParameterTypes()[0];
			try
			{
				invokeSetter(parameterType, method, object);
			}
			catch (InvocationTargetException ignored)
			{
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void invokeSetter(Class<?> parameterType, Method method, ItemMeta object) throws InvocationTargetException, IllegalAccessException
	{
		if (boolean.class.isAssignableFrom(parameterType))
		{
			method.invoke(object, true);
		}
		if (parameterType == String.class)
		{
			method.invoke(object, "Hello world!");
		}
		if (int.class.isAssignableFrom(parameterType))
		{
			method.invoke(object, 1);
		}
		if (short.class.isAssignableFrom(parameterType))
		{
			method.invoke(object, (short) 1);
		}
		if (byte.class.isAssignableFrom(parameterType))
		{
			method.invoke(object, (byte) 1);
		}
		if (long.class.isAssignableFrom(parameterType))
		{
			method.invoke(object, 1L);
		}
		if (double.class.isAssignableFrom(parameterType))
		{
			method.invoke(object, 1D);
		}
		if (float.class.isAssignableFrom(parameterType))
		{
			method.invoke(object, 1F);
		}
		if (Enum.class.isAssignableFrom(parameterType))
		{
			Enum[] enums = (Enum[]) parameterType.getEnumConstants();
			method.invoke(object, enums[enums.length - 1]);
		}
	}

	// Copied from org.mockbukkit.mockbukkit.inventory.ItemFactory#testGetItemMetaCorrectClass and similar
	// Only replace "factory.getItemMeta" with "new ItemStackMock" followed by a .getItemMeta()
	@Test
	void testGetItemMetaCorrectClass()
	{
		assertInstanceOf(ItemMetaMock.class, new ItemStackMock(Material.DIRT).getItemMeta());
		assertInstanceOf(SkullMetaMock.class, new ItemStackMock(Material.PLAYER_HEAD).getItemMeta());

		assertInstanceOf(BookMetaMock.class, new ItemStackMock(Material.WRITABLE_BOOK).getItemMeta());
		assertInstanceOf(BookMetaMock.class, new ItemStackMock(Material.WRITTEN_BOOK).getItemMeta());
		assertInstanceOf(EnchantmentStorageMetaMock.class, new ItemStackMock(Material.ENCHANTED_BOOK).getItemMeta());
		assertInstanceOf(KnowledgeBookMetaMock.class, new ItemStackMock(Material.KNOWLEDGE_BOOK).getItemMeta());

		assertInstanceOf(FireworkEffectMetaMock.class, new ItemStackMock(Material.FIREWORK_STAR).getItemMeta());
		assertInstanceOf(FireworkMetaMock.class, new ItemStackMock(Material.FIREWORK_ROCKET).getItemMeta());

		assertInstanceOf(SuspiciousStewMetaMock.class, new ItemStackMock(Material.SUSPICIOUS_STEW).getItemMeta());
		assertInstanceOf(PotionMetaMock.class, new ItemStackMock(Material.POTION).getItemMeta());
		assertInstanceOf(PotionMetaMock.class, new ItemStackMock(Material.TIPPED_ARROW).getItemMeta());

		assertInstanceOf(ColorableArmorMetaMock.class, new ItemStackMock(Material.LEATHER_HELMET).getItemMeta());
		assertInstanceOf(ColorableArmorMetaMock.class, new ItemStackMock(Material.LEATHER_CHESTPLATE).getItemMeta());
		assertInstanceOf(ColorableArmorMetaMock.class, new ItemStackMock(Material.LEATHER_LEGGINGS).getItemMeta());
		assertInstanceOf(ColorableArmorMetaMock.class, new ItemStackMock(Material.LEATHER_BOOTS).getItemMeta());
		assertInstanceOf(ColorableArmorMetaMock.class, new ItemStackMock(Material.WOLF_ARMOR).getItemMeta());
		assertInstanceOf(LeatherArmorMetaMock.class, new ItemStackMock(Material.LEATHER_HORSE_ARMOR).getItemMeta());

		assertInstanceOf(ShieldMetaMock.class, new ItemStackMock(Material.SHIELD).getItemMeta());

		assertInstanceOf(AxolotlBucketMetaMock.class, new ItemStackMock(Material.AXOLOTL_BUCKET).getItemMeta());
		assertInstanceOf(BundleMetaMock.class, new ItemStackMock(Material.BUNDLE).getItemMeta());
		assertInstanceOf(MapMetaMock.class, new ItemStackMock(Material.FILLED_MAP).getItemMeta());
		assertInstanceOf(CompassMetaMock.class, new ItemStackMock(Material.COMPASS).getItemMeta());
		assertInstanceOf(CrossbowMetaMock.class, new ItemStackMock(Material.CROSSBOW).getItemMeta());
		assertInstanceOf(ArmorStandMetaMock.class, new ItemStackMock(Material.ARMOR_STAND).getItemMeta());
		assertInstanceOf(TropicalFishBucketMetaMock.class, new ItemStackMock(Material.TROPICAL_FISH_BUCKET).getItemMeta());
		assertInstanceOf(OminousBottleMetaMock.class, new ItemStackMock(Material.OMINOUS_BOTTLE).getItemMeta());

	}

	@Test
	void setVersion_ShouldNotThrow()
	{
		assertDoesNotThrow(() -> meta.setVersion(1));
	}

	@Nested
	class HasItemName
	{

		@Test
		void givenMetaWithoutName()
		{
			assertFalse(meta.hasItemName());
		}

		@Test
		void givenMetaWithName()
		{
			meta.itemName(Component.text("This is the item name"));

			assertTrue(meta.hasItemName());
		}

	}

	@Nested
	class ItemName
	{

		@Test
		void givenMetaWithoutName()
		{
			Component actual = meta.itemName();
			assertNotNull(actual);
			assertEquals(Component.empty(), actual);
		}

		@Test
		void givenMetaWithName()
		{
			meta.itemName(Component.text("Name").color(NamedTextColor.RED));

			Component actual = meta.itemName();
			assertNotNull(actual);
			assertEquals(Component.text("Name").color(NamedTextColor.RED), actual);
		}

	}

	@Nested
	class GetItemName
	{

		@Test
		void givenMetaWithoutName()
		{
			String actual = meta.getItemName();
			assertNotNull(actual);
			assertEquals("", actual);
		}

		@Test
		void givenMetaWithNullName()
		{
			meta.setItemName(null);

			String actual = meta.getItemName();
			assertNotNull(actual);
			assertEquals("", actual);
		}

		@Test
		void givenMetaWithName()
		{
			meta.setItemName("§cThis §b§lis §ea §1name");

			String actual = meta.getItemName();
			assertNotNull(actual);
			assertEquals("§cThis §b§lis §ea §1name", actual);
		}

	}

	@Nested
	class GetEnchantmentGlintOverride
	{
		@Test
		void givenDefault()
		{
			assertFalse(meta.hasEnchantmentGlintOverride());
			IllegalStateException e = assertThrows(IllegalStateException.class, () -> meta.getEnchantmentGlintOverride());
			assertEquals("We don't have enchantment_glint_override! Check hasEnchantmentGlintOverride first!", e.getMessage());
		}

		@Test
		void givenNullValue()
		{
			meta.setEnchantmentGlintOverride(null);

			assertFalse(meta.hasEnchantmentGlintOverride());
			IllegalStateException e = assertThrows(IllegalStateException.class, () -> meta.getEnchantmentGlintOverride());
			assertEquals("We don't have enchantment_glint_override! Check hasEnchantmentGlintOverride first!", e.getMessage());
		}

		@ParameterizedTest
		@ValueSource(booleans = {true, false})
		void givenPossibleValues(boolean value)
		{
			meta.setEnchantmentGlintOverride(value);

			assertTrue(meta.hasEnchantmentGlintOverride());
			assertEquals(value, meta.getEnchantmentGlintOverride());
		}

	}

	@Nested
	class GetRarity
	{
		@Test
		void givenDefault()
		{
			assertFalse(meta.hasRarity());
			IllegalStateException e = assertThrows(IllegalStateException.class, () -> meta.getRarity());
			assertEquals("We don't have rarity! Check hasRarity first!", e.getMessage());
		}

		@Test
		void givenNullValue()
		{
			meta.setEnchantmentGlintOverride(null);

			assertFalse(meta.hasRarity());
			IllegalStateException e = assertThrows(IllegalStateException.class, () -> meta.getRarity());
			assertEquals("We don't have rarity! Check hasRarity first!", e.getMessage());
		}

		@ParameterizedTest
		@EnumSource(ItemRarity.class)
		void givenPossibleValues(ItemRarity value)
		{
			meta.setRarity(value);

			assertTrue(meta.hasRarity());
			assertEquals(value, meta.getRarity());
		}

	}

	@ParameterizedTest
	@MethodSource("spawnEgg_Materials")
	void testGetItemMetaCorrectClass_SpawnEgg(Material egg)
	{
		assertInstanceOf(SpawnEggMetaMock.class, new ItemStackMock(egg).getItemMeta());
	}

	@ParameterizedTest
	@MethodSource("banners_Materials")
	void testGetItemMetaCorrectClass_Banners(Material banner)
	{
		assertInstanceOf(BannerMetaMock.class, new ItemStackMock(banner).getItemMeta());
	}

	@ParameterizedTest
	@MethodSource("trimmable_Materials")
	void testGetItemMetaCorrectClass_Trimmable(Material armor)
	{
		assertInstanceOf(ArmorMetaMock.class, new ItemStackMock(armor).getItemMeta());
	}

	@ParameterizedTest
	@MethodSource("skulls_Materials")
	void testGetItemMetaCorrectClass_Skulls(Material skull)
	{
		assertInstanceOf(SkullMetaMock.class, new ItemStackMock(skull).getItemMeta());
	}

	public static Stream<Arguments> spawnEgg_Materials()
	{
		return MaterialTags.SPAWN_EGGS.getValues().stream().map(Arguments::of);
	}

	public static Stream<Arguments> banners_Materials()
	{
		return Tag.ITEMS_BANNERS.getValues().stream().map(Arguments::of);
	}

	public static Stream<Arguments> trimmable_Materials()
	{
		return Tag.ITEMS_TRIMMABLE_ARMOR.getValues().stream().map(Arguments::of);
	}

	public static Stream<Arguments> skulls_Materials()
	{
		return Tag.ITEMS_SKULLS.getValues().stream().map(Arguments::of);
	}

}
