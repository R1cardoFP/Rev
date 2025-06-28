package cliente;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClienteReversi extends Application {
    @Override
    public void start(Stage primaryStage) {
        new InterfaceJogo(primaryStage).mostrar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}