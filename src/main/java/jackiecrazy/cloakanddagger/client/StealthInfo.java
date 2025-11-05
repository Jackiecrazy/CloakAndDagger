package jackiecrazy.cloakanddagger.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jackiecrazy.cloakanddagger.api.Awareness;
import jackiecrazy.cloakanddagger.capability.vision.SenseData;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class StealthInfo {
    private static final StealthInfo welp = new StealthInfo(Awareness.ALERT, 1, 1);
    private static final Cache<LivingEntity, StealthInfo> cache = CacheBuilder.newBuilder().weakKeys().expireAfterWrite(1, TimeUnit.SECONDS).build();

    public double getRange() {
        return range;
    }

    public double getDetect() {
        return detect;
    }

    public Awareness getAwareness() {
        return awareness;
    }

    private double range;
    private double detect;
    public Awareness awareness;

    public StealthInfo(Awareness awareness, double range, double detect) {
        this.range = range;
        this.detect = detect;
        this.awareness = awareness;
    }

    static StealthInfo stealthInfo(LivingEntity at) {
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return welp;
        try {
            return cache.get(at, () -> {
                Awareness a = StealthOverride.INSTANCE.getAwareness(player, at);
                double mult = player.getVisibilityPercent(at);
                return new StealthInfo(a, mult * SenseData.getCap(at).visionRange(), SenseData.getCap(at).getDetectionPerc(player));
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new StealthInfo(Awareness.ALERT, 1d, SenseData.getCap(at).getDetectionPerc(player));
    }
}
