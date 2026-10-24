package dev.hae.btt.component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import dev.hae.btt.BeforeTheTerminal;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.hae.btt.game.GameEvents;
import dev.hae.btt.role.Roles;
import dev.hae.btt.mechanic.DelayedAbility;












public class PlayerState implements AutoSyncedComponent {
    public static final ComponentKey<PlayerState> KEY =
            ComponentRegistry.getOrCreate(Identifier.of(BeforeTheTerminal.MOD_ID, "player"), PlayerState.class);

    private final PlayerEntity player;
    public NbtCompound advancementRound = new NbtCompound();


    public transient int observedInstinctColor = -1;
    public transient int observedPassiveColor = -1;
    public transient String observedCohort = "";
    public transient String observedGroup = "";
    public transient String observedVoters = "";
    public transient String observedGuideRole = "";
    public transient String observedInspectRole = "";
    public transient boolean observedLookout;

    public int voodooPhase = 0;
    public String voodooOwner = "";
    public long voodooUntil = 0;
    public String angelTarget = "";
    public int angelShields = 0;
    public transient boolean angelSacrifice = false;
    public String cageMembers = "";
    public int cagePhase = 0;
    public long cageAt = 0;
    public String cageOwner = "";
    public long cageDeadline = 0;
    public boolean cageChooseDeath = false;
    public boolean cageForfeit = false;



    public boolean messiahFollower = false;

    public boolean doused = false;



    public int priestKnifeUses = 0;

    public int witchUses = 0;

    public int hasKilled = 0;

    public int novelistHits = 0;

    public int novelistRequired = 0;


    public int hunterShot = 0;

    public int drunkTicks = 0;
    public boolean bttSlowFalling = false;
    public boolean bttNightVision = false;
    public String philosopherSources = "";

    public String partner = "";

    public String relType = "";

    public int muteTicks = 0;

    public int partyUses = 0;




    public int voicePitchTicks = 0;

    public String delayedKind = "";

    public String delayedTarget = "";

    public int delayedTicks = 0;

    public String delayedKind2 = "";
    public String delayedTarget2 = "";
    public int delayedTicks2 = 0;

    public String markedTarget = "";

    public boolean bombPlaced = false;

    public boolean bombBeeping = false;

    public int bombTimer = 0;

    public int beepTimer = 0;

    public int bombTransferCd = 0;

    public int bombLastSec = -1;

    public String bombSource = "";

    public int engineerScanTicks = 0;

    public int engineerCameraStatus = 0;

    public int thiefRevealTicks = 0;

    public boolean professorShield = false;

    public boolean lawyerShield = false;

    public boolean fakeDead = false;

    public boolean sculptorUsed = false;

    public String executionerTarget = "";

    public int executionerExecuted = 0;

    public int pipeTicks = 0;

    public boolean consoleOpen = false;

    private String consoleDimension = "";

    public void openConsole() {
        consoleDimension = player.getWorld().getRegistryKey().getValue().toString();
        consoleOpen = true;
        sync();
    }

    public void closeConsole() {
        if (!consoleOpen && consoleDimension.isEmpty()) return;
        consoleOpen = false;
        consoleDimension = "";
        sync();
    }

    public boolean hasConsoleSession() {
        return consoleOpen && consoleDimension.equals(
                player.getWorld().getRegistryKey().getValue().toString());
    }

    public boolean pipeHeld = false;


    public String redirectTarget = "";

    public String redirectOwner = "";


    public String astralPassenger = "";

    public String astralPassenger2 = "";

    public String astralKiller = "";


    public int coldWaveTicks = 0;




    public boolean majoRecruit = false;




    public boolean majoShield = true;

    public String swallowedBy = "";




    public String attachedTo = "";

    public String attachKind = "";

    public int digestTicks = 0;
    public float digestDrain = 0f;
    public boolean digestible = false;


    public void beginDigestion(String hostUuid, float mood, float drain, boolean digestible) {
        this.swallowedBy = hostUuid;
        this.swallowedMoodOk = mood > 0f;
        this.digestDrain = drain;
        this.digestible = digestible;
        this.digestTicks = (!digestible || drain <= 0f) ? 0 : Math.max(1, Math.round(mood / drain));
        this.sync();
    }

    public void clearDigestion() {
        this.swallowedBy = "";
        this.digestTicks = 0;
        this.digestDrain = 0f;
        this.digestible = false;
        this.sync();
    }





    public String priorRole = "";

    public int priorRoleTicks = 0;







    public String initialRole = "";
    public net.minecraft.item.ItemStack craftItem = net.minecraft.item.ItemStack.EMPTY;
    public long craftReadyAt = 0;
    public String initialWeapon = "";
    public String weaponCamp = "PASSENGER";
    public boolean epilogueCrowbarGiven = false;
    public boolean colonelFeigning = false;

    public boolean swallowedMoodOk = false;

