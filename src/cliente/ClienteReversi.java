package cliente;

// Importa as classes necessárias da biblioteca JavaFX
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Esta é a classe principal do lado do cliente para o jogo Reversi.
 *
 * Ela usa a biblioteca JavaFX para criar uma interface gráfica (janela com botões, tabuleiro, etc.).
 * A classe estende `Application`, o que significa que é uma aplicação gráfica em Java.
 */
public class ClienteReversi extends Application {

    /**
     * Este método é chamado automaticamente quando a aplicação gráfica começa.
     * Aqui é onde a interface do jogo é criada e mostrada ao utilizador.
     *
     * @param primaryStage A janela principal (o "ecrã" da aplicação).
     */
    @Override
    public void start(Stage primaryStage) {
        // Cria um novo objecto da interface do jogo e mostra-o na janela principal
        new InterfaceJogo(primaryStage).mostrar();
    }

    /**
     * Este é o ponto de entrada da aplicação — o programa começa a correr a partir daqui.
     * O método `launch` inicia a aplicação JavaFX, que por sua vez chama o método `start`.
     *
     * @param args Argumentos da linha de comandos (não são usados aqui, mas podem ser)
     */
    public static void main(String[] args) {
        launch(args); // Lança a aplicação gráfica
    }
}
