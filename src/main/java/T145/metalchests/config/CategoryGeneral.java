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
package T145.metalchests.config;

import net.minecraftforge.common.config.Config;

public class CategoryGeneral {

	@Config.Comment("Whether or not you want to recieve an in-game notification if an update is available.")
	public boolean checkForUpdates = true;

	@Config.Comment("Whether or not all metal chest models are like the vanilla chest; black in the middle.\n* Hollow textures contributed by phyne")
	public boolean hollowModelTextures;

	@Config.Comment("Whether or not you want to enable the Minecarts with Metal Chests.")
	@Config.RequiresMcRestart
	public boolean enableMinecarts = true;

	@Config.Comment("If Thaumcraft is installed, whether or not you want to enable the Metal Hungry Chests.")
	@Config.RequiresMcRestart
	public boolean enableMetalHungryChests = true;

	@Config.Comment("If Refined Relocation 2 is installed, whether or not you want to enable the Metal Hungry Sorting Chests.")
	@Config.RequiresMcRestart
	public boolean enableMetalHungrySortingChests = true;
}
