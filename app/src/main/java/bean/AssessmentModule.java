package bean;

import java.io.Serializable;

public class AssessmentModule implements Serializable {
    private String id; // 模块ID，如 "E", "EV", "A"
    private String title; // 模块名称
    private String description; // 模块描述
    private String timeEstimate; // 预计时长
    private boolean isCompleted; // 是否已完成
    private boolean isReportGenerated; // 是否已生成干预报告
    private String lastTestDate; // 最近一次测评日期
    private int iconResId; // 图标资源ID

    public AssessmentModule(String id, String title, String description, String timeEstimate, int iconResId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.timeEstimate = timeEstimate;
        this.iconResId = iconResId;
        this.isCompleted = false;
        this.isReportGenerated = false;
        this.lastTestDate = "";
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTimeEstimate() {
        return timeEstimate;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isReportGenerated() {
        return isReportGenerated;
    }

    public void setReportGenerated(boolean reportGenerated) {
        isReportGenerated = reportGenerated;
    }

    public String getLastTestDate() {
        return lastTestDate;
    }

    public void setLastTestDate(String lastTestDate) {
        this.lastTestDate = lastTestDate;
    }

    public int getIconResId() {
        return iconResId;
    }
}
