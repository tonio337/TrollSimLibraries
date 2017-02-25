package com.rfsoftware.tonio337.grid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

    WeatherView weatherView;
    GridView gridView;

    int changeCounter = 0;
    final int changeDelay = 5;

    public void init() {
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);
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
        JSlider tempSlider = new JSlider(20, 100, 65);
        tempSlider.setMinorTickSpacing(5);
        tempSlider.setMajorTickSpacing(20);
        tempSlider.setPaintTicks(true);
        tempSlider.setPaintLabels(true);
        tempSlider.addChangeListener(this);
        p.add(tempSlider);
        add("Center", p);

        //weatherView = new WeatherView();
        gridView = new GridView();
        gridView.setupGrid(10);
        //p.add("Center", weatherView);
        p.add("South", gridView);

    }

    public void stateChanged(ChangeEvent e) {
        JSlider slider = (JSlider)e.getSource();
        //weatherView.setTemperature(slider.getValue());
        if (changeCounter++ > changeDelay){
            gridView.tick();
            changeCounter = 0;
        }
        gridView.setMarkerSize(slider.getValue());
    }

    static class WeatherView extends Component {

    int temperature = 65;

    String[] conditions = { "Snow", "Rain", "Cloud", "Sun"};
    Color textColor = Color.yellow;
    String condStr = "";
    String feels = "";

    void setTemperature(int temp) {
        temperature = temp;
        repaint();
    }

    public Dimension getPreferredSize(){
        return new Dimension(450, 125);
    }

    void setupText(String s1, String s2) {
        if (temperature <= 32) {
            textColor = Color.blue;
            feels = "Freezing";
        } else if (temperature <= 50) {
            textColor = Color.green;
            feels = "Cold";
        } else if (temperature <= 65) {
            textColor = Color.yellow;
            feels = "Cool";
        } else if (temperature <= 75) {
            textColor = Color.orange;
            feels = "Warm";
        } else {
            textColor = Color.red;
            feels = "Hot";
        }
        condStr = s1;
        if (s2 != null) {
            condStr += "/" + s2;
        }
    }

    void setupWeatherReport() {
        if (temperature <= 32) {
            setupText("Snow", null);
        } else if (temperature <= 40) {
            setupText("Snow", "Rain");
        } else if (temperature <= 50) {
            setupText("Rain", null);
        } else if (temperature <= 58) {
            setupText("Rain", "Cloud");
        }  else if (temperature <= 65) {
            setupText("Cloud", null);
        }  else if (temperature <= 75) {
            setupText("Cloud", "Sun");
        } else {
            setupText("Sun", null);
        }
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();

        setupWeatherReport();

        // Freezing, Cold, Cool, Warm, Hot,
        // Blue, Green, Yellow, Orange, Red
        Font font = new Font("Serif", Font.PLAIN, 36);
        g.setFont(font);

        String tempString = feels + " " + temperature+"F";
        FontRenderContext frc = ((Graphics2D)g).getFontRenderContext();
        Rectangle2D boundsTemp = font.getStringBounds(tempString, frc);
        Rectangle2D boundsCond = font.getStringBounds(condStr, frc);
        int wText = Math.max((int)boundsTemp.getWidth(), (int)boundsCond.getWidth());
        int hText = (int)boundsTemp.getHeight() + (int)boundsCond.getHeight();
        int rX = (size.width-wText)/2;
        int rY = (size.height-hText)/2;

        g.setColor(Color.LIGHT_GRAY);
        g2.fillRect(rX, rY, wText, hText);

        g.setColor(textColor);
        int xTextTemp = rX-(int)boundsTemp.getX();
        int yTextTemp = rY-(int)boundsTemp.getY();
        g.drawString(tempString, xTextTemp, yTextTemp);

        int xTextCond = rX-(int)boundsCond.getX();
        int yTextCond = rY-(int)boundsCond.getY() + (int)boundsTemp.getHeight();
        g.drawString(condStr, xTextCond, yTextCond);

    }
}

    static class GridView extends Component {

        Grid2D grid;
        Grid2D.Object2D[] objects;

        int markerSize = 5;

        public void setupGrid(int numObjects){
            grid = new Grid2D(0,300,0,300,30,30);
            objects = new Grid2D.Object2D[numObjects];
            //TODO: setup random number of objects

            for (int o = 0; o < objects.length; o++) {
                objects[o] = new Player2D(grid);
                objects[o].setMyBearingTo(objects[0]);
                objects[o].setFieldOfVision(70);
            }
            setMarkerSize(50);
        }

        void setMarkerSize(int mSize) { markerSize = mSize; repaint(); }

        public Dimension getPreferredSize(){
            return (grid == null ? new Dimension(450, 125) :
                    new Dimension((int)(grid.getxMax()-grid.getxMin()+10),
                            (int)(grid.getyMax()-grid.getyMin())+10));
        }

        public void tick() {

        }

        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            Dimension size = getSize();

            //setupWeatherReport();

            // Freezing, Cold, Cool, Warm, Hot,
            // Blue, Green, Yellow, Orange, Red
            Font font = new Font("Serif", Font.PLAIN, 36);
            g.setFont(font);

            // draw grid

            for (double y = grid.getyMin(); y <= grid.getyMax(); y += grid.getyStep())
                for (double x = grid.getxMin(); x <= grid.getxMax(); x += grid.getxStep()) {
                    g.drawLine((int)x,(int)grid.getyMin(),(int)x,(int)grid.getyMax());
                    g.drawLine((int)grid.getxMin(),(int)y,(int)grid.getxMax(),(int)y);
                }

            // draw objects

            for (Grid2D.Object2D obj : objects){
                int x = (int)obj.location().x;
                int y = (int)obj.location().y;

                int ox = x-markerSize/2;
                int oy = y-markerSize/2;

                int bx = (int) (obj.getBearingX()*markerSize/2);
                int by = (int) (obj.getBearingY()*markerSize/2);

                g.setColor(Color.CYAN);
                g.fillOval(ox, oy, markerSize, markerSize);

                // draw bearing
                g.setColor(Color.MAGENTA);
                g.drawLine(x,y,x+bx,y+by);

                //draw cone of vision
                int fovHalf = (int) (obj.fieldOfVision/2);
                int dist = (int) obj.sightDistance;
                g.setColor(Color.RED);

                int bxRight = (int) (obj.getBearingX(fovHalf)*dist);
                int byRight = (int) (obj.getBearingY(fovHalf)*dist);
                g.drawLine(x,y,x+bxRight,y+byRight);

                int bxLeft = (int) (obj.getBearingX(-fovHalf)*dist);
                int byLeft = (int) (obj.getBearingY(-fovHalf)*dist);
                g.drawLine(x,y,x+bxLeft,y+byLeft);
            }

            /*
            String tempString = feels + " " + temperature+"F";
            FontRenderContext frc = g2.getFontRenderContext();
            Rectangle2D boundsTemp = font.getStringBounds(tempString, frc);
            Rectangle2D boundsCond = font.getStringBounds(condStr, frc);
            int wText = Math.max((int)boundsTemp.getWidth(), (int)boundsCond.getWidth());
            int hText = (int)boundsTemp.getHeight() + (int)boundsCond.getHeight();
            int rX = (size.width-wText)/2;
            int rY = (size.height-hText)/2;

            g.setColor(Color.LIGHT_GRAY);
            g2.fillRect(rX, rY, wText, hText);

            g.setColor(textColor);
            int xTextTemp = rX-(int)boundsTemp.getX();
            int yTextTemp = rY-(int)boundsTemp.getY();
            g.drawString(tempString, xTextTemp, yTextTemp);

            int xTextCond = rX-(int)boundsCond.getX();
            int yTextCond = rY-(int)boundsCond.getY() + (int)boundsTemp.getHeight();
            g.drawString(condStr, xTextCond, yTextCond);
            */


        }
    }
}
