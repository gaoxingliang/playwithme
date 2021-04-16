package scugxl.playwithme.svc;

import cn.hutool.core.io.*;
import cn.hutool.core.io.file.*;
import cn.hutool.core.text.*;
import cn.hutool.http.*;
import com.alibaba.fastjson.*;
import com.google.common.util.concurrent.*;
import lombok.extern.log4j.*;
import scugxl.playwithme.baidu.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

@Log4j2
public class BaiduUtils {

    public static volatile TokenInfo currentTokenInfo;
    private static final ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();

    /**
     * access_token 	string 	获取到的授权token，作为调用其他接口访问用户数据的凭证
     * expires_in 	int 	access_token的有效期，单位：秒
     * refresh_token 	string 	用于刷新access_token, 有效期为10年
     * scope 	string 	access_token最终的访问权限，即用户的实际授权列表
     * @param resp
     */
    public static boolean register(String resp, Consumer<JSONObject> consumer) {
        JSONObject json = JSONObject.parseObject(resp);
        int expireInSec = json.getInteger("expires_in");
        long willExpire = 0;
        if (json.containsKey("generatedAtMills")) {
            willExpire = json.getLong("generatedAtMills") + TimeUnit.SECONDS.toMillis(expireInSec);
            if (System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1) > willExpire) {
                return false;
            }
        } else {
            willExpire = System.currentTimeMillis() +  TimeUnit.SECONDS.toMillis(expireInSec) - TimeUnit.MINUTES.toMillis(1);
            json.put("generatedAtMills", System.currentTimeMillis());
        }

        currentTokenInfo = TokenInfo.builder().access_token(json.getString("access_token"))
                .refresh_token(json.getString("refresh_token"))
                .expire_after_mills(willExpire)
                .expires_in(json.getInteger("expires_in")).build();
        if (consumer != null) {
            consumer.accept(json);
        }
        es.scheduleAtFixedRate(() -> refresh(), 30, 60, TimeUnit.SECONDS);
        return true;
    }

    private static void refresh() {
        LOG.info("Start refresh ");
    }


    public static List<FileObject> listFiles(ApiCredential apiCredential, String dir) {
        String resp = HttpUtil.get(String.format(
                "https://pan.baidu.com/rest/2.0/xpan/file?method=list&access_token=%s&dir=%s&folder=0&showempty=0&start=0&limit=1000",
                apiCredential.getToken(), URLEncoder.encode(dir)));
        LOG.info("Query dir {} response ={}", dir, resp);
        JSONObject o = JSONObject.parseObject(resp);
        if (o.getIntValue("errno") != 0) {
            throw new IllegalStateException("Fail to query " + o.getIntValue("errno") + resp);
        }
        JSONArray arr = o.getJSONArray("list");
        List<FileObject> res = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject c = arr.getJSONObject(i);
            String name = UnicodeUtil.toString(c.getString("server_filename"));
            String absPath = Paths.get(dir, name).toAbsolutePath().toString();
            FileObject file = FileObject.builder()
                    .fsid(c.getLong("fs_id"))
                    .ismusic(FileObject.canPlay(FileNameUtil.getSuffix(name)))
                    .filename(name).path(absPath).isDir(c.getIntValue("isdir") == 1).build();
            res.add(file);
        }
        return res;
    }


    public static List<FileObject> listAllFilesRecursively(ApiCredential credential, String dir) {
        return null;
    }

    public static void downloadFile(ApiCredential credential, FileObject source, File localTargetFile) {
        // https://pan.baidu.com/rest/2.0/xpan/multimedia?method=filemetas
        String resp = HttpUtil.get(String.format("https://pan.baidu.com/rest/2.0/xpan/multimedia?dlink=1&method=filemetas&access_token=%s&fsids=[%d]", credential.getToken(), source.getFsid()));
        LOG.info("Get download link for source ={}, resp= {}", source , resp);
        JSONObject o = JSONObject.parseObject(resp);
        if (o.getIntValue("errno") != 0) {
            throw new IllegalStateException("Fail to query " + o.getIntValue("errno"));
        }
        JSONArray arr = o.getJSONArray("list");
        String dlink = arr.getJSONObject(0).getString("dlink");

        // download the file
        downloadFileFromUrl(credential, dlink, localTargetFile);
    }

    public static void downloadFileFromUrl(ApiCredential apiCredential, String dlink, File localTargetFile) {
        // User-Agent
        String url = dlink + "&access_token=" + apiCredential.getToken();
        int timeout = 30 * 3 * 1000;
        LOG.info("Downloading with url {}", url);
        HttpResponse response = HttpRequest.get(url).header("User-Agent", "pan.baidu.com").timeout(timeout).executeAsync();
        if (response.getStatus() == 302) {
            // redirect?
            String redirect = response.header("Location");
            url = redirect + "&access_token=" + apiCredential.getToken();
            LOG.info("Redirect to {}", url);
            response = HttpRequest.get(url).header("User-Agent", "pan.baidu.com").timeout(timeout).executeAsync();
        }
        if (!response.isOk()) {
            throw new IllegalStateException("Failed to download " + response);
        }
        if (!localTargetFile.getParentFile().exists()) {
            localTargetFile.getParentFile().mkdirs();
        }

        final RateLimiter r = RateLimiter.create(0.1d);
        response.writeBody(localTargetFile, new StreamProgress() {
            @Override
            public void start() {
                LOG.info("Downloading started {}", localTargetFile);
            }

            @Override
            public void progress(long progressSize) {
                if (r.tryAcquire()) {
                    LOG.info("Download file {} progressSize {}", localTargetFile.getName(), progressSize);
                }
            }

            @Override
            public void finish() {
                LOG.info("Finished {}", localTargetFile);
            }
        });

    }

    public static void main(String[] args) throws Exception {
        String testString = "{\"expires_in\":2592000,\"refresh_token\":\"122.cc609c71e8c1915c2c5054e8ddb78ec2" +
                ".Y3J6iVx9-CNrTVqMJMTo0igVux1NTtkUF4opEGx.tGxGtg\",\"access_token\":\"121.893b59b3ddd1184c639bad9504e62239" +
                ".Y589dKLDPM0zDsP_8Zxt3MTDKt8LwUb-glGDBQS.ZWEWLw\",\"session_secret\":\"\",\"session_key\":\"\",\"scope\":\"basic " +
                "netdisk\"}\n";

        register(testString, null);
        ApiCredential apiCredential = ApiCredential.builder().token(currentTokenInfo.getAccess_token()).build();
        List<FileObject> res = listFiles(apiCredential, "/测试文件夹");
        res.stream().filter(f -> !f.isDir()).forEach(f -> {
            downloadFile(apiCredential, f, new File(f.getFilename()));
        });
        Thread.sleep(100000);
    }
}
