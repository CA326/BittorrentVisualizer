/*Author: Kevin Sweeney

Date: 10th March 2014

Basic Interface Code

*/

package btv.client.gui;
import btv.download.DLManager;
import btv.event.torrent.TorrentEvent;
import btv.event.torrent.TorrentListener;

import java.util.HashMap;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;


public class BTVUI extends JFrame {


    private DLManager downloadManager;
    private HashMap<String, Integer> torrents;
    private int numTorrents = 0;
    
	//panels
	private JPanel basicPanel, topPanel, bottomPanel;

    // Components
    private JButton start, pause, stop, remove;
    
    private JList name, downloaded, connections;
    private DefaultListModel<String> nameListModel; 
    private DefaultListModel<Integer> downloadedModel, connectionModel;


    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem menuItem;



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

        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        // Set bottom panel and list.
        bottomPanel = new JPanel();
        nameListModel = new DefaultListModel<String>();
        downloadedModel = new DefaultListModel<Integer>();
        connectionModel = new DefaultListModel<Integer>();
        name = new JList();
        downloaded = new JList();
        connections = new JList();
        name.setModel(nameListModel);
        
        downloaded.setModel(downloadedModel);
       
        connections.setModel(connectionModel);
 
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(name, BorderLayout.LINE_START);
        bottomPanel.add(downloaded, BorderLayout.CENTER);
        bottomPanel.add(connections, BorderLayout.LINE_END);
        basicPanel.add(bottomPanel, BorderLayout.SOUTH);


    }


    public void addTorrent(String fileName) {
        String name = downloadManager.add(fileName);
        downloadManager.addTorrentListener(new MyTorrentListener(), name);
        torrents.put(name, numTorrents);
        numTorrents++;
        nameListModel.addElement(name);
        downloadedModel.addElement(0);
        connectionModel.addElement(0);
    }

    public void startTorrent(String name) {
        downloadManager.start(name);
    }

    class ButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JButton o = (JButton) e.getSource();
            int selected = name.getSelectedIndex();
                        
            if(o == start) {
                String name = (String) nameListModel.get(selected);
                startTorrent(name);
            }
            else if(o ==stop)
                System.out.println("stop button!");
            else if(o == pause)
                System.out.println("pause button!");
            else if(o == remove)
                System.out.println("remove button!");
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

            downloadedModel.setElementAt(downloaded, index);
            connectionModel.setElementAt(connections, index);
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