    public String impMark = "";





    public String guardTarget = "";

    public String guardedBy = "";

    public int guardedTicks = 0;

    public String parasiteHost = "";





    public boolean blackdeathPatient = false;




    public String blackdeathHost = "";

    public String kins = "";

    public String werewolfOwner = "";
    public boolean werewolfAlly = false;
    public String objectiveVictims = "";
    public String branchKins = "";
    public boolean holdingFireAxe = false;
    public String drunkKins = "";
    public String drunkBranchKins = "";

    public String campOverride = "";




    public boolean deified = false;

    public boolean soloObjectiveComplete = false;
    public boolean memorySolved = false;
    public long memoryNextGuess = 0;
    public int gamblerTarget = 0;
    public String gamblerGuesses = "";
    public String gamblerAttempted = "";
    public long goonNextChange = 0;
    public String joinedCampRole = "";

    public String deifiedBy = "";





    public String moodOverride = "";

    public int tenaciousTicks = 0;

    public boolean tenaciousUsed = false;

    public String tenaciousReason = "";
    public String tenaciousKiller = "";
    public String jesterExecutioner = "";






    public String mediumRetain = "";

    public int actionTaskProgress = 0;


    public String passageDim = "";
    public int passageX = 0;
    public int passageY = 0;
    public int passageZ = 0;

    public int passageTicks = 0;






    public boolean virginHonored = false;

    public String starMark = "";




    public int starGlowTicks = 0;


    public int correctExecutions = 0;

    public boolean killedPrincipal = false;

    public boolean killedOutsider = false;


    public String decoyRole = "";

    public String folkTarget = "";

    public int folkTicks = 0;

    public boolean projecting = false;

    public int gasolineHintTicks = 0;

    public double bodyX = 0;
    public double bodyY = 0;
    public double bodyZ = 0;


    public double roomX = 0;
    public double roomY = 0;
    public double roomZ = 0;

    public boolean aviateurUsed = false;
    public String smugglerHost = "";
    public long smugglerReleaseAt = 0;
    public long traitTrustAt = 0;
    public long openingNoticeAt = 0;
    public String openingNoticeRole = "";
    public String openingNoticeLabel = "";
    public int monkShields = 0;
    public boolean coldWaveSanity = false;
    public boolean afterlifeShieldUsed = false;

    public long speakingUntil = 0;

    public PlayerState(PlayerEntity player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        NbtCompound saved = new NbtCompound();
        writeToNbt(saved, buf.getRegistryManager());
        saved.putInt("engineerCameraStatus", engineerCameraStatus);
        saved.remove("philosopherSources");
        var ownerView = dev.hae.btt.game.SeatAssignment.isBttMode(player.getWorld())
                && player instanceof ServerPlayerEntity owner ? PlayerStateSync.forOwner(saved, owner) : saved;
        var view = player.getUuid().equals(recipient.getUuid()) ? ownerView
                : player instanceof ServerPlayerEntity target ? PlayerStateSync.forPlayer(saved, target, recipient)
                : PlayerStateSync.forTracking(saved);
        buf.writeNbt(view);
    }


    public boolean isMessiahFollower() {
        return messiahFollower;
    }

    public void setMessiahFollower(boolean value) {
        this.messiahFollower = value;
        this.sync();

        if (value && this.player instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
            dev.hae.btt.trait.relationship.Relationships.onCampChanged(sp);
        }
    }


    public boolean isMajoRecruit() {
        return majoRecruit;
    }



    public boolean applyLawyerShield() {
        if (lawyerShield) return false;
        lawyerShield = true;
        sync();
        return true;
    }


    public void consumeLawyerShield() {
        lawyerShield = false;
        sync();
    }


    public void joinMajo() {
        this.majoRecruit = true;
        setCampOverride(Roles.CAMP_OUTSIDER);
    }







    public void setCampOverride(String camp) {
        String value = camp == null ? "" : camp;
        if (value.equals(this.campOverride)) return;
        if (this.player instanceof net.minecraft.server.network.ServerPlayerEntity owner) dev.hae.btt.advancement.BttAdvancements.changedCamp(owner);
        this.campOverride = value;
        this.sync();
        if (this.player instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
            dev.hae.btt.trait.relationship.Relationships.onCampChanged(sp);
        }
    }


    public boolean isDrunk() {
        return drunkTicks > 0 || !philosopherSources.isEmpty();
    }


    public void applyDrunk(int ticks) {
        boolean was = this.drunkTicks > 0;
        this.drunkTicks = Math.max(ticks, this.drunkTicks);
        dev.hae.btt.role.Drunk.interruptToggleAbilities(player);

        if (!was && this.drunkTicks > 0) {
            sync();
        }
    }


