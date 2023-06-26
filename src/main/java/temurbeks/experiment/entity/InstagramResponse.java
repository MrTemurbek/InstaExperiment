package temurbeks.experiment;

public class InstagramResponse {
    private long takenAt;
    private long pk;
    private String id;
    private long deviceTimestamp;
    private int mediaType;
    private String code;
    private String clientCacheKey;
    private int filterType;
    private boolean canViewerReshare;
    private String caption;
    // Дополнительные поля...
    // ...

    // Конструктор
    public InstagramResponse() {
    }

    // Геттеры и сеттеры для всех полей

    public long getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(long takenAt) {
        this.takenAt = takenAt;
    }

    public long getPk() {
        return pk;
    }

    public void setPk(long pk) {
        this.pk = pk;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getDeviceTimestamp() {
        return deviceTimestamp;
    }

    public void setDeviceTimestamp(long deviceTimestamp) {
        this.deviceTimestamp = deviceTimestamp;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getClientCacheKey() {
        return clientCacheKey;
    }

    public void setClientCacheKey(String clientCacheKey) {
        this.clientCacheKey = clientCacheKey;
    }

    public int getFilterType() {
        return filterType;
    }

    public void setFilterType(int filterType) {
        this.filterType = filterType;
    }

    public boolean isCanViewerReshare() {
        return canViewerReshare;
    }

    public void setCanViewerReshare(boolean canViewerReshare) {
        this.canViewerReshare = canViewerReshare;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
