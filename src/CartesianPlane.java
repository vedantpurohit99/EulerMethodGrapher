import javafx.application.*;
import javafx.beans.binding.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.scene.control.*;
import javafx.event.*;
import javafx.scene.input.*;
import java.util.*;
import java.util.function.*;

public class CartesianPlane extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    Plot plot;
    GridPane layout;
    TextField differentialEquation, initialValue, stepValue, xValue, yValue, yInterpolation;
    TextArea eulerPoints, slopes, solutions;
    Button generateSolution, help;
    Stage helpWindow;

    double xMin=-5, xMax=5, yMin=-5, yMax=5, xIncr=1, yIncr=1, zoomFactor=0.75;
    int pxSize = 800;
    public void start(Stage stage) {
        plot = new Plot(new ArrayList<Point>(), new Axes(pxSize, pxSize, xMin, xMax, yMin, yMax));

        layout = new GridPane();
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setHgap(20); layout.setVgap(10);
        layout.setPadding(new Insets(30, 50, 30, 50));
        layout.setStyle("-fx-background-color: rgb(35, 39, 50);");
        layout.add(plot, 0, 0, 6, 2);

        eulerPoints = new TextArea(); eulerPoints.setEditable(false);
        eulerPoints.setPrefRowCount(20); eulerPoints.setPrefColumnCount(10);
        eulerPoints.setPrefHeight(pxSize); eulerPoints.setPrefWidth(150);
        layout.add(eulerPoints, 6, 1);
        Label pointsList = new Label("Euler points"); pointsList.setStyle("-fx-text-fill: rgb(248, 248, 242)");
        layout.add(pointsList, 6, 0);

        slopes = new TextArea(); slopes.setEditable(false);
        slopes.setPrefRowCount(20); slopes.setPrefColumnCount(10);
        slopes.setPrefHeight(pxSize); slopes.setPrefWidth(150);
        layout.add(slopes, 7, 1);
        Label slopesList = new Label("Slopes"); slopesList.setStyle("-fx-text-fill: rgb(248, 248, 242)");
        layout.add(slopesList, 7, 0);

        eulerPoints.scrollTopProperty().bindBidirectional(slopes.scrollTopProperty());

        xValue = new TextField();
        Label xLabel = new Label("Estimate y at x = "); xLabel.setStyle("-fx-text-fill: rgb(248, 248, 242)");
        layout.add(xLabel, 6, 3); layout.add(xValue, 6, 4);
        xValue.setOnKeyPressed(new EventHandler<KeyEvent>(){
                public void handle(KeyEvent e){
                    if(e.getCode().equals(KeyCode.ENTER)){
                        double x = 0, y = 0;
                        try{
                            x = new InfixCalculator(xValue.getText()).calculate(0, 0);
                        } catch(Exception ex){ ex.printStackTrace(); return; }

                        try{
                            y = plot.interpolate(x);
                            plot.refresh(x, y);
                            yValue.setText(y+""); xValue.setText(x+"");
                        } catch(Exception ex){
                            Point p = plot.pointsGenerator.extrapolate(x);
                            System.out.println("Extrapolated point = "+p);
                            y = p.y;
                            yValue.setText(y+""); xValue.setText(x+"");
                            ex.printStackTrace();
                        }
                    }
                }
            });

        yValue = new TextField(); yValue.setEditable(false);
        Label yLabel = new Label("Estimated y = "); yLabel.setStyle("-fx-text-fill: rgb(248, 248, 242)");
        layout.add(yLabel, 7, 3); layout.add(yValue, 7, 4);

        yInterpolation = new TextField();
        solutions = new TextArea(); solutions.setEditable(false);
        solutions.setPrefRowCount(1);
        Label solutionsLabel = new Label("Interpolate solutions for y = ");
        solutionsLabel.setStyle("-fx-text-fill: rgb(248, 248, 242)");
        Label interpolationsLabel = new Label("Interpolated solutions points = ");
        interpolationsLabel.setStyle("-fx-text-fill: rgb(248, 248, 242)");
        layout.add(solutionsLabel, 0, 5); layout.add(yInterpolation, 0, 6);
        layout.add(interpolationsLabel, 1, 5);
        layout.add(solutions, 2, 5, 3, 2);
        yInterpolation.setOnKeyPressed(new EventHandler<KeyEvent>(){
                public void handle(KeyEvent e){
                    if(e.getCode().equals(KeyCode.ENTER)){
                        try{
                            double y = new InfixCalculator(yInterpolation.getText()).calculate(0, 0);
                            ArrayList<Point> solutionPoints = plot.solve(y);
                            plot.refresh(solutionPoints);
                            solutions.setText(solutionPoints.toString().substring(1, solutionPoints.toString().length()-1));
                        } catch(Exception ex){
                            ex.printStackTrace();
                            solutions.setText(""); yInterpolation.requestFocus();
                        }
                    }
                }
            });

        help = new Button("?"); initialiseHelp();
        help.setOnAction(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent e){
                    helpWindow.show();
                }
            });
        layout.add(help, 5, 4);

        differentialEquation = new TextField("");
        differentialEquation.setOnKeyPressed(new EventHandler<KeyEvent>(){
                public void handle(KeyEvent e){
                    if(e.getCode().equals(KeyCode.ENTER)) graphSolution();
                }
            });
        layout.add(differentialEquation, 0, 4);

        initialValue = new TextField("");
        initialValue.setOnKeyPressed(new EventHandler<KeyEvent>(){
                public void handle(KeyEvent e){
                    if(e.getCode().equals(KeyCode.ENTER)) graphSolution();
                }
            });
        layout.add(initialValue, 1, 4);

        stepValue = new TextField("");
        stepValue.setOnKeyPressed(new EventHandler<KeyEvent>(){
                public void handle(KeyEvent e){
                    if(e.getCode().equals(KeyCode.ENTER)) graphSolution();
                }
            });
        layout.add(stepValue, 2, 4);

        Label diff = new Label("Differential dy/dx = "); diff.setStyle("-fx-text-fill: rgb(248, 248, 242)");
        Label init = new Label("Initial point (x0, y0) "); init.setStyle("-fx-text-fill: rgb(248, 248, 242)");
        Label step = new Label("Step value"); step.setStyle("-fx-text-fill: rgb(248, 248, 242)");
        layout.add(diff, 0, 3);
        layout.add(init, 1, 3);
        layout.add(step, 2, 3);

        generateSolution = new Button("Generate Solution Curve");
        generateSolution.setOnAction(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent e){
                    graphSolution();
                }
            });
        layout.add(generateSolution, 3, 4);

        stage.setTitle("Euler's Method");
        stage.setScene(new Scene(layout, Color.rgb(35, 39, 50)));
        stage.show();

        differentialEquation.requestFocus();
    }

    public void initialiseHelp(){
        helpWindow = new Stage();

        GridPane layout = new GridPane();
        Label instructions = new Label();

        instructions.setText("Multiplication sign must be explicitly typed in for multiplication (so 5*x instead of 5x).\n\n"+
                             "Functions supported are sin cos tg tan arcsin arccos arctg arctan sqrt lg log ln^ + - * /\n"+
                             "Constants supported are e pi\n"+
                             "    Brackets need to be placed when using the functions up to and including ln (so sin(pi) as opposed to sin pi). Only round brackets are to be used.\n"+
                             "    In any functions that involves 2 operators (so ^ + - * /), omitting an argument would be same as having 0 as the missing argument (so 5^ means 5^0 and *3 means 0*3)\n\n"+
                             "Negative numbers can be entered by placing a single – sign before the number (or variable).\n\n"+
                             "By default, program tracks values of y based mouse location for x. Hold down CTRL and hover over graph to solve for possible x based on mouse location for y.\n"+
                             "    Enter the x-value in the x \"Estimate y at x\" textbox and press enter to approximate y at x\n"+
                             "    Enter the y-value in the solve for x textbox and press enter to interpolate solutions for x\n"+
                             "Interpolation only works for range currently displayed\n\n"+
                             "Mathematical expressions can be entered for all textfields (i.e. it is valid to enter \"5*pi+2\" as interpolation value for step value)\n\n\n"+
                             "Program created by 薛定谔的老花猫 (lim495062), Abdul Arif, Vedant Purohit and Raymond Hao for MCV 4U0");
        instructions.setStyle("-fx-text-fill: rgb(248, 248, 242)");

        layout = new GridPane();
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setHgap(20); layout.setVgap(10);
        layout.setPadding(new Insets(30, 50, 30, 50));
        layout.setStyle("-fx-background-color: rgb(35, 39, 50);");
        layout.add(instructions, 0, 0);

        helpWindow.setTitle("Instructions");
        helpWindow.setScene(new Scene(layout, Color.rgb(35, 39, 50)));
    }

    private void graphSolution(){
        Point initialPoint = null; double step = 0;

        if(differentialEquation.getText().equals("")){
            differentialEquation.requestFocus(); return;
        }

        if(initialValue.getText().equals("")){
            initialValue.requestFocus(); return;
        } else{
            try{ initialPoint = new Point(initialValue.getText()); }
            catch(Exception e){ initialValue.requestFocus(); return; }
        }

        if(stepValue.getText().equals("")){
            stepValue.requestFocus(); return;
        } else{
            try{
                step = new InfixCalculator(stepValue.getText()).calculate(0, 0);
                if(step == 0) throw new Exception();
            }
            catch(Exception e){ stepValue.requestFocus(); return; }
        }

        System.out.println("Generate solution");
        layout.getChildren().remove(plot);
        plot.axes = new Axes(pxSize, pxSize,
                             xMin, xMax,
                             yMin, yMax);
        EulersMethodPointsGenerator g = new EulersMethodPointsGenerator(differentialEquation.getText(),
                                                                        plot.axes.xLow, plot.axes.xHi, step, initialPoint);
        plot.pointsGenerator = g;


        plot.refresh();
        layout.add(plot, 0, 0, 6, 2);

        /*String pointsText = "";
        for(Point p : plot.points) pointsText += p.toString()+"\n";
        eulerPoints.setText(pointsText);
        System.out.println(pointsText);*/

        differentialEquation.requestFocus();
    }

    class Axes extends Pane {
        private NumberAxis xAxis;
        private NumberAxis yAxis;

        public double xLow, xHi, yLow, yHi;

        private double getTickUnit(double range){
            /*double tickPower = 0;
            if(range == 10) tickPower = 0;
            else tickPower = Math.floor(Math.log10(range/10));

            double[] tickSizes = new double[]{1*Math.pow(10, tickPower), 2*Math.pow(10, tickPower), 5*Math.pow(10, tickPower)};
            System.out.println(Arrays.toString(tickSizes));

            double tickUnit = tickSizes[0];
            for(double tick : tickSizes){
                if(Math.abs(tickUnit*10 - range) > Math.abs(tick*10 - range)) tickUnit = tick;
            }

            if(tickUnit >= 1) tickUnit = new Double(tickUnit).intValue();
            System.out.printf("Tick unit = %f\n", tickUnit);

            return tickSizes[0];*/ return 1;
        }

        public Axes(int width, int height,
                    double xLow, double xHi,
                    double yLow, double yHi) {
            this.xLow = xLow; this.xHi = xHi; this.yLow = yLow; this.yHi = yHi;

            setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
            setPrefSize(width, height);
            setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);


            xAxis = new NumberAxis();//xLow, xHi, getTickUnit(Math.abs(xHi-xLow)));
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(xLow); xAxis.setUpperBound(xHi);
            xAxis.setTickUnit(getTickUnit(Math.abs(xHi-xLow)));
            xAxis.setMinorTickVisible(false);
            xAxis.setSide(Side.BOTTOM);
            xAxis.setPrefWidth(width);
            if(yLow*yHi < 0)
                xAxis.setLayoutY(height - Math.abs(yLow / (yHi - yLow)) * height);
            else if(yLow > 0 && yHi > 0) xAxis.setLayoutY(height);
            else xAxis.setLayoutY(0);
            getChildren().add(xAxis);

            yAxis = new NumberAxis();//yLow, yHi, getTickUnit(Math.abs(yHi-yLow)));
            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(yLow); yAxis.setUpperBound(yHi);
            yAxis.setTickUnit(getTickUnit(Math.abs(yHi-yLow)));
            yAxis.setMinorTickVisible(false);
            yAxis.setSide(Side.LEFT);
            yAxis.setPrefHeight(height);
            if(xLow*xHi < 0)
                yAxis.layoutXProperty().bind(Bindings.subtract(1+(width * (Math.abs(xLow / (xHi - xLow)))),
                                                               yAxis.widthProperty()));
            else if(xLow < 0 && xHi < 0)
                yAxis.layoutXProperty().bind(Bindings.subtract(1+width, yAxis.widthProperty()));
            else yAxis.layoutXProperty().bind(Bindings.subtract(1, yAxis.widthProperty()));
            getChildren().add(yAxis);
        }

        public NumberAxis getXAxis() {
            return xAxis;
        }

        public NumberAxis getYAxis() {
            return yAxis;
        }
    }

    class Plot extends Pane {
        ArrayList<Point> points = new ArrayList<Point>();
        Axes axes;

        EulersMethodPointsGenerator pointsGenerator;

        double zoomFactor = 40, shiftLimit = 1;

        Point initDrag = null;
        public Plot(ArrayList<Point> points, Axes a){
            this.points = points; this.axes = a;
            refresh();

            this.setOnScroll(new EventHandler<ScrollEvent>(){
                    public void handle(ScrollEvent e){
                        System.out.println(e.getDeltaY());

                        double xRange = axes.xHi - axes.xLow,
                            yRange = axes.yHi - axes.yLow;

                        if(e.getDeltaY() < 0){ xRange /= 0.75; yRange /= 0.75; zoomFactor *= 0.75;  shiftLimit /= 0.75; }
                        else{ xRange *= 0.75; yRange *= 0.75; zoomFactor /= 0.75; shiftLimit *= 0.75; }

                        axes = new Axes(pxSize, pxSize,
                                        (axes.xLow+axes.xHi)/2 - xRange/2, (axes.xLow+axes.xHi)/2 + xRange/2,
                                        (axes.yLow+axes.yHi)/2 - yRange/2, (axes.yLow+axes.yHi)/2 + yRange/2);
                        if(pointsGenerator != null){
                            pointsGenerator.xMin = axes.xLow;
                            pointsGenerator.xMax = axes.xHi;
                        }

                        refresh();
                    }
                });

            this.setOnMouseDragged(new EventHandler<MouseEvent>(){
                    public void handle(MouseEvent e){
                        if(initDrag == null)
                            initDrag = new Point(e.getX(), e.getY());

                        Point current = new Point(e.getX(), e.getY());

                        double xDrag = current.x-initDrag.x, yDrag = current.y-initDrag.y;
                        xDrag = -xDrag;

                        if(Math.abs(xDrag) > shiftLimit || Math.abs(yDrag) > shiftLimit) initDrag = null;

                        axes = new Axes(pxSize, pxSize,
                                        axes.xLow + xDrag/zoomFactor, axes.xHi + xDrag/zoomFactor,
                                        axes.yLow + yDrag/zoomFactor, axes.yHi + yDrag/zoomFactor);
                        if(pointsGenerator != null){
                            pointsGenerator.xMin = axes.xLow;
                            pointsGenerator.xMax = axes.xHi;
                        }

                        xValue.setText(""); yValue.setText("");
                        refresh();
                    }
                });

            this.setOnMouseReleased(new EventHandler<MouseEvent>(){
                    public void handle(MouseEvent e){
                        initDrag = null;
                    }
                });

            this.setOnDragDone(new EventHandler<DragEvent>(){
                    public void handle(DragEvent e){
                        initDrag = null;
                    }
                });

            this.setOnDragOver(new EventHandler<DragEvent>(){
                    public void handle(DragEvent e){
                        initDrag = null;
                    }
                });

            this.setOnMouseMoved(new EventHandler<MouseEvent>(){
                    public void handle(MouseEvent e){
                        System.out.printf("\nCurrent mouse location: (%f, %f)\n", e.getSceneX(), e.getSceneY());
                        System.out.printf("Current point: (%f, %f)\n",
                                          (e.getSceneX()-50)/pxSize*(axes.xHi-axes.xLow) + axes.xLow,
                                          -((e.getSceneY()-42)/pxSize*(axes.yHi-axes.yLow)) + axes.yHi);
                        try{
                            if(e.isControlDown()){
                                double y = -((e.getSceneY()-42)/pxSize*(axes.yHi-axes.yLow)) + axes.yHi;
                                ArrayList<Point> solutionPoints = solve(y);
                                refresh(solutionPoints);
                                yInterpolation.setText(y+"");
                                solutions.setText(solutionPoints.toString().substring(1, solutionPoints.toString().length()-1));
                            } else{
                                double
                                    x = (e.getSceneX()-50)/pxSize*(axes.xHi-axes.xLow) + axes.xLow,
                                    y = interpolate(x);
                                refresh(x, y);

                                xValue.setText(x+""); yValue.setText(y+"");
                            }
                        } catch(Exception ex){}//ex.printStackTrace(); }
                    }
                });
        }


        public void refresh(){
            refresh(new ArrayList<Point>());
        }

        public void refresh(double x, double y){
            ArrayList<Point> l = new ArrayList<Point>(); l.add(new Point(x, y));
            refresh(l);
        }

        public void refresh(ArrayList<Point> interpolations){//double... xy){
            if(pointsGenerator != null){
                pointsGenerator.xMin = axes.xLow;
                pointsGenerator.xMax = axes.xHi;

                ArrayList<Point> pastPoints = new ArrayList<Point>();
                for(Point p : points) pastPoints.add(p);

                try{ points = pointsGenerator.generatePoints(); }
                catch(Exception e){ points = pastPoints; }

                //System.out.printf("start = %f, end = %f\n", pointsGenerator.xMin, pointsGenerator.xMax);
            }

            Path path = new Path();
            path.setStroke(Color.ORANGE.deriveColor(0, 1, 1, 0.6));
            path.setStrokeWidth(2);

            path.setClip(new Rectangle(0, 0, axes.getPrefWidth(), axes.getPrefHeight()));

            //System.out.println("points = " + points);
            if(points.size() > 0){
                double x = points.get(0).x;
                double y = points.get(0).y;
                path.getElements().add(new MoveTo(mapX(x, axes), mapY(y, axes)));

                for(int i=0; i<points.size(); i++){
                    x = points.get(i).x;
                    y = points.get(i).y;
                    path.getElements().add(new LineTo(mapX(x, axes), mapY(y, axes)));
                }
            }

            if(points != null && eulerPoints != null && slopes != null){
                String pointsText = "", slopesText = ""; boolean padding = false;
                for(Point p : points){
                    if(p.toString().length() > 21) padding = true;
                    pointsText += p.toString()+"\n";
                }
                eulerPoints.setText(pointsText);
                //System.out.println(pointsText);
                for(Point p : points) slopesText += String.format("%f\n", p.slope);
                if(padding) slopesText += "                                      ";
                slopes.setText(slopesText);
            }

            setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
            setPrefSize(axes.getPrefWidth(), axes.getPrefHeight());
            setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

            //if(xy.length != 0){
            System.out.println("\ninterpolations: "+interpolations);
            getChildren().setAll(axes, path);
            if(interpolations.size() != 0){
                try{
                    for(int i=0; i<interpolations.size(); i++){
                        double
                            currentX = interpolations.get(i).x, //xy[0], //(e.getSceneX()-50)/pxSize*(axes.xHi-axes.xLow) + axes.xLow,
                            currentY = interpolations.get(i).y; // xy[1]; //interpolate(currentX);

                        // if(!(axes.xLow <= currentX && currentX <= axes.xHi) ||
                        //    !(axes.yLow <= currentY && currentY <= axes.yHi)) throw new Exception();

                        Path vertical = new Path();
                        vertical.setStroke(Color.LIGHTPINK);//Color.ORANGE.deriveColor(0, 1, 1, 0.6));
                        vertical.setStrokeWidth(1);
                        vertical.setClip(new Rectangle(0, 0, axes.getPrefWidth(), axes.getPrefHeight()));
                        vertical.getElements().add(new MoveTo(mapX(currentX, axes), 0));
                        vertical.getElements().add(new LineTo(mapX(currentX, axes), pxSize));

                        Path horizontal = new Path();
                        horizontal.setStroke(Color.LIGHTPINK);//Color.ORANGE.deriveColor(0, 1, 1, 0.6));
                        horizontal.setStrokeWidth(1);
                        horizontal.setClip(new Rectangle(0, 0, axes.getPrefWidth(), axes.getPrefHeight()));
                        horizontal.getElements().add(new MoveTo(0, mapY(currentY, axes)));
                        horizontal.getElements().add(new LineTo(pxSize, mapY(currentY,axes)));

                        //getChildren().setAll(axes, path, vertical, horizontal);
                        getChildren().add(vertical); getChildren().add(horizontal);

                        System.out.printf("Focused (%f, %f)\n",
                                          currentX, currentY);
                    }
                } catch(Exception ex){
                    ex.printStackTrace();
                    getChildren().setAll(axes, path);
                }
            } else getChildren().setAll(axes, path);
        }

        public double interpolate(double x) throws Exception{
            if(!(axes.xLow <= x && x <= axes.xHi)) throw new Exception();

            for(int i=0; i<points.size()-1; i++){
                if(points.get(i).x <= x && x <= points.get(i+1).x)
                    return points.get(i).y + Math.abs(x - points.get(i).x) * ((points.get(i+1).y - points.get(i).y) /
                                                                              (points.get(i+1).x - points.get(i).x));
            }

            throw new Exception();
        }

        public ArrayList<Point> solve(double y) throws Exception{
            //System.out.println(points);
            if(!(axes.yLow <= y && y <= axes.yHi)) throw new Exception();

            ArrayList<Point> solutions = new ArrayList<Point>();
            for(int i=0; i<points.size()-1; i++){
                if(points.get(i).y == y) solutions.add(points.get(i));
                else if(points.get(i+1).y == y) solutions.add(points.get(i+1));
                else if((points.get(i).y <= y && y <= points.get(i+1).y) ||
                        (points.get(i+1).y <= y && y <= points.get(i).y))
                    solutions.add(new Point(points.get(i).x +
                                            Math.abs(points.get(i+1).x - points.get(i).x) * ((y - points.get(i).y) /
                                                                                             (points.get(i+1).y - points.get(i).y)),
                                            y));
            } return solutions;
        }

        public double mapX(double x, Axes axes) {
            double
                xLow = axes.getXAxis().getLowerBound(),
                xHi = axes.getXAxis().getUpperBound();
            double tx = axes.getPrefWidth() * (Math.abs(xLow / (xHi - xLow)));
            if(xLow > 0 && xHi > 0) tx = -tx;
            //else if(xLow < 0 && xHi < 0) tx = axes.getPrefWidth() + tx; // * (xLow*xHi>0 ? -1:1);
            double sx = axes.getPrefWidth() /
                (axes.getXAxis().getUpperBound() -
                 axes.getXAxis().getLowerBound());

            //System.out.println((x) * sx + tx);
            return (x) * sx + tx;
        }

        public double invMapX(double x, Axes axes){
            double
                xLow = axes.getXAxis().getLowerBound(),
                xHi = axes.getXAxis().getUpperBound();
            double tx = axes.getPrefWidth() * (Math.abs(xLow / (xHi - xLow)));
            if(xLow > 0 && xHi > 0) tx = -tx;
            //else if(xLow < 0 && xHi < 0) tx = axes.getPrefWidth() + tx; // * (xLow*xHi>0 ? -1:1);
            double sx = axes.getPrefWidth() /
                (axes.getXAxis().getUpperBound() -
                 axes.getXAxis().getLowerBound());

            //System.out.println((x) * sx + tx);
            return (x-tx)/sx;
        }

        public double mapY(double y, Axes axes) {
            double
                yLow = axes.getYAxis().getLowerBound(),
                yHi = axes.getYAxis().getUpperBound();
            double ty = axes.getPrefHeight() * (1-Math.abs(yLow / (yHi - yLow)));
            //else if(yLow < 0 && yHi < 0) ty = axes.getPrefHeight() + ty; // * (yLow*yHi>0 ? -1:1);
            double sy = axes.getPrefHeight() /
                (axes.getYAxis().getUpperBound() -
                 axes.getYAxis().getLowerBound());
            if(yLow > 0 && yHi > 0) ty = axes.getPrefHeight() + yLow * sy;

            //System.out.printf("%f, %f\n", y, -(y) * sy + ty);
            return -(y) * sy + ty;
        }

        public double invMapY(double y, Axes axes) {
            double
                yLow = axes.getYAxis().getLowerBound(),
                yHi = axes.getYAxis().getUpperBound();
            double ty = axes.getPrefHeight() * (1-Math.abs(yLow / (yHi - yLow)));
            //else if(yLow < 0 && yHi < 0) ty = axes.getPrefHeight() + ty; // * (yLow*yHi>0 ? -1:1);
            double sy = axes.getPrefHeight() /
                (axes.getYAxis().getUpperBound() -
                 axes.getYAxis().getLowerBound());
            if(yLow > 0 && yHi > 0) ty = axes.getPrefHeight() + yLow * sy;

            //System.out.printf("%f, %f\n", y, -(y) * sy + ty);
            return -(y-ty)/sy;
            //return -(y) * sy + ty;
        }
    }
}
