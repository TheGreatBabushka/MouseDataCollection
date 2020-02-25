package gui.charting;

import data.MouseData;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class MouseDataCharter extends JDialog
{
    private JPanel contentPane;
    private JPanel chartPanel;
    private JButton playButton;
    private JButton speedDownBtn;
    private JButton speedUpBtn;

    private JSlider slider1;
    private JLabel sliderLbl;
    private JLabel speedLbl;

    LinkedList<MouseData> data = new LinkedList<>();
    boolean dataLoaded = false;

    boolean playing = false;
    float speed = 1;

    public MouseDataCharter(LinkedList<MouseData> data)
    {
        this.data = data;
        setup();
    }

    public MouseDataCharter(File dataDirectory)
    {
        if(!loadDataFiles(dataDirectory) || data.isEmpty())
        {
            System.out.println("Failed to load any data.");
            return;
        }
        ((MouseChartCanvas)chartPanel).setData(data);
        setup();
    }

    void setup()
    {
        setContentPane(contentPane);
        setMinimumSize(new Dimension(1024, 768));

        ((MouseChartCanvas)chartPanel).setData(data);

        slider1.setMaximum(data.size());
        updateSliderLabel();

        slider1.addChangeListener(e ->
        {
            ((MouseChartCanvas)chartPanel).setCursorPosition(slider1.getValue());
            updateSliderLabel();
        });

        playButton.addActionListener(e ->
        {
            playing = !playing;
            playButton.setText(playing ? "Pause" : "Play");
        });

        speedDownBtn.addActionListener(e ->
        {
            if(speed <= .5) speed -= .05;
            else if(speed <= 1) speed -= .1;
            else if(speed <= 10) speed -= 1;
            else speed -= 10;

            if(speed < .01) speed = .01f;
            speedLbl.setText(String.format("%.2f", speed) + "x");
        });

        speedUpBtn.addActionListener(e ->
        {
            if(speed >= 10) speed += 10;
            else if(speed >= 1) speed += 1;
            else if(speed >= .5)speed += .1;
            else if (Math.abs(speed - .01) < .001) speed += .04;
            else speed += .05;

            if(speed > 100) speed = 100;

            speedLbl.setText(String.format("%.2f", speed) + "x");
        });

        Thread playThread = new Thread(() ->
        {
            while(true){

                if(playing)
                {
                    if(slider1.getValue() == slider1.getMaximum())
                        slider1.setValue(0);
                    else
                        slider1.setValue(slider1.getValue() + 1);
                }

                int sleep = 10;
                sleep = (int) Math.max (1,  10 * 1 / speed);

                try{ Thread.sleep(sleep); }
                catch (InterruptedException e){ e.printStackTrace(); }
            }
        });
        playThread.start();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);
        pack();
        setVisible(true);
    }

    void updateSliderLabel()
    {
        sliderLbl.setText(slider1.getValue() + "/" + data.size());
    }

    private boolean loadDataFiles(File selectedDirectory)
    {
        String[] files = selectedDirectory.list();
        System.out.println("Loading " + files.length + " data files.");

        long startLoadTime = System.currentTimeMillis();
        int i = 0;
        int loadedPoints = 0;
        for (String fileName : files)
        {
            try{
                Scanner scanner = new Scanner(new File(selectedDirectory.getPath() + "/" + fileName));
                Point lastPoint = new Point(0, 0);
                while(scanner.hasNext())
                {
                    String line = scanner.nextLine();
                    String[] tokens = line.split(",");
                    if(tokens.length == 3)
                    {
                        int x = Integer.parseInt(tokens[0]);
                        int y = Integer.parseInt(tokens[1]);
                        long time = Long.parseLong(tokens[2]);

                        Point p = new Point(x, y);
                        if(!(lastPoint.x == p.x && p.y == p.y))
                        {
                            data.add(new MouseData(x, y, time));
                            lastPoint = p;
                        }

                        ++loadedPoints;
                    }
                }
            } catch (FileNotFoundException e){
                e.printStackTrace();
                return false;
            }
            ++i;
        }

        System.out.println("Done loading files. Loaded " + loadedPoints + " mouse positions in " + (System.currentTimeMillis() - startLoadTime) +" ms.");
        return true;
    }

    private void createUIComponents()
    {
        chartPanel = new MouseChartCanvas();
    }
}
