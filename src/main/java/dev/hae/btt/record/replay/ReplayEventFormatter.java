package dev.hae.btt.record.replay;

import dev.hae.btt.record.GameRecordEvent;
import dev.hae.btt.record.MatchRecord;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;





@FunctionalInterface
public interface ReplayEventFormatter {
    @Nullable
    Text format(GameRecordEvent event, MatchRecord match, ServerWorld world);
}