    public void applySourceDrunk(net.minecraft.server.network.ServerPlayerEntity source) {
        if (!dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(source)) return;
        var sources = new java.util.LinkedHashSet<>(java.util.Arrays.asList(philosopherSources.split(",")));
        sources.remove(""); sources.add(source.getUuidAsString());
        philosopherSources = String.join(",", sources);
        dev.hae.btt.role.Drunk.interruptToggleAbilities(player);
        sync();
    }

    public void decrementDrunk() {
        if (!player.getWorld().isClient && !philosopherSources.isEmpty()) {
            var world = (net.minecraft.server.world.ServerWorld) player.getWorld();
            var sources = new java.util.ArrayList<String>();
            for (String id : philosopherSources.split(",")) {
                try {
                    var source = world.getPlayerByUuid(java.util.UUID.fromString(id));
                    if (source == null || dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(source)) sources.add(id);
                } catch (IllegalArgumentException ignored) {}
            }
            String remaining = String.join(",", sources);
            if (!remaining.equals(philosopherSources)) { philosopherSources = remaining; sync(); }
        }
        if (isDrunk()) dev.hae.btt.role.Drunk.interruptToggleAbilities(player);
        if (this.drunkTicks <= 0) return;
        if (--this.drunkTicks <= 0) sync();
    }





    public void applyVoicePitch(int ticks) {
        this.voicePitchTicks = Math.max(this.voicePitchTicks, Math.max(0, ticks));
        this.sync();
    }


    public void decrementVoicePitch() {
        if (this.voicePitchTicks <= 0) return;
        this.voicePitchTicks--;
        if (this.voicePitchTicks == 0 || this.voicePitchTicks % 10 == 0) this.sync();
    }





    public void applyPermanentDrunk() {
        this.drunkTicks = Integer.MAX_VALUE / 4;
        dev.hae.btt.role.Drunk.interruptToggleAbilities(player);
        this.sync();
    }


    public boolean isMuted() {
        return muteTicks > 0 || cageDeadline > player.getWorld().getTime();
    }


    public void applyMute(int ticks) {
        this.muteTicks = Math.max(ticks, this.muteTicks);
        this.sync();
    }


    public void decrementMute() {
        if (this.muteTicks > 0) this.muteTicks--;
    }





    public boolean isDoused() {
        return this.doused;
    }


    public void setDoused() {
        this.doused = true;
        this.sync();
    }


    public void scheduleGasolineHint(int ticks) {
        this.gasolineHintTicks = ticks;
    }



    public boolean hasProfessorShield() {
        return this.professorShield;
    }


    public boolean applyProfessorShield() {
        if (this.professorShield) return false;
        this.professorShield = true;
        return true;
    }


    public void consumeProfessorShield() {
        this.professorShield = false;
    }



    public boolean hasPriorRole() {
        return !this.priorRole.isEmpty();
    }


    public void setPriorRole(String roleId, int ticks) {
        this.priorRole = roleId;
        this.priorRoleTicks = Math.max(0, ticks);
        this.sync();
    }

    public void clearPriorRole() {
        this.priorRole = "";
        this.priorRoleTicks = 0;
        this.sync();
    }





    public boolean isAttached() {
        return !this.attachedTo.isEmpty();
    }

    public void setAttached(String hostUuid, String kind) {
        this.attachedTo = hostUuid;
        this.attachKind = kind == null ? "" : kind;
        this.sync();
    }

    public void clearAttached() {
        this.attachedTo = "";
        this.attachKind = "";
        this.sync();
    }

    public boolean isSwallowed() {
        return !this.swallowedBy.isEmpty();
    }


    public void setSwallowedBy(String kidnapperUuid) {
        this.swallowedBy = kidnapperUuid;
        this.sync();
    }


    public void clearSwallowed() {
        this.swallowedBy = "";
        this.sync();
    }



    public boolean isFakeDead() {
        return this.fakeDead;
    }


    public void setFakeDead(boolean value) {
        this.fakeDead = value;
        this.sync();
    }




    public void setThiefReveal(int ticks) {
        this.thiefRevealTicks = ticks;
        this.sync();
    }

    public boolean isProjecting() {
        return this.projecting;
    }


    public void startProjecting(double x, double y, double z) {
       this.bodyX = x;
       this.bodyY = y;
       this.bodyZ = z;
        this.projecting = true;
        this.sync();
    }


    public void stopProjecting() {
        this.projecting = false;
        this.sync();
    }


