package cliente;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import modelo.Tabuleiro;

import java.io.*;
import java.net.Socket;

public class InterfaceJogo {
    private final Stage stage;
    private final GridPane grelha;
    private final Tabuleiro tabuleiro;
    private final Label temporizadorLabel;
    private Timeline temporizador;
    private int tempoRestante;

    private char minhaCor;
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter saida;
    private boolean meuTurno = false;

    // Guardar a jogada selecionada
    private int jogadaLinha = -1;
    private int jogadaColuna = -1;

    public InterfaceJogo(Stage stage) {
        this.stage = stage;
        this.grelha = new GridPane();
        this.tabuleiro = new Tabuleiro();
        this.temporizadorLabel = new Label("Tempo restante: ");
    }

    public void mostrar() {
        mostrarJanelaConexao();
    }

    private void mostrarJanelaConexao() {
        VBox caixa = new VBox(10);
        caixa.setPadding(new Insets(20));
        caixa.setAlignment(Pos.CENTER);

        TextField ipField = new TextField("");
        ipField.setPromptText("Endereço IP do servidor");

        TextField portaField = new TextField("");
        portaField.setPromptText("Porta");

        TextField nomeJogadorField = new TextField();
        nomeJogadorField.setPromptText("Nome do jogador");

        Button conectarBtn = new Button("Conectar");

        Label erroLabel = new Label();
        erroLabel.setTextFill(Color.RED);

        caixa.getChildren().addAll(ipField, portaField, nomeJogadorField, conectarBtn, erroLabel);

        Scene cenaConexao = new Scene(caixa, 300, 220);
        stage.setScene(cenaConexao);
        stage.setTitle("Conectar ao Servidor");
        stage.show();

        conectarBtn.setOnAction(e -> {
            String ip = ipField.getText().trim();
            String portaTexto = portaField.getText().trim();
            String nomeJogador = nomeJogadorField.getText().trim();

            if (ip.isEmpty() || portaTexto.isEmpty() || nomeJogador.isEmpty()) {
                erroLabel.setText("Por favor, preencha todos os campos.");
                return;
            }

            int porta;
            try {
                porta = Integer.parseInt(portaTexto);
            } catch (NumberFormatException ex) {
                erroLabel.setText("Porta inválida.");
                return;
            }

            ligarAoServidor(ip, porta, nomeJogador);
        });
    }

    private void mostrarJanelaEspera() {
        VBox caixa = new VBox(15);
        caixa.setPadding(new Insets(30));
        caixa.setAlignment(Pos.CENTER);

        Label esperaLabel = new Label("A aguardar outro jogador para iniciar o jogo...");
        esperaLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        caixa.getChildren().add(esperaLabel);

        Scene cenaEspera = new Scene(caixa, 350, 150);
        Platform.runLater(() -> {
            stage.setScene(cenaEspera);
            stage.setTitle("Esperar pelo outro jogador");
            stage.show();
        });
    }

