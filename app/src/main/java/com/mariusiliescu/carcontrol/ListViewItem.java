package com.mariusiliescu.carcontrol;

/**
 * Created by Marius on 04.06.2016.
 */
public class ListViewItem {
    private int btImage;
    private String btName;
    private String btAdress;

    public ListViewItem(int image, String name , String btAdress){
        this.btImage = image;
        this.btName = name;
        this.btAdress = btAdress;
    }

    public int getBtImage() {
        return btImage;
    }

    public void setBtImage(int btImage) {
        this.btImage = btImage;
    }

    public String getBtName() {
        return btName;
    }

    public void setBtName(String btName) {
        this.btName = btName;
    }

    public String getBtAdress() {
        return btAdress;
    }

    public void setBtAdress(String btAdress) {
        this.btAdress = btAdress;
    }
}
