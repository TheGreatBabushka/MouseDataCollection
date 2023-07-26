package gui.charting;

import data.MouseData;
import main.Main;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.List;

public class MouseChartCanvas extends JPanel
{
    final int TRAIL_SIZE = 100;
    List<MouseData> data = new ArrayList<>();
    MouseDataCharter charter;

    int cursorPos = 0;

    boolean filesLoaded = false;
    public MouseChartCanvas()
    {
        this.charter = charter;
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        g.setColor(Color.red);

        float dataScale = 1.0f / ((float)Math.floor(dataWidth / getWidth()) + 1);

        int index = 0;
        int TRAIL_LENGTH = 100;
        if(cursorPos > 100) index = cursorPos - TRAIL_LENGTH;

        for(int i = index; i < cursorPos; ++i)
        {
            Point p = new Point(data.get(i).x, data.get(i).y);
            g.fillOval((int)(p.x * dataScale), (int)(p.y * dataScale), 4, 4 );
        }

        g.setColor(Color.black);
        g.drawRect((int)(minX * dataScale), (int)(minY * dataScale), (int)(maxX * dataScale), (int)(maxY * dataScale));
    }

    public void setData(List<MouseData> data)
    {
        this.data = data;
        analyzeData(data);
    }

    public void setCursorPosition(int index)
    {
        cursorPos = index;
        repaint();;
    }

    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    int dataWidth = 0;
    int dataHeight = 0;
    void analyzeData(List<MouseData> data){
        for(MouseData p : data){
            if(p.x < minX) minX = p.x;
            if(p.x > maxX) maxX = p.x;

            if(p.y < minY) minY = p.y;
            if(p.y > maxY) maxY = p.y;
        }

        dataWidth = maxX - minX;
        dataHeight = maxY - minY;
    }
}
