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
package t145.metalchests.api.consts;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.annotation.Nullable;

import org.apache.commons.lang3.text.WordUtils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.IStringSerializable;

public class ChestUpgrade implements IStringSerializable {

	public static final ObjectList<ChestUpgrade> TIERS = new ObjectArrayList<>();

	static {
		ChestType.TIERS.forEach(type -> TIERS.add(new ChestUpgrade(null, type)));

		Deque<ChestType> temp = new ArrayDeque<>(ChestType.TIERS);

		while (!temp.isEmpty()) {
			ChestType base = temp.remove();

			temp.forEach(upgrade -> {
				TIERS.add(new ChestUpgrade(base, upgrade));
			});
		}
	}

	private static int meta;
	private int ordinal;
	private final ChestType base;
	private final ChestType upgrade;

	private ChestUpgrade(@Nullable ChestType base, ChestType upgrade) {
		this.base = base;
		this.upgrade = upgrade;
		this.ordinal = meta++;
	}

	public int ordinal() {
		return ordinal;
	}

	@Nullable
	public ChestType getBase() {
		return base;
	}

	public ChestType getUpgrade() {
		return upgrade;
	}

	@Override
	public String getName() {
		String upgradeName = WordUtils.capitalize(upgrade.getName());
		return base == null ? String.format("wood%s", upgradeName) : String.format("%s%s", base.getName(), upgradeName);
	}

	public boolean isForWood() {
		return base == null;
	}

	public boolean isRegistered() {
		return base == null ? upgrade.isRegistered() : base.isRegistered() && upgrade.isRegistered();
	}
}