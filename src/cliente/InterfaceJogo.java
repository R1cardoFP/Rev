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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

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

    // Adicione estas variáveis para o chat
    private TextArea chatArea;
    private TextField chatInput;
    private Button chatSendBtn;

    // Adicione estas variáveis de instância para evitar erro de escopo
    private Label nomesJogadoresLabel;
    private Label contagemPecasLabel;

    public InterfaceJogo(Stage stage) {
        this.stage = stage;
        this.grelha = new GridPane();
        this.tabuleiro = new Tabuleiro();
        this.temporizadorLabel = new Label("");
        grelha.setGridLinesVisible(false);
    }

    public void mostrar() {
        mostrarJanelaConexao();
    }

    private void mostrarJanelaConexao() {
        VBox caixa = new VBox(18);
        caixa.setPadding(new Insets(36, 36, 36, 36));
        caixa.setAlignment(Pos.CENTER);
        caixa.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #f6f3ee, #e9e4d9);" +
            "-fx-border-radius: 18px; -fx-background-radius: 18px;" +
            "-fx-effect: dropshadow(gaussian, #8B5C2A55, 18, 0.2, 0, 4);"
        );

        Label titulo = new Label("Conectar ao Servidor Reversi");
        titulo.setStyle("-fx-font-size: 23px; -fx-font-weight: bold; -fx-text-fill: #8B5C2A; -fx-padding: 0 0 10 0;");

        TextField ipField = new TextField("");
        ipField.setPromptText("Endereço IP do servidor");
        ipField.setStyle("-fx-font-size: 16px; -fx-background-radius: 10px; -fx-padding: 8 12 8 12;");

        TextField portaField = new TextField("");
        portaField.setPromptText("Porta");
        portaField.setStyle("-fx-font-size: 16px; -fx-background-radius: 10px; -fx-padding: 8 12 8 12;");

        TextField nomeJogadorField = new TextField();
        nomeJogadorField.setPromptText("Nome do jogador");
        nomeJogadorField.setStyle("-fx-font-size: 16px; -fx-background-radius: 10px; -fx-padding: 8 12 8 12;");

        Button conectarBtn = new Button("Conectar");
        conectarBtn.setStyle(
            "-fx-background-color: #8B5C2A; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; " +
            "-fx-background-radius: 12px; -fx-pref-width: 180px; -fx-pref-height: 44px; -fx-effect: dropshadow(gaussian, #b0b0b0, 2, 0, 0, 1);"
        );
        conectarBtn.setOnMouseEntered(e -> conectarBtn.setStyle(
            "-fx-background-color: #B0B0B0; -fx-text-fill: #222; -fx-font-size: 18px; -fx-font-weight: bold; " +
            "-fx-background-radius: 12px; -fx-pref-width: 180px; -fx-pref-height: 44px; -fx-effect: dropshadow(gaussian, #8B5C2A, 2, 0, 0, 1);"
        ));
        conectarBtn.setOnMouseExited(e -> conectarBtn.setStyle(
            "-fx-background-color: #8B5C2A; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; " +
            "-fx-background-radius: 12px; -fx-pref-width: 180px; -fx-pref-height: 44px; -fx-effect: dropshadow(gaussian, #b0b0b0, 2, 0, 0, 1);"
        ));

        Label erroLabel = new Label();
        erroLabel.setTextFill(Color.web("#c0392b"));
        erroLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        caixa.getChildren().addAll(
            titulo,
            ipField,
            portaField,
            nomeJogadorField,
            conectarBtn,
            erroLabel
        );

        Scene cenaConexao = new Scene(caixa, 400, 350);
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
        VBox caixa = new VBox(22);
        caixa.setPadding(new Insets(48, 36, 48, 36));
        caixa.setAlignment(Pos.CENTER);
        caixa.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #f6f3ee, #e9e4d9);" +
            "-fx-border-radius: 18px; -fx-background-radius: 18px;" +
            "-fx-effect: dropshadow(gaussian, #8B5C2A55, 18, 0.2, 0, 4);"
        );

        Label titulo = new Label("Aguardando outro jogador...");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #8B5C2A; -fx-padding: 0 0 10 0;");

        Label info = new Label("Assim que outro jogador se conectar,\no jogo irá começar automaticamente.");
        info.setStyle("-fx-font-size: 16px; -fx-text-fill: #444; -fx-padding: 0 0 10 0; -fx-alignment: center;");

        // Animação simples de "loading"
        Label loading = new Label("⏳");
        loading.setStyle("-fx-font-size: 32px; -fx-padding: 10 0 0 0;");

        caixa.getChildren().addAll(titulo, info, loading);

        Scene cenaEspera = new Scene(caixa, 400, 220);
        Platform.runLater(() -> {
            stage.setScene(cenaEspera);
            stage.setTitle("Aguardando Jogador");
            stage.show();
        });
    }

    private void ligarAoServidor(String ip, int porto, String nomeJogador) {
        // Só tenta conectar se o IP for válido
        if (!ip.matches("^\\d{1,3}(\\.\\d{1,3}){3}$") && !ip.equalsIgnoreCase("localhost")) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("IP inválido");
                alert.setContentText("Por favor, insira um endereço IP válido.");
                alert.showAndWait();
            });
            return;
        }

        new Thread(() -> {
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

                Platform.runLater(this::mostrarJanelaEspera);

                Thread leituraThread = new Thread(() -> {
                    try {
                        String msg;
                        while ((msg = entrada.readLine()) != null) {
                            if (msg.equals("COMEÇAR")) {
                                Platform.runLater(this::mostrarJanelaJogo);
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
                            } else if (msg.startsWith("CHAT ")) {
                                String chatMsg = msg.substring(5);
                                // Só adiciona se não for a última mensagem local (evita duplicação)
                                Platform.runLater(() -> {
                                    if (!chatMsg.startsWith(nomeJogadorLocal + ":")) {
                                        adicionarMensagemChat(chatMsg);
                                    }
                                });
                            }
                        }
                    } catch (IOException e) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setHeaderText("Erro de comunicação com o servidor");
                            alert.setContentText(e.getMessage());
                            alert.showAndWait();
                        });
                    }
                });
                leituraThread.setDaemon(true);
                leituraThread.start();

            } catch (IOException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Erro a ligar ao servidor");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private void mostrarJanelaJogo() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f6f3ee, #e9e4d9);");

        VBox topo = new VBox();
        topo.setAlignment(Pos.CENTER);
        topo.setSpacing(8);

        nomesJogadoresLabel = new Label();
        contagemPecasLabel = new Label();

        nomesJogadoresLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #7a4c1e;");
        contagemPecasLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #444;");

        atualizarCabecalhoJogadores();
        atualizarContagemPecas();

        temporizadorLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #8B5C2A;");

        HBox botoes = new HBox(30);
        botoes.setAlignment(Pos.CENTER);
        botoes.setPadding(new Insets(10, 0, 10, 0));

        Button confirmarBtn = new Button("Confirmar Jogada");
        Button regrasBtn = new Button("Regras");
        Button sairBtn = new Button("Sair");

        // Tamanhos fixos para evitar bugs de hitbox
        confirmarBtn.setMinWidth(180);
        confirmarBtn.setPrefWidth(180);
        confirmarBtn.setMaxWidth(180);
        regrasBtn.setMinWidth(120);
        regrasBtn.setPrefWidth(120);
        regrasBtn.setMaxWidth(120);
        sairBtn.setMinWidth(100);
        sairBtn.setPrefWidth(100);
        sairBtn.setMaxWidth(100);

        confirmarBtn.setMinHeight(42);
        confirmarBtn.setPrefHeight(42);
        confirmarBtn.setMaxHeight(42);
        regrasBtn.setMinHeight(42);
        regrasBtn.setPrefHeight(42);
        regrasBtn.setMaxHeight(42);
        sairBtn.setMinHeight(42);
        sairBtn.setPrefHeight(42);
        sairBtn.setMaxHeight(42);

        String estiloBtn = "-fx-background-color: #8B5C2A; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 17px; -fx-background-radius: 10px;";
        String estiloBtnHover = "-fx-background-color: #B0B0B0; -fx-text-fill: #222; -fx-font-weight: bold; -fx-font-size: 17px; -fx-background-radius: 10px;";

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

        topo.getChildren().addAll(nomesJogadoresLabel, contagemPecasLabel, temporizadorLabel, botoes);
        root.setTop(topo);

        grelha.setStyle("-fx-background-color: #8B5C2A; -fx-border-color: #333; -fx-border-width: 3px; -fx-border-radius: 8px;");
        grelha.setMinSize(400, 400);
        grelha.setMaxSize(400, 400);
        grelha.setPrefSize(400, 400);

        HBox tabuleiroContainer = new HBox();
        tabuleiroContainer.setAlignment(Pos.CENTER);
        tabuleiroContainer.getChildren().add(grelha);
        tabuleiroContainer.setStyle("-fx-background-color: #8B5C2A; -fx-border-color: #333; -fx-border-width: 3px; -fx-border-radius: 8px;");
        root.setCenter(tabuleiroContainer);
        VBox centro = new VBox(grelha);
        centro.setAlignment(Pos.CENTER);

        // Chat ao lado do tabuleiro
        VBox chatBox = new VBox(8);
        chatBox.setPadding(new Insets(10));
        chatBox.setAlignment(Pos.TOP_CENTER);
        chatBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #8B5C2A; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-background-radius: 10px;");
        chatBox.setPrefWidth(260);

        Label chatTitulo = new Label("Chat");
        chatTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #8B5C2A;");

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefHeight(320);
        chatArea.setStyle("-fx-font-size: 14px; -fx-control-inner-background: #f9f9f9;");

        HBox chatInputBox = new HBox(6);
        chatInput = new TextField();
        chatInput.setPromptText("Escreva uma mensagem...");
        chatInput.setPrefWidth(150);
        chatSendBtn = new Button("Enviar");
        chatSendBtn.setStyle("-fx-background-color: #8B5C2A; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8px;");

        chatInputBox.getChildren().addAll(chatInput, chatSendBtn);

        chatBox.getChildren().addAll(chatTitulo, chatArea, chatInputBox);

        // Layout principal: tabuleiro à esquerda, chat à direita
        HBox conteudo = new HBox(20, centro, chatBox);
        conteudo.setAlignment(Pos.CENTER);
        conteudo.setPadding(new Insets(20, 0, 20, 0));
        root.setCenter(conteudo);

        grelha.setStyle("-fx-background-color: #8B5C2A; -fx-border-color: #333; -fx-border-width: 3px; -fx-border-radius: 8px;");

        Scene cenaJogo = new Scene(root, 720, 650);
        stage.setScene(cenaJogo);
        stage.setTitle("Jogo Reversi");
        stage.show();

        atualizarTabuleiro();

        grelha.setOnMouseClicked(e -> {
            if (!meuTurno) return;
            double cellSize = 400.0 / 8.0;
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
            // Só permite confirmar se for o turno do jogador
            if (!meuTurno) return;
            if (jogadaLinha != -1 && jogadaColuna != -1) {
                meuTurno = false; // Impede novas jogadas até receber SUA_VEZ do servidor
                saida.println("JOGADA " + jogadaLinha + " " + jogadaColuna);
                pararTemporizador();
                jogadaLinha = -1;
                jogadaColuna = -1;
                atualizarTabuleiro();
            } else {
                mostrarAlertaBonito("Selecione uma jogada", "Selecione uma posição válida no tabuleiro antes de confirmar.", Alert.AlertType.INFORMATION);
            }
        });

        regrasBtn.setOnAction(e -> {
            mostrarPopupRegras();
        });

        sairBtn.setOnAction(e -> {
            try {
                if (saida != null) {
                    saida.println("SAIR");
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                // Ignorar erros ao fechar
            }
            meuTurno = false;
            pararTemporizador();
            // Volta para a tela de conexão para procurar novo jogador no mesmo IP
            Platform.runLater(this::mostrarJanelaConexao);
        });

        // Chat: enviar mensagem ao clicar ou pressionar Enter
        chatSendBtn.setOnAction(e -> enviarMensagemChat());
        chatInput.setOnAction(e -> enviarMensagemChat());
    }

    private void enviarMensagemChat() {
        String texto = chatInput.getText().trim();
        if (!texto.isEmpty()) {
            String mensagem = nomeJogadorLocal + ": " + texto;
            // Envia sempre, independentemente do turno
            saida.println("CHAT " + mensagem);
            adicionarMensagemChat(mensagem); // Mostra imediatamente no próprio chat
            chatInput.clear();
        }
    }

    private void adicionarMensagemChat(String mensagem) {
        // Evita duplicação: só adiciona se a última linha for diferente
        String conteudoAtual = chatArea.getText();
        if (!conteudoAtual.endsWith(mensagem + "\n")) {
            chatArea.appendText(mensagem + "\n");
        }
    }

    private void mostrarPopupRegras() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Regras do Reversi");
        alert.setHeaderText(null);

        // HTML-like format para melhor apresentação
        String regras =
            " <b>Objetivo:</b> Ter mais peças da sua cor no final do jogo.<br><br>" +
            " <b>Regras:</b><br>" +
            " 1. Só pode jogar onde capturar pelo menos uma peça adversária.<br>" +
            " 2. As peças capturadas mudam para a sua cor.<br>" +
            " 3. O jogo termina quando não há mais jogadas possíveis.<br><br>" +
            " <b>Dica:</b> Tente controlar os cantos do tabuleiro!";

        // Usar um Label estilizado para simular HTML
        Label regrasLabel = new Label();
        regrasLabel.setText("Objetivo:\n  Ter mais peças da sua cor no final do jogo.\n\n" +
                "Regras:\n" +
                "  1. Só pode jogar onde capturar pelo menos uma peça adversária.\n" +
                "  2. As peças capturadas mudam para a sua cor.\n" +
                "  3. O jogo termina quando não há mais jogadas possíveis.\n\n" +
                "Dica: Tente controlar os cantos do tabuleiro!");
        regrasLabel.setStyle("-fx-font-size: 15px; -fx-font-family: 'Segoe UI', sans-serif; -fx-padding: 10 0 0 0;");

        alert.getDialogPane().setContent(regrasLabel);
        alert.getDialogPane().setStyle(
            "-fx-background-color: linear-gradient(to bottom, #ece9e6, #ffffff); " +
            "-fx-font-size: 15px; -fx-font-family: 'Segoe UI', sans-serif; " +
            "-fx-border-color: #8B5C2A; -fx-border-width: 2px; -fx-border-radius: 10px;"
        );
        alert.showAndWait();
    }

    private double getCellSize() {
        // Tamanho fixo do tabuleiro
        return 400.0 / 8.0;
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

        // Atualizar o temporizador com a mensagem correta
        if (meuTurno) {
            temporizadorLabel.setText("Turno: Você | Tempo restante: " + tempoRestante);
        } else {
            temporizadorLabel.setText("Turno: " + nomeJogadorAdversario + " | Aguardando...");
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
