package com.openclaw.spring.skill.example;

import com.openclaw.spring.skill.SkillRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CalculatorSkill 测试
 */
class CalculatorSkillTest {

    private SkillRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SkillRegistry();
        registry.register(new CalculatorSkill());
    }

    @Test
    @DisplayName("加法测试")
    void testAdd() throws Exception {
        Object result = registry.execute("calculator", "add", Map.of("a", 3.0, "b", 5.0));
        assertEquals("🔢 3 + 5 = 8", result);
    }

    @Test
    @DisplayName("减法测试")
    void testSubtract() throws Exception {
        Object result = registry.execute("calculator", "subtract", Map.of("a", 10.0, "b", 3.0));
        assertEquals("🔢 10 - 3 = 7", result);
    }

    @Test
    @DisplayName("乘法测试")
    void testMultiply() throws Exception {
        Object result = registry.execute("calculator", "multiply", Map.of("a", 4.0, "b", 5.0));
        assertEquals("🔢 4 × 5 = 20", result);
    }

    @Test
    @DisplayName("除法测试")
    void testDivide() throws Exception {
        Object result = registry.execute("calculator", "divide", Map.of("a", 10.0, "b", 4.0));
        assertTrue(result.toString().contains("2.50"));
    }

    @Test
    @DisplayName("除零测试")
    void testDivideByZero() throws Exception {
        Object result = registry.execute("calculator", "divide", Map.of("a", 10.0, "b", 0.0));
        assertTrue(result.toString().contains("除数不能为零"));
    }

    @Test
    @DisplayName("幂运算测试")
    void testPower() throws Exception {
        Object result = registry.execute("calculator", "power", Map.of("base", 2.0, "exponent", 10.0));
        assertTrue(result.toString().contains("1024"));
    }

    @Test
    @DisplayName("平方根测试")
    void testSqrt() throws Exception {
        Object result = registry.execute("calculator", "sqrt", Map.of("number", 16.0));
        assertTrue(result.toString().contains("4.00"));
    }

    @Test
    @DisplayName("负数平方根测试")
    void testSqrtNegative() throws Exception {
        Object result = registry.execute("calculator", "sqrt", Map.of("number", -4.0));
        assertTrue(result.toString().contains("不能对负数开平方根"));
    }

    @Test
    @DisplayName("多参数 name-based 映射验证")
    void testNameBasedMapping() throws Exception {
        // 参数顺序与方法定义不同，验证 name-based 映射正确
        Object result = registry.execute("calculator", "add", Map.of("b", 100.0, "a", 200.0));
        assertEquals("🔢 200 + 100 = 300", result);
    }

    @Test
    @DisplayName("help 命令")
    void testHelp() throws Exception {
        Object result = registry.execute("calculator", "help", Map.of());
        assertTrue(result.toString().contains("计算器支持的操作"));
    }
}
