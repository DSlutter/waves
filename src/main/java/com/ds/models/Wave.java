package com.ds.models;

import java.util.Stack;
import net.minecraft.entity.EntityType;

public record Wave(Stack<EntityType<?>> monsters) {

}
