package jackiecrazy.cloakanddagger.entity;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CnDEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CloakAndDagger.MODID);

    public static final RegistryObject<EntityType<DecoyEntity>> DECOY = ENTITIES.register("decoy", () -> EntityType.Builder
            .of(DecoyEntity::new, MobCategory.MISC)
            .sized(0F, 0F)
            .build("decoy"));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
