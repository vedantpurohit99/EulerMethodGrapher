import java.util.*;

public class EulersMethodPointsGenerator{
    InfixCalculator differentialEquation;
    double xMin, xMax, xSkip;
    Point initial;

    public EulersMethodPointsGenerator(String differential, double xMin, double xMax, double xSkip, Point initial){
        this.xMin = xMin; this.xMax = xMax; this.xSkip = xSkip; this.initial = initial;
        this.differentialEquation = new InfixCalculator(differential);
    }

    public Point extrapolate(double xValue){
        double prevMin = xMin, prevMax = xMax;
        xMin = xValue-xSkip; xMax = xValue;
        ArrayList<Point> points = generatePoints();
        System.out.println("Extrapolations = " + points);
        Point p = points.get(points.size()-2);
        xMin = prevMin; xMax = prevMax;
        if(xValue < xMin) p = points.get(1);
        return p;
    }
    
    public ArrayList<Point> generatePoints(){        
        ArrayList<Point> points = new ArrayList<Point>();
        points.add(initial);
        initial.slope = slope(initial);

        double currentSlope;
        if(xMin <= initial.x && initial.x <= xMax){
            Point currentPoint = initial;
            while(xMin <= currentPoint.x && currentPoint.x <= xMax){
                Point nextPoint = new Point(currentPoint.x + xSkip, 0);
                currentSlope = slope(currentPoint);
                nextPoint.y = currentPoint.y + currentSlope*xSkip; nextPoint.slope = currentSlope;
                points.add(nextPoint);

                currentPoint = nextPoint;
            }

            currentPoint = initial; //xSkip = -xSkip;
            while(xMin <= currentPoint.x && currentPoint.x <= xMax){
                Point nextPoint = new Point(currentPoint.x - xSkip, 0);
                currentSlope = slope(currentPoint);
                nextPoint.y = currentPoint.y - slope(currentPoint)*xSkip; nextPoint.slope = slope(nextPoint);
                points.add(0, nextPoint);

                currentPoint = nextPoint;
            }
        } else if(initial.x < xMin){
            Point currentPoint = initial;
            while(currentPoint.x <= xMax){
                Point nextPoint = new Point(currentPoint.x + xSkip, 0);
                currentSlope = slope(currentPoint);
                nextPoint.y = currentPoint.y + slope(currentPoint)*xSkip; nextPoint.slope = slope(nextPoint);
                points.add(nextPoint);

                currentPoint = nextPoint;
            }
        } else{
            Point currentPoint = initial;
            while(xMin <= currentPoint.x){
                Point nextPoint = new Point(currentPoint.x - xSkip, 0);
                currentSlope = slope(currentPoint);
                nextPoint.y = currentPoint.y - slope(currentPoint)*xSkip; nextPoint.slope = slope(nextPoint);
                points.add(0, nextPoint);

                currentPoint = nextPoint;
            }
        }

        return points;
    }

    public double slope(Point p){
        return differentialEquation.calculate(p.x, p.y);
        //return p.x*p.x - p.y*p.y;
    }

    public static void main(String[] args) throws Exception{
        //EulersMethodPointsGenerator g = new EulersMethodPointsGenerator("x^2 - y^2", 2, 4, 0.2, new Point(2, 1));
        //EulersMethodPointsGenerator g = new EulersMethodPointsGenerator("x+y", -5, 5, 0.2, new Point(0, 1));
        EulersMethodPointsGenerator g = new EulersMethodPointsGenerator("y", -5, 5, 1, new Point(2, 2));
        ArrayList<Point> a = g.generatePoints();
        for(int i=0; i<a.size(); i++) System.out.println(a.get(i));
        //System.out.println(g.generatePoints());
    }
}
