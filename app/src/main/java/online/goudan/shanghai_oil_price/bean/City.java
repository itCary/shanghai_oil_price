package online.goudan.shanghai_oil_price.bean;

import org.litepal.crud.DataSupport;

/**
 * @author 刘成龙
 * @date 2020/4/7
 */
public class City extends DataSupport {
    private int id;
    private String cityName;
    private int provincedId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getProvincedId() {
        return provincedId;
    }

    public void setProvincedId(int provincedId) {
        this.provincedId = provincedId;
    }
}
