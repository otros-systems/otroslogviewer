package pl.otros.logview.api.loading;

import java.util.Date;

public class LoadStatistic {

    private Source source;
    private long position;
    private long total;
    private Date date;

    public LoadStatistic(Source source, long position, long total) {
        this.source = source;
        this.position = position;
        this.total = total;
        date = new Date();
    }

    public Source getSource() {
        return source;
    }

    public long getPosition() {
        return position;
    }

    public long getTotal() {
        return total;
    }

    public float getPercent(){
        return 100f*position/total;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "LoadStatistic{" +
          "source=" + source +
          ", position=" + position +
          ", total=" + total +
          ", date=" + date +
          '}';
    }
}
