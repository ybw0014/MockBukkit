package org.mockbukkit.mockbukkit.block.data;

import com.destroystokyo.paper.MaterialTags;
import com.google.common.base.Preconditions;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.SoundGroup;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockSupport;
import org.bukkit.block.BlockType;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.VoxelShape;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mockbukkit.mockbukkit.block.state.BlockStateMock;
import org.mockbukkit.mockbukkit.exception.UnimplementedOperationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mock implementation of {@link BlockData}.
 * Also manages the creation of new BlockData with the appropriate mock class.
 */
public class BlockDataMock implements BlockData
{

	private static final String NULL_MATERIAL_EXCEPTION_MESSAGE = "Material cannot be null";

	private final @NotNull Material type;
	private @NotNull Map<String, Object> data;
	private static final Pattern BLOCK_DATA_PATTERN = Pattern.compile("(^[a-z_:]+)(?:(\\[(.+)\\]$)|($))");

	/**
	 * Constructs a new {@link BlockDataMock} for the provided {@link Material}.
	 *
	 * @param material The material this data is for.
	 */
	public BlockDataMock(@NotNull Material material)
	{
		checkMaterial(material);

		this.type = material;
		this.data = new LinkedHashMap<>();
	}

	@ApiStatus.Internal
	public static BlockDataMock newData(BlockType blockType, String data)
	{
		String modifiedData;
		if (blockType != null)
		{
			if (data == null)
			{
				modifiedData = blockType.getKey().toString();
			}
			else
			{
				Matcher dataMatcher = BLOCK_DATA_PATTERN.matcher(data);
				Preconditions.checkArgument(dataMatcher.find(), "String is not in a block data format: " + data);
				String onlyFields = dataMatcher.group(2);
				modifiedData = onlyFields == null ? blockType.getKey().toString() : blockType.getKey() + onlyFields;
			}
		}
		else
		{
			modifiedData = data;
		}
		return createNewData(modifiedData);
	}

	private static BlockDataMock createNewData(String dataString)
	{
		Matcher blockDataMatcher = BLOCK_DATA_PATTERN.matcher(dataString);
		Preconditions.checkArgument(blockDataMatcher.find(), "String is not in a block data format: " + dataString);
		NamespacedKey blockKey = NamespacedKey.fromString(blockDataMatcher.group(1));
		Preconditions.checkArgument(blockKey != null, "Invalid namespace key: " + blockDataMatcher.group(1));
		String blockDataString = blockDataMatcher.group(3);
		Material material = Registry.MATERIAL.get(blockKey);
		Preconditions.checkArgument(material != null, "Invalid material: " + blockKey);
		Map<String, Object> data = new HashMap<>();
		BlockDataMock blockData = BlockDataMock.mock(material);
		if (blockDataString == null)
		{
			blockData.data.clear();
			return blockData;
		}
		String[] blockDataArguments = blockDataString.split(",");
		for (String blockDataArgument : blockDataArguments)
		{
			String[] split = blockDataArgument.split("=");
			String key = split[0].strip();
			String valueString = split[1].strip();
			Preconditions.checkArgument(BlockDataKey.isRegistered(key), "Unknown block data key: " + key);
			BlockDataKey blockDataKey = BlockDataKey.fromKey(key);
			Preconditions.checkArgument(blockDataKey.appliesTo(blockData), "Can not apply block data key to '" + blockKey + "': " + key);
			Object value = blockDataKey.constructValue(valueString);
			Preconditions.checkArgument(value != null, "Unknown block data value: " + valueString);
			data.put(key, value);
		}
		blockData.data = data;
		return blockData;
	}

	// region Type Checking

	/**
	 * Ensures the provided material is one of the expected materials provided.
	 *
	 * @param material The material to test.
	 * @param expected The expected materials.
	 */
	protected void checkType(@NotNull Material material, @NotNull Material... expected)
	{
		Preconditions.checkArgument(Arrays.stream(expected).anyMatch(m -> material == m), "Cannot create a " + getClass().getSimpleName() + " from " + material);
	}

	/**
	 * Ensures the provided material is contained in the {@link Tag}.
	 *
	 * @param material The material to test.
	 * @param expected The expected tag.
	 */
	protected void checkType(@NotNull Material material, @NotNull Tag<Material> expected)
	{
		Preconditions.checkArgument(expected.isTagged(material), "Cannot create a " + getClass().getSimpleName() + " from " + material);
	}

