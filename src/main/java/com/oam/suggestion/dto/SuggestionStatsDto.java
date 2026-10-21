package com.oam.suggestion.dto;

import java.util.Map;

/** 建议反馈统计：总量、各状态、各分类数量 */
public class SuggestionStatsDto {

    private long total;
    private long pending;
    private long processing;
    private long resolved;
    private Map<String, Long> byCategory;

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public long getPending() { return pending; }
    public void setPending(long pending) { this.pending = pending; }
    public long getProcessing() { return processing; }
    public void setProcessing(long processing) { this.processing = processing; }
    public long getResolved() { return resolved; }
    public void setResolved(long resolved) { this.resolved = resolved; }
    public Map<String, Long> getByCategory() { return byCategory; }
    public void setByCategory(Map<String, Long> byCategory) { this.byCategory = byCategory; }
}
