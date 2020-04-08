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
    private String href;
    private String disHref;

    public String getDisHref() {
        return disHref;
    }

    public void setDisHref(String disHref) {
        this.disHref = disHref;
    }

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

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public String toString() {
        return "City{" +
                "id=" + id +
                ", cityName='" + cityName + '\'' +
                ", provincedId=" + provincedId +
                ", href='" + href + '\'' +
                ", disHref='" + disHref + '\'' +
                '}';
    }
}
