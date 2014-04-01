/*
    Author: Kevin Sweeney

    Date: 10th March 2014

    Basic Interface Code

*/

package btv.client.gui;
import btv.download.DLManager;
import btv.bencoding.BDecodingException;
import btv.download.peer.Peer;
import btv.download.utils.ByteCalculator;
import btv.event.torrent.TorrentEvent;
import btv.event.torrent.TorrentListener;
import btv.event.peer.PeerConnectionListener;
import btv.event.peer.PeerCommunicationListener;

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

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;

class BTVUI extends JFrame {


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
        tableModel.addColumn("Complete");
        tableModel.addColumn("Peers");
        table = new JTable(tableModel);
        table.setToolTipText("Double click a row to open a visualisation.");
        TableColumn col = table.getColumnModel().getColumn(2);
        col.setCellRenderer(new ProgressCellRenderer());

        // Listener to handle Double clicks which open a visualisation.
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int index = table.rowAtPoint(p);
                if(e.getClickCount() == 2) {
                    // Open Visualization
                    String name = (String) tableModel.getValueAt(index, 0);
                    openVisualisation(name);
                }
            }
        });
    }

    public void openVisualisation(String name) {
        new Visualisation(this, name);
    }

    public void addTorrent(String fileName) {
        try {
            String name = downloadManager.add(fileName);
            downloadManager.addTorrentListener(new MyTorrentListener(), name);
            torrents.put(name, numTorrents);

            tableModel.addRow(new Object [] {name, "0", 0, 0});
            numTorrents++;
        }
        catch(FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
        }
        catch(BDecodingException e) {
            System.out.println("Could not read file: " + fileName);
        }
    }

    public void startTorrent(String name) {
        try {
            downloadManager.start(name);
        }
        catch(FileNotFoundException e) {}
        catch(BDecodingException e) {}
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

    public void addPeerConnectionListener(PeerConnectionListener p, String name) {
        downloadManager.addPeerConnectionListener(p, name);
    }

    public void addPeerCommunicationListener(PeerCommunicationListener p, String name) {
        downloadManager.addPeerCommunicationListener(p, name);
    }

    public ArrayList<Peer> getConnections(String name) {
        return downloadManager.getConnections(name);
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
            int downloadedPercent = e.getDownloadedPercent();
            String downloadedBytes = ByteCalculator.convert(e.getDownloadedBytes());
            int connections = e.getConnections();
            int index = -1;
            // Get the index by searching the table rows.
            for(int i = 0; i < tableModel.getRowCount(); i++) {
                String n = (String) table.getValueAt(i, 0);
                if(name.equals(n)) {
                    index = i;
                    break;
                }
            }
            tableModel.setValueAt(downloadedBytes, index, 1);
            tableModel.setValueAt(downloadedPercent, index, 2);
            tableModel.setValueAt(connections, index, 3);
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