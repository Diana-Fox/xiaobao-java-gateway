package com.xiaobao.core.config;

import com.xiaobao.common.util.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

@Slf4j
/**
 * 配置文件的加载
 */
public class ConfigLoader {
    //配置文件
    private static final String CONFIG_FILE = "gateway.properties";
    //环境变量读取
    private static final String ENV_PREFIX = "GATEWAY_";
    //JVM参数
    private static final String JVM_PREFIX = "gateway.";

    //单例，饿汉式
    private static final ConfigLoader INSTANCE = new ConfigLoader();

    private ConfigLoader() {

    }

    //获取加载器
    public static ConfigLoader getInstance() {
        return INSTANCE;
    }

    private Config config;

    public static Config getConfig() {
        return INSTANCE.config;
    }

    /**
     * 优先级高的会覆盖优先级低的
     * 运行参数-》JVM-》环境变量-》配置文件-》配置对象默认值
     *
     * @param args
     * @return
     */
    public Config load(String args[]) {
        //配置对象的默认值
        config = new Config();
        //配置文件
        loadFromConfigFile();
        //环境变量
        loadFromEnv();
        //jvm参数
        loadFromJVM();
        //运行参数
        loadFromArgs(args);
        return config;
    }

    private void loadFromArgs(String args[]) {
        if (args != null && args.length > 0) {
            Properties properties = new Properties();
            for (String arg : args) {
                if (arg.startsWith("--") && arg.contains("=")) {
                    properties.put(arg.substring(2, arg.indexOf("=")),
                            arg.substring(arg.indexOf("=") + 1));

                }
            }
            PropertiesUtils.properties2Object(properties,config);
        }
    }

    private void loadFromJVM() {
        Properties properties = System.getProperties();
        PropertiesUtils.properties2Object(properties, config, JVM_PREFIX);

    }

    private void loadFromEnv() {
        Map<String, String> env = System.getenv();
        Properties properties = new Properties();
        properties.putAll(env);
        PropertiesUtils.properties2Object(properties, config, ENV_PREFIX);
    }

    private void loadFromConfigFile() {
        InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
        if (inputStream != null) {
            Properties properties = new Properties();
            try {
                properties.load(inputStream);
                PropertiesUtils.properties2Object(properties, config);
            } catch (IOException e) {
                log.warn("load config file{} error", CONFIG_FILE, e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {

                    }
                }
            }
        }
    }
}
