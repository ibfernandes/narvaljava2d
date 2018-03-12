package editor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GLCapabilities;

import engine.engine.Engine;
import engine.engine.Window;
import engine.utilities.BufferUtilities;

import static org.lwjgl.opengl.GL11.*;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Editor extends Application implements Runnable{

    private Stage primaryStage;
    private VBox rootLayout;
    private GraphicsContext gc;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("AddressApp");

        initFxml();

        Window w;
		w = new Window(1280,720,"Engine Early Alpha");
		
		Engine e;
		e = new Engine(w);
		e.init();
        
		new AnimationTimer(){
			public void handle(long currentNanoTime){
				e.update();
				e.render();

				render();
			}
	    }.start();
    }

    /**
     * Inicializa o root layout (layout base).
     */
    public void initFxml() {
        try {
            // Carrega o root layout do arquivo fxml.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/root.fxml"));
            rootLayout = (VBox) loader.load();
            
      
            
            // Mostra a scene (cena) contendo o root layout.
            Scene scene = new Scene(rootLayout);
            
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.setScene(scene);
            primaryStage.show();
            Canvas c = (Canvas) rootLayout.getChildren().get(1);
            gc = c.getGraphicsContext2D();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    public void invertRows(int[] array) {
    	for(int i = 0; i < array.length / 2; i++) {
    	    int temp = array[i];
    	    array[i] = array[array.length - i - 1];
    	    array[array.length - i - 1] = temp;
    	}
    	
    }
    
    public int[] invertCols(int arr[], int rowSize) {
    	int colSize = arr.length/ rowSize;
    	int buffer[] = new int[rowSize*colSize];
    	
    	for(int k =0; k<colSize; k++) {
	    	for(int i = 0; i< rowSize; i++) {
	    		buffer[i+ k*rowSize] = arr[k*rowSize -i-1+rowSize];
	    	}
    	}
    	return buffer;
    }

	public void render() {
		IntBuffer ib = BufferUtilities.createIntBuffer(1280*720);
		int buffer[] = new int[1280*720];

		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		ib.clear();
		
        GL11.glReadPixels(0, 0, 1280, 720, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, ib);
        ib.get(buffer);
       // buffer = invertRows(buffer, 1280);
        invertRows(buffer);
        buffer = invertCols(buffer, 1280);

        PixelWriter pw = gc.getPixelWriter();
        PixelFormat<IntBuffer> pf = PixelFormat.getIntArgbInstance();
        
        pw.setPixels(0, 0, 1280, 720, pf, buffer, 0, 1280);
    }

	@Override
	public void run() {
		launch();
	}
}