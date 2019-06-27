/*******************************************************************************
 * Copyright 2018-2019 T145
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package T145.metalchests.compat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.text.WordUtils;

import T145.metalchests.api.chests.IMetalChest;
import T145.metalchests.api.chests.UpgradeRegistry;
import T145.metalchests.api.config.ConfigMC;
import T145.metalchests.api.consts.ChestType;
import T145.metalchests.api.consts.ChestUpgrade;
import T145.metalchests.api.consts.RegistryMC;
import T145.metalchests.api.obj.BlocksMC;
import T145.metalchests.api.obj.ItemsMC;
import T145.metalchests.blocks.BlockMetalChest;
import T145.metalchests.blocks.BlockMetalChestItem;
import T145.metalchests.client.render.blocks.RenderMetalChest;
import T145.metalchests.client.render.blocks.RenderMetalSortingChest;
import T145.metalchests.items.ItemChestUpgrade;
import T145.metalchests.tiles.TileMetalHungryChest;
import T145.metalchests.tiles.TileMetalSortingHungryChest;
import T145.tbone.core.TBone;
import T145.tbone.lib.ChestAnimator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.IForgeRegistry;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.crafting.ShapedArcaneRecipe;

@EventBusSubscriber(modid = RegistryMC.ID)
class CompatThaumcraft {

	private CompatThaumcraft() {}

	private static ItemStack tryToInsertStack(IItemHandler inv, @Nonnull ItemStack stack) {
		for (int slot = 0; slot < inv.getSlots(); ++slot) {
			if (!inv.getStackInSlot(slot).isEmpty()) {
				stack = inv.insertItem(slot, stack, false);

				if (stack.isEmpty()) {
					return ItemStack.EMPTY;
				}
			}
		}

		for (int slot = 0; slot < inv.getSlots(); ++slot) {
			stack = inv.insertItem(slot, stack, false);

			if (stack.isEmpty()) {
				return ItemStack.EMPTY;
			}
		}

		return stack;
	}

	private static void tryToEatItem(World world, BlockPos pos, IBlockState state, Entity entity, Block receiver) {
		TileEntity te = world.getTileEntity(pos);

		if (te instanceof IMetalChest && entity instanceof EntityItem && !entity.isDead) {
			IMetalChest chest = (IMetalChest) te;
			EntityItem item = (EntityItem) entity;
			ItemStack stack = item.getItem();
			ItemStack leftovers = tryToInsertStack(chest.getInventory(), stack);

			if (leftovers == null || leftovers.getCount() != stack.getCount()) {
				entity.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.25F, (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
				world.addBlockEvent(pos, receiver, ChestAnimator.EVENT_CHEST_NOM, 2);
			}

			if (leftovers != null) {
				item.setItem(leftovers);
			} else {
				entity.setDead();
			}
		}
	}

	@Optional.Method(modid = RegistryMC.ID_THAUMCRAFT)
	@SubscribeEvent
	public static void registerBlocks(final RegistryEvent.Register<Block> event) {
		final IForgeRegistry<Block> registry = event.getRegistry();

		registry.register(BlocksMC.METAL_HUNGRY_CHEST = new BlockMetalChest() {

			@Override
			protected void registerResource() {
				this.registerResource(RegistryMC.RESOURCE_METAL_HUNGRY_CHEST);
			}

			@Nullable
			@Override
			public TileEntity createTileEntity(World world, IBlockState state) {
				return new TileMetalHungryChest(state.getValue(IMetalChest.VARIANT));
			}

			@Override
			public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
				tryToEatItem(world, pos, state, entity, this);
			}
		});

		TBone.registerTileEntity(TileMetalHungryChest.class, RegistryMC.ID);

		if (ConfigMC.hasRefinedRelocation()) {
			registry.register(BlocksMC.METAL_SORTING_HUNGRY_CHEST = new BlockMetalChest() {

				protected void registerResource() {
					this.registerResource(RegistryMC.RESOURCE_METAL_SORTING_HUNGRY_CHEST);
				}

				@Nullable
				@Override
				public TileEntity createTileEntity(World world, IBlockState state) {
					return new TileMetalSortingHungryChest(state.getValue(IMetalChest.VARIANT));
				}

				@Override
				public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
					tryToEatItem(world, pos, state, entity, this);
				}
			});

			TBone.registerTileEntity(TileMetalSortingHungryChest.class, RegistryMC.ID);
		}
	}

	@Optional.Method(modid = RegistryMC.ID_THAUMCRAFT)
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();

		registry.register(new BlockMetalChestItem(ChestType.TIERS, BlocksMC.METAL_HUNGRY_CHEST));

		if (ConfigMC.hasRefinedRelocation()) {
			registry.register(new BlockMetalChestItem(ChestType.TIERS, BlocksMC.METAL_SORTING_HUNGRY_CHEST));
		}

		registry.register(ItemsMC.HUNGRY_CHEST_UPGRADE = new ItemChestUpgrade(RegistryMC.RESOURCE_HUNGRY_CHEST_UPGRADE));
	}

	@Optional.Method(modid = RegistryMC.ID_THAUMCRAFT)
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		for (ChestType type : ChestType.values()) {
			TBone.registerModel(RegistryMC.ID, BlocksMC.METAL_HUNGRY_CHEST, type.ordinal(), TBone.getVariantName(type));
		}

		for (short i = 0; i < ChestUpgrade.TIERS.size(); ++i) {
			ChestUpgrade type = ChestUpgrade.TIERS.get(i);
			TBone.registerModel(RegistryMC.ID, ItemsMC.HUNGRY_CHEST_UPGRADE, String.format("item_%s", RegistryMC.KEY_HUNGRY_CHEST_UPGRADE), i, String.format("item=%s", type.getName()));
		}

		TBone.registerTileRenderer(TileMetalHungryChest.class, new RenderMetalChest() {

			@Override
			protected ResourceLocation getActiveResource(ChestType type) {
				return new ResourceLocation(RegistryMC.ID, String.format("textures/entity/chest/hungry/%s.png", type.getName()));
			}
		});

		if (ConfigMC.hasRefinedRelocation()) {
			for (ChestType type : ChestType.values()) {
				TBone.registerModel(RegistryMC.ID, BlocksMC.METAL_SORTING_HUNGRY_CHEST, type.ordinal(), TBone.getVariantName(type));
			}

			TBone.registerTileRenderer(TileMetalSortingHungryChest.class, new RenderMetalSortingChest() {

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

	@Optional.Method(modid = RegistryMC.ID_THAUMCRAFT)
	private static void registerUpgradeRecipes(Item upgrade, Object base, String postfix) {
		for (short i = 0; i < ChestUpgrade.TIERS.size(); ++i) {
			ChestUpgrade type = ChestUpgrade.byMetadata(i);
			String recipeName = String.format("upgrade%s%s", WordUtils.capitalize(type.getName()), postfix);

//			ThaumcraftApi.addCrucibleRecipe(
//					RegistryMC.getResource(recipeName),
//					new CrucibleRecipe("HUNGRYCHEST",
//							new ItemStack(upgrade, 1, i),
//							new ItemStack(ItemsMC.CHEST_UPGRADE, 1, i), new AspectList().merge(Aspect.EARTH, 1).merge(Aspect.WATER, 1)));

			ThaumcraftApi.addArcaneCraftingRecipe(RegistryMC.getResource(recipeName),
					new ShapedArcaneRecipe(RegistryMC.RECIPE_GROUP, "HUNGRYCHEST", 15,
							new AspectList().add(Aspect.EARTH, 1).add(Aspect.WATER, 1),
							new ItemStack(upgrade, 1, i),
							"aaa", "aaa", "baa",
							'a', type.getUpgrade().getOreName(),
							'b', ChestUpgrade.getBaseIngredient(upgrade, base, i)));
		}
	}

	@Optional.Method(modid = RegistryMC.ID_THAUMCRAFT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		ChestType.registerRecipes(BlocksTC.hungryChest, BlocksMC.METAL_HUNGRY_CHEST, "Hungry");
		registerUpgradeRecipes(ItemsMC.HUNGRY_CHEST_UPGRADE, BlocksTC.plankGreatwood, "Hungry");
		UpgradeRegistry.register(ItemsMC.HUNGRY_CHEST_UPGRADE, BlocksTC.hungryChest, BlocksMC.METAL_HUNGRY_CHEST);

		if (ConfigMC.hasRefinedRelocation()) {
			ChestType.registerRecipes(BlocksMC.METAL_HUNGRY_CHEST, BlocksMC.METAL_SORTING_HUNGRY_CHEST, "SortingHungry");
		}
	}
}