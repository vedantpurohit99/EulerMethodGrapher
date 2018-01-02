import java.io.*;

public class Point{
    double x, y, slope;
    public Point(double x, double y){ this.x = x; this.y = y; }
    public Point(String point){
        //String noBrackets = point.substring(1, point.length()-1);
        String[] xy = point.substring(1, point.length()-1).split(",");
        x = new InfixCalculator(xy[0]).calculate(0, 0);
        y = new InfixCalculator(xy[1]).calculate(0, 0);
    }
    public String toString(){ return String.format("(%f, %f)", x, y); }

    public static void main(String[] args) throws Exception{
        System.out.println(new Point("(1+2, 5/4)"));
    }
}
