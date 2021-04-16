package scugxl.playwithme.ctrl;

import cn.hutool.http.*;
import lombok.extern.log4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import scugxl.playwithme.conf.*;
import scugxl.playwithme.db.*;
import scugxl.playwithme.svc.*;

import javax.servlet.http.*;
import java.net.*;

@Log4j2
@Controller
public class MainController {

    @Autowired
    BaiduConfig baiduConfig;



    @GetMapping("/")
    public String index(Model model) {
        String tokeninfo = baiduConfig.getConfString(Conf.BAIDU_TOKENINFO);
        if (tokeninfo != null) {
            LOG.info("Token info exists {}", tokeninfo);
            if (BaiduUtils.register(tokeninfo, json -> baiduConfig.saveConf(Conf.BAIDU_TOKENINFO, json.toJSONString()))) {
                return "filelist";
            }
            LOG.info("Token info not working. need relogin {}", tokeninfo);
        }
        return "index";
    }

    @GetMapping("/api/filelist")
    public String filelist() {
        return "filelist";
    }


    @GetMapping("/api/baiduloginok")
    public String baiduloginok(@RequestParam String code, HttpServletResponse httpServletResponse) {
        LOG.info("code {}", code);
        // GET https://openapi.baidu.com/oauth/2.0/token?grant_type=authorization_code&code=CODE&client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&redirect_uri=YOUR_REGISTERED_REDIRECT_URI
        String url = String.format("https://openapi.baidu.com/oauth/2.0/token?grant_type=authorization_code&code=%s&client_id=%s&client_secret=%s&redirect_uri=%s",
                code, baiduConfig.getAppkey(), baiduConfig.getSecretkey(), URLEncoder.encode("http://localhost:9096/baiduloginok"));

        LOG.info("The request key url {}", url);

        String resp = HttpUtil.get(url);
        LOG.info("Response for get key {}", resp);

        BaiduUtils.register(resp, json -> {
            baiduConfig.saveConf(Conf.BAIDU_TOKENINFO, json.toJSONString());
        });
        return "filelist";
    }


}