    public void reset() {
        observedInstinctColor = -1;
        observedPassiveColor = -1;
        observedCohort = "";
        observedGroup = "";
        observedVoters = "";
        observedGuideRole = "";
        observedInspectRole = "";
        observedLookout = false;
        dev.hae.btt.trait.TraitStatusEffects.clear(player);
        bttSlowFalling = false;
        bttNightVision = false;
        voodooPhase = 0; voodooOwner = ""; voodooUntil = 0;
        angelTarget = ""; angelShields = 0; angelSacrifice = false;
        cageMembers = ""; cagePhase = 0; cageAt = 0;
        cageOwner = ""; cageDeadline = 0; cageChooseDeath = false; cageForfeit = false;
        messiahFollower = false;

        roomX = 0;
        roomY = 0;
        roomZ = 0;
        aviateurUsed = false;
        smugglerHost = ""; smugglerReleaseAt = 0;
        traitTrustAt = 0;
        openingNoticeAt = 0; openingNoticeRole = ""; openingNoticeLabel = "";
        monkShields = 0; coldWaveSanity = false;
        afterlifeShieldUsed = false;
        speakingUntil = 0;
        doused = false;
        priestKnifeUses = 0;
        witchUses = 0;
        hasKilled = 0;
        novelistHits = 0;
        novelistRequired = 0;
        hunterShot = 0;
        advancementRound = new NbtCompound();
        drunkTicks = 0;
        philosopherSources = "";
        muteTicks = 0;
        partner = "";
        relType = "";
        partyUses = 0;
        voicePitchTicks = 0;
        delayedKind = "";
        delayedTarget = "";
        delayedTicks = 0;
        delayedKind2 = "";
        delayedTarget2 = "";
        delayedTicks2 = 0;
        markedTarget = "";
        bombPlaced = false;
        bombBeeping = false;
        bombTimer = 0;
        beepTimer = 0;
        bombTransferCd = 0;
        bombLastSec = -1;
        bombSource = "";
        engineerScanTicks = 0;
        engineerCameraStatus = 0;
        thiefRevealTicks = 0;
        professorShield = false;
        lawyerShield = false;
        fakeDead = false;
        sculptorUsed = false;
        executionerTarget = "";
        executionerExecuted = 0;
        pipeTicks = 0;
        pipeHeld = false;
        consoleOpen = false;
        consoleDimension = "";
        redirectTarget = "";
        redirectOwner = "";
        astralPassenger = "";
        astralPassenger2 = "";
        astralKiller = "";
        coldWaveTicks = 0;
        majoRecruit = false;
        majoShield = true;
        blackdeathPatient = false;
        blackdeathHost = "";
        swallowedBy = "";
        attachedTo = "";
        attachKind = "";
        digestTicks = 0;
        digestDrain = 0f;
        digestible = false;
        priorRole = "";
        priorRoleTicks = 0;
        initialRole = "";
        craftItem = net.minecraft.item.ItemStack.EMPTY;
        craftReadyAt = 0;
        initialWeapon = "";
        weaponCamp = "PASSENGER";
        epilogueCrowbarGiven = false;
        colonelFeigning = false;
        impMark = "";
        guardTarget = "";
        guardedBy = "";
        guardedTicks = 0;
        parasiteHost = "";
        holdingFireAxe = false;
        kins = "";
        werewolfOwner = ""; werewolfAlly = false; objectiveVictims = "";
        branchKins = ""; drunkKins = ""; drunkBranchKins = "";
        campOverride = "";
        deified = false;
        soloObjectiveComplete = false;
        memorySolved = false;
        memoryNextGuess = 0;
        gamblerTarget = 0;
        gamblerGuesses = "";
        gamblerAttempted = "";
        goonNextChange = 0;
        joinedCampRole = "";
        deifiedBy = "";
        moodOverride = "";
        tenaciousTicks = 0;
        tenaciousUsed = false;
        tenaciousReason = "";
        tenaciousKiller = "";
        jesterExecutioner = "";
        mediumRetain = "";
        actionTaskProgress = 0;
        passageDim = "";
        passageTicks = 0;
        correctExecutions = 0;
        virginHonored = false;
        starMark = "";
        starGlowTicks = 0;
        killedPrincipal = false;
        killedOutsider = false;
        decoyRole = "";
        folkTarget = "";
        folkTicks = 0;
        swallowedMoodOk = false;
        projecting = false;
        gasolineHintTicks = 0;
        bodyX = 0;
        bodyY = 0;
        bodyZ = 0;
        this.sync();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putBoolean("aviateurUsed", aviateurUsed);
        tag.putString("smugglerHost", smugglerHost);
        tag.putLong("smugglerReleaseAt", smugglerReleaseAt);
        tag.putBoolean("bttSlowFalling", bttSlowFalling);
        tag.putBoolean("bttNightVision", bttNightVision);
        tag.putInt("voodooPhase", voodooPhase); tag.putString("voodooOwner", voodooOwner); tag.putLong("voodooUntil", voodooUntil);
        tag.putString("angelTarget", angelTarget);
        tag.putInt("angelShields", angelShields);
        tag.putString("cageMembers", cageMembers);
        tag.putInt("cagePhase", cagePhase);
        tag.putLong("cageAt", cageAt);
        tag.putString("cageOwner", cageOwner);
        tag.putLong("cageDeadline", cageDeadline);
        tag.putBoolean("cageChooseDeath", cageChooseDeath);
        tag.putBoolean("cageForfeit", cageForfeit);
        tag.putBoolean("messiahFollower", messiahFollower);
        tag.putBoolean("doused", doused);
        tag.putInt("priestKnifeUses", priestKnifeUses);
        tag.putInt("witchUses", witchUses);
        tag.putInt("hasKilled", hasKilled);
        tag.putInt("novelistHits", novelistHits);
        tag.putInt("hunterShot", hunterShot);
        tag.put("advancementRound", advancementRound.copy());
        tag.putInt("drunkTicks", drunkTicks);
        tag.putString("philosopherSources", philosopherSources);
        tag.putInt("muteTicks", muteTicks);
        tag.putString("partner", partner);
        tag.putString("relType", relType);
        tag.putInt("partyUses", partyUses);
        tag.putInt("voicePitchTicks", voicePitchTicks);
        tag.putString("delayedKind", delayedKind);
        tag.putString("delayedTarget", delayedTarget);
        tag.putInt("delayedTicks", delayedTicks);
        tag.putString("delayedKind2", delayedKind2);
        tag.putString("delayedTarget2", delayedTarget2);
        tag.putInt("delayedTicks2", delayedTicks2);
        tag.putString("markedTarget", markedTarget);
        tag.putBoolean("bombPlaced", bombPlaced);
        tag.putBoolean("bombBeeping", bombBeeping);
        tag.putInt("bombTimer", bombTimer);
        tag.putInt("beepTimer", beepTimer);
        tag.putInt("bombTransferCd", bombTransferCd);
        tag.putInt("bombLastSec", bombLastSec);
        tag.putString("bombSource", bombSource);
        tag.putInt("engineerScanTicks", engineerScanTicks);
        tag.putInt("thiefRevealTicks", thiefRevealTicks);
        tag.putBoolean("professorShield", professorShield);
        tag.putBoolean("lawyerShield", lawyerShield);
        tag.putBoolean("fakeDead", fakeDead);
        tag.putBoolean("sculptorUsed", sculptorUsed);
        tag.putString("executionerTarget", executionerTarget);
        tag.putInt("executionerExecuted", executionerExecuted);
        tag.putInt("pipeTicks", pipeTicks);
        tag.putBoolean("pipeHeld", pipeHeld);
        tag.putBoolean("consoleOpen", consoleOpen);
        tag.putString("redirectTarget", redirectTarget);
        tag.putString("redirectOwner", redirectOwner);
        tag.putString("astralPassenger", astralPassenger);
        tag.putString("astralPassenger2", astralPassenger2);
        tag.putString("astralKiller", astralKiller);
        tag.putInt("coldWaveTicks", coldWaveTicks);
        tag.putBoolean("majoRecruit", majoRecruit);
        tag.putBoolean("majoShield", majoShield);
        tag.putBoolean("blackdeathPatient", blackdeathPatient);
        tag.putString("blackdeathHost", blackdeathHost);
        tag.putString("swallowedBy", swallowedBy);
        tag.putString("attachedTo", attachedTo);
        tag.putString("attachKind", attachKind);
        tag.putInt("digestTicks", digestTicks);
        tag.putFloat("digestDrain", digestDrain);
        tag.putBoolean("digestible", digestible);
        tag.putString("priorRole", priorRole);
        tag.putInt("priorRoleTicks", priorRoleTicks);
        tag.putString("initialRole", initialRole);
        tag.put("craftItem", craftItem.encodeAllowEmpty(registryLookup));
        tag.putLong("craftReadyAt", craftReadyAt);
        tag.putString("initialWeapon", initialWeapon);
        tag.putString("weaponCamp", weaponCamp);
        tag.putBoolean("epilogueCrowbarGiven", epilogueCrowbarGiven);
        tag.putBoolean("colonelFeigning", colonelFeigning);
        tag.putString("impMark", impMark);
        tag.putString("guardTarget", guardTarget);
        tag.putString("guardedBy", guardedBy);
        tag.putInt("guardedTicks", guardedTicks);
        tag.putString("parasiteHost", parasiteHost);
        tag.putString("kins", kins);
        tag.putString("werewolfOwner", werewolfOwner);
        tag.putString("objectiveVictims", objectiveVictims);
        tag.putString("branchKins", branchKins);
        tag.putString("drunkKins", drunkKins);
        tag.putString("drunkBranchKins", drunkBranchKins);
        tag.putString("campOverride", campOverride);
        tag.putBoolean("deified", deified);
        tag.putBoolean("soloObjectiveComplete", soloObjectiveComplete);
        tag.putBoolean("memorySolved", memorySolved);
        tag.putLong("memoryNextGuess", memoryNextGuess);
        tag.putInt("gamblerTarget", gamblerTarget);
        tag.putString("gamblerGuesses", gamblerGuesses);
        tag.putString("gamblerAttempted", gamblerAttempted);
        tag.putLong("goonNextChange", goonNextChange);
        tag.putString("joinedCampRole", joinedCampRole);
        tag.putString("deifiedBy", deifiedBy);
        tag.putString("moodOverride", moodOverride);
        tag.putInt("tenaciousTicks", tenaciousTicks);
        tag.putBoolean("tenaciousUsed", tenaciousUsed);
        tag.putString("tenaciousReason", tenaciousReason);
        tag.putString("tenaciousKiller", tenaciousKiller);
        tag.putString("jesterExecutioner", jesterExecutioner);
        tag.putString("mediumRetain", mediumRetain);
        tag.putLong("traitTrustAt", traitTrustAt);
        tag.putLong("openingNoticeAt", openingNoticeAt);
        tag.putString("openingNoticeRole", openingNoticeRole);
        tag.putString("openingNoticeLabel", openingNoticeLabel);
        tag.putInt("monkShields", monkShields);
        tag.putBoolean("coldWaveSanity", coldWaveSanity);
        tag.putBoolean("afterlifeShieldUsed", afterlifeShieldUsed);
        tag.putInt("actionTaskProgress", actionTaskProgress);
        tag.putDouble("roomX", roomX);
        tag.putDouble("roomY", roomY);
        tag.putDouble("roomZ", roomZ);
        tag.putString("passageDim", passageDim);
        tag.putInt("passageX", passageX);
        tag.putInt("passageY", passageY);
        tag.putInt("passageZ", passageZ);
        tag.putInt("passageTicks", passageTicks);
        tag.putInt("correctExecutions", correctExecutions);
        tag.putBoolean("virginHonored", virginHonored);
        tag.putString("starMark", starMark);
        tag.putInt("starGlowTicks", starGlowTicks);
        tag.putBoolean("killedPrincipal", killedPrincipal);
        tag.putBoolean("killedOutsider", killedOutsider);
        tag.putString("decoyRole", decoyRole);
        tag.putString("folkTarget", folkTarget);
        tag.putInt("folkTicks", folkTicks);
        tag.putBoolean("swallowedMoodOk", swallowedMoodOk);
        tag.putBoolean("projecting", projecting);
        tag.putInt("gasolineHintTicks", gasolineHintTicks);
        tag.putDouble("bodyX", bodyX);
        tag.putDouble("bodyY", bodyY);
        tag.putDouble("bodyZ", bodyZ);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        aviateurUsed = tag.getBoolean("aviateurUsed");
        smugglerHost = tag.getString("smugglerHost");
        smugglerReleaseAt = tag.getLong("smugglerReleaseAt");
        observedInstinctColor = tag.contains("observedInstinctColor") ? tag.getInt("observedInstinctColor") : -1;
        observedPassiveColor = tag.contains("observedPassiveColor") ? tag.getInt("observedPassiveColor") : -1;
        observedCohort = tag.getString("observedCohort");
        observedGroup = tag.getString("observedGroup");
        observedVoters = tag.getString("observedVoters");
        observedGuideRole = tag.getString("observedGuideRole");
        observedInspectRole = tag.getString("observedInspectRole");
        observedLookout = tag.getBoolean("observedLookout");
        bttSlowFalling = tag.getBoolean("bttSlowFalling");
        bttNightVision = tag.getBoolean("bttNightVision");
        voodooPhase = tag.getInt("voodooPhase"); voodooOwner = tag.getString("voodooOwner"); voodooUntil = tag.getLong("voodooUntil");
        angelTarget = tag.getString("angelTarget"); angelShields = tag.getInt("angelShields");
        cageMembers = tag.getString("cageMembers"); cagePhase = tag.getInt("cagePhase"); cageAt = tag.getLong("cageAt");
        cageOwner = tag.getString("cageOwner"); cageDeadline = tag.getLong("cageDeadline"); cageChooseDeath = tag.getBoolean("cageChooseDeath"); cageForfeit = tag.getBoolean("cageForfeit");
        this.messiahFollower = tag.contains("messiahFollower") && tag.getBoolean("messiahFollower");
        this.doused = tag.contains("doused") && tag.getBoolean("doused");
        this.priestKnifeUses = tag.getInt("priestKnifeUses");
        this.witchUses = tag.getInt("witchUses");
        this.hasKilled = tag.getInt("hasKilled");
        this.novelistHits = tag.getInt("novelistHits");
        this.novelistRequired = tag.getInt("novelistRequired");
        this.hunterShot = tag.getInt("hunterShot");
        advancementRound = tag.getCompound("advancementRound").copy();
        this.drunkTicks = tag.getInt("drunkTicks");
        this.philosopherSources = tag.getString("philosopherSources");
        this.muteTicks = tag.getInt("muteTicks");
        this.partner = tag.contains("partner") ? tag.getString("partner") : "";
        this.relType = tag.contains("relType") ? tag.getString("relType") : "";
        this.partyUses = tag.getInt("partyUses");
        this.voicePitchTicks = tag.getInt("voicePitchTicks");
        this.delayedKind = tag.contains("delayedKind") ? tag.getString("delayedKind") : "";
        this.delayedTarget = tag.contains("delayedTarget") ? tag.getString("delayedTarget") : "";
        this.delayedTicks = tag.getInt("delayedTicks");
        this.delayedKind2 = tag.contains("delayedKind2") ? tag.getString("delayedKind2") : "";
        this.delayedTarget2 = tag.contains("delayedTarget2") ? tag.getString("delayedTarget2") : "";
        this.delayedTicks2 = tag.getInt("delayedTicks2");
        this.markedTarget = tag.contains("markedTarget") ? tag.getString("markedTarget") : "";
        this.bombPlaced = tag.contains("bombPlaced") && tag.getBoolean("bombPlaced");
        this.bombBeeping = tag.contains("bombBeeping") && tag.getBoolean("bombBeeping");
        this.bombTimer = tag.getInt("bombTimer");
        this.beepTimer = tag.getInt("beepTimer");
        this.bombTransferCd = tag.getInt("bombTransferCd");
        this.bombLastSec = tag.getInt("bombLastSec");
        this.bombSource = tag.contains("bombSource") ? tag.getString("bombSource") : "";
        this.engineerScanTicks = tag.getInt("engineerScanTicks");
        this.engineerCameraStatus = player.getWorld().isClient() ? tag.getInt("engineerCameraStatus") : 0;
        this.thiefRevealTicks = tag.getInt("thiefRevealTicks");
        this.professorShield = tag.contains("professorShield") && tag.getBoolean("professorShield");
        this.lawyerShield = tag.contains("lawyerShield") && tag.getBoolean("lawyerShield");
        this.fakeDead = tag.contains("fakeDead") && tag.getBoolean("fakeDead");
        this.sculptorUsed = tag.contains("sculptorUsed") && tag.getBoolean("sculptorUsed");
        this.executionerTarget = tag.contains("executionerTarget") ? tag.getString("executionerTarget") : "";
        this.executionerExecuted = tag.getInt("executionerExecuted");
        this.pipeTicks = tag.getInt("pipeTicks");
        this.pipeHeld = tag.contains("pipeHeld") && tag.getBoolean("pipeHeld");
        this.consoleOpen = tag.contains("consoleOpen") && tag.getBoolean("consoleOpen");
        this.redirectTarget = tag.contains("redirectTarget") ? tag.getString("redirectTarget") : "";
        this.redirectOwner = tag.contains("redirectOwner") ? tag.getString("redirectOwner") : "";
        this.astralPassenger = tag.contains("astralPassenger") ? tag.getString("astralPassenger") : "";
        this.astralPassenger2 = tag.contains("astralPassenger2") ? tag.getString("astralPassenger2") : "";
        this.astralKiller = tag.contains("astralKiller") ? tag.getString("astralKiller") : "";
        this.coldWaveTicks = tag.getInt("coldWaveTicks");
        this.majoRecruit = tag.contains("majoRecruit") && tag.getBoolean("majoRecruit");
        this.majoShield = !tag.contains("majoShield") || tag.getBoolean("majoShield");
        this.blackdeathPatient = tag.contains("blackdeathPatient") && tag.getBoolean("blackdeathPatient");
        this.blackdeathHost = tag.contains("blackdeathHost") ? tag.getString("blackdeathHost") : "";
        this.swallowedBy = tag.contains("swallowedBy") ? tag.getString("swallowedBy") : "";
        this.attachedTo = tag.contains("attachedTo") ? tag.getString("attachedTo") : "";
        this.attachKind = tag.contains("attachKind") ? tag.getString("attachKind") : "";
        this.digestTicks = tag.getInt("digestTicks");
        this.digestDrain = tag.getFloat("digestDrain");
        this.digestible = tag.contains("digestible") && tag.getBoolean("digestible");
        this.priorRole = tag.contains("priorRole") ? tag.getString("priorRole") : "";
        this.priorRoleTicks = tag.getInt("priorRoleTicks");
        this.initialRole = tag.contains("initialRole") ? tag.getString("initialRole") : "";
        this.craftItem = net.minecraft.item.ItemStack.fromNbtOrEmpty(registryLookup, tag.getCompound("craftItem"));
        this.craftReadyAt = tag.getLong("craftReadyAt");
        this.initialWeapon = tag.getString("initialWeapon");
        this.weaponCamp = tag.contains("weaponCamp") ? tag.getString("weaponCamp") : "PASSENGER";
        this.epilogueCrowbarGiven = tag.getBoolean("epilogueCrowbarGiven");
        this.colonelFeigning = tag.getBoolean("colonelFeigning");
        this.impMark = tag.contains("impMark") ? tag.getString("impMark") : "";
        this.guardTarget = tag.contains("guardTarget") ? tag.getString("guardTarget") : "";
        this.guardedBy = tag.contains("guardedBy") ? tag.getString("guardedBy") : "";
        this.guardedTicks = tag.getInt("guardedTicks");
        this.parasiteHost = tag.contains("parasiteHost") ? tag.getString("parasiteHost") : "";
        this.kins = tag.contains("kins") ? tag.getString("kins") : "";
        this.werewolfOwner = tag.getString("werewolfOwner");
        this.werewolfAlly = tag.getBoolean("werewolfAlly");
        this.objectiveVictims = tag.getString("objectiveVictims");
        this.branchKins = tag.getString("branchKins");
        this.drunkKins = tag.getString("drunkKins");
        this.drunkBranchKins = tag.getString("drunkBranchKins");
        this.campOverride = tag.contains("campOverride") ? tag.getString("campOverride") : "";
        this.deified = tag.contains("deified") && tag.getBoolean("deified");
        this.soloObjectiveComplete = tag.getBoolean("soloObjectiveComplete");
        this.memorySolved = tag.getBoolean("memorySolved");
        this.memoryNextGuess = tag.getLong("memoryNextGuess");
        gamblerTarget = tag.getInt("gamblerTarget");
        gamblerGuesses = tag.getString("gamblerGuesses");
        gamblerAttempted = tag.getString("gamblerAttempted");
        goonNextChange = tag.getLong("goonNextChange");
        joinedCampRole = tag.getString("joinedCampRole");
        this.deifiedBy = tag.contains("deifiedBy") ? tag.getString("deifiedBy") : "";
        this.moodOverride = tag.contains("moodOverride") ? tag.getString("moodOverride") : "";
        this.tenaciousTicks = tag.getInt("tenaciousTicks");
        this.tenaciousUsed = tag.contains("tenaciousUsed") && tag.getBoolean("tenaciousUsed");
        this.tenaciousReason = tag.contains("tenaciousReason") ? tag.getString("tenaciousReason") : "";
        this.tenaciousKiller = tag.getString("tenaciousKiller");
        this.jesterExecutioner = tag.getString("jesterExecutioner");
        this.mediumRetain = tag.contains("mediumRetain") ? tag.getString("mediumRetain") : "";
        traitTrustAt = tag.getLong("traitTrustAt");
        openingNoticeAt = tag.getLong("openingNoticeAt");
        openingNoticeRole = tag.getString("openingNoticeRole");
        openingNoticeLabel = tag.getString("openingNoticeLabel");
        monkShields = tag.getInt("monkShields");
        coldWaveSanity = tag.getBoolean("coldWaveSanity");
        afterlifeShieldUsed = tag.getBoolean("afterlifeShieldUsed");
        speakingUntil = 0;
        this.actionTaskProgress = tag.getInt("actionTaskProgress");
        this.roomX = tag.getDouble("roomX");
        this.roomY = tag.getDouble("roomY");
        this.roomZ = tag.getDouble("roomZ");
        this.passageDim = tag.contains("passageDim") ? tag.getString("passageDim") : "";
        this.passageX = tag.getInt("passageX");
        this.passageY = tag.getInt("passageY");
        this.passageZ = tag.getInt("passageZ");
        this.passageTicks = tag.getInt("passageTicks");
        this.correctExecutions = tag.getInt("correctExecutions");
        this.virginHonored = tag.contains("virginHonored") && tag.getBoolean("virginHonored");
        this.starMark = tag.contains("starMark") ? tag.getString("starMark") : "";
        this.starGlowTicks = tag.getInt("starGlowTicks");
        this.killedPrincipal = tag.contains("killedPrincipal") && tag.getBoolean("killedPrincipal");
        this.killedOutsider = tag.contains("killedOutsider") && tag.getBoolean("killedOutsider");
        this.decoyRole = tag.contains("decoyRole") ? tag.getString("decoyRole") : "";
        this.folkTarget = tag.contains("folkTarget") ? tag.getString("folkTarget") : "";
        this.folkTicks = tag.getInt("folkTicks");
        this.swallowedMoodOk = tag.contains("swallowedMoodOk") && tag.getBoolean("swallowedMoodOk");
        this.projecting = tag.contains("projecting") && tag.getBoolean("projecting");
        this.gasolineHintTicks = tag.getInt("gasolineHintTicks");
        this.bodyX = tag.contains("bodyX") ? tag.getDouble("bodyX") : 0;
        this.bodyY = tag.contains("bodyY") ? tag.getDouble("bodyY") : 0;
        this.bodyZ = tag.contains("bodyZ") ? tag.getDouble("bodyZ") : 0;
    }
}