	/**
	 * Ensures the provided material is valid for minecraft.
	 *
	 * @param material The material to test.
	 */
	protected static void checkMaterial(@NotNull Material material)
	{
		Preconditions.checkNotNull(material, NULL_MATERIAL_EXCEPTION_MESSAGE);
		Preconditions.checkState(material.isBlock(), "Can't create a block from " + material);
	}

	/**
	 * Ensures the provided material/state combination is valid for minecraft.
	 *
	 * @param property The state to test.
	 */
	protected void checkProperty(BlockDataKey property)
	{
		Preconditions.checkState(property.appliesTo(this), property + " is not a valid property for " + getMaterial());
	}
	// endregion

	/**
	 * Sets a data value.
	 *
	 * @param key   The data key.
	 * @param value The data value.
	 * @param <T>   The type of the data.
	 * @see BlockDataKey
	 */
	protected <T> void set(@NotNull BlockDataKey key, @NotNull T value)
	{
		Preconditions.checkNotNull(key, "Key cannot be null");
		Preconditions.checkNotNull(value, "Value cannot be null");

		checkProperty(key);

		this.data.put(key.key(), value);
	}

	/**
	 * Gets a data value.
	 * Will throw an {@link IllegalArgumentException} if no data is set for the provided key.
	 *
	 * @param key The data key.
	 * @param <T> The type of the data.
	 * @return The data attached to the key.
	 * @see BlockDataKey
	 */
	@SuppressWarnings("unchecked")
	protected <T> @NotNull T get(@NotNull BlockDataKey key)
	{
		Preconditions.checkNotNull(key, "Key cannot be null");
		checkProperty(key);
		T value = (T) this.data.get(key.key());
		if (value == null)
		{
			Object temp = BlockDataMockRegistry.getInstance().getDefault(getMaterial(), key.key());
			if (temp instanceof String string)
			{
				value = (T) key.constructValue(string);
			}
			else
			{
				value = (T) temp;
			}
		}
		Preconditions.checkArgument(value != null, "Cannot get property " + key + " as it does not exist");
		return value;
	}

	@Override
	public @NotNull Material getMaterial()
	{
		return this.type;
	}

	@Override
	public @NotNull String getAsString()
	{
		return getAsString(false);
	}

	@Override
	public @NotNull String getAsString(boolean hideUnspecified)
	{
		StringBuilder stateString = new StringBuilder(getMaterial().getKey() + "[");

		List<String> keysToShow = new ArrayList<>(hideUnspecified ? data.keySet() : BlockDataMockRegistry.getInstance().getBlockData(type).keySet());
		Collections.sort(keysToShow);

		boolean isFirst = true;
		for (String key : keysToShow)
		{
			Object value = data.get(key);
			if (value instanceof Enum<?> enumValue)
			{
				value = enumValue.name().toLowerCase(Locale.ROOT);
			}
			Object defaultValue = BlockDataMockRegistry.getInstance().getDefault(type, key);
			if (hideUnspecified && Objects.equals(value, defaultValue))
			{
				continue;
			}
			if (!isFirst)
			{
				stateString.append(',');
			}
			stateString.append(key).append("=").append((value == null ? defaultValue : value).toString().toLowerCase());

			isFirst = false;
		}
		stateString.append(']');

		return stateString.toString().replace("[]", "");
	}

	@Override
	public @NotNull BlockData merge(@NotNull BlockData data)
	{
		Preconditions.checkNotNull(data, "Data cannot be null");
		Preconditions.checkState(this.getMaterial() == data.getMaterial(), "Materials should match exactly");

		BlockDataMock mock = this.clone();
		mock.data.putAll(((BlockDataMock) data).data);
		return mock;
	}

