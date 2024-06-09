package com.ds.models;

import net.minecraft.entity.EntityType;

import java.util.Stack;

public record Wave(Stack<EntityType<?>> monsters) {
}
