package online.goudan.shanghai_oil_price;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.LitePalApplication;
import org.litepal.crud.DataSupport;

import java.util.List;

import online.goudan.shanghai_oil_price.bean.City;
import online.goudan.shanghai_oil_price.bean.Province;
import online.goudan.shanghai_oil_price.utils.Common;
import online.goudan.shanghai_oil_price.utils.HttpUtils;

public class App extends LitePalApplication {

    @Override
    public void onCreate() {
        super.onCreate();
    }

}
