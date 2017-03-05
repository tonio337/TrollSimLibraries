package com.rfsoftware.tonio337.grid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Project: TrollSimInterface
 * Package: com.rfsoftware.tonio337.grid
 * Created by adlee on 2/21/2017.
 */
public class GridApp extends JApplet
    implements ChangeListener
{
    private static GridApp currentApp;

    public GridApp(){ super(); }
    public GridApp(Grid2D grid){
        this();
        setGrid(grid);
    }

    GridView gridView;

    int numObjects = 2;
    int changeCounter = 0;
    final int changeDelay = 5;

    public void init() {
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        currentApp = this;
    }

    public void start() {
        initComponents();
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Grid App");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JApplet gridApp = new GridApp();
        gridApp.init();
        gridApp.start();
        f.add("Center", gridApp);
        f.pack();
        f.setVisible(true);
    }

    public void initComponents() {

        setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.add("North",new JLabel("GridView"));
        JSlider markerSlider = new JSlider(60, 300, 100);
        markerSlider.setMinorTickSpacing(20);
        markerSlider.setMajorTickSpacing(60);
        markerSlider.setPaintTicks(true);
        markerSlider.setPaintLabels(true);
        markerSlider.addChangeListener(this);
        p.add("North",markerSlider);
        add("Center", p);

        gridView = new GridView();
        gridView.setupGrid(numObjects);

        p.add("Center", new JScrollPane(gridView,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));

    }

    public void stateChanged(ChangeEvent e) {
        JSlider slider = (JSlider)e.getSource();
        if (changeCounter++ > changeDelay){
            gridView.tick();
            changeCounter = 0;
        }
        gridView.setMarkerSize(slider.getValue()/2);
    }

    public GridApp setGrid(Grid2D grid) {
        if (grid != null){
            gridView.grid = grid;
            gridView.setupGrid(numObjects);
        }
        return this;
    }

    static public GridApp getCurrentApp(){
        return currentApp;
    }

    static class GridView extends Component {

        Grid2D grid;
        Player2DDemo[] objects;

        int markerSize = 5;
        double scale = 1;

        public void setupGrid(int numObjects){

            String[] names = {"Anna", "Berry", "Charlie", "Dave", "Edna",
                                "Florence", "Ginger", "Holly", "Icarus", "Juniper"};

            // if grid does not exist, create a new one
            if (grid == null)
                grid = new Grid2D(0,300,0,300,30,30);

            // if grid object list does not exist, create a new one with random objects
            if (grid.gridObject2DList.size() == 0) {
                objects = new Player2DDemo[numObjects];
                //TODO: setup random number of objects

                for (int o = 0; o < objects.length; o++) {
                    objects[o] = new Player2DDemo(names[o] + " " + o / names.length + 1, grid);
                    objects[o].setMyBearingTo(objects[0]);
                    objects[o].setFieldOfVision(70);
                }
            }
            setMarkerSize(50);
        }

        void setMarkerSize(int mSize) { markerSize = mSize/2; scale = (double)mSize/50; repaint(); }

        public Dimension getPreferredSize(){
            return (grid == null ? new Dimension(450, 125) :
                    new Dimension((int)(grid.getxMax()-grid.getxMin()+10),
                            (int)(grid.getyMax()-grid.getyMin())+10));
        }

        public void tick() {
            Player2DDemo.assimilateTick(grid);
            if (Player2DDemo.numAlive(grid) <= 1) {
                // TODO: End the game
            }
        }

        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            Dimension size = getSize();

            Font font = new Font("Serif", Font.PLAIN, 12);
            g.setFont(font);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // draw grid

            g2.scale(scale,scale);

            for (double y = grid.getyMin(); y <= grid.getyMax(); y += grid.getyStep())
                for (double x = grid.getxMin(); x <= grid.getxMax(); x += grid.getxStep()) {
                    g.drawLine((int)x,(int)grid.getyMin(),(int)x,(int)grid.getyMax());
                    g.drawLine((int)grid.getxMin(),(int)y,(int)grid.getxMax(),(int)y);
                }

            // draw objects

            Iterator<Player2DDemo> players = grid.gridObject2DList.iterator();
            while (players.hasNext()) {
                Player2DDemo player = players.next();

                // draw oval
                int centerX = (int)player.location().x;
                int centerY = (int)player.location().y;

                // oval coords start at top left of shape
                int ovalX = centerX-markerSize/2;
                int ovalY = centerY-markerSize/2;

                g.setColor(Color.CYAN);
                g.fillOval(ovalX, ovalY, markerSize, markerSize);

                // TODO: translate bearing before drawing bearing related objects
                // Will likely need a static getBearingX/Y function

                double mapBearing = Player2D.Bearing.translate(
                        new Player2D.Bearing(player.getBearing(), Player2D.Bearing.Direction.UP, Player2D.Bearing.Orientation.CLOCKWISE),
                        Player2D.Bearing.Direction.DOWN, Player2D.Bearing.Orientation.CLOCKWISE);

                // draw bearing
                int bx = (int) (Player2D.bearingX(mapBearing)*markerSize/2);
                int by = (int) (Player2D.bearingY(mapBearing)*markerSize/2);

                g.setColor(Color.MAGENTA);
                g.drawLine(centerX,centerY,centerX+bx,centerY+by);

                //draw cone of vision

                int fovHalf = (int) (player.fieldOfVision/2);
                int dist = (int) player.sightDistance;
                g.setColor(Color.RED);

                int bxRight = (int) (Player2D.bearingX(mapBearing,fovHalf)*dist);
                int byRight = (int) (Player2D.bearingY(mapBearing,fovHalf)*dist);
                g.drawLine(centerX,centerY,centerX+bxRight,centerY+byRight);

                int bxLeft = (int) (Player2D.bearingX(mapBearing,-fovHalf)*dist);
                int byLeft = (int) (Player2D.bearingY(mapBearing,-fovHalf)*dist);
                g.drawLine(centerX,centerY,centerX+bxLeft,centerY+byLeft);

                g.setColor(Color.BLACK);
                double arcDegrees = Player2D.Bearing.translate(
                        new Player2D.Bearing(player.getBearing(), Player2D.Bearing.Direction.UP, Player2D.Bearing.Orientation.CLOCKWISE),
                        Player2D.Bearing.Direction.RIGHT, Player2D.Bearing.Orientation.COUNTERCLOCKWISE);
                g2.drawArc(ovalX,ovalY,markerSize,markerSize,(int)arcDegrees-fovHalf,fovHalf*2);
            }

            // draw helper text on top of everything else
            players = grid.gridObject2DList.iterator();
            while (players.hasNext()) {
                Player2DDemo player = players.next();
                g2.drawString(player.name() + " - " + String.format("%.2f", player.getBearing()),
                        (int) player.location().x,
                        (int) player.location().y);
            }
        }
    }
}

