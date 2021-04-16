package scugxl.playwithme.db;

import lombok.*;

@Getter
@Setter
public class Conf {
    String confkey;
    String confvalue;


    public static final String BAIDU_PREFIX = "_baidu_";
    public static final String BAIDU_TOKENINFO = BAIDU_PREFIX + "tokeninfo";
}
