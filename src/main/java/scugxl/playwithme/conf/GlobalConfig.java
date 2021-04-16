package scugxl.playwithme.conf;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;

@Component
@Getter
public class GlobalConfig {
    @Value("${localtargetdir:./Music}")
    String localtargetdir;
}
