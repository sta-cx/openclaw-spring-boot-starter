package com.openclaw.spring.model;

/**
 * Skill 信息模型
 */
public class SkillInfo {

    private String name;
    private String description;
    private String version;
    private String author;

    public SkillInfo() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}
