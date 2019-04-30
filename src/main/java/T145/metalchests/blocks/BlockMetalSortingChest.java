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
package T145.metalchests.blocks;

import javax.annotation.Nullable;

import T145.metalchests.api.chests.IMetalChest;
import T145.metalchests.api.immutable.RegistryMC;
import T145.metalchests.tiles.TileMetalSortingChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockMetalSortingChest extends BlockMetalChest {

	@Override
	protected void registerResource() {
		this.registerResource(RegistryMC.RESOURCE_METAL_SORTING_CHEST);
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileMetalSortingChest(state.getValue(IMetalChest.VARIANT));
	}
}
