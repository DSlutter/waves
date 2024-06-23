package com.ds.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

public class CustomEntity extends HostileEntity {

  protected CustomEntity(EntityType<? extends HostileEntity> entityType, World world) {
    super(entityType, world);
  }
}
