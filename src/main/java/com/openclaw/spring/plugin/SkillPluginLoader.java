package com.openclaw.spring.plugin;

import com.openclaw.spring.skill.OpenClawSkill;
import com.openclaw.spring.skill.SkillRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Skill 插件加载器
 *
 * 从外部 JAR 文件动态加载 Skill 实现。
 * 支持：
 * - 扫描指定目录下所有 .jar 文件
 * - 热加载：监控插件目录，自动发现新 JAR
 * - 卸载：移除 JAR 文件时自动注销相关 Skill
 * - 版本管理：通过 JAR manifest 读取插件元信息
 *
 * JAR 文件要求：
 * - 包含至少一个 @OpenClawSkill 注解的类
 * - 类必须有无参构造函数
 * - 可选 manifest 属性：Skill-Name, Skill-Version, Skill-Author
 *
 * 示例 manifest:
 *   Skill-Name: my-plugin
 *   Skill-Version: 1.0.0
 *   Skill-Author: Developer Name
 */
public class SkillPluginLoader {

    private static final Logger log = LoggerFactory.getLogger(SkillPluginLoader.class);

    private final SkillRegistry skillRegistry;
    private final Path pluginDirectory;

    // 已加载的插件信息
    private final ConcurrentHashMap<String, LoadedPlugin> loadedPlugins = new ConcurrentHashMap<>();
    private WatchService watchService;
    private Thread watchThread;
    private volatile boolean watching = false;

    public SkillPluginLoader(SkillRegistry skillRegistry, String pluginDirectory) {
        this.skillRegistry = skillRegistry;
        this.pluginDirectory = Path.of(pluginDirectory);
    }

    /**
     * 扫描并加载插件目录中的所有 JAR
     */
    public synchronized void scanAndLoad() {
        File dir = pluginDirectory.toFile();
        if (!dir.exists()) {
            log.info("Plugin directory does not exist, creating: {}", pluginDirectory);
            dir.mkdirs();
            return;
        }

        if (!dir.isDirectory()) {
            log.error("Plugin path is not a directory: {}", pluginDirectory);
            return;
        }

        File[] jars = dir.listFiles((d, name) -> name.endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            log.info("No JAR files found in plugin directory: {}", pluginDirectory);
            return;
        }

        int loaded = 0;
        for (File jar : jars) {
            try {
                if (!loadedPlugins.containsKey(jar.getName())) {
                    loadJar(jar);
                    loaded++;
                }
            } catch (Exception e) {
                log.error("Failed to load plugin JAR: {}", jar.getName(), e);
            }
        }
        log.info("Plugin scan complete: {} new plugin(s) loaded from {}", loaded, pluginDirectory);
    }

