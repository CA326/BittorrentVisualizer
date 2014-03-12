/*Author: Kevin Sweeney

Date: 10th March 2014

Basic Interface Code

*/

package btv.client.gui;
import btv.download.DLManager;
import btv.event.torrent.TorrentEvent;
import btv.event.torrent.TorrentListener;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import javafx.embed.swing.JFXPanel;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.io.File;

public class BTVUI extends JFrame {


    private DLManager downloadManager;
    private HashMap<String, Integer> torrents;
    private int numTorrents = 0;
    
    //panels
    private JPanel basicPanel, topPanel, bottomPanel;

    // Components
    private JButton start, pause, stop, remove;
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem menuItem;
    private JTable table;
    private DefaultTableModel tableModel;



    public BTVUI() {
        super("BTV");
        torrents = new HashMap<String, Integer>();
        downloadManager = new DLManager();
        initUI();
    }

    public final void initUI() {

        menuBar = new JMenuBar();
        menu = new JMenu("File");
        menuBar.add(menu);
        menuItem = new JMenuItem("Add");
        menuItem.addActionListener(new MenuListener(this));
        menu.add(menuItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);


        basicPanel = new JPanel();
        basicPanel.setLayout(new BoxLayout(basicPanel, BoxLayout.Y_AXIS));
        add(basicPanel);


        // Top panel
        topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());

        start = new JButton("Start");
        start.addActionListener(new ButtonListener());

        pause = new JButton("Pause");
        pause.addActionListener(new ButtonListener());

        stop = new JButton("Stop");
        stop.addActionListener(new ButtonListener());

        remove = new JButton("Remove");
        remove.addActionListener(new ButtonListener());

        topPanel.add(start);
        topPanel.add(pause);
        topPanel.add(stop);
        topPanel.add(remove);

        basicPanel.add(topPanel, BorderLayout.NORTH);

        // Set bottom panel and table.
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        setupTable();
        bottomPanel.add(table.getTableHeader(), BorderLayout.PAGE_START);
        bottomPanel.add(table);

        basicPanel.add(bottomPanel);

        add(basicPanel);

        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    } 

    public void setupTable() {
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Name");
        tableModel.addColumn("Downloaded");
        tableModel.addColumn("Peers");
        table = new JTable(tableModel);
        TableColumn col = table.getColumnModel().getColumn(1);
        col.setCellRenderer(new ProgressCellRenderer());

        // Listener to handle Double clicks which open a visualisation.
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int index = table.rowAtPoint(p);
                if(e.getClickCount() == 2) {
                    // Open Visualization
                    String name = (String) tableModel.getValueAt(index, 0);
                    new Visualisation(name);
                }
            }
        });
    }

    public void addTorrent(String fileName) {
        String name = downloadManager.add(fileName);
        downloadManager.addTorrentListener(new MyTorrentListener(), name);
        torrents.put(name, numTorrents);

        tableModel.addRow(new Object [] {name, 0, 0});
        numTorrents++;
    }

    public void startTorrent(String name) {
        downloadManager.start(name);
    }

    public void stopTorrent(String name) {
        downloadManager.stop(name);
    }

    public void pauseTorrent(String name) {
        downloadManager.pause(name);
    }

    public void removeTorrent(String name, int index) {
        downloadManager.remove(name);
        // Remove from table.
        tableModel.removeRow(index);
    }

    class ButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JButton o = (JButton) e.getSource();
            int index = table.getSelectedRow();
            String name = (String)tableModel.getValueAt(index, 0); // Row, col          
            if(o == start) {
                startTorrent(name);
            }
            else if(o == stop) {
                stopTorrent(name);
            }
            else if(o == pause) {
                pauseTorrent(name);
            }
            else if(o == remove) {
                removeTorrent(name, index);
            }    
        }
    }

    class MenuListener implements ActionListener {
        private JFrame parent;

        public MenuListener(JFrame j) {
            parent = j;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.addChoosableFileFilter(new FileFilter() {
                
                public boolean accept(File f) {
                    return f.getName().endsWith(".torrent");
                }

                public String getDescription() {
                    return "Torrent files";
                }
            });

            int returnValue = chooser.showOpenDialog(parent);
            if(returnValue == JFileChooser.APPROVE_OPTION) {
                addTorrent(chooser.getSelectedFile().getName());
            }
            else {
                System.out.println("No file chosen.");
            }
        }
    }

    class MyTorrentListener implements TorrentListener {
        public void handleTorrentEvent(TorrentEvent e) {
            String name = e.getName();
            int downloaded = e.getDownloaded();
            int connections = e.getConnections();
            int index = torrents.get(name);

            tableModel.setValueAt(downloaded, index, 1);
            tableModel.setValueAt(connections, index, 2);
        }
    }

    public class ProgressCellRenderer extends JProgressBar
                        implements TableCellRenderer {
 
        public ProgressCellRenderer() {
            super(0, 100);
            setValue(0);
            setString("0%");
            setStringPainted(true);
        }
 
        public Component getTableCellRendererComponent(
                                    JTable table,
                                    Object value,
                                    boolean isSelected,
                                    boolean hasFocus,
                                    int row,
                                    int column) {
 
            String n = value.toString();
            int num = Integer.parseInt(n);  
            setValue(num);
            setString(n + "%");
            return this;
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {


            public void run() {
                BTVUI b = new BTVUI();
                b.setVisible(true);
            }
        });
    }
}

class Visualisation extends JFrame {
    private JFXPanel panel;
    private String name;

    public Visualisation(String n) {
        super(n + " - Visualization");
        name = n;
        panel = new JFXPanel();
        add(panel);
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);


        // Runnable stuff
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                panel.setScene(createScene());
            }
       });
    }

    public Scene createScene() {
        Group  root  =  new  Group();
        Scene  scene  =  new  Scene(root, Color.ALICEBLUE);
        Text  text  =  new  Text();
        
        text.setX(40);
        text.setY(100);
        text.setFont(new Font(25));
        text.setText("Welcome JavaFX!");

        root.getChildren().add(text);

        return (scene);
    }
}