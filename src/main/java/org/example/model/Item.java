package org.example.model;

import java.util.Comparator;
import java.util.Objects;

public class Item{
    private final String group;
    private final String type;
    private final long number;
    private final long weight;

    public Item(String groupId, String type, long number, long weight) {
        this.group = groupId;
        this.type = type;
        this.number = number;
        this.weight = weight;
    }

    public String getGroup() {
        return this.group;
    }

    public String getType() {
        return this.type;
    }

    public long getNumber() {
        return this.number;
    }

    public long getWeight() {
        return this.weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(group, item.group) && Objects.equals(type, item.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, type);
    }

    @Override
    public String toString() {
        return group+","+type+","+number+","+weight;
    }
   public static Comparator<Item> itemComparator = Comparator.comparing((Item x) -> x.getGroup() + x.getType());

}