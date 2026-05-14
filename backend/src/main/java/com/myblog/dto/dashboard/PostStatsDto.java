package com.myblog.dto.dashboard;

public class PostStatsDto {

    private long total;
    private long published;
    private long draft;

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getPublished() { return published; }
    public void setPublished(long published) { this.published = published; }

    public long getDraft() { return draft; }
    public void setDraft(long draft) { this.draft = draft; }
}