    private void ligarAoServidor(String ip, int porto, String nomeJogador) {
        try {
            socket = new Socket(ip, porto);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            saida = new PrintWriter(socket.getOutputStream(), true);

            // Enviar o nome do jogador ao servidor (se o protocolo aceitar)
            saida.println(nomeJogador);

            String cor = entrada.readLine();
            if (cor == null || cor.isEmpty()) {
                throw new IOException("Não foi possível obter a cor do servidor.");
            }
            minhaCor = cor.charAt(0);

            // Mostrar janela de espera depois da conexão e cor atribuída
            mostrarJanelaEspera();

            // Thread para receber mensagens do servidor
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = entrada.readLine()) != null) {
                        if (msg.equals("COMEÇAR")) {
                            // Quando receber esta mensagem, mostrar o tabuleiro e começar o jogo
                            Platform.runLater(() -> mostrarJanelaJogo());
                        } else if (msg.startsWith("JOGADA")) {
                            String[] partes = msg.split(" ");
                            int linha = Integer.parseInt(partes[1]);
                            int coluna = Integer.parseInt(partes[2]);
                            char corJogada = partes[3].charAt(0);
                            tabuleiro.jogar(linha, coluna, corJogada);
                            Platform.runLater(this::atualizarTabuleiro);
                        } else if (msg.equals("FIM")) {
                            pararTemporizador();
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setHeaderText("Fim do Jogo");
                                int pretas = tabuleiro.contarPecas('B');
                                int brancas = tabuleiro.contarPecas('W');
                                alert.setContentText("Pretas: " + pretas + "\nBrancas: " + brancas);
                                alert.showAndWait();
                            });
                        } else if (msg.equals("SUA_VEZ")) {
                            meuTurno = true;
                            iniciarTemporizador();
                            Platform.runLater(this::atualizarTabuleiro);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText("Erro de comunicação com o servidor");
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    });
                }
            }).start();

        } catch (IOException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Erro a ligar ao servidor");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            });
        }
    }

    private void mostrarJanelaJogo() {
        BorderPane root = new BorderPane();

        // Título no topo
        VBox topo = new VBox();
        topo.setAlignment(Pos.CENTER);
        topo.setSpacing(5);
        Label titulo = new Label("Jogo Reversi");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        topo.getChildren().addAll(titulo, temporizadorLabel);
        root.setTop(topo);

        root.setCenter(grelha);

        // Botões castanhos em baixo
        HBox botoes = new HBox(20);
        botoes.setAlignment(Pos.CENTER);
        botoes.setPadding(new Insets(15, 0, 15, 0));

        Button confirmarBtn = new Button("Confirmar Jogada");
        Button regrasBtn = new Button("Regras");
        Button sairBtn = new Button("Sair");

        String estiloCastanho = "-fx-background-color: #8B5C2A; -fx-text-fill: white; -fx-font-weight: bold;";

        confirmarBtn.setStyle(estiloCastanho);
        regrasBtn.setStyle(estiloCastanho);
        sairBtn.setStyle(estiloCastanho);

        botoes.getChildren().addAll(confirmarBtn, regrasBtn, sairBtn);
        root.setBottom(botoes);

        Scene cenaJogo = new Scene(root, 420, 500);
        stage.setScene(cenaJogo);
        stage.setTitle("Jogo Reversi");
        stage.show();

        atualizarTabuleiro();

        // Selecionar jogada ao clicar no tabuleiro
        grelha.setOnMouseClicked(e -> {
            if (!meuTurno) return;
            int coluna = (int) (e.getX() / 50);
            int linha = (int) (e.getY() / 50);
            if (tabuleiro.jogadaValida(linha, coluna, minhaCor)) {
                jogadaLinha = linha;
                jogadaColuna = coluna;
                atualizarTabuleiro();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Jogada inválida");
                alert.setHeaderText(null);
                alert.setContentText("Essa jogada não é válida. Tente outra posição.");
                alert.showAndWait();
            }
        });

        // Confirmar jogada
        confirmarBtn.setOnAction(e -> {
            if (!meuTurno) return;
            if (jogadaLinha != -1 && jogadaColuna != -1) {
                saida.println("JOGADA " + jogadaLinha + " " + jogadaColuna);
                meuTurno = false;
                pararTemporizador();
                jogadaLinha = -1;
                jogadaColuna = -1;
                atualizarTabuleiro();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Selecione uma jogada");
                alert.setHeaderText(null);
                alert.setContentText("Selecione uma posição válida no tabuleiro antes de confirmar.");
                alert.showAndWait();
            }
        });

        // Mostrar regras
        regrasBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Regras do Reversi");
            alert.setHeaderText("Como jogar Reversi");
            alert.setContentText(
                "1. O objetivo é ter mais peças da sua cor no final do jogo.\n" +
                "2. Só pode jogar onde capturar pelo menos uma peça adversária.\n" +
                "3. As peças capturadas mudam para a sua cor.\n" +
                "4. O jogo termina quando não há mais jogadas possíveis."
            );
            alert.showAndWait();
        });

        // Sair do jogo
        sairBtn.setOnAction(e -> {
            Platform.exit();
        });
    }

    private void atualizarTabuleiro() {
        grelha.getChildren().clear();
        for (int linha = 0; linha < 8; linha++) {
            for (int coluna = 0; coluna < 8; coluna++) {
                Rectangle r = new Rectangle(50, 50);
                r.setFill(Color.GREEN);
                r.setStroke(Color.BLACK);
                grelha.add(r, coluna, linha);

                char peca = tabuleiro.getPeca(linha, coluna);
                if (peca != '-') {
                    Circle c = new Circle(20);
                    c.setFill(peca == 'B' ? Color.BLACK : Color.WHITE);
                    grelha.add(c, coluna, linha);
                } else if (meuTurno && tabuleiro.jogadaValida(linha, coluna, minhaCor)) {
                    // Destacar jogada selecionada
                    if (linha == jogadaLinha && coluna == jogadaColuna) {
                        Circle marcador = new Circle(20);
                        marcador.setFill(Color.web("#FFD70080")); // amarelo transparente
                        grelha.add(marcador, coluna, linha);
                    } else {
                        Circle marcador = new Circle(5);
                        marcador.setFill(Color.YELLOW);
                        grelha.add(marcador, coluna, linha);
                    }
                }
            }
        }
    }

    private void iniciarTemporizador() {
        pararTemporizador();
        tempoRestante = 30;
        temporizadorLabel.setText("Tempo restante: " + tempoRestante);

        temporizador = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            tempoRestante--;
            temporizadorLabel.setText("Tempo restante: " + tempoRestante);
            if (tempoRestante <= 0) {
                pararTemporizador();
                meuTurno = false;
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Tempo esgotado");
                    alert.setHeaderText(null);
                    alert.setContentText("Tempo esgotado! A sua vez foi passada.");
                    alert.showAndWait();
                });
                saida.println("TEMPO_ESGOTADO");
            }
        }));
        temporizador.setCycleCount(Timeline.INDEFINITE);
        temporizador.play();
    }

    private void pararTemporizador() {
        if (temporizador != null) {
            temporizador.stop();
        }
    }
}
