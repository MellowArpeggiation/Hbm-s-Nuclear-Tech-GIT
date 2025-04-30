package com.hbm.entity.mob;

import net.minecraft.world.World;

public class EntityScrapFish extends EntityFish implements IEntityEnumMulti {

    public enum ScrapFish {
        STEEL,
        ALUMINIUM,
        ISOTOPE,
        CADMIUM,
        TECH,
        BLOOD,
        HORROR,
    }

    public ScrapFish type;

    public EntityScrapFish(World world) {
        super(world, 0.8, 4.0F);

        type = ScrapFish.values()[world.rand.nextInt(ScrapFish.values().length)];
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enum getEnum() {
        return type;
    }

}
