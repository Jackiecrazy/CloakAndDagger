package com.flarelabsmc.missinginaction.entity;

import com.flarelabsmc.missinginaction.MissingInAction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class MiAAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, MissingInAction.MODID);

    public static final RegistryObject<Attribute> STEALTH = ATTRIBUTES.register(
        "stealth",
        () -> new RangedAttribute("attribute.name.missinginaction.stealth", 0.0D, -1024, 1024) {
            @Override
            public boolean isClientSyncable() {
                return true;
            }
        }
    );

    public static void registerAttributes(IEventBus bus) {
        ATTRIBUTES.register(bus);
    }
}
