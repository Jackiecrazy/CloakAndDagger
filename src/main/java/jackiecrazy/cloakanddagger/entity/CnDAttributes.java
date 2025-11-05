package jackiecrazy.cloakanddagger.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CnDAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, "cloakanddagger");

    public static final RegistryObject<Attribute> STEALTH = ATTRIBUTES.register(
        "stealth",
        () -> new RangedAttribute("attribute.name.cloakanddagger.stealth", 0.0D, -1024, 1024) {
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
