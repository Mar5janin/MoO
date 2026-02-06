package model.orbits;

public class OrbitSlot {
    private int index;
    private OrbitObject object;

    public OrbitSlot(int index, OrbitObject object) {
        this.index = index;
        this.object = object;
    }

    public int getIndex() {
        return index;
    }

    public OrbitObject getObject() {
        return object;
    }
}

