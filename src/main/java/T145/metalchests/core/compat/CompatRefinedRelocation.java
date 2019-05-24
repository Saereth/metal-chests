package T145.metalchests.core.compat;

import javax.annotation.Nullable;

import org.apache.commons.lang3.text.WordUtils;

import T145.metalchests.api.BlocksMC;
import T145.metalchests.api.chests.IMetalChest;
import T145.metalchests.api.chests.UpgradeRegistry;
import T145.metalchests.api.constants.ChestType;
import T145.metalchests.api.constants.ConfigMC;
import T145.metalchests.api.constants.RegistryMC;
import T145.metalchests.blocks.BlockMetalChest;
import T145.metalchests.client.render.blocks.RenderMetalSortingChest;
import T145.metalchests.core.MetalChests;
import T145.metalchests.tiles.TileMetalChest;
import T145.metalchests.tiles.TileMetalHungryChest;
import T145.metalchests.tiles.TileMetalHungrySortingChest;
import T145.metalchests.tiles.TileMetalSortingChest;
import net.blay09.mods.refinedrelocation.ModBlocks;
import net.blay09.mods.refinedrelocation.item.ItemSortingUpgrade;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(modid = RegistryMC.ID)
class CompatRefinedRelocation {

	private CompatRefinedRelocation() {}

	@Optional.Method(modid = RegistryMC.ID_RR2)
	@SubscribeEvent
	public static void registerBlocks(final RegistryEvent.Register<Block> event) {
		final IForgeRegistry<Block> registry = event.getRegistry();

		registry.register(BlocksMC.METAL_SORTING_CHEST = new BlockMetalChest() {

			@Override
			protected void registerResource() {
				this.registerResource(RegistryMC.RESOURCE_METAL_SORTING_CHEST);
			}

			@Nullable
			@Override
			public TileEntity createTileEntity(World world, IBlockState state) {
				return new TileMetalSortingChest(state.getValue(IMetalChest.VARIANT));
			}
		});

		MetalChests.registerTileEntity(TileMetalSortingChest.class);
	}

	@Optional.Method(modid = RegistryMC.ID_RR2)
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();

		MetalChests.registerItemBlock(registry, BlocksMC.METAL_SORTING_CHEST, ChestType.class);
	}

	@Optional.Method(modid = RegistryMC.ID_RR2)
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		for (ChestType type : ChestType.values()) {
			MetalChests.registerModel(BlocksMC.METAL_SORTING_CHEST, type.ordinal(), MetalChests.getVariantName(type));
		}

		MetalChests.registerTileRenderer(TileMetalSortingChest.class, new RenderMetalSortingChest());

		if (ConfigMC.hasThaumcraft()) {
			for (ChestType type : ChestType.values()) {
				MetalChests.registerModel(BlocksMC.METAL_HUNGRY_SORTING_CHEST, type.ordinal(), MetalChests.getVariantName(type));
			}

			MetalChests.registerTileRenderer(TileMetalHungrySortingChest.class, new RenderMetalSortingChest() {

				@Override
				protected ResourceLocation getActiveResource(ChestType type) {
					return new ResourceLocation(RegistryMC.ID, String.format("textures/entity/chest/hungry/%s.png", type.getName()));
				}

				@Override
				protected ResourceLocation getActiveOverlay(ChestType type) {
					return new ResourceLocation(RegistryMC.ID, String.format("textures/entity/chest/hungry/overlay/sorting_%s.png", type.getName()));
				}
			});
		}
	}

	private static void registerSortingChestRecipe(Block baseChest, Block metalChest, ChestType type, String name) {
		GameRegistry.addShapedRecipe(new ResourceLocation(RegistryMC.ID, String.format("recipe_%s_sorting_%s", type.getName(), name)), null,
				new ItemStack(metalChest, 1, type.ordinal()),
				" a ", "bcb", " d ",
				'a', Items.WRITABLE_BOOK,
				'b', Items.REDSTONE,
				'c', new ItemStack(baseChest, 1, type.ordinal()),
				'd', Blocks.HOPPER);
	}

	@Optional.Method(modid = RegistryMC.ID_RR2)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		UpgradeRegistry.registerChest(ModBlocks.sortingChest, BlocksMC.METAL_SORTING_CHEST);

		for (ChestType type : ChestType.values()) {
			if (type.isRegistered()) {
				ItemStack stack = new ItemStack(BlocksMC.METAL_SORTING_CHEST, 1, type.ordinal());
				OreDictionary.registerOre("chest", stack);
				OreDictionary.registerOre(String.format("chestSorting%s", WordUtils.capitalize(type.getName())), stack);
				registerSortingChestRecipe(BlocksMC.METAL_CHEST, BlocksMC.METAL_SORTING_CHEST, type, "chest");

				if (ConfigMC.hasThaumcraft()) {
					registerSortingChestRecipe(BlocksMC.METAL_HUNGRY_CHEST, BlocksMC.METAL_HUNGRY_SORTING_CHEST, type, "hungry_chest");
				}
			}
		}
	}

	@Optional.Method(modid = RegistryMC.ID_RR2)
	@SubscribeEvent
	public static void onRightClick(RightClickBlock event) {
		World world = event.getWorld();

		if (world.isRemote) {
			return;
		}

		BlockPos pos = event.getPos();
		TileEntity te = world.getTileEntity(pos);

		if (te == null) {
			return;
		}

		EntityPlayer player = event.getEntityPlayer();
		ItemStack stack = event.getItemStack();

		if (player.isSneaking() && te instanceof TileMetalChest && stack.getItem() instanceof ItemSortingUpgrade) {
			te.updateContainingBlockInfo();

			TileMetalChest oldChest = (TileMetalChest) te;
			TileMetalChest newChest = te instanceof TileMetalHungryChest ? new TileMetalHungrySortingChest(oldChest.getChestType()) : new TileMetalSortingChest(oldChest.getChestType());
			Block chestBlock = newChest instanceof TileMetalHungrySortingChest ? BlocksMC.METAL_HUNGRY_SORTING_CHEST : BlocksMC.METAL_SORTING_CHEST;

			world.removeTileEntity(pos);
			world.setBlockToAir(pos);
			world.setTileEntity(pos, newChest);

			IBlockState state = chestBlock.getDefaultState().withProperty(IMetalChest.VARIANT, newChest.getChestType());
			world.setBlockState(pos, state, 3);
			world.notifyBlockUpdate(pos, state, state, 3);

			TileEntity tile = world.getTileEntity(pos);

			if (tile instanceof TileMetalChest) {
				TileMetalChest chest = (TileMetalChest) tile;
				chest.setInventory(oldChest.getInventory());
				chest.setFront(oldChest.getFront());
			}
		}
	}
}
