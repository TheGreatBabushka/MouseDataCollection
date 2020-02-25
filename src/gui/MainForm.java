package gui;

import data.MouseData;
import gui.charting.MouseDataCharter;
import main.Main;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.LinkedList;

public class MainForm extends JFrame
{
    private JPanel contentPane;
    private JTabbedPane tabPane;
    private JButton openViewerBtn;
    private JButton collectBtn;
    private JLabel statusLbl;
    private JButton pauseBtn;
    private JButton stopBtn;
    private JButton selectFolderBtn;
    private JRadioButton fileRb;
    private JRadioButton databaseRb;
    private JButton openDbBtn;
    private JLabel processStatusLbl;
    private JLabel dataPointsLbl;
    private JLabel loadedFilesLbl;
    private JButton exportToDbBtn;

    public MainForm()
    {
        setPreferredSize(new Dimension(400, 300));
        setContentPane(contentPane);

        // call onCancel() when cross is clicked
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
               exit();
            }
        });

        openViewerBtn.addActionListener(e ->
        {
            new MouseDataCharter(Main.instance().getLoadedData());
        });

        collectBtn.addActionListener(e ->
        {
            statusLbl.setText("Collecting");
            collectBtn.setEnabled(false);
            pauseBtn.setEnabled(true);
            stopBtn.setEnabled(true);
            Main.collector().setCollectData(true);

        });

        pauseBtn.addActionListener(e ->
        {
            if(Main.collector().isCollecting())
            {
                Main.collector().pause();
                pauseBtn.setText("Resume");
            }
            else
            {
                Main.collector().resumeCollection();
                pauseBtn.setText("Pause");
            }
        });

        stopBtn.addActionListener(e ->
        {
            Main.collector().pause();
        });


        selectFolderBtn.addActionListener(e ->
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("Data Folder Selection");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(MainForm.this) == JFileChooser.APPROVE_OPTION) {
                updateProcessingStatusLbl("Loading Files...");
                Main.instance().setLoadedData(Main.instance().database().loadMouseDataCSVGroup(chooser.getSelectedFile()));
                updateComponents();
                updateProcessingStatusLbl("Idle");
            }
            else {
                System.out.println("No Selection ");
            }
        });

        openDbBtn.addActionListener(e ->
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("Database File Selection");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(MainForm.this) == JFileChooser.APPROVE_OPTION) {
                //load the data
                updateProcessingStatusLbl("Loading...");

                LinkedList<MouseData> data = Main.instance().database().loadMouseDataSQLite(chooser.getSelectedFile());
                if(data != null)
                {
                    updateDataPointsLoadedLbl(data.size());
                    updateLoadedFilesLbl(1);
                    Main.instance().setLoadedData(data);
                }
                else
                {
                    updateDataPointsLoadedLbl(0);
                    updateLoadedFilesLbl(0);
                    Main.instance().setLoadedData(new LinkedList<>());
                }
                updateComponents();

                updateProcessingStatusLbl("Idle");
            }
            else {
                System.out.println("No Selection ");
            }
        });

        exportToDbBtn.addActionListener(e ->
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("Export to database");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(new FileFilter()
            {
                public boolean accept(File f) { return f.getName().endsWith(".db"); }
                public String getDescription() { return "*.db"; }
            });

            if (chooser.showOpenDialog(MainForm.this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if(!file.getPath().endsWith(".db"))
                    file = new File(file.getPath() + ".db");

                if(file.exists())
                {
                    // warn about duplicating data in a db and show source database and new database name comparison
                }
                else
                {
                    final File dbFile = file;
                    Thread t = new Thread(() -> {
                        updateProcessingStatusLbl("Exporting to DB");
                        Main.instance().database().save(dbFile, Main.instance().getLoadedData());
                        updateProcessingStatusLbl("Idle");
                    });
                    t.start();
                }
            }
            else {
                System.out.println("No Selection ");
            }
        });

        fileRb.addActionListener(e -> outputChanged(fileRb));
        databaseRb.addActionListener(e -> outputChanged(databaseRb));

        tabPane.addChangeListener(e -> tabChanged());
    }

    private void updateComponents()
    {
        openViewerBtn.setEnabled(Main.instance().hasLoadedData());
        exportToDbBtn.setEnabled(Main.instance().hasLoadedData());
        updateDataPointsLoadedLbl(Main.instance().getLoadedData().size());
    }

    private void tabChanged()
    {
        updateComponents();;
    }

    void updateLoadedFilesLbl(int num) { loadedFilesLbl.setText("Loaded Files: " + num); }
    void updateDataPointsLoadedLbl(int num) { dataPointsLbl.setText("Data Points: " + num); }
    void updateProcessingStatusLbl(String status) { processStatusLbl.setText("Status: " + status); }

    private void exit(){
        Main.exit();
    }

    void outputChanged(JRadioButton clicked)
    {
        if(clicked == databaseRb)
        {
            if(databaseRb.isSelected()) fileRb.setSelected(false);
            else if (!fileRb.isSelected()) databaseRb.setSelected(true);
        }
        else
        {
            if(fileRb.isSelected()) databaseRb.setSelected(false);
            else if (!databaseRb.isSelected()) fileRb.setSelected(true);
        }
    }

    private void createUIComponents()
    {
    }
}
