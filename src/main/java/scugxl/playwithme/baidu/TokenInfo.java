package scugxl.playwithme.baidu;

import lombok.*;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TokenInfo {
    private String refresh_token;
    private String access_token;
    private int expires_in; // in seconds

    private volatile long expire_after_mills;
}
