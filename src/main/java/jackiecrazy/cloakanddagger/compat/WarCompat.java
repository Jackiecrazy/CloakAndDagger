package jackiecrazy.cloakanddagger.compat;

import net.minecraftforge.fml.ModList;

public class WarCompat {
    public static boolean elenaiDodge;

    public static void checkCompatStatus() {
        elenaiDodge = ModList.get().isLoaded("elenaidodge2") && GeneralConfig.elenai;
    }
}
