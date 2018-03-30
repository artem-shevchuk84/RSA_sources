package ua.rsa.gd.AvantaErarh;

import java.util.ArrayList;

/**
 * Created on 8/13/16 by Roman Komarov (0503412392)
 */
public interface Item {

    String getTitle();

    float getPrice(int index);

    float getStock();

    int getOrder();

    int getDiscount();

    boolean inHistory();

    void setInHistory(boolean inHistory);

    void setDiscount(int percent);

    void setDiscount(String percent);

    String getNds();

    String getBrand();

    void setOrder(int order);

    boolean isTopSku();

    boolean isParent();

    void setPrice(int index, float price);

    void setPrices(float[] prices);

    String getId();

    String getParentId();

    String getStockText();

    String getOrderText();

    int getBackgroundColor();

    ArrayList<Item> getChilds();
}
