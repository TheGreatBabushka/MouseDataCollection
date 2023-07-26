package data;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class DataCollector extends Thread
{
    final int DATA_DUMP_SIZE = 5000;
    final List<MouseData> data = new LinkedList<>();

    volatile boolean collectData = false;
    volatile boolean survive = true;
    long lastCollectTime = 0;
    File outputDir =  new File(System.getProperty("user.dir") + "/data");
    String saveFileName = "mouse-data-%1.mscsv";

    OutputType outputType = OutputType.FILE;

    @Override
    public void run()
    {
        System.out.println("Data collector starting. Data collection set to: " + collectData);

        if(!outputDir.exists() || !outputDir.isDirectory())
        {
            System.out.println("Creating output directory: " + outputDir);
            outputDir.mkdir();
        }

        while(survive)
        {
            while (collectData)
            {
                if ((System.currentTimeMillis() - lastCollectTime) > 10)
                    collect();

                if(data.size() >= DATA_DUMP_SIZE)
                {
                    dump();
                }
            }

            try{ Thread.sleep(10); }
            catch (Exception e) { System.out.println("Sleep interrupted. Breaking."); break;}
        }

        System.out.println("Data collector exiting");
    }

    private void dump()
    {
        switch (outputType){
            case FILE:
                saveToFile();
                break;
            case DATABASE:
                saveToDatabase();
                break;
        }
    }

    private void collect()
    {
        Point p = MouseInfo.getPointerInfo().getLocation();
        if(data.isEmpty()) data.add(new MouseData(p.x, p.y, lastCollectTime = System.currentTimeMillis()));
        else {
            MouseData lastPoint = data.get(data.size() - 1);

            if (lastPoint.x != p.x || lastPoint.y != p.y)
                data.add(new MouseData(p.x, p.y, lastCollectTime = System.currentTimeMillis()));
        }
    }

    private void saveToDatabase()
    {

    }

    // Saves the current data list to the current time stamped file. Clears the data array on success or failure
    private void saveToFile()
    {
        String name = outputDir.getPath() + "/" + saveFileName.replace("%1", getCurrentTimeStamp().replace(":", "-"));
        System.out.println(name);

        File file = new File(name);
        try
        {
            if (!file.exists()) file.createNewFile();

            FileWriter fw = new FileWriter(file);
            for (MouseData d : data)
                fw.append(d.x + "," + d.y + "," + d.time + "\n");

            fw.close();

            System.out.println("Saved " + data.size() + " data items");
            data.clear();
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    public File getOutputDir(){
        return outputDir;
    }

    public void kill()
    {
        collectData = survive = false;
        System.out.println("Killing Data Collector.");
    }

    public void pause()
    {
        collectData = false;
        System.out.println("Paused Data Collection.");
    }

    public void resumeCollection()
    {
        collectData = true;
        System.out.println("Resumed Data Collection.");
    }

    public boolean isCollecting()
    {
        return collectData && survive;
    }

    public void setCollection(boolean collect)
    {
        collectData = collect;
        System.out.println("Set Data Collection: " + collect);
    }

    public void setSaveFile(String fileName)
    {
        saveFileName = fileName;
    }

    public boolean doCollectData()
    {
        return collectData;
    }

    public void setCollectData(boolean collectData)
    {
        this.collectData = collectData;
        System.out.println("Set data collection: " + collectData);
    }

    String getCurrentTimeStamp()
    {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public enum OutputType{
        DATABASE,
        FILE
    }
}
