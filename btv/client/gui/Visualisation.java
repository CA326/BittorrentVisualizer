/*
    This is our Visualisation class. It is associated with a Torrent.
    It will use PeerConnection and PeerCommunication listeners to show
    the flow of data involved with the download.

    Peers are shown as circles with their IP labled on the Circle.
    Circles are Red for connecting peers and Green for established connections.
    Data coming from and going to peers is shown via transitions. 
    We have different colour transition nodes for the different type of
    bittorrent messages. PeerCommunicationEvents have a message type
    which we use to get the colour of the transition from a predefined array.

    TODO: Need to take into account, viewing visualisation in the 
    middle of a download. Need to get list of already connected peers.

*/
package btv.client.gui;

import btv.event.peer.PeerConnectionListener;
import btv.event.peer.PeerConnectionEvent;
import btv.event.peer.PeerCommunicationListener;
import btv.event.peer.PeerCommunicationEvent;
import btv.download.peer.Peer;

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

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

import javax.swing.*;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

class Visualisation extends JFrame implements WindowListener {
    private BTVUI parent;
    private JFXPanel panel;
    private Group root; // The group that will hold our nodes.
    private Group key; // The group that will hold the key.
    private StackPane rootNode;
    private ArrayList<StackPane> nodes;
    private HashMap<StackPane, ArrayList> transitions;
    private static final Color [] transitionColors = new Color [] {
      Color.BLACK, Color.RED, Color.YELLOW, Color.MAGENTA, Color.PURPLE,
      Color.LIGHTBLUE, Color.GRAY, Color.BLUE, Color.GREEN, Color.PINK
    };
    private static final String [] messageTypes = new String [] {
      "Keep Alive", "Choke", "Unchoke", "Interested", "Not interested",
      "Have", "Bitfield", "Request", "Piece", "Cancel"
    };

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
        key = new Group();
        Scene scene = new Scene(root, 500, 500, Color.WHITE);
        addRootNode();
        setupKey();
        getAlreadyConnectedPeers();
        return scene;
    }

    public void setupKey() {

        key.setOpacity(0.1);
        StackPane connecting = createNode(40.0f, 30.0f, 20.0f, "Connecting",
                              Color.WHITE, Color.RED, Color.BLACK);
        StackPane connected = createNode(100.0f, 30.0f, 20.0f, "Connected",
                                Color.WHITE, Color.GREEN, Color.BLACK);
        key.getChildren().addAll(connecting, connected);

        for(int i = 0; i < messageTypes.length; i++) {
            StackPane messageType = createNode(160.0f + (i * 80.0f), 30.0f, 
                          60.0f, 60.0f, messageTypes[i], transitionColors[i],
                                  transitionColors[i], Color.WHITE);
            key.getChildren().add(messageType);
        }

        key.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                FadeTransition ft = new FadeTransition(Duration.millis(1000), key);
                ft.setFromValue(key.getOpacity());
                ft.setToValue(1.0);
                ft.setCycleCount(1);
                ft.setAutoReverse(false);
                ft.play();
            } 
        });

        key.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                FadeTransition ft = new FadeTransition(Duration.millis(1000), key);
                ft.setFromValue(key.getOpacity());
                ft.setToValue(0.1);
                ft.setCycleCount(1);
                ft.setAutoReverse(false);
                ft.play();
            } 
        });

        root.getChildren().add(key);
    }

    public void getAlreadyConnectedPeers() {
        /*
            If we start the Visualisation for a download in progress
            we need to get the peers already connected.
        */
        ArrayList<Peer> alreadyConnected = parent.getConnections(name);
        for(Peer p : alreadyConnected) {
            if(p.connected()) {
                addPeer(p.getIP());
                nodeConnection(p.getIP());
            }
        }
    }

    public StackPane createNode(double x, double y, double r, String text, 
                              Color fill, Color outline, Color textColor) {
      /*
          Create a StackPane with a circle and text to add to the window.
      */

      Circle c = new Circle();
      c.setRadius(r);
      c.setFill(fill);
      c.setStroke(outline);
      Text t = createText(text, textColor);
      Node [] nodes = new Node [] {c, t};
      StackPane s = createStackPane(nodes);
      s.setLayoutX(x);
      s.setLayoutY(y);
      return s;
   }

   public StackPane createNode(double x, double y, double len, double wid,
                        String text, Color fill, Color outline, Color textColor) {
      Rectangle r = new Rectangle();
      r.setWidth(wid);
      r.setHeight(len);
      r.setFill(fill);
      r.setStroke(outline);
      Text t = createText(text, textColor);
      Node [] nodes = new Node [] {r, t};
      StackPane s = createStackPane(nodes);
      s.setLayoutX(x);
      s.setLayoutY(y);
      return s;

   }

   public Text createText(String text, Color color) {
      Text  t  =  new  Text(text);
      t.setBoundsType(TextBoundsType.VISUAL);
      t.setFont(Font.font("Verdana", FontWeight.BOLD, 8));
      t.setFill(color);
      return t;
   } 

   public StackPane createStackPane(Node [] nodes) {
      StackPane s = new StackPane();
      s.getChildren().addAll(nodes);
      return s;
   }

   public void addPaths(StackPane s) {
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

   public void addPeer(String ip) {
      double x = (80.0f * nodes.size()) + 50.0f;
      double y = 100.0f;
      StackPane s = createNode(x, y, 40.0f, ip, Color.WHITE, Color.RED,
                               Color.BLACK);
      root.getChildren().add(s);
      nodes.add(s);
      addPaths(s);
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
      rootNode = createNode(550.0f, 700.0f, 40.0f, "BTV", Color.WHITE, 
                            Color.BLACK, Color.BLACK);
      root.getChildren().add(rootNode);
   }

   public void peerConnectionEvent(String ip, boolean connecting, 
                                boolean connected, boolean disconnected) {
        if(connecting) {
            addPeer(ip);
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
                                        boolean dataReceived, int messageType) {
        StackPane node = getNodeWithIP(ip);
        if(node != null) {
            ArrayList<Path> paths = transitions.get(node);
            if(dataSent) {
                showDataMovement(paths.get(0), transitionColors[messageType + 1]);
            }
            else if(dataReceived) {
                showDataMovement(paths.get(1), transitionColors[messageType + 1]);
            }
        }
    }

    public void windowClosing(WindowEvent e) {
        dispose();
    }

    public void windowClosed(WindowEvent e) {
        
    }

    public void windowOpened(WindowEvent e) {
        
    }

    public void windowIconified(WindowEvent e) {
        
    }

    public void windowDeiconified(WindowEvent e) {
        
    }

    public void windowActivated(WindowEvent e) {
        
    }

    public void windowDeactivated(WindowEvent e) {
        
    }

    public void windowGainedFocus(WindowEvent e) {
        
    }

    public void windowLostFocus(WindowEvent e) {
        
    }

    public void windowStateChanged(WindowEvent e) {
        
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
        final int messageType = e.getMessageType();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                parent.peerCommunicationEvent(ip, dataSent, dataReceived, 
                                              messageType);
            }
       }); 
    }
}