package com.tagtart.solstick.datagen;

import com.tagtart.solstick.SOLStick;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SOLStick.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // This is where to datagen models for mod items.
    }
}
