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

public class BTVUI extends JFrame {
    
	//panels
	private JPanel basicPanel, topPanel, bottomPanel;

    // Components
    private JButton start, pause, stop, remove;
    private JList list;
    private DefaultListModel<String> listModel;



    public BTVUI() {
        super("BTV");
        initUI();
    }

    public final void initUI() {

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

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {


            public void run() {
                BTVUI b = new BTVUI();
                b.setVisible(true);
            }
        });
    }
}