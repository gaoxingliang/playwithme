package scugxl.playwithme.ctrl;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;


@AllArgsConstructor
@Getter
public class RestResponse {
    private int state;
    private String error = "";
    private Object data;

    public static RestResponse ok(Object data) {
        return new RestResponse(200, "", data);
    }

    public static RestResponse ok() {
        return ok("");
    }

    public static RestResponse e400(String error) {
        return new RestResponse(400, error, "");
    }

    public static RestResponse e401(String error) {
        return new RestResponse(401, error, "");
    }

    public static RestResponse e404(String error) {
        return new RestResponse(404, error, "");
    }

    public static RestResponse error(Throwable error) {
        return new RestResponse(500, error == null ? "eror found" : ExceptionUtils.getRootCauseMessage(error), "");
    }
}
