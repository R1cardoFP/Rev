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

    private String nomeJogadorLocal = "";
    private String nomeJogadorAdversario = "Aguardando...";

    public InterfaceJogo(Stage stage) {
        this.stage = stage;
        this.grelha = new GridPane();
        this.tabuleiro = new Tabuleiro();
        this.temporizadorLabel = new Label("Tempo restante: ");
        grelha.setGridLinesVisible(false);
    }

    public void mostrar() {
        mostrarJanelaConexao();
    }

    private void mostrarJanelaConexao() {
        VBox caixa = new VBox(14);
        caixa.setPadding(new Insets(30));
        caixa.setAlignment(Pos.CENTER);
        caixa.setStyle("-fx-background-color: linear-gradient(to bottom, #ece9e6, #ffffff); -fx-border-radius: 12px; -fx-background-radius: 12px;");

        TextField ipField = new TextField("");
        ipField.setPromptText("Endereço IP do servidor");
        ipField.setStyle("-fx-font-size: 15px; -fx-background-radius: 8px;");

        TextField portaField = new TextField("");
        portaField.setPromptText("Porta");
        portaField.setStyle("-fx-font-size: 15px; -fx-background-radius: 8px;");

        TextField nomeJogadorField = new TextField();
        nomeJogadorField.setPromptText("Nome do jogador");
        nomeJogadorField.setStyle("-fx-font-size: 15px; -fx-background-radius: 8px;");

        Button conectarBtn = new Button("Conectar");
        conectarBtn.setStyle(
            "-fx-background-color: #8B5C2A; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-pref-width: 160px; -fx-pref-height: 40px;"
        );
        conectarBtn.setOnMouseEntered(e -> conectarBtn.setStyle(
            "-fx-background-color: #B0B0B0; -fx-text-fill: #222; -fx-font-size: 17px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-pref-width: 160px; -fx-pref-height: 40px;"
        ));
        conectarBtn.setOnMouseExited(e -> conectarBtn.setStyle(
            "-fx-background-color: #8B5C2A; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-pref-width: 160px; -fx-pref-height: 40px;"
        ));

        Label erroLabel = new Label();
        erroLabel.setTextFill(Color.RED);
        erroLabel.setStyle("-fx-font-size: 13px;");

        caixa.getChildren().addAll(ipField, portaField, nomeJogadorField, conectarBtn, erroLabel);

        Scene cenaConexao = new Scene(caixa, 340, 270);
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

            nomeJogadorLocal = nomeJogador; // Salva o nome local

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
        VBox caixa = new VBox(18);
        caixa.setPadding(new Insets(40));
        caixa.setAlignment(Pos.CENTER);
        caixa.setStyle("-fx-background-color: linear-gradient(to bottom, #ece9e6, #ffffff); -fx-border-radius: 12px; -fx-background-radius: 12px;");

        Label esperaLabel = new Label("A aguardar outro jogador para iniciar o jogo...");
        esperaLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #8B5C2A;");

        caixa.getChildren().add(esperaLabel);

        Scene cenaEspera = new Scene(caixa, 400, 180);
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

            // Enviar o nome do jogador ao servidor
            saida.println(nomeJogador);

            String cor = entrada.readLine();
            if (cor == null || cor.isEmpty()) {
                throw new IOException("Não foi possível obter a cor do servidor.");
            }
            minhaCor = cor.charAt(0);

            mostrarJanelaEspera();

            // Thread para receber mensagens do servidor
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = entrada.readLine()) != null) {
                        if (msg.equals("COMEÇAR")) {
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
                        } else if (msg.startsWith("NOME_ADVERSARIO ")) {
                            nomeJogadorAdversario = msg.substring("NOME_ADVERSARIO ".length());
                            Platform.runLater(this::atualizarCabecalhoJogadores);
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

    // Elementos para atualizar nomes e contagem de peças
    private Label nomesJogadoresLabel = new Label();
    private Label contagemPecasLabel = new Label();

    private void mostrarJanelaJogo() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f6f3ee, #e9e4d9);");

        VBox topo = new VBox();
        topo.setAlignment(Pos.CENTER);
        topo.setSpacing(8);

        nomesJogadoresLabel = new Label();
        nomesJogadoresLabel.setStyle("-fx-font-size: 2.5vw; -fx-font-weight: bold; -fx-text-fill: #7a4c1e;");
        atualizarCabecalhoJogadores();

        contagemPecasLabel = new Label();
        contagemPecasLabel.setStyle("-fx-font-size: 2vw; -fx-font-weight: bold; -fx-text-fill: #444;");
        atualizarContagemPecas();

        temporizadorLabel.setStyle("-fx-font-size: 1.7vw; -fx-text-fill: #8B5C2A;");

        HBox botoes = new HBox(30);
        botoes.setAlignment(Pos.CENTER);
        botoes.setPadding(new Insets(10, 0, 10, 0));

        Button confirmarBtn = new Button("Confirmar Jogada");
        Button regrasBtn = new Button("Regras");
        Button sairBtn = new Button("Sair");

        // Responsividade dos botões
        confirmarBtn.setMaxWidth(Double.MAX_VALUE);
        regrasBtn.setMaxWidth(Double.MAX_VALUE);
        sairBtn.setMaxWidth(Double.MAX_VALUE);

        String estiloBtn = "-fx-background-color: #8B5C2A; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 1.7vw; -fx-background-radius: 10px;";
        String estiloBtnHover = "-fx-background-color: #B0B0B0; -fx-text-fill: #222; -fx-font-weight: bold; -fx-font-size: 1.7vw; -fx-background-radius: 10px;";

        confirmarBtn.setStyle(estiloBtn);
        regrasBtn.setStyle(estiloBtn);
        sairBtn.setStyle(estiloBtn);

        confirmarBtn.setOnMouseEntered(e -> confirmarBtn.setStyle(estiloBtnHover));
        confirmarBtn.setOnMouseExited(e -> confirmarBtn.setStyle(estiloBtn));
        regrasBtn.setOnMouseEntered(e -> regrasBtn.setStyle(estiloBtnHover));
        regrasBtn.setOnMouseExited(e -> regrasBtn.setStyle(estiloBtn));
        sairBtn.setOnMouseEntered(e -> sairBtn.setStyle(estiloBtnHover));
        sairBtn.setOnMouseExited(e -> sairBtn.setStyle(estiloBtn));

        botoes.getChildren().addAll(confirmarBtn, regrasBtn, sairBtn);

        // Responsividade dos botões no HBox
        HBox.setHgrow(confirmarBtn, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(regrasBtn, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(sairBtn, javafx.scene.layout.Priority.ALWAYS);

        topo.getChildren().addAll(nomesJogadoresLabel, contagemPecasLabel, temporizadorLabel, botoes);
        root.setTop(topo);

        grelha.setStyle("-fx-background-color: #8B5C2A; -fx-border-color: #333; -fx-border-width: 3px; -fx-border-radius: 8px;");
        root.setCenter(grelha);

        Scene cenaJogo = new Scene(root, 540, 630);
        stage.setScene(cenaJogo);
        stage.setTitle("Jogo Reversi");
        stage.show();

        // Responsividade do tabuleiro
        grelha.prefWidthProperty().bind(root.widthProperty());
        grelha.prefHeightProperty().bind(root.heightProperty().subtract(topo.heightProperty()).subtract(40));

        root.widthProperty().addListener((obs, oldVal, newVal) -> atualizarTabuleiro());
        root.heightProperty().addListener((obs, oldVal, newVal) -> atualizarTabuleiro());

        atualizarTabuleiro();

        grelha.setOnMouseClicked(e -> {
            if (!meuTurno) return;
            double cellSize = getCellSize();
            int coluna = (int) (e.getX() / cellSize);
            int linha = (int) (e.getY() / cellSize);
            if (tabuleiro.jogadaValida(linha, coluna, minhaCor)) {
                jogadaLinha = linha;
                jogadaColuna = coluna;
                atualizarTabuleiro();
            } else {
                mostrarAlertaBonito("Jogada inválida", "Essa jogada não é válida. Tente outra posição.", Alert.AlertType.WARNING);
            }
        });

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
                mostrarAlertaBonito("Selecione uma jogada", "Selecione uma posição válida no tabuleiro antes de confirmar.", Alert.AlertType.INFORMATION);
            }
        });

        regrasBtn.setOnAction(e -> {
            mostrarAlertaBonito(
                "Regras do Reversi",
                "1. O objetivo é ter mais peças da sua cor no final do jogo.\n" +
                "2. Só pode jogar onde capturar pelo menos uma peça adversária.\n" +
                "3. As peças capturadas mudam para a sua cor.\n" +
                "4. O jogo termina quando não há mais jogadas possíveis.",
                Alert.AlertType.INFORMATION
            );
        });

        sairBtn.setOnAction(e -> {
            Platform.exit();
        });
    }

    // Calcula o tamanho da célula do tabuleiro de acordo com o espaço disponível
    private double getCellSize() {
        double largura = grelha.getWidth();
        double altura = grelha.getHeight();
        if (largura == 0 || altura == 0) {
            largura = stage.getScene().getWidth();
            altura = stage.getScene().getHeight() - 180;
        }
        return Math.min(largura, altura) / 8.0;
    }

    private void atualizarTabuleiro() {
        grelha.getChildren().clear();
        double cellSize = getCellSize();
        for (int linha = 0; linha < 8; linha++) {
            for (int coluna = 0; coluna < 8; coluna++) {
                Rectangle r = new Rectangle(cellSize, cellSize);
                if ((linha + coluna) % 2 == 0) {
                    r.setFill(Color.web("#B0B0B0"));
                } else {
                    r.setFill(Color.web("#8B5C2A"));
                }
                r.setArcWidth(cellSize * 0.24);
                r.setArcHeight(cellSize * 0.24);
                r.setStroke(Color.web("#333"));
                r.setStrokeWidth(cellSize * 0.025);
                grelha.add(r, coluna, linha);

                char peca = tabuleiro.getPeca(linha, coluna);
                if (peca != '-') {
                    Circle c = new Circle(cellSize * 0.4);
                    c.setFill(peca == 'B' ? Color.BLACK : Color.WHITE);
                    c.setStroke(Color.web("#555"));
                    c.setStrokeWidth(cellSize * 0.09);
                    grelha.add(c, coluna, linha);
                } else if (meuTurno && tabuleiro.jogadaValida(linha, coluna, minhaCor)) {
                    if (linha == jogadaLinha && coluna == jogadaColuna) {
                        Circle marcador = new Circle(cellSize * 0.4);
                        marcador.setFill(Color.web("#FFD70080"));
                        grelha.add(marcador, coluna, linha);
                    } else {
                        Circle marcador = new Circle(cellSize * 0.14);
                        marcador.setFill(Color.web("#FFD700B0"));
                        grelha.add(marcador, coluna, linha);
                    }
                }
            }
        }
        atualizarContagemPecas();
    }

    private void atualizarCabecalhoJogadores() {
        String corLocal = minhaCor == 'B' ? "Pretas" : "Brancas";
        String corAdv = minhaCor == 'B' ? "Brancas" : "Pretas";
        nomesJogadoresLabel.setText(
            nomeJogadorLocal + " (" + corLocal + ")  vs  " +
            nomeJogadorAdversario + " (" + corAdv + ")"
        );
    }

    private void atualizarContagemPecas() {
        int pretas = tabuleiro.contarPecas('B');
        int brancas = tabuleiro.contarPecas('W');
        contagemPecasLabel.setText("⚫ Pretas: " + pretas + "   |   ⚪ Brancas: " + brancas);
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
                    mostrarAlertaBonito("Tempo esgotado", "Tempo esgotado! A sua vez foi passada.", Alert.AlertType.WARNING);
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

    // Adicione este método à classe InterfaceJogo
    private void mostrarAlertaBonito(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.getDialogPane().setStyle(
            "-fx-background-color: linear-gradient(to bottom, #ece9e6, #ffffff); " +
            "-fx-font-size: 15px; -fx-font-family: 'Segoe UI', sans-serif; " +
            "-fx-border-color: #8B5C2A; -fx-border-width: 2px; -fx-border-radius: 10px;"
        );
        alert.showAndWait();
    }
}
