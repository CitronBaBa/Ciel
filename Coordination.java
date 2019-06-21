import javafx.beans.property.*;

public class Coordination
{   DoubleProperty x = new SimpleDoubleProperty(); 
    DoubleProperty y = new SimpleDoubleProperty();
    public Coordination(double x, double y)
    {   this.x.set(x); 
        this.y.set(y);
    }
    public double getX() { return x.getValue();}
    public double getY() { return y.getValue();}
    public DoubleProperty getXProperty(){ return x;}
    public DoubleProperty getYProperty(){ return y;}
    public void setX(double x) { this.x.set(x);}
    public void setY(double y) { this.y.set(y);}
}