	@Override
	public @NotNull SoundGroup getSoundGroup()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public int getLightEmission()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public boolean isOccluding()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public boolean requiresCorrectToolForDrops()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public boolean isSupported(@NotNull Block block)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();

	}

	@Override
	public boolean isSupported(@NotNull Location location)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();

	}

	@Override
	public boolean isFaceSturdy(@NotNull BlockFace face, @NotNull BlockSupport support)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public @NotNull VoxelShape getCollisionShape(@NotNull Location location)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public @NotNull Color getMapColor()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public @NotNull Material getPlacementMaterial()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public void rotate(@NotNull StructureRotation rotation)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public void mirror(@NotNull Mirror mirror)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public void copyTo(@NotNull BlockData blockData)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public @NotNull BlockState createBlockState()
	{
		return BlockStateMock.mockState(getMaterial());
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack itemStack, boolean considerEnchants)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public boolean isRandomlyTicked()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public boolean isPreferredTool(@NotNull ItemStack tool)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public @NotNull PistonMoveReaction getPistonMoveReaction()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public boolean matches(@Nullable BlockData data)
	{
		if (data == null || data.getMaterial() != this.getMaterial())
		{
			return false;
		}

		boolean matches = this.equals(data);

		if (!matches)
		{
			return this.merge(data).equals(this);
		}

		return matches;
	}

	@Override
	public int hashCode()
	{
		return this.type.hashCode() * this.data.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof BlockDataMock mock && this.getMaterial() == mock.getMaterial() && this.data.equals(mock.data);
	}

	@Override
	public @NotNull BlockDataMock clone()
	{
		try
		{
			BlockDataMock clonedObject = (BlockDataMock) super.clone();
			clonedObject.data = new LinkedHashMap<>(clonedObject.data);
			return clonedObject;
		}
		catch (CloneNotSupportedException e)
		{
			return BlockDataMock.mock(type, this.data);
		}
	}

	/**
	 * Attempts to construct a BlockDataMock by the provided material.
	 * Will return a basic {@link BlockDataMock} if no implementation is found.
	 *
	 * @param material The material to create the BlockData from.
	 * @return The BlockData.
	 */
	public static @NotNull BlockDataMock mock(@NotNull Material material)
	{
		Preconditions.checkNotNull(material, NULL_MATERIAL_EXCEPTION_MESSAGE);

		BlockDataMock mock = attemptMockByPaperMaterialTags(material);
		if (mock != null)
		{
			return mock;
		}

		mock = attemptMockByTag(material);
		if (mock != null)
		{
			return mock;
		}

		// Special cases
		return switch (material)
		{
			case AMETHYST_CLUSTER -> new AmethystClusterDataMock(material);
			case LEVER -> new SwitchDataMock(material);
			case BAMBOO -> new BambooDataMock();
			default -> new BlockDataMock(material);
		};
	}

	private static @NotNull BlockDataMock mock(@NotNull Material material, @NotNull Map<String, Object> previousData)
	{
		Preconditions.checkNotNull(previousData, "previousData should not be null");

		BlockDataMock blockDataMock = BlockDataMock.mock(material);
		blockDataMock.data.putAll(previousData);
		return blockDataMock;
	}

	/**
	 * Attempts to construct a BlockDataMock object by matching against Paper MaterialTags. Returns null if the given
	 * material does not match any supported MaterialSetTag.
	 *
	 * @param material Material which we will attempt to mock
	 * @return BlockDataMock if matched, null otherwise
	 */
	private static BlockDataMock attemptMockByPaperMaterialTags(@NotNull Material material)
	{
		Preconditions.checkNotNull(material, NULL_MATERIAL_EXCEPTION_MESSAGE);
		if (MaterialTags.BEDS.isTagged(material))
		{
			return new BedDataMock(material);
		}
		return null;
	}

	/**
	 * Attempts to construct a BlockDataMock object by matching against Bukkit Tags. Returns null if the given material
	 * does not match any supported Tag.
	 *
	 * @param material Material which we will attempt to mock
	 * @return BlockDataMock if matched, null otherwise
	 */
	private static BlockDataMock attemptMockByTag(@NotNull Material material)
	{
		Preconditions.checkNotNull(material, NULL_MATERIAL_EXCEPTION_MESSAGE);
		if (Tag.SLABS.isTagged(material))
		{
			return new SlabDataMock(material);
		}
		else if (Tag.STAIRS.isTagged(material))
		{
			return new StairsDataMock(material);
		}
		else if (Tag.TRAPDOORS.isTagged(material))
		{
			return new TrapDoorDataMock(material);
		}
		else if (Tag.CAMPFIRES.isTagged(material))
		{
			return new CampfireDataMock(material);
		}
		else if (Tag.WALL_SIGNS.isTagged(material))
		{
			return new WallSignDataMock(material);
		}
		else if (Tag.BUTTONS.isTagged(material))
		{
			return new SwitchDataMock(material);
		}
		else if (Tag.FENCES.isTagged(material))
		{
			return new FenceDataMock(material);
		}
		return null;
	}

}
