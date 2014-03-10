/*Author: Kevin Sweeney

Date: 10th March 2014

Basic Interface Code

*/

package btv.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;


public class BTVUI extends JFrame {
    
	//panels
	private JPanel basicPanel, topPanel, bottomPanel;

    // Components
    private JButton start, pause, stop, remove;
    private JList list;
    private DefaultListModel<String> listModel;
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem menuItem;



    public BTVUI() {
        super("BTV");
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
        listModel = new DefaultListModel<String>();
        listModel.addElement("Item 1");
        listModel.addElement("Item 2");
        listModel.addElement("Item 3");
        listModel.addElement("Item 4");
        list = new JList();
        list.setModel(listModel);
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(list);
        basicPanel.add(bottomPanel, BorderLayout.SOUTH);


    }

    class ButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JButton o = (JButton) e.getSource();
            int [] selected = list.getSelectedIndices();
            for(int i = 0; i < selected.length; i++) {
                System.out.println(selected[i]);
            }
            

            if(o == start)
                System.out.println("Start button!");
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
                System.out.println(chooser.getSelectedFile().getName());
            }
            else {
                System.out.println("No file chosen.");
            }
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