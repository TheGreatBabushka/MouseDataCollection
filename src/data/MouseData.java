package data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MouseData
{
    public int x,  y;
    public long time;
    public MouseData(int x, int y, long time)
    {
        this.x = x;
        this.y = y;
        this.time = time;
    }

    public String toString()
    {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");//dd/MM/yyyy
        String strDate = sdfDate.format(time);
        return new String("x: " + x + ", y: " + y + ", time: " + strDate);
    }
}



