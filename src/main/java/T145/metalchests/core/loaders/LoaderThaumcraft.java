/*******************************************************************************
 * Copyright 2018 T145
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
package T145.metalchests.core.loaders;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.text.WordUtils;

import T145.metalchests.api.BlocksMC;
import T145.metalchests.api.ItemsMC;
import T145.metalchests.api.chests.IInventoryHandler;
import T145.metalchests.api.chests.IMetalChest;
import T145.metalchests.api.chests.UpgradeRegistry;
import T145.metalchests.api.immutable.ChestType;
import T145.metalchests.api.immutable.ChestUpgrade;
import T145.metalchests.api.immutable.ModSupport;
import T145.metalchests.api.immutable.RegistryMC;
import T145.metalchests.blocks.BlockMetalChest;
import T145.metalchests.blocks.BlockSortingMetalChest;
import T145.metalchests.client.render.blocks.RenderMetalChest;
import T145.metalchests.items.ItemChestUpgrade;
import T145.metalchests.tiles.TileHungryMetalChest;
import T145.metalchests.tiles.TileSortingHungryMetalChest;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.crafting.ShapedArcaneRecipe;

class LoaderThaumcraft {

	@EventBusSubscriber(modid = RegistryMC.MOD_ID)
	static class ServerLoader {

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

			if (te instanceof IInventoryHandler && entity instanceof EntityItem && !entity.isDead) {
				IInventoryHandler chest = (IInventoryHandler) te;
				EntityItem item = (EntityItem) entity;
				ItemStack stack = item.getItem();
				ItemStack leftovers = tryToInsertStack(chest.getInventory(), stack);

				if (leftovers == null || leftovers.getCount() != stack.getCount()) {
					entity.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.25F, (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
					world.addBlockEvent(pos, receiver, 2, 2);
				}

				if (leftovers != null) {
					item.setItem(leftovers);
				} else {
					entity.setDead();
				}
			}
		}

		@Optional.Method(modid = ModSupport.Thaumcraft.MOD_ID)
		@SubscribeEvent
		public static void registerBlocks(final RegistryEvent.Register<Block> event) {
			if (ModSupport.hasThaumcraft()) {
				final IForgeRegistry<Block> registry = event.getRegistry();

				registry.register(BlocksMC.HUNGRY_METAL_CHEST = new BlockMetalChest() {

					@Override
					protected void registerResource() {
						this.registerResource(RegistryMC.RESOURCE_HUNGRY_METAL_CHEST);
					}

					@Nullable
					@Override
					public TileEntity createTileEntity(World world, IBlockState state) {
						return new TileHungryMetalChest(state.getValue(IMetalChest.VARIANT));
					}

					@Override
					public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
						tryToEatItem(world, pos, state, entity, this);
					}
				});

				LoaderMod.registerTileEntity(TileHungryMetalChest.class);

				if (ModSupport.hasRefinedRelocation()) {
					registry.register(BlocksMC.SORTING_HUNGRY_METAL_CHEST = new BlockSortingMetalChest() {

						protected void registerResource() {
							this.registerResource(RegistryMC.RESOURCE_SORTING_HUNGRY_METAL_CHEST);
						}

						@Nullable
						@Override
						public TileEntity createTileEntity(World world, IBlockState state) {
							return new TileSortingHungryMetalChest(state.getValue(IMetalChest.VARIANT));
						}

						@Override
						public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
							tryToEatItem(world, pos, state, entity, this);
						}
					});

					LoaderMod.registerTileEntity(TileSortingHungryMetalChest.class);
				}
			}
		}

		@Optional.Method(modid = ModSupport.Thaumcraft.MOD_ID)
		@SubscribeEvent
		public static void registerItems(final RegistryEvent.Register<Item> event) {
			if (ModSupport.hasThaumcraft()) {
				final IForgeRegistry<Item> registry = event.getRegistry();

				LoaderMod.registerItemBlock(registry, BlocksMC.HUNGRY_METAL_CHEST, ChestType.class);

				registry.register(ItemsMC.HUNGRY_CHEST_UPGRADE = new ItemChestUpgrade(RegistryMC.RESOURCE_HUNGRY_CHEST_UPGRADE));
				UpgradeRegistry.registerChest(RegistryMC.RESOURCE_HUNGRY_CHEST_UPGRADE, BlocksTC.hungryChest, BlocksMC.HUNGRY_METAL_CHEST);

				if (ModSupport.hasRefinedRelocation()) {
					LoaderMod.registerItemBlock(registry, BlocksMC.SORTING_HUNGRY_METAL_CHEST, ChestType.class);
				}
			}
		}

		@Optional.Method(modid = ModSupport.Thaumcraft.MOD_ID)
		@SubscribeEvent(priority = EventPriority.HIGHEST)
		public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
			if (ModSupport.hasThaumcraft()) {
				for (ChestType type : ChestType.values()) {
					if (type.isRegistered()) {
						String capitalizedName = WordUtils.capitalize(type.getName());
						ItemStack chestStack = new ItemStack(BlocksMC.HUNGRY_METAL_CHEST, 1, type.ordinal());

						OreDictionary.registerOre("chest", chestStack);
						OreDictionary.registerOre("chestHungry" + capitalizedName, chestStack);

						if (ModSupport.hasRefinedRelocation()) {
							chestStack = new ItemStack(BlocksMC.SORTING_HUNGRY_METAL_CHEST, 1, type.ordinal());

							OreDictionary.registerOre("chest", chestStack);
							OreDictionary.registerOre("chestSortingHungry" + capitalizedName, chestStack);
						}
					}
				}

				if (ChestType.COPPER.isRegistered()) {
					ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(RegistryMC.MOD_ID, "HungryCopperChest"), new ShapedArcaneRecipe(ModSupport.Thaumcraft.DEFAULT_GROUP, "HUNGRYMETALCHESTS", 15, new AspectList().add(Aspect.EARTH, 1).add(Aspect.WATER, 1), new ItemStack(BlocksMC.HUNGRY_METAL_CHEST, 1, 0), "III", "ICI", "III", 'I', "ingotCopper", 'C', new ItemStack(BlocksTC.hungryChest)));
					ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(RegistryMC.MOD_ID, "HungryIronChest"), new ShapedArcaneRecipe(ModSupport.Thaumcraft.DEFAULT_GROUP, "HUNGRYMETALCHESTS", 15, new AspectList().add(Aspect.EARTH, 1).add(Aspect.WATER, 1), new ItemStack(BlocksMC.HUNGRY_METAL_CHEST, 1, 1), "III", "ICI", "III", 'I', "ingotIron", 'C', new ItemStack(BlocksMC.HUNGRY_METAL_CHEST, 1, 0)));
				} else {
					ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(RegistryMC.MOD_ID, "HungryIronChest"), new ShapedArcaneRecipe(ModSupport.Thaumcraft.DEFAULT_GROUP, "HUNGRYMETALCHESTS", 15, new AspectList().add(Aspect.EARTH, 1).add(Aspect.WATER, 1), new ItemStack(BlocksMC.HUNGRY_METAL_CHEST, 1, 1), "III", "ICI", "III", 'I', "ingotIron", 'C', new ItemStack(BlocksTC.hungryChest)));
				}

				if (ChestType.SILVER.isRegistered()) {
					ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(RegistryMC.MOD_ID, "HungrySilverChest"), new ShapedArcaneRecipe(ModSupport.Thaumcraft.DEFAULT_GROUP, "HUNGRYMETALCHESTS", 15, new AspectList().add(Aspect.EARTH, 1).add(Aspect.WATER, 1), new ItemStack(BlocksMC.HUNGRY_METAL_CHEST, 1, 2), "III", "ICI", "III", 'I', "ingotSilver", 'C', new ItemStack(BlocksMC.HUNGRY_METAL_CHEST, 1, 1)));
					ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(RegistryMC.MOD_ID, "HungryGoldChest"), new ShapedArcaneRecipe(ModSupport.Thaumcraft.DEFAULT_GROUP, "HUNGRYMETALCHESTS", 15, new AspectList().add(Aspect.EARTH, 1).add(Aspect.WATER, 1), new ItemStack(BlocksMC.HUNGRY_METAL_CHEST, 1, 3), "III", "ICI", "III", 'I', "ingotGold", 'C', new ItemStack(BlocksMC.HUNGRY_METAL_CHEST, 1, 2)));
				} else {
					ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(RegistryMC.MOD_ID, "HungryGoldChest"), new ShapedArcaneRecipe(ModSupport.Thaumcraft.DEFAULT_GROUP, "HUNGRYMETALCHESTS", 15, new AspectList().add(Aspect.EARTH, 1).add(Aspect.WATER, 1), new ItemStack(BlocksMC.HUNGRY_METAL_CHEST, 1, 3), "III", "ICI", "III", 'I', "ingotGold", 'C', new ItemStack(BlocksMC.HUNGRY_METAL_CHEST, 1, 1)));
				}

				ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(RegistryMC.MOD_ID, "HungryDiamondChest"), new ShapedArcaneRecipe(ModSupport.Thaumcraft.DEFAULT_GROUP, "HUNGRYMETALCHESTS", 15, new AspectList().add(Aspect.EARTH, 1).add(Aspect.WATER, 1), new ItemStack(BlocksMC.HUNGRY_METAL_CHEST, 1, 4), "III", "ICI", "III", 'I', "gemDiamond", 'C', new ItemStack(BlocksMC.HUNGRY_METAL_CHEST, 1, 3)));
				ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(RegistryMC.MOD_ID, "HungryObsidianChest"), new ShapedArcaneRecipe(ModSupport.Thaumcraft.DEFAULT_GROUP, "HUNGRYMETALCHESTS", 15, new AspectList().add(Aspect.EARTH, 1).add(Aspect.WATER, 1), new ItemStack(BlocksMC.HUNGRY_METAL_CHEST, 1, 5), "III", "ICI", "III", 'I', "obsidian", 'C', new ItemStack(BlocksMC.HUNGRY_METAL_CHEST, 1, 4)));

				for (ChestUpgrade upgrade : ChestUpgrade.values()) {
					ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(RegistryMC.MOD_ID, upgrade.getName() + "_hungry_chest_upgrade"), new ShapedArcaneRecipe(ModSupport.Thaumcraft.DEFAULT_GROUP, "HUNGRYMETALCHESTSUPGRADES", 15, new AspectList().add(Aspect.EARTH, 1).add(Aspect.WATER, 1), new ItemStack(ItemsMC.HUNGRY_CHEST_UPGRADE, 1, upgrade.ordinal()), "III", "III", "CII", 'I', upgrade.getUpgrade().getOreName(), 'C', LoaderMod.getUpgradeBase(BlocksTC.plankGreatwood, ItemsMC.HUNGRY_CHEST_UPGRADE, upgrade)));
				}
			}
		}
	}

	@EventBusSubscriber(modid = RegistryMC.MOD_ID, value = Side.CLIENT)
	static class ClientLoader {

		@Optional.Method(modid = ModSupport.Thaumcraft.MOD_ID)
		@SubscribeEvent
		public static void onModelRegistration(ModelRegistryEvent event) {
			if (ModSupport.hasThaumcraft()) {
				for (ChestType type : ChestType.values()) {
					LoaderMod.registerModel(BlocksMC.HUNGRY_METAL_CHEST, type.ordinal(), LoaderMod.getVariantName(type));
				}

				LoaderMod.registerTileRenderer(TileHungryMetalChest.class, new RenderMetalChest() {

					@Override
					protected ResourceLocation getActiveResource(ChestType type) {
						return new ResourceLocation(RegistryMC.MOD_ID, "textures/entity/chest/hungry/" + type.getName() + ".png");
					}
				});

				for (ChestUpgrade type : ChestUpgrade.values()) {
					LoaderMod.registerModel(ItemsMC.HUNGRY_CHEST_UPGRADE, "item_hungry_chest_upgrade", type.ordinal(), "item=" + type.getName());
				}
			}
		}
	}
}
