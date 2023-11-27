package jackiecrazy.cloakanddagger.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jackiecrazy.cloakanddagger.capability.vision.SenseData;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import jackiecrazy.footwork.utils.StealthUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class StealthInfo {
    private static final StealthInfo welp = new StealthInfo(StealthUtils.Awareness.ALERT, 1, 1);
    private static final Cache<LivingEntity, StealthInfo> cache = CacheBuilder.newBuilder().weakKeys().expireAfterWrite(1, TimeUnit.SECONDS).build();

    public double getRange() {
        return range;
    }

    public double getDetect() {
        return detect;
    }

    public StealthUtils.Awareness getAwareness() {
        return awareness;
    }

    private double range;
    private double detect;
    public StealthUtils.Awareness awareness;

    public StealthInfo(StealthUtils.Awareness awareness, double range, double detect) {
        this.range = range;
        this.detect = detect;
        this.awareness = awareness;
    }

    static StealthInfo stealthInfo(LivingEntity at) {
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return welp;
        try {
            return cache.get(at, () -> {
                StealthOverride.Awareness a = StealthUtils.INSTANCE.getAwareness(player, at);
                double mult = player.getVisibilityPercent(at);
                return new StealthInfo(a, mult * SenseData.getCap(at).visionRange(), SenseData.getCap(at).getDetectionPerc(player));
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new StealthInfo(StealthOverride.Awareness.ALERT, 1d, SenseData.getCap(at).getDetectionPerc(player));
    }
}
