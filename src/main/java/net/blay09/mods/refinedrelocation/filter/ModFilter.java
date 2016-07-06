package net.blay09.mods.refinedrelocation.filter;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.blay09.mods.refinedrelocation.RefinedRelocation;
import net.blay09.mods.refinedrelocation.api.TileOrMultipart;
import net.blay09.mods.refinedrelocation.api.client.IFilterIcon;
import net.blay09.mods.refinedrelocation.api.filter.IChecklistFilter;
import net.blay09.mods.refinedrelocation.client.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.Set;

public class ModFilter implements IChecklistFilter {

	public static final String ID = RefinedRelocation.MOD_ID + ":ModFilter";

	private static class ModWithName {
		private final int index;
		private final String id;
		private final String name;

		public ModWithName(int index, String id, String name) {
			this.index = index;
			this.id = id;
			this.name = name;
		}

		public int getIndex() {
			return index;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}
	}

	private static final Map<String, ModWithName> modList = Maps.newHashMap();
	public static String[] modIds = new String[0];

	public static void gatherMods() {
		Set<String> modSet = Sets.newHashSet();
		for(ResourceLocation registryName : Block.REGISTRY.getKeys()) {
			modSet.add(registryName.getResourceDomain());
		}
		// TODO sort alphabetically
		setModList(modSet.toArray(new String[modSet.size()]));
	}

	public static void setModList(String[] modIDs) {
		ModFilter.modIds = modIDs;
		Map<String, ModContainer> mods = Loader.instance().getIndexedModList();
		modList.clear();
		for(int i = 0; i < modIDs.length; i++) {
			if(modIDs[i].equals("minecraft")) {
				modList.put(modIDs[i], new ModWithName(i, modIDs[i], "Minecraft"));
			} else {
				ModContainer modContainer = mods.get(modIDs[i]);
				modList.put(modIDs[i], new ModWithName(i, modIDs[i], modContainer != null ? modContainer.getName() : modIDs[i]));
			}
		}
	}

	private final boolean[] modStates = new boolean[modIds.length];

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	public boolean isFilterUsable(TileOrMultipart tileEntity) {
		return true;
	}

	@Override
	public boolean passes(TileOrMultipart tileEntity, ItemStack itemStack) {
		ModWithName modWithName = modList.get(itemStack.getItem().getRegistryName().getResourceDomain());
		return modWithName != null && modStates[modWithName.getIndex()];
	}

	@Override
	public NBTBase serializeNBT() {
		NBTTagList list = new NBTTagList();
		for(int i = 0; i < modStates.length; i++) {
			if(modStates[i]) {
				list.appendTag(new NBTTagString(modIds[i]));
			}
		}
		return list;
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		NBTTagList list = (NBTTagList) nbt;
		for(int i = 0; i < list.tagCount(); i++) {
			String modId = list.getStringTagAt(i);
			for(int j = 0; j < modIds.length; j++) {
				if(modIds[j].equals(modId)) {
					modStates[j] = true;
				}
			}
		}
	}

	@Override
	public String getLangKey() {
		return "filter.refinedrelocation:ModFilter";
	}

	@Override
	public String getDescriptionLangKey() {
		return "filter.refinedrelocation:ModFilter.description";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IFilterIcon getFilterIcon() {
		return ClientProxy.TEXTURE_ATLAS.getSprite("refinedrelocation:icon_ModFilter");
	}

	@Override
	public String getOptionLangKey(int option) {
		return modList.get(modIds[option]).getName();
	}

	@Override
	public void setOptionChecked(int option, boolean checked) {
		modStates[option] = checked;
	}

	@Override
	public boolean isOptionChecked(int option) {
		return modStates[option];
	}

	@Override
	public int getOptionCount() {
		return modIds.length;
	}

}
