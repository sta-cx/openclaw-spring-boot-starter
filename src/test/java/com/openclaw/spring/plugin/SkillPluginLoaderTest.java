package com.openclaw.spring.plugin;

import com.openclaw.spring.skill.SkillRegistry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Skill 插件加载器测试
 */
class SkillPluginLoaderTest {

    @TempDir
    Path tempDir;

    private SkillRegistry skillRegistry;
    private SkillPluginLoader loader;

    @BeforeEach
    void setUp() {
        skillRegistry = new SkillRegistry();
        loader = new SkillPluginLoader(skillRegistry, tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        loader.stopWatching();
    }

    @Test
    @DisplayName("空目录扫描不报错")
    void shouldHandleEmptyDirectory() {
        assertDoesNotThrow(() -> loader.scanAndLoad());
        assertEquals(0, loader.getLoadedPluginCount());
    }

    @Test
    @DisplayName("不存在的目录自动创建")
    void shouldCreateNonExistentDirectory() {
        Path nonExistent = tempDir.resolve("new-plugins");
        SkillPluginLoader newLoader = new SkillPluginLoader(skillRegistry, nonExistent.toString());

        assertDoesNotThrow(() -> newLoader.scanAndLoad());
        assertTrue(nonExistent.toFile().exists());
    }

    @Test
    @DisplayName("非目录路径处理")
    void shouldHandleNonDirectoryPath() throws IOException {
        Path file = tempDir.resolve("not-a-dir.txt");
        file.toFile().createNewFile();

        SkillPluginLoader badLoader = new SkillPluginLoader(skillRegistry, file.toString());
        assertDoesNotThrow(() -> badLoader.scanAndLoad());
        assertEquals(0, badLoader.getLoadedPluginCount());
    }

    @Test
    @DisplayName("插件元数据读取")
    void shouldReadPluginMetadata() {
        SkillPluginLoader.PluginMetadata metadata = SkillPluginLoader.PluginMetadata.unknown();
        assertEquals("unknown", metadata.name());
        assertEquals("unknown", metadata.version());
        assertEquals("unknown", metadata.author());
    }

    @Test
    @DisplayName("已加载插件查询")
    void shouldReturnLoadedPlugins() {
        Map<String, SkillPluginLoader.LoadedPlugin> plugins = loader.getLoadedPlugins();
        assertNotNull(plugins);
        assertTrue(plugins.isEmpty());
    }

    @Test
    @DisplayName("监控状态查询")
    void shouldTrackWatchingState() {
        assertFalse(loader.isWatching());

        loader.startWatching();
        assertTrue(loader.isWatching());

        loader.stopWatching();
        assertFalse(loader.isWatching());
    }

    @Test
    @DisplayName("无 Skill 类的 JAR 不注册")
    void shouldIgnoreJarWithoutSkills() throws Exception {
        // Create an empty JAR (no @OpenClawSkill classes)
        File emptyJar = createEmptyJar("empty-plugin.jar");

        // This should not throw, just log a warning
        SkillPluginLoader.LoadedPlugin plugin = loader.loadJar(emptyJar);
        assertNull(plugin);
        assertEquals(0, loader.getLoadedPluginCount());
    }

    @Test
    @DisplayName("重复加载同一 JAR 不重复注册")
    void shouldNotLoadDuplicateJar() throws Exception {
        File jar = createEmptyJar("test-plugin.jar");

        // First load returns null (no skills), second should not try again
        loader.loadJar(jar);
        loader.scanAndLoad(); // Should skip already loaded

        assertEquals(0, loader.getLoadedPluginCount());
    }

    @Test
    @DisplayName("卸载不存在的插件不报错")
    void shouldHandleUnloadNonExistent() {
        assertDoesNotThrow(() -> loader.unloadPlugin("non-existent.jar"));
    }

    private File createEmptyJar(String name) throws IOException {
        File jar = tempDir.resolve(name).toFile();
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Skill-Name", "test");
        manifest.getMainAttributes().putValue("Skill-Version", "1.0.0");

        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar), manifest)) {
            // Empty JAR with just manifest
        }
        return jar;
    }
}
