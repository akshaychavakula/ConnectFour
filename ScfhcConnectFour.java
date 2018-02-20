/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scfhcconnectfour;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author aksha
 */
public class ScfhcConnectFour extends Application {
    
    private static final int tileSize = 80;
    private static final int columns = 7; 
    private static final int rows = 6;
    
    private boolean redMove = true;
    private Disc[][] grid  = new Disc[columns][rows];
    
    private Pane discRoot = new Pane();
    
    private Parent createContent(){
        Pane root = new Pane();
        root.getChildren().add(discRoot);
        
        Shape gridShape = makeGrid();
        root.getChildren().add(gridShape);
        root.getChildren().addAll(makeColumns());
        
        
        return root;
    }
    
    private Shape makeGrid(){
        Shape shape = new Rectangle((columns + 1) * tileSize, (rows + 1) * tileSize);
        
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                Circle circle = new Circle(tileSize / 2);
                circle.setCenterX(tileSize / 2);
                circle.setCenterY(tileSize / 2);
                circle.setTranslateX(j * (tileSize + 5) + tileSize / 4);
                circle.setTranslateY(i * (tileSize + 5) + tileSize / 4);
                
                shape = Shape.subtract(shape, circle);
            }
        }
        Light.Distant light = new Light.Distant();
        light.setAzimuth(45.0);
        light.setElevation(30.0);
        Lighting lighting = new Lighting();
        lighting.setLight(light);
        lighting.setSurfaceScale(5.0);
        
        shape.setFill(Color.BLUE);
        shape.setEffect(lighting);
        return shape;
    }
    
    private List<Rectangle> makeColumns(){
        List<Rectangle> list = new ArrayList<>();
        
        for(int i = 0; i < columns; i++){
            Rectangle rect = new Rectangle(tileSize , (rows + 1) * tileSize);
            rect.setTranslateX(i * (tileSize + 5) + tileSize / 4);
            rect.setFill(Color.TRANSPARENT);
            rect.setOnMouseEntered(e -> rect.setFill(Color.rgb(200, 200, 50, 0.3)));
            rect.setOnMouseExited(e -> rect.setFill(Color.TRANSPARENT));
            
            final int column = i;
            rect.setOnMouseClicked(e -> placeDisc(new Disc(redMove), column));
            
            
            
            list.add(rect);
        }
        
        return list;
    }
    
    private void placeDisc(Disc disc, int column){
        int row = rows - 1;
        do{
            if(!getDisc(column, row).isPresent())
                break;
            row--;
            
        } while( row >= 0);
        
        if(row < 0){
            return;
        }
        grid[column][row] = disc;
        discRoot.getChildren().add(disc);
        disc.setTranslateX(column * (tileSize + 5) + tileSize / 4);
        
        final int currentRow = row;
        TranslateTransition animation = new TranslateTransition(Duration.seconds(0.5), disc);
        animation.setToY(row * (tileSize + 5) + tileSize / 4);
        animation.setOnFinished(e -> {
            if(gameEnded(column, currentRow)){
                gameOver();
            }
            redMove = !redMove;
        });
        animation.play();
    }
    
    private boolean gameEnded(int column, int row){
        List<Point2D> vertical = IntStream.rangeClosed(row - 3, row + 3)
                .mapToObj(r -> new Point2D(column, r))
                .collect(Collectors.toList());
        
         List<Point2D> horizontal = IntStream.rangeClosed(column - 3, column + 3)
                .mapToObj(c -> new Point2D(c, row))
                .collect(Collectors.toList());
         
         Point2D topLeft = new Point2D(column - 3, row - 3);
         List<Point2D> diagonol1 = IntStream.rangeClosed(0, 6)
                 .mapToObj(i -> topLeft.add(i, i))
                 .collect(Collectors.toList());
         
         Point2D botLeft = new Point2D(column - 3, row + 3);
         List<Point2D> diagonol2 = IntStream.rangeClosed(0, 6)
                 .mapToObj(i -> botLeft.add(i, -i))
                 .collect(Collectors.toList());
         
         return checkRange(vertical) || checkRange(horizontal) || checkRange(diagonol1) || checkRange(diagonol2);
    }
    
    private boolean checkRange(List<Point2D> points){
        int chain = 0;
        
        for(Point2D p : points){
            int column = (int) p.getX();
            int row = (int) p.getY();
            
            Disc disc = getDisc(column, row).orElse(new Disc(!redMove));
            if(disc.red == redMove){
                chain++;
                if(chain == 4){
                    return true;
                }                
            }else{
                chain = 0;
            }
        }
        return false;
    }
    
    private void gameOver(){
        System.out.println("winner: " + (redMove ? "RED" : "YELLOW"));
    }
    
    private Optional<Disc> getDisc(int column, int row){
        if(column < 0 || column  >= columns || row < 0 || row >= rows){
            return Optional.empty();
        }
        
        return Optional.ofNullable(grid[column][row]);
    }
    
    private static class Disc extends Circle{
        private final boolean red;
        public Disc(boolean red){
            super(tileSize/2, red ? Color.RED : Color.YELLOW);
            this.red = red;
            
            setCenterX(tileSize / 2);
            setCenterY(tileSize / 2);
        }
    }
    
   
    
    @Override
    public void start(Stage primaryStage) {
       primaryStage.setScene(new Scene(createContent()));
       primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
