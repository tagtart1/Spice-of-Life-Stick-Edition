package com.tagtart.solstick.datagen;

import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.sound.ModSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class ModSoundProvider extends SoundDefinitionsProvider {

    public ModSoundProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SOLStick.MODID, existingFileHelper);
    }

    @Override
    public void registerSounds() {
        add(ModSounds.LUNCH_BAG_OPEN, SoundDefinition.definition()
                .with(
                        sound("solstick:lunch_bag_open_1"),
                        sound("solstick:lunch_bag_open_2")
                )
                .subtitle("sound.solstick.lunch_bag_open")
        );

        add(ModSounds.LUNCH_BAG_CLOSE, SoundDefinition.definition()
                .with(
                        sound("solstick:lunch_bag_close_1"),
                        sound("solstick:lunch_bag_close_2")
                )
                .subtitle("sound.solstick.lunch_bag_close")
        );

        add(ModSounds.LUNCH_BAG_INSERT, SoundDefinition.definition()
                .with(
                        sound("solstick:lunch_bag_insert_1"),
                        sound("solstick:lunch_bag_insert_2"),
                        sound("solstick:lunch_bag_insert_3")
                )
                .subtitle("sound.solstick.lunch_bag_insert")
        );

        add(ModSounds.LUNCH_BAG_OUTPUT, SoundDefinition.definition()
                .with(
                        sound("solstick:lunch_bag_output_1"),
                        sound("solstick:lunch_bag_output_2")
                )
                .subtitle("sound.solstick.lunch_bag_output")
        );
    }
}
