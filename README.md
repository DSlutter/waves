public class InvasionManager {

    public static boolean invasionOnThisNight_Last = false;
    public static boolean isDayLast = false;

    public static boolean canPlayerSkipInvasion(EntityPlayer player) {
        World world = player.world;
        boolean skipped = player.getEntityData().getBoolean(DynamicDifficulty.dataPlayerInvasionSkipping);
        if (!skipped) {
            if (isInvasionTonight(world)) {
                //only allow skip during day before its actually active
                if (!CoroUtilWorldTime.isNightPadded(world)) {
                    int skipCount = player.getEntityData().getInteger(DynamicDifficulty.dataPlayerInvasionSkipCount);
                    if (ConfigInvasion.maxConsecutiveInvasionSkips == -1 || skipCount < ConfigInvasion.maxConsecutiveInvasionSkips) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean skipNextInvasionForPlayer(EntityPlayer player) {

        World world = player.world;
        boolean skipped = player.getEntityData().getBoolean(DynamicDifficulty.dataPlayerInvasionSkipping);
        if (!skipped) {
            if (isInvasionTonight(world)) {
                //only allow skip during day before its actually active
                if (!CoroUtilWorldTime.isNightPadded(world)) {
                    int skipCount = player.getEntityData().getInteger(DynamicDifficulty.dataPlayerInvasionSkipCount);
                    if (ConfigInvasion.maxConsecutiveInvasionSkips == -1 || skipCount < ConfigInvasion.maxConsecutiveInvasionSkips) {
                        skipCount++;
                        player.getEntityData().setBoolean(DynamicDifficulty.dataPlayerInvasionSkipping, true);
                        player.getEntityData().setInteger(DynamicDifficulty.dataPlayerInvasionSkipCount, skipCount);

                        int skipCountAllTime = player.getEntityData().getInteger(DynamicDifficulty.dataPlayerInvasionSkipCountForMultiplier) + 1;
                        player.getEntityData().setInteger(DynamicDifficulty.dataPlayerInvasionSkipCountForMultiplier, skipCountAllTime);

                        player.sendMessage(new TextComponentString(String.format(ConfigInvasion.Invasion_Message_skipping, skipCount)));
                        return true;
                    } else {
                        player.sendMessage(new TextComponentString(String.format(ConfigInvasion.Invasion_Message_skippedTooMany, ConfigInvasion.maxConsecutiveInvasionSkips)));
                    }

                } else {
                    player.sendMessage(new TextComponentString(ConfigInvasion.Invasion_Message_tooLate));
                }
            } else {
                player.sendMessage(new TextComponentString(ConfigInvasion.Invasion_Message_notInvasionNight));
            }
        } else {
            player.sendMessage(new TextComponentString(ConfigInvasion.Invasion_Message_alreadySkipping));
        }
        return false;
    }

    /**
     * Ticked every tick
     *
     * @param player
     */
    public static void tickPlayer(EntityPlayer player) {

        //morpheus workaround
        if (ConfigInvasion.preventSleepDuringInvasions) {
            if (InvasionManager.shouldLockOutFeaturesForPossibleActiveInvasion(player.world)) {
                if (player.isPlayerSleeping()) {
                    player.wakeUpPlayer(true, true, false);
                    player.sendMessage(new TextComponentString(ConfigInvasion.Invasion_Message_cantSleep));
                }
            }
        }
    }
    public static void tickPlayerEverywhere(EntityPlayer player) {
        if (ConfigInvasion.forcePlayersToOverworldDuringInvasion) {
            if (CoroUtilWorldTime.isNightPadded(player.world) && InvasionManager.isInvasionTonight(player.world)) {
                //ignore skipping players
                if (!InvasionManager.isPlayerSkippingInvasion(player)) {
                    PlayerDataInstance storage = player.getCapability(Invasion.PLAYER_DATA_INSTANCE, null);

                    if (player.dimension == 0) {
                        storage.ticksNotInOverworld = 0;
                    } else {
                        if (ConfigInvasion.forcePlayersToOverworldDuringInvasion_TickDelay > 0) {
                            if (storage.ticksNotInOverworld == 0 && !ConfigInvasion.forcePlayersToOverworldDuringInvasion_FirstWarningMessage.equals("")) {
                                //first warning message if theres time for them to react
                                player.sendMessage(new TextComponentString(
                                        String.format(ConfigInvasion.forcePlayersToOverworldDuringInvasion_FirstWarningMessage,
                                                (int) (ConfigInvasion.forcePlayersToOverworldDuringInvasion_TickDelay / 20))
                                ));
                            }

                            if (ConfigInvasion.forcePlayersToOverworldDuringInvasion_TickDelay - storage.ticksNotInOverworld == 200 &&
                                    !ConfigInvasion.forcePlayersToOverworldDuringInvasion_FinalWarningMessage.equals("")) {
                                player.sendMessage(new TextComponentString(ConfigInvasion.forcePlayersToOverworldDuringInvasion_FinalWarningMessage));
                            }
                        }
                        CULog.dbg("time: " + player.world.getTotalWorldTime() + " - " +
                                storage.ticksNotInOverworld);
                        storage.ticksNotInOverworld++;

                        if (storage.ticksNotInOverworld >= ConfigInvasion.forcePlayersToOverworldDuringInvasion_TickDelay) {
                            if (player instanceof EntityPlayerMP) {
                                player = (EntityPlayer) player.changeDimension(0);

                                if (player != null) {
                                    BlockPos spawnPos = player.getBedLocation(0);
                                    if (spawnPos != null) {
                                        spawnPos = EntityPlayer.getBedSpawnLocation(
                                                ((EntityPlayerMP) player).world, spawnPos, false);
                                        if (spawnPos == null) {
                                            ((EntityPlayerMP) player).connection.sendPacket(new SPacketChangeGameState(0, 0.0F));
                                        }
                                    }
                                    if (spawnPos == null) {
                                        CULog.dbg("force tp, no bed location found, reverting to world spawn");
                                        spawnPos = player.world.provider.getRandomizedSpawnPoint();
                                    }
                                    CULog.dbg("spawnpoint to use: " + spawnPos);
                                    ((EntityPlayerMP) player).connection.setPlayerLocation(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), player.rotationYaw, player.rotationPitch);
                                    if (!ConfigInvasion.forcePlayersToOverworldDuringInvasion_TPMessage.equals("")) {
                                        player.sendMessage(new TextComponentString(ConfigInvasion.forcePlayersToOverworldDuringInvasion_TPMessage));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * Ticked every 20 ticks
     *
     * Need to do some stuff before main player loop runs
     *
     * @param player
     */
    public static void tickPlayerSlowPre(EntityPlayer player) {
        if (ConfigInvasion.invasionCountingPerPlayer) {
            long ticksPlayed = player.getEntityData().getLong(DynamicDifficulty.dataPlayerServerTicks);
            //doing +1 for case of player time = server time, since first invasion technically happens before 3 day count (72000 ticks)
            //isInvasionTonight does same thing of +1
            int dayNum = (int) (ticksPlayed / CoroUtilWorldTime.getDayLength()) + 1;
            //CULog.dbg("per player day num: " + dayNum);
            if (dayNum < ConfigInvasion.firstInvasionNight) {
                //CULog.dbg("too soon for specific player: " + player.getDisplayNameString() + ", skipping invasion");
                player.getEntityData().setBoolean(DynamicDifficulty.dataPlayerInvasionSkippingTooSoon, true);
            }
        }
    }

    /**
     *
     *
     * @param player
     */
    public static void tickPlayerSlow(EntityPlayer player) {
        try {
            World world = player.world;
            net.minecraft.util.math.Vec3d posVec = new net.minecraft.util.math.Vec3d(player.posX, player.posY + (player.getEyeHeight() - player.getDefaultEyeHeight()), player.posZ);//player.getPosition(1F);
            BlockCoord pos = new BlockCoord(MathHelper.floor(posVec.x), MathHelper.floor(posVec.y), MathHelper.floor(posVec.z));

            float difficultyScale = DynamicDifficulty.getDifficultyScaleAverage(world, player, pos);

            PlayerDataInstance storage = player.getCapability(Invasion.PLAYER_DATA_INSTANCE, null);

            //tickInvasionData(player, difficultyScale);

            ///Chunk chunk = world.getChunkFromBlockCoords(pos);
            //long inhabTime = chunk.getInhabitedTime();
            //System.out.println("difficultyScale: " + difficultyScale);

            //start at "1"
            long dayNumber = (world.getWorldTime() / CoroUtilWorldTime.getDayLength()) + 1;
            //System.out.println("daynumber: " + dayNumber + " - " + world.getWorldTime() + " - " + world.isDaytime());

            boolean invasionActive = false;

            //debug
            //difficultyScale = 1F;

            boolean activeBool = storage.dataPlayerInvasionActive;
            boolean skippingBool = isPlayerSkippingInvasion(player);

            //track state of invasion for proper init and reset for wave counts, etc
            //new day starts just as sun is rising, so invasion stops just at the right time when sun is imminent, they burn 300 ticks before invasion ends, thats ok
            //FYI night val is based on sunlight level, so its not night ends @ 24000 cycle, its a bit before, 400ish ticks before, thats ok
            boolean invasionOnThisNight = isInvasionTonight(world);

            if (invasionOnThisNight != invasionOnThisNight_Last) {
                InvLog.dbg("invasionOnThisNight: " + invasionOnThisNight);
                invasionOnThisNight_Last = invasionOnThisNight;
            }

            boolean isDay = !CoroUtilWorldTime.isNightPadded(world);

            if (isDay != isDayLast) {
                InvLog.dbg("world.isDaytime(): " + isDay + ", time: " + world.getWorldTime() + ", timemod: " + world.getWorldTime() % CoroUtilWorldTime.getDayLength());
                isDayLast = isDay;
            }

            if (invasionOnThisNight && isDay) {
                if (!storage.dataPlayerInvasionWarned && !storage.dataPlayerInvasionHappenedThisDay) {
                    if (player.getEntityData().getBoolean(DynamicDifficulty.dataPlayerInvasionSkippingTooSoon)) {
                        //if (world.playerEntities.size() > 1) {
                        if (isAnyoneBeingInvadedTonight(player.world)) {
                            //CULog.dbg("someone being invaded");
                            //if others on server
                            if (!ConfigInvasion.Invasion_Message_startsTonightButNotYou.equals("")) {
                                player.sendMessage(new TextComponentString(String.format(ConfigInvasion.Invasion_Message_startsTonightButNotYou, ConfigInvasion.firstInvasionNight)));
                            }
                        } else {
                            //CULog.dbg("noone being invaded");
                        }
                    } else {
                        if (!ConfigInvasion.Invasion_Message_startsTonight.equals("")) {
                            player.sendMessage(new TextComponentString(ConfigInvasion.Invasion_Message_startsTonight));
                        }
                    }
                    storage.dataPlayerInvasionWarned = true;
                }
            }

            if (!invasionOnThisNight) {
                storage.dataPlayerInvasionHappenedThisDay = false;
            }

            if (invasionOnThisNight && !isDay) {

                storage.dataPlayerInvasionHappenedThisDay = true;

                invasionActive = true;
                if (!activeBool) {
                    if (!skippingBool) {
                        //System.out.println("triggering invasion start");
                        InvLog.dbg("attempting to start invasion for player: " + player.getName());
                        invasionStart(player, difficultyScale);
                    } else {
                        //from invasionStart() to keep state correct for other things since initial design didnt account for skipping players too well
                        storage.dataPlayerInvasionActive = true;

                        if (player.getEntityData().getBoolean(DynamicDifficulty.dataPlayerInvasionSkipping)) {
                            if (!ConfigInvasion.Invasion_Message_startedButSkippedForYou.equals("")) {
                                player.sendMessage(new TextComponentString(ConfigInvasion.Invasion_Message_startedButSkippedForYou));
                            }
                        } else if (player.getEntityData().getBoolean(DynamicDifficulty.dataPlayerInvasionSkippingTooSoon)) {
                            if (isAnyoneBeingInvadedTonight(player.world)) {
                                //CULog.dbg("someone being invaded");
                                if (!ConfigInvasion.Invasion_Message_startedButSkippedForYouTooSoon.equals("")) {
                                    player.sendMessage(new TextComponentString(String.format(ConfigInvasion.Invasion_Message_startedButSkippedForYouTooSoon, ConfigInvasion.firstInvasionNight)));
                                }
                            } else {
                                //CULog.dbg("noone being invaded");
                            }
                        }
                    }
                }
            } else {
                invasionActive = false;
                if (activeBool) {
                    //System.out.println("triggering invasion stop");

                    //before the skipping flag is reset for all, check if wasnt skipping, and reset their skip counter
                    //might be a better place to put this
                    if (!skippingBool) {
                        player.getEntityData().setInteger(DynamicDifficulty.dataPlayerInvasionSkipCount, 0);
                        if (ConfigInvasion.Sacrifice_CountNeeded_Multiplier_ResetOnInvasionNoSkip) {
                            player.getEntityData().setInteger(DynamicDifficulty.dataPlayerInvasionSkipCountForMultiplier, 0);
                        }
                    }

                    invasionStopReset(player);
                }
            }

            //now that warn flag is serialized, we need to reset it incase time changes during warning stage
            if (!invasionOnThisNight) {
                storage.dataPlayerInvasionWarned = false;

                //also done in invasionStopReset, might be redundant but cant be sure for all cases, logic is fickle
                player.getEntityData().setBoolean(DynamicDifficulty.dataPlayerInvasionSkipping, false);
                player.getEntityData().setBoolean(DynamicDifficulty.dataPlayerInvasionSkippingTooSoon, false);
            }

            //int playerRating = UtilPlayer.getPlayerRating(player);

            //System.out.println("invasion?: " + invasionActive + " - day# " + dayNumber + " - time: " + world.getWorldTime() + " - invasion tonight: " + invasionOnThisNight);
            //System.out.println("inv info: " + getInvasionDebug(difficultyScale));
            //System.out.println("player rating: " + playerRating);

            //debug
            //invasionActive = true;
            //world.getDifficultyForLocation(player.playerLocation);

            if (invasionActive && !skippingBool) {

                if (world.getTotalWorldTime() % ConfigAdvancedOptions.aiTickRateEnhance == 0) {
                    if (ConfigAdvancedOptions.enhanceAllMobsOfSpawnedTypesForOmniscience) {

                        boolean debugTPSSpike = false;
                        if (debugTPSSpike) {
                            player.sendMessage(new TextComponentString("enhancing extra mobs with omniscience"));
                            CULog.dbg("enhancing extra mobs with omniscience");
                        }

                        //old way
                        //int range = getTargettingRangeBuff(difficultyScale);
                        int range = ConfigAdvancedOptions.aiOmniscienceRange;

                        List<EntityCreature> listEnts = world.getEntitiesWithinAABB(EntityCreature.class, new AxisAlignedBB(pos.posX, pos.posY, pos.posZ, pos.posX, pos.posY, pos.posZ).grow(range, range, range));

                        //enhances only mobs of that _type_ that have been invading

                        List<Class> listClassesSpawned = storage.getSpawnableClasses();

                        for (EntityCreature ent : listEnts) {

                            boolean shouldEnhanceEntity = listClassesSpawned.contains(ent.getClass());

                            //no point in giving a cow only omniscience
                            boolean hostileMobsOnly = true;

                            if (shouldEnhanceEntity && (!hostileMobsOnly || ent instanceof EntityMob)) {

                                //note, these arent being added in a way where its persistant, which is fine since this runs all the time anyways
                                //still needs a way to stop after invasion done

                                //this should flag the entity so tasks will get removed later
                                ent.getEntityData().setBoolean(UtilEntityBuffs.dataEntityBuffed, true);
                                ent.getEntityData().setBoolean(UtilEntityBuffs.dataEntityBuffed_AI_Omniscience, true);

                                //stagger the first pathfind delay
                                ent.getEntityData().setLong(UtilEntityBuffs.dataEntityBuffed_LastTimePathfindLongDist, ent.world.getTotalWorldTime() + (ent.getEntityId() % 20));

                                //targetting
                                if (!UtilEntityBuffs.hasTask(ent, EntityAINearestAttackablePlayerOmniscience.class, true)) {
                                    //InvLog.dbg("trying to enhance with omniscience via pre-existing mob way: " + ent.getName());
                                    UtilEntityBuffs.addTask(ent, EntityAINearestAttackablePlayerOmniscience.class, 10, true);
                                }

                                //long distance pathing
                                if (!UtilEntityBuffs.hasTask(ent, EntityAIChaseFromFar.class, false)) {
                                    UtilEntityBuffs.addTask(ent, EntityAIChaseFromFar.class, 4, false);
                                }
                            }
                        }
                    }
                }

                /**
                 * Spawn extra with buffs
                 */

                if (world.getTotalWorldTime() % ConfigAdvancedOptions.aiTickRateSpawning == 0) {

                    spawnNewMobFromProfile(player, difficultyScale);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void tickSpawning(EntityPlayer player, float difficulty) {

    }

    public static void invasionStart(EntityPlayer player, float difficultyScale) {
        PlayerDataInstance storage = player.getCapability(Invasion.PLAYER_DATA_INSTANCE, null);

        //initNewInvasion(player, difficultyScale);

        InvLog.dbg("resetInvasion() for start");
        storage.resetInvasion();
        storage.dataPlayerInvasionActive = true;
        storage.setDifficultyForInvasion(difficultyScale);
        storage.lastWaveNumber++;

        int waveNumberToUse;
        if (ConfigInvasion.invasionCountingPerPlayer) {
            waveNumberToUse = storage.lastWaveNumber;
        } else {
            waveNumberToUse = InvasionManager.getInvasionNumber(player.world);
        }

        DataMobSpawnsTemplate profile = chooseInvasionProfile(player, new DifficultyQueryContext(ConditionContext.TYPE_INVASION, waveNumberToUse, difficultyScale));
        if (profile != null) {
            storage.initNewInvasion(profile);
        } else {
            //TODO: no invasions for you! also this is bad?, perhaps hardcode a fallback default, what if no invasion is modpack makers intent
        }


        //System.out.println("invasion started");


        if (profile != null && !profile.wave_message.equals("<NULL>")) {
            //support for no message override for wave, might as well just check if its blank and prevent code from running
            if (!profile.wave_message.equals("")) {
                player.sendMessage(new TextComponentString(profile.wave_message));
            }
        } else {
            player.sendMessage(new TextComponentString(ConfigInvasion.Invasion_Message_started));
        }

        //add buff for player based on how many invasions they skipped (and only if this isnt a skipped invasion)
        //if (!player.getEntityData().getBoolean(DynamicDifficulty.dataPlayerInvasionSkipping)) {
            float buffBase = 0.5F;
            float skipCount = player.getEntityData().getInteger(DynamicDifficulty.dataPlayerInvasionSkipCount);
            float finalCalc = buffBase * skipCount;
            InvLog.dbg("buffing invasion, inv count: " + skipCount + ", actual buff: " + finalCalc);
            DynamicDifficulty.setInvasionSkipBuff(player, finalCalc);
        //}
    }

    public static DataMobSpawnsTemplate getInvasionTestData(EntityPlayer player, DifficultyQueryContext context) {
        PlayerDataInstance storage = player.getCapability(Invasion.PLAYER_DATA_INSTANCE, null);

        DataMobSpawnsTemplate profile = chooseInvasionProfile(player, context);
        /*if (profile != null) {
            storage.initNewInvasion(profile);
        } else {
            //TODO: no invasions for you! also this is bad?, perhaps hardcode a fallback default, what if no invasion is modpack makers intent
        }*/
        return profile;
    }

    public static void invasionStopReset(EntityPlayer player) {
        PlayerDataInstance storage = player.getCapability(Invasion.PLAYER_DATA_INSTANCE, null);
        //System.out.println("invasion ended");
        if (!isPlayerSkippingInvasion(player)) {
            if (!ConfigInvasion.Invasion_Message_ended.equals("")) {
                player.sendMessage(new TextComponentString(String.format(ConfigInvasion.Invasion_Message_ended, ConfigInvasion.invadeEveryXDays)));
            }
        }

        storage.dataPlayerInvasionActive = false;
        storage.dataPlayerInvasionWarned = false;
        InvLog.dbg("resetInvasion() for stop reset");
        storage.resetInvasion();

        player.getEntityData().setBoolean(DynamicDifficulty.dataPlayerInvasionSkipping, false);
        player.getEntityData().setBoolean(DynamicDifficulty.dataPlayerInvasionSkippingTooSoon, false);

        //remove invasion specific buff since invasion stopped
        DynamicDifficulty.setInvasionSkipBuff(player, 0);
    }

    public static boolean isInvasionTonight(World world) {
        int dayAdjust = 0;

        if (!ConfigInvasion.invasionCountingPerPlayer) {
            dayAdjust = ConfigInvasion.firstInvasionNight;
        }

        int dayNumber = (int)(world.getWorldTime() / CoroUtilWorldTime.getDayLength()) + 1;
        int dayStart = (dayNumber-dayAdjust);

        if (!ConfigInvasion.invasionCountingPerPlayer) {
            if (dayStart < 0) {
                return false;
            }
        }

        return (float)dayStart % (float)ConfigInvasion.invadeEveryXDays == 0;

        /*return dayNumber >= dayAdjust &&
                (dayNumber-dayAdjust == 0 ||
                        (dayNumber-dayAdjust) % Math.max(1, ConfigInvasion.invadeEveryXDays + 1) == 0);*/
    }

    public static boolean isPlayerSkippingInvasion(EntityPlayer player) {
        return player.getEntityData().getBoolean(DynamicDifficulty.dataPlayerInvasionSkipping) ||
                player.getEntityData().getBoolean(DynamicDifficulty.dataPlayerInvasionSkippingTooSoon);
    }

    public static boolean shouldLockOutFeaturesForPossibleActiveInvasion(World world) {
        if (CoroUtilWorldTime.isNightPadded(world) && InvasionManager.isInvasionTonight(world)) {
            if (InvasionManager.isAnyoneBeingInvadedTonight(world)) {
                return true;
            }
        }
        return false;
    }
}