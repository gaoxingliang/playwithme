package scugxl.playwithme.baidu;

import cn.hutool.core.io.file.*;
import lombok.*;

import java.util.*;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString
public class FileObject {
    String path;
    String filename;
    boolean isDir;
    long fsid;
    boolean ismusic;

    @Setter
    boolean existOnDisk;

    public String getFileType() {
        return FileNameUtil.getSuffix(filename);
    }

    public String getFilePrefix() {
        return FileNameUtil.getPrefix(filename);
    }

    private static final Set<String> supportedTypes = new HashSet<String>();
    static {
        supportedTypes.add("mp3");
        supportedTypes.add("flac");
        //supportedTypes.add("dsf");
    }
    public static boolean canPlay(String fileType) {
        fileType = fileType.toLowerCase();
        return supportedTypes.contains(fileType);
    }
}
