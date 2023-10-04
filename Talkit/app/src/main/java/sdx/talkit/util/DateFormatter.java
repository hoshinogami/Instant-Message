package sdx.talkit.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateFormatter {
    private final SimpleDateFormat longFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private final SimpleDateFormat middleFormatter = new SimpleDateFormat("MM-dd HH:mm");
    private final SimpleDateFormat shortFormatter = new SimpleDateFormat("HH:mm");

    private final Calendar now = Calendar.getInstance();

    public String fromTimeStamp(long timestamp){
        Calendar time = Calendar.getInstance();
        time.setTime(new Date(timestamp));
        if (now.get(Calendar.YEAR) == time.get(Calendar.YEAR) ){
            if (now.get(Calendar.MONTH) == time.get(Calendar.MONTH) &&
                now.get(Calendar.DAY_OF_MONTH) == time.get(Calendar.DAY_OF_MONTH)){
                return shortFormatter.format(new Date(timestamp));
            }
            return middleFormatter.format(new Date(timestamp));
        }
        return longFormatter.format(new Date(timestamp));
    }
}
