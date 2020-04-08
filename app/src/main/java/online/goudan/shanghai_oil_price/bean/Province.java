package online.goudan.shanghai_oil_price.bean;

import org.litepal.crud.DataSupport;

/**
 * @author 刘成龙
 * @date 2020/4/7
 */
public class Province extends DataSupport {

    private int id;
    private String provinceName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    @Override
    public String toString() {
        return "Province{" +
                "id=" + id +
                ", provinceName='" + provinceName + '\'' +
                '}';
    }
}
