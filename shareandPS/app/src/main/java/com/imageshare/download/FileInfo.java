package com.imageshare.download;
/**
 * Created by jaygo on 14-4-20.
 */
public class FileInfo {
    public int id;
    public String image;
    public String name;

    public FileInfo() {
        super();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
