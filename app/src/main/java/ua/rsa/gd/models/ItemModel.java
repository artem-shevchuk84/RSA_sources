package ua.rsa.gd.models;

public class ItemModel {

    private String mId;
    private String mName;

    public ItemModel() {
    }

    public ItemModel(String id, String name) {
        mId = id;
        mName = name;
    }

    public void clear() {
        mId = null;
        mName = null;
    }

    public boolean isEmpty() {
        return mId == null;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }
}
