/*
    This is our Visualisation class. It is associated with a Torrent.
    It will use PeerConnection and PeerCommunication listeners to show
    the flow of data involved with the download.

*/
package btv.client.gui;

import btv.event.peer.PeerConnectionListener;
import btv.event.peer.PeerConnectionEvent;
import btv.event.peer.PeerCommunicationListener;
import btv.event.peer.PeerCommunicationEvent;

import javafx.embed.swing.JFXPanel;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.scene.text.TextBoundsType;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.Line;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.animation.PathTransition;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.beans.value.*;
import javafx.animation.Animation.Status;
import javafx.util.Duration;

import javax.swing.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

class Visualisation extends JFrame {
    private BTVUI parent;
    private JFXPanel panel;
    private Group root;
    private StackPane rootNode;
    private ArrayList<StackPane> nodes;
    private HashMap<StackPane, ArrayList> transitions;

    private String name;

    public Visualisation(BTVUI b, String n) {
        super(n + " - Visualisation");
        name = n;
        parent = b;
        parent.addPeerConnectionListener(new MyConnectionListener(this), name);
        parent.addPeerCommunicationListener(new MyCommunicationListener(this),
                                                name);
        nodes = new ArrayList<StackPane>();
        transitions = new HashMap<StackPane, ArrayList>();

        panel = new JFXPanel();
        add(panel);
        setSize(1100, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);


        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                panel.setScene(createScene());
            }
       });
    }

    public Scene createScene() {
        /*
            Set up our scene with the root BTV node shown.
        */
        root = new Group();
        Scene scene = new Scene(root, 500, 500, Color.WHITE);
        addRootNode();

        return scene;
    }

    public void addNode(String ip) {
      Circle c = new Circle();
      c.setRadius(40.0f);
      c.setFill(Color.WHITE);
      c.setStroke(Color.RED);
      Text  t  =  new  Text(ip);
      t.setBoundsType(TextBoundsType.VISUAL);
      t.setFont(Font.font("Verdana", FontWeight.BOLD, 8));
      StackPane s = new StackPane();
      s.getChildren().add(c);
      s.getChildren().add(t);
      s.setLayoutX((80.0f * nodes.size()) + 50.0f);
      s.setLayoutY(100.0f);
      root.getChildren().add(s);
      nodes.add(s);

      Path to = new Path();
      MoveTo moveTo = new MoveTo();
      moveTo.setX(rootNode.getLayoutX() + 40.0f);
      moveTo.setY(rootNode.getLayoutY());
     
      LineTo lineTo = new LineTo();
      lineTo.setX(s.getLayoutX() + 40.0f);
      lineTo.setY(s.getLayoutY() + 80.f);
     
      to.getElements().add(moveTo);
      to.getElements().add(lineTo);
      to.setStrokeWidth(1);
      to.setStroke(Color.BLACK);

      Path from = new Path();
      MoveTo fromMoveTo = new MoveTo();
      fromMoveTo.setX(s.getLayoutX() + 40.0f);
      fromMoveTo.setY(s.getLayoutY() + 80.0f);
     
      LineTo fromLineTo = new LineTo();
      fromLineTo.setX(rootNode.getLayoutX() + 40.0f);
      fromLineTo.setY(rootNode.getLayoutY());
     
      from.getElements().add(fromMoveTo);
      from.getElements().add(fromLineTo);
      from.setStrokeWidth(1);
      from.setStroke(Color.BLACK);

      ArrayList<Path> paths = new ArrayList<Path>();
      paths.add(to); paths.add(from);
      transitions.put(s, paths);

   }

   public void nodeConnection(String ip) {
        StackPane node = getNodeWithIP(ip);
        if(node != null) {
            // Change colour of circle
            for(Node n : node.getChildren()) {
                if(n instanceof Circle) {
                    Circle c = (Circle) n;
                    c.setStroke(Color.GREEN);
                }
            }
            ArrayList<Path> paths = transitions.get(node);
            if(paths != null) {
                root.getChildren().add(paths.get(0));
                root.getChildren().add(paths.get(1));
            }
        } 
   }

   public void nodeDisconnection(String ip) {
        System.out.println("Removing node: " + ip);
        StackPane node = getNodeWithIP(ip);
        if(node != null) {
            ArrayList<Path> paths = transitions.get(node);
            root.getChildren().remove(node);
            root.getChildren().remove(paths.get(0));
            root.getChildren().remove(paths.get(1));
        }
   }

   private StackPane getNodeWithIP(String ip) {
        StackPane node = null;
        for(StackPane s : nodes) {
            for(Node n : s.getChildren()) {
                if(n instanceof Text) {
                    Text t = (Text) n;
                    String nodeIP = t.getText();
                    if(nodeIP.equals(ip)) {
                        node = s;
                        break;
                    }
                }
            }
        }
        return node;
   }

   public void showDataMovement(Path p, Color c) {
      final Rectangle rectPath = new Rectangle (0, 0, 40, 40);
      rectPath.setArcHeight(10);
      rectPath.setArcWidth(10);
      rectPath.setFill(c);
      root.getChildren().add(rectPath);

      PathTransition pathTransition = new PathTransition();
      pathTransition.setDuration(Duration.millis(2000));
      pathTransition.setPath(p);
      pathTransition.setNode(rectPath);
      pathTransition.setCycleCount(1);
      pathTransition.setAutoReverse(false);

      pathTransition.statusProperty().addListener(new ChangeListener<Status>() {

        @Override
        public void changed(ObservableValue<? extends Status> observableValue,
                        Status oldValue, Status newValue) {
                if(newValue != Status.RUNNING) {
                    root.getChildren().remove(rectPath);
                }
            }
        });

      pathTransition.play();

   }

   public void addRootNode() {
      Circle c = new Circle();
      c.setRadius(40.0f);
      c.setFill(Color.WHITE);
      c.setStroke(Color.BLACK);
      Text  t  =  new  Text("BTV");
      t.setBoundsType(TextBoundsType.VISUAL);
      t.setFont(Font.font("Verdana", FontWeight.BOLD, 8));
      rootNode = new StackPane();
      rootNode.getChildren().add(c);
      rootNode.getChildren().add(t);
      rootNode.setLayoutX(550.0f);
      rootNode.setLayoutY(500.0f);
      root.getChildren().add(rootNode);
   }

   public void peerConnectionEvent(String ip, boolean connecting, 
                                boolean connected, boolean disconnected) {
        if(connecting) {
            addNode(ip);
        }
        else if(connected) {
            // Draw connection lines and change colour to green
            nodeConnection(ip);
        }
        else if(disconnected) {
            // Remove the node.
            nodeDisconnection(ip);
        }
    }

    public void peerCommunicationEvent(String ip, boolean dataSent, 
                                        boolean dataReceived) {
        StackPane node = getNodeWithIP(ip);
        if(node != null) {
            ArrayList<Path> paths = transitions.get(node);
            if(dataSent) {
                showDataMovement(paths.get(0), Color.BLUE);
            }
            else if(dataReceived) {
                showDataMovement(paths.get(1), Color.GREEN);
            }
        }
    }
}

/*
    Event handling classes
*/
class MyConnectionListener implements PeerConnectionListener {
    private Visualisation parent;

    public MyConnectionListener(Visualisation v) {
        parent = v;
    }

    public void handlePeerConnectionEvent(PeerConnectionEvent e) {
        final String ip = e.getIP();
        final boolean connecting = e.connecting();
        final boolean connected = e.connected();
        final boolean disconnected = e.disconnected();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                parent.peerConnectionEvent(ip, connecting, connected, 
                                            disconnected);
            }
       }); 
    }
}

class MyCommunicationListener implements PeerCommunicationListener {
    private Visualisation parent;

    public MyCommunicationListener(Visualisation v) {
        parent = v;
    }

    public void handlePeerEvent(PeerCommunicationEvent e) {
        final String ip = e.getIP();
        final boolean dataSent = e.dataSent();
        final boolean dataReceived = e.dataReceived();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                parent.peerCommunicationEvent(ip, dataSent, dataReceived);
            }
       }); 
    }
}