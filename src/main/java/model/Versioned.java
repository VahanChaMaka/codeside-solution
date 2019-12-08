package model;

import util.StreamUtil;

import java.util.HashMap;
import java.util.Map;

public class Versioned {
    private Map<Integer, UnitAction> inner;
    public Map<Integer, UnitAction> getInner() { return inner; }
    public void setInner(Map<Integer, UnitAction> inner) { this.inner = inner; }
    public Versioned() {}
    public Versioned(Map<Integer, UnitAction> inner) {
        this.inner = inner;
    }
    public static Versioned readFrom(java.io.InputStream stream) throws java.io.IOException {
        Versioned result = new Versioned();
        int innerSize = StreamUtil.readInt(stream);
        result.inner = new HashMap<>(innerSize);
        for (int i = 0; i < innerSize; i++) {
            int innerKey;
            innerKey = StreamUtil.readInt(stream);
            UnitAction innerValue;
            innerValue = UnitAction.readFrom(stream);
            result.inner.put(innerKey, innerValue);
        }
        return result;
    }
    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        StreamUtil.writeInt(stream, 43981);
        StreamUtil.writeInt(stream, inner.size());
        for (Map.Entry<Integer, UnitAction> innerEntry : inner.entrySet()) {
            int innerKey = innerEntry.getKey();
            UnitAction innerValue = innerEntry.getValue();
            StreamUtil.writeInt(stream, innerKey);
            innerValue.writeTo(stream);
        }
    }
}
