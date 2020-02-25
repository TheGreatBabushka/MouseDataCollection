package main;

import data.DataCollector;
import data.MouseData;
import gui.MainForm;
import sql.MouseMovementDatabase;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Main {

    MouseMovementDatabase database;
    DataCollector collector;

    LinkedList<MouseData> loadedData = new LinkedList<>();

    Thread guiThread = null;
    Thread collectorThread = null;

    public Main(boolean headless)
    {
        collectorThread = new Thread(() ->
        {
            collector = new DataCollector();
            collector.run();
        });
        collectorThread.start();

        // setup the default database
        database = new MouseMovementDatabase();

        System.out.println("Running headless: " + headless);
        if(!headless)
        {
            guiThread = new Thread(() ->
            {
                MainForm dialog = new MainForm();
                dialog.pack();
                dialog.setVisible(true);
            });
            guiThread.start();
        }
    }

    public MouseMovementDatabase database() { return database; }
    public void setLoadedData(LinkedList<MouseData> data) { this.loadedData = data; }
    public LinkedList<MouseData> getLoadedData() { return loadedData; }

    public void quit(){
        collector.kill();
        guiThread.interrupt();
        collectorThread.interrupt();
    }

    public static DataCollector collector(){
        return main.collector;
    }

    public static void exit(){
        main.quit();
    }

    static Main main;
    public static void main(String[] args) throws InterruptedException
    {
        main = new Main(false);
    }

    public static Main instance(){ return main;}

    public boolean hasLoadedData() { return loadedData.size() > 0; }
}