class Player2DDemo extends Player2D implements Grid.DeltaOI<Grid2D.Location2D>{
    int score;
    boolean alive;
    final int maxSpeed = 5;

    Player2DDemo(String name, Grid2D grid) {
        super(name, grid);
        score = 0;
        alive = true;
    }

    static int numAlive(Grid2D grid){
        int count = 0;

        Iterator<Player2DDemo> players = grid.gridObject2DList.iterator();
        while (players.hasNext()) {
            Player2DDemo player = players.next();
            if (player.alive)
                count++;
        }

        return count;
    }

    void tick(){
        // find closest player
        Player2DDemo closest = this;
        Iterator<Player2DDemo> players = baseGrid.gridObject2DList.iterator();
        while (players.hasNext()){
            Player2DDemo other = players.next();
            if (other.alive && distance(other) < distance(closest)) closest = other;
        }


        // move towards them up to within touch range
        if (closest == this) return;
        moveTo(closest);
    }

    static void assimilateTick(Grid2D grid){
        Iterator<Player2DDemo> players = grid.gridObject2DList.iterator();
        while (players.hasNext()) {
            Player2DDemo player = players.next();
            player.tick();
        }
    }

    @Override
    public boolean moveTo(Grid2D.Location2D target) {
        moveTo(target,maxSpeed);
        return true;
    }

    @Override
    public boolean moveTo(Grid2D.Location2D target, double maxSpeed) {
        return true;
    }

    public boolean moveTo(Player2DDemo player){
        moveTo(player,maxSpeed);
        return true;
    }

    public boolean moveTo(Player2DDemo target, double maxSpeed) {
        if (this == target) return false;
        setMyBearingTo(target);

        double moveDist = 0;
        // if within touch pathDistance, cover target
        if (distance(target) < TOUCH_DISTANCE) {
            setLocation(target);
            return true;
        }
        // if within touch pathDistance after move, move to just inside touch pathDistance
        else if (distance(target) < maxSpeed + TOUCH_DISTANCE)
            moveDist = distance(target) - TOUCH_DISTANCE;
        else moveDist = maxSpeed;

        setLocation(getBearingX()*moveDist, getBearingY()*moveDist);

        return true;
    }
}
