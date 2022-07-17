package net.orcinus.galosphere.items;

import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.Item;
import net.orcinus.galosphere.Galosphere;

public class CTHorseArmorItem extends HorseArmorItem {

    public CTHorseArmorItem(int protection, String name) {
        super(protection, name, new Item.Properties().stacksTo(1).tab(Galosphere.GALOSPHERE));
    }

}
