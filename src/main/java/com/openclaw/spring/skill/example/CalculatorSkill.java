package com.openclaw.spring.skill.example;

import com.openclaw.spring.skill.OpenClawSkill;
import com.openclaw.spring.skill.SkillAction;
import com.openclaw.spring.skill.SkillParam;

import java.util.Map;

/**
 * 示例 Skill：计算器
 *
 * 演示多参数 Skill 的正确用法（@SkillParam name-based mapping）
 */
@OpenClawSkill(name = "calculator", description = "数学计算器", version = "1.0.0")
public class CalculatorSkill {

    @SkillAction(name = "add", description = "两数相加")
    public String add(
            @SkillParam(value = "a", description = "第一个数") double a,
            @SkillParam(value = "b", description = "第二个数") double b) {
        double result = a + b;
        return formatResult(a, "+", b, result);
    }

    @SkillAction(name = "subtract", description = "两数相减")
    public String subtract(
            @SkillParam(value = "a", description = "被减数") double a,
            @SkillParam(value = "b", description = "减数") double b) {
        double result = a - b;
        return formatResult(a, "-", b, result);
    }

    @SkillAction(name = "multiply", description = "两数相乘")
    public String multiply(
            @SkillParam(value = "a", description = "第一个数") double a,
            @SkillParam(value = "b", description = "第二个数") double b) {
        double result = a * b;
        return formatResult(a, "×", b, result);
    }

    @SkillAction(name = "divide", description = "两数相除")
    public String divide(
            @SkillParam(value = "a", description = "被除数") double a,
            @SkillParam(value = "b", description = "除数") double b) {
        if (b == 0) {
            return "❌ 错误：除数不能为零";
        }
        double result = a / b;
        return formatResult(a, "÷", b, result);
    }

    @SkillAction(name = "power", description = "幂运算")
    public String power(
            @SkillParam(value = "base", description = "底数") double base,
            @SkillParam(value = "exponent", description = "指数") double exponent) {
        double result = Math.pow(base, exponent);
        return String.format("🔢 %.2f ^ %.2f = %.2f", base, exponent, result);
    }

    @SkillAction(name = "sqrt", description = "平方根")
    public String sqrt(@SkillParam(value = "number", description = "数字") double number) {
        if (number < 0) {
            return "❌ 错误：不能对负数开平方根";
        }
        return String.format("🔢 √%.2f = %.2f", number, Math.sqrt(number));
    }

    @SkillAction(name = "help", description = "显示所有运算")
    public String help() {
        return """
                🔢 计算器支持的操作：
                • add — 两数相加 {a, b}
                • subtract — 两数相减 {a, b}
                • multiply — 两数相乘 {a, b}
                • divide — 两数相除 {a, b}
                • power — 幂运算 {base, exponent}
                • sqrt — 平方根 {number}
                """;
    }

    private String formatResult(double a, String op, double b, double result) {
        // 整数则不显示小数部分
        if (result == Math.floor(result) && !Double.isInfinite(result)) {
            return String.format("🔢 %.0f %s %.0f = %.0f", a, op, b, result);
        }
        return String.format("🔢 %.2f %s %.2f = %.2f", a, op, b, result);
    }
}
