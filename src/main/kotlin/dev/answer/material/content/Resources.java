package dev.answer.material.content;

import java.io.InputStream;
import java.net.URL;

/**
 * @author AnswerDev
 * @date 2026/2/9 00:39
 * @description Resources
 */
public class Resources {

    protected Resources() {
    }

    public static URL loadURL(String path) {
        return Resources.class.getResource(path);
    }

    public static String load(String path) {
        return loadURL(path).toString();
    }

    public static InputStream loadStream(String name) {
        return Resources.class.getResourceAsStream(name);
    }

}
