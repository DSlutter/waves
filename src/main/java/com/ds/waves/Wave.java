package com.ds.waves;

import net.minecraft.entity.Entity;

import java.util.Stack;

public class Wave {
    private Stack<Entity> monsters;

    public Stack<Entity> getMonsters() {
        return monsters;
    }

    public void setMonsters(Stack<Entity> monsters) {
        this.monsters = monsters;
    }
}