    /**
     * 加载单个 JAR 文件
     */
    public synchronized LoadedPlugin loadJar(File jarFile) throws Exception {
        String jarName = jarFile.getName();
        log.info("Loading plugin JAR: {}", jarName);

        // 创建独立的 ClassLoader
        URL jarUrl = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarUrl},
                getClass().getClassLoader()
        );

        // 读取 Manifest 元信息
        PluginMetadata metadata = readMetadata(jarFile, classLoader);

        // 扫描 JAR 中的 @OpenClawSkill 类
        List<Object> skillInstances = new ArrayList<>();
        List<String> skillNames = new ArrayList<>();

        try (JarFile jar = new JarFile(jarFile)) {
            jar.stream()
                    .filter(entry -> entry.getName().endsWith(".class") && !entry.getName().contains("$"))
                    .forEach(entry -> {
                        try {
                            String className = entry.getName()
                                    .replace("/", ".")
                                    .replace(".class", "");
                            Class<?> clazz = classLoader.loadClass(className);

                            if (clazz.isAnnotationPresent(OpenClawSkill.class)) {
                                // 检查是否有无参构造
                                if (clazz.getConstructors().length == 0 ||
                                    Arrays.stream(clazz.getConstructors())
                                            .anyMatch(c -> c.getParameterCount() == 0)) {

                                    Object instance = clazz.getDeclaredConstructor().newInstance();
                                    skillRegistry.register(instance);

                                    OpenClawSkill annotation = clazz.getAnnotation(OpenClawSkill.class);
                                    skillInstances.add(instance);
                                    skillNames.add(annotation.name());

                                    log.info("  Registered skill: {} ({}) from {}",
                                            annotation.name(), annotation.description(), jarName);
                                } else {
                                    log.warn("  Skipping {} - no default constructor", className);
                                }
                            }
                        } catch (Exception e) {
                            log.debug("  Skipping class {}: {}", entry.getName(), e.getMessage());
                        }
                    });
        }

        if (skillInstances.isEmpty()) {
            log.warn("No @OpenClawSkill classes found in {}", jarName);
            classLoader.close();
            return null;
        }

        LoadedPlugin plugin = new LoadedPlugin(
                jarName, jarFile.getAbsolutePath(), metadata,
                skillNames, skillInstances, classLoader
        );
        loadedPlugins.put(jarName, plugin);

        log.info("Plugin loaded successfully: {} (skills: {})", jarName, skillNames);
        return plugin;
    }

    /**
     * 卸载插件
     */
    public synchronized void unloadPlugin(String jarName) throws IOException {
        LoadedPlugin plugin = loadedPlugins.remove(jarName);
        if (plugin == null) {
            log.warn("Plugin not found: {}", jarName);
            return;
        }

        // 从注册表移除 Skill
        for (String skillName : plugin.skillNames) {
            var removed = skillRegistry.unregister(skillName);
            if (removed != null) {
                log.info("  Unregistered skill: {}", skillName);
            }
        }

        // 关闭 ClassLoader
        plugin.classLoader.close();
        log.info("Plugin unloaded: {}", jarName);
    }

    /**
     * 启动目录监控（热加载）
     */
    public void startWatching() {
        if (watching) return;

        try {
            watchService = FileSystems.getDefault().newWatchService();
            pluginDirectory.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);

            watching = true;
            watchThread = new Thread(this::watchLoop, "openclaw-plugin-watcher");
            watchThread.setDaemon(true);
            watchThread.start();

            log.info("Plugin directory watcher started: {}", pluginDirectory);
        } catch (IOException e) {
            log.error("Failed to start plugin watcher", e);
        }
    }

    /**
     * 停止目录监控
     */
    public void stopWatching() {
        watching = false;
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                log.debug("Error closing watch service", e);
            }
        }
        log.info("Plugin directory watcher stopped");
    }

    private void watchLoop() {
        while (watching) {
            try {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path fileName = (Path) event.context();
                    String name = fileName.toString();

                    if (!name.endsWith(".jar")) continue;

                    File jarFile = pluginDirectory.resolve(fileName).toFile();

                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        log.info("New JAR detected: {}", name);
                        try {
                            Thread.sleep(500); // Wait for file to be fully written
                            loadJar(jarFile);
                        } catch (Exception e) {
                            log.error("Failed to load new plugin: {}", name, e);
                        }
                    } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                        log.info("JAR removed: {}", name);
                        try {
                            unloadPlugin(name);
                        } catch (IOException e) {
                            log.error("Failed to unload plugin: {}", name, e);
                        }
                    }
                }
                key.reset();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ClosedWatchServiceException e) {
                break;
            }
        }
    }

    private PluginMetadata readMetadata(File jarFile, URLClassLoader classLoader) {
        try (JarFile jar = new JarFile(jarFile)) {
            Manifest manifest = jar.getManifest();
            if (manifest == null) return PluginMetadata.unknown();

            var attrs = manifest.getMainAttributes();
            return new PluginMetadata(
                    attrs.getValue("Skill-Name"),
                    attrs.getValue("Skill-Version"),
                    attrs.getValue("Skill-Author")
            );
        } catch (IOException e) {
            return PluginMetadata.unknown();
        }
    }

    // ---- 查询方法 ----

    public Map<String, LoadedPlugin> getLoadedPlugins() {
        return Collections.unmodifiableMap(loadedPlugins);
    }

    public int getLoadedPluginCount() {
        return loadedPlugins.size();
    }

    public boolean isWatching() {
        return watching;
    }

    // ---- 内部类 ----

    public record LoadedPlugin(
            String jarName,
            String jarPath,
            PluginMetadata metadata,
            List<String> skillNames,
            List<Object> skillInstances,
            URLClassLoader classLoader
    ) {}

    public record PluginMetadata(
            String name,
            String version,
            String author
    ) {
        public static PluginMetadata unknown() {
            return new PluginMetadata("unknown", "unknown", "unknown");
        }
    }
}
