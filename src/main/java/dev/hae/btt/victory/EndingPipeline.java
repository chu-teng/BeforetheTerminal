package dev.hae.btt.victory;

import dev.hae.btt.victory.Endings;

import dev.hae.btt.api.event.BttEvents;
import net.minecraft.server.world.ServerWorld;














public final class EndingPipeline {
    private EndingPipeline() {}


    public static Endings.Ending resolve(ServerWorld world, Endings.Ending decided) {
        var out = rewrite(world, decided);
        return allow(world, out) ? out : Endings.Ending.NONE;
    }

    public static Endings.Ending rewrite(ServerWorld world, Endings.Ending decided) {
        if (decided == null || decided == Endings.Ending.NONE) return Endings.Ending.NONE;
        Endings.Ending rewritten = BttEvents.EndingRewrite.EVENT.invoker().rewrite(world, decided);
        return rewritten == null ? decided : rewritten;
    }

    public static boolean allow(ServerWorld world, Endings.Ending ending) {
        return ending == Endings.Ending.NONE || BttEvents.EndingVeto.EVENT.invoker().allow(world, ending);
    }
}
