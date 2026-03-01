package com.tagtart.solstick.datagen;

import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SOLStick.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.LUNCH_BAG.get());
    }
}
