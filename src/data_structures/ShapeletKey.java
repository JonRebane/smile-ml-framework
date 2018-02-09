package data_structures;

import stife.shapelet_size2.Shapelet_Size2;

import java.util.Objects;

public class ShapeletKey {
    public int relation, eventId1, eventId2;

    public ShapeletKey(int relation, int eventId1, int eventId2) {
        this.relation = relation;
        this.eventId1 = eventId1;
        this.eventId2 = eventId2;
    }

    public ShapeletKey(Shapelet_Size2 shapeletOfColumn) {
        this.relation = shapeletOfColumn.getRelationshipId();
        this.eventId1 = shapeletOfColumn.getEventId1();
        this.eventId2 = shapeletOfColumn.getEventId2();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShapeletKey that = (ShapeletKey) o;
        return relation == that.relation &&
                eventId1 == that.eventId1 &&
                eventId2 == that.eventId2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(relation, eventId1, eventId2);
    }
}
