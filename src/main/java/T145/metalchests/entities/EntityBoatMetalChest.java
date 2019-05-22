/*******************************************************************************
 * Copyright 2019 T145
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
package T145.metalchests.entities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import T145.metalchests.api.BlocksMC;
import T145.metalchests.api.chests.ChestAnimator;
import T145.metalchests.api.chests.IMetalChest;
import T145.metalchests.api.constants.ChestType;
import T145.metalchests.api.constants.ChestUpgrade;
import T145.metalchests.api.constants.RegistryMC;
import T145.metalchests.blocks.BlockMetalChest;
import T145.metalchests.core.MetalChests;
import T145.metalchests.items.ItemChestUpgrade;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class EntityBoatMetalChest extends EntityBoat implements IMetalChest {

	public ChestAnimator animator;

	private static final DataParameter<ChestType> CHEST_TYPE = EntityDataManager.<ChestType>createKey(EntityBoatMetalChest.class, MetalChests.CHEST_TYPE);
	private static final DataParameter<Byte> ENCHANT_LEVEL = EntityDataManager.createKey(EntityBoatMetalChest.class, DataSerializers.BYTE);
	private final ItemStackHandler inventory = new ItemStackHandler(getChestType().getInventorySize());

	public EntityBoatMetalChest(World world) {
		super(world);
		this.animator = new ChestAnimator(this);
	}

	public EntityBoatMetalChest(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityBoatMetalChest(EntityBoat boat) {
		this(boat.getEntityWorld(), boat.prevPosX, boat.prevPosY, boat.prevPosZ);
		this.posX = boat.posX;
		this.posY = boat.posY;
		this.posZ = boat.posZ;
		this.motionX = boat.motionX;
		this.motionY = boat.motionY;
		this.motionZ = boat.motionZ;
		this.rotationPitch = boat.rotationPitch;
		this.rotationYaw = boat.rotationYaw;
		this.setBoatType(boat.getBoatType());
	}

	@Override
	public ItemStackHandler getInventory() {
		return inventory;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return !this.isDead && player.getDistanceSq(this) <= 64.0D;
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public ChestAnimator getChestAnimator() {
		return animator;
	}

	@Override
	public ChestType getChestType() {
		return dataManager.get(CHEST_TYPE);
	}

	@Override
	public void setChestType(ChestType type) {
		dataManager.set(CHEST_TYPE, type);
	}

	@Override
	public EnumFacing getFront() {
		return EnumFacing.SOUTH;
	}

	@Override
	public void setFront(EnumFacing front) {}

	@Override
	public boolean isTrapped() {
		return false;
	}

	@Override
	public void setTrapped(boolean trapped) {}

	@Override
	public boolean isLuminous() {
		return false;
	}

	@Override
	public void setLuminous(boolean luminous) {}

	@Override
	public byte getEnchantLevel() {
		return this.dataManager.get(ENCHANT_LEVEL);
	}

	@Override
	public void setEnchantLevel(byte enchantLevel) {
		this.dataManager.set(ENCHANT_LEVEL, enchantLevel);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(CHEST_TYPE, ChestType.OBSIDIAN);
		dataManager.register(ENCHANT_LEVEL, (byte) 0);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setString(TAG_CHEST_TYPE, getChestType().toString());
		//tag.setTag(TAG_INVENTORY, inventory.serializeNBT());
		this.writeInventoryTag(tag);
		tag.setByte(TAG_ENCHANT_LEVEL, this.getEnchantLevel());
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		this.setChestType(ChestType.valueOf(tag.getString(TAG_CHEST_TYPE)));
		//inventory.deserializeNBT(tag.getCompoundTag(TAG_INVENTORY));
		this.readInventoryTag(tag);
		this.setEnchantLevel(tag.getByte(TAG_ENCHANT_LEVEL));
	}

	@Override
	public String getName() {
		if (hasCustomName()) {
			return getCustomNameTag();
		} else {
			return I18n.format(String.format("metalchests:%s_boat_metal_chest.%s.name", getBoatType().getName(), getChestType().getName()));
		}
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Override
	protected boolean canFitPassenger(@Nonnull Entity passenger) {
		return false;
	}

	@Nullable
	public Entity getControllingPassenger() {
		return null;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source)) {
			return false;
		} else if (!this.world.isRemote && !this.isDead) {
			if (source instanceof EntityDamageSourceIndirect && source.getTrueSource() != null && this.isPassenger(source.getTrueSource())) {
				return false;
			} else {
				this.setForwardDirection(-this.getForwardDirection());
				this.setTimeSinceHit(10);
				this.setDamageTaken(this.getDamageTaken() + amount * 10.0F);
				this.markVelocityChanged();
				boolean flag = source.getTrueSource() instanceof EntityPlayer && ((EntityPlayer) source.getTrueSource()).capabilities.isCreativeMode;

				if (flag || this.getDamageTaken() > 40.0F) {
					if (!flag && this.world.getGameRules().getBoolean("doEntityDrops")) {
						this.dropItemWithOffset(this.getItemBoat(), 1, 0.0F);

						BlockMetalChest.dropItems(this, world, getPosition());
						entityDropItem(BlockMetalChest.getDropStack(this, BlocksMC.METAL_CHEST), 0.0F);
					}

					this.setDead();
				}

				return true;
			}
		} else {
			return true;
		}
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
		if (world.isRemote) {
			return true;
		}

		ItemStack stack = player.getHeldItem(hand);

		if (stack.getItem() instanceof ItemChestUpgrade) {
			ChestUpgrade upgrade = ChestUpgrade.byMetadata(stack.getItemDamage());

			if (getChestType() == upgrade.getBase()) {
				setChestType(upgrade.getUpgrade());

				if (!player.capabilities.isCreativeMode) {
					stack.shrink(1);
				}

				player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 0.4F, 0.8F);
				return true;
			}
		}

		player.openGui(RegistryMC.ID, hashCode(), world, 0, 0, 0);
		return true;
	}
}
