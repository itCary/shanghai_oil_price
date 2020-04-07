package online.goudan.shanghai_oil_price.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.crud.DataSupport;

import java.util.List;

import online.goudan.shanghai_oil_price.bean.City;
import online.goudan.shanghai_oil_price.bean.Province;

public class InitUils {
    public void initProvince() {
        DataSupport.deleteAll(Province.class);
        DataSupport.deleteAll(City.class);
        HttpUtils httpUtils = new HttpUtils();
        String getString = httpUtils.doGet(Common.T_URL);
        Document document = Jsoup.parse(getString);
        //所有的省
        Elements provinces = document.select("div.province");
        StringBuilder stringBuilder = new StringBuilder();
        for (Element province : provinces) {
            //每个省的省名
            Elements provinceName = province.select("div.media-left h4");
            Province provinceBean = new Province();
            provinceBean.setProvinceName(provinceName.text());
            provinceBean.save();
            provinceBean = DataSupport.where("provinceName = ?", provinceBean.getProvinceName()).find(Province.class).get(0);
            //找到了每个市
            Elements citys = province.select("div.media-body ul li");
            for (Element city : citys) {
                City cityBean = new City();
                cityBean.setCityName(city.text());
                cityBean.setProvincedId(provinceBean.getId());
                cityBean.save();
            }
        }
    }

}
