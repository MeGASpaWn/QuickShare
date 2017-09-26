package com.megaspawn.quickshare.util;

/**
 * Created by Varun on 20-09-2017.
 */

public class Item {

    private String key;

    private int type;

    private String text;

    private int action;

    public Item(String key, String text) {
        this.key = key;
        this.text = text;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "Item{" +
                "key='" + key + '\'' +
                ", type=" + type +
                ", text='" + text + '\'' +
                ", action=" + action +
                '}';
    }
}
