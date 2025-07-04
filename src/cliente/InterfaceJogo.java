package cliente;

// Importações necessárias para a interface gráfica e comunicação
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
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import modelo.Tabuleiro;
import modelo.Casa;
import modelo.Peca;

import java.io.*;
import java.net.Socket;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

/**
 * Classe principal da interface do jogo Reversi (Othello).
 * Responsável por toda a interação visual e comunicação com o servidor.
 *
 * Comentários detalhados para facilitar a compreensão de quem não tem experiência com programação.
 */
public class InterfaceJogo {
    // Janela principal do jogo
    private final Stage stage;
    // Painel onde o tabuleiro será desenhado
    private final Pane tabuleiroPane; // Substitui o GridPane grelha
    // Objeto que representa o estado do tabuleiro (peças, jogadas, etc)
    private final Tabuleiro tabuleiro;
    // Label que mostra o temporizador (tempo de cada jogada)
    private final Label temporizadorLabel;
    // Objeto que controla a contagem do tempo
    private Timeline temporizador;
    // Tempo restante para o jogador jogar
    private int tempoRestante;

    // Cor do jogador local ('B' para pretas, 'W' para brancas)
    private char minhaCor;
    // Objetos para comunicação com o servidor
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter saida;
    // Indica se é a vez deste jogador
    private boolean meuTurno = false;

    // Guarda a linha e coluna da jogada selecionada
    private int jogadaLinha = -1;
    private int jogadaColuna = -1;

    // Nome do jogador local e do adversário
    private String nomeJogadorLocal = "";
    private String nomeJogadorAdversario = "Aguardando...";

    // Variáveis para o chat
    private TextArea chatArea;
    private TextField chatInput;
    private Button chatSendBtn;

    // Labels para mostrar nomes e contagem de peças
    private Label nomesJogadoresLabel;
    private Label contagemPecasLabel;

    // Controla se pode mostrar as jogadas possíveis (hitbox)
    private boolean podeMostrarJogadas = true;
    // Indica se o jogo terminou
    private boolean jogoTerminou = false;

    /**
     * Construtor da interface. Inicializa os componentes principais.
     * stage: Janela principal do JavaFX
     */
    public InterfaceJogo(Stage stage) {
        this.stage = stage;
        this.tabuleiroPane = new Pane();
        this.tabuleiro = new Tabuleiro();
        this.temporizadorLabel = new Label("");
        // grelha removido (não é mais usado)
    }

    /**
     * Método para mostrar a interface inicial (janela de conexão).
     */
    public void mostrar() {
        mostrarJanelaConexao();
    }

    /**
     * Mostra a janela para o jogador inserir IP, porta e nome para conectar ao servidor.
     * Tem validações para garantir que os campos estão preenchidos corretamente.
     */
    private void mostrarJanelaConexao() {
        VBox caixa = new VBox(18); // Caixa vertical para organizar os campos
        caixa.setPadding(new Insets(36, 36, 36, 36));
        caixa.setAlignment(Pos.CENTER);
        caixa.setStyle(
            "-fx-background-color: #e9e4d9;" +
            "-fx-border-radius: 18px; -fx-background-radius: 18px;" +
            "-fx-effect: dropshadow(gaussian, #8B5C2A55, 18, 0.2, 0, 4);"
        );

        // Título da janela
        Label titulo = new Label("Conectar ao Servidor Reversi");
        titulo.setStyle("-fx-font-size: 23px; -fx-font-weight: bold; -fx-text-fill: #8B5C2A; -fx-padding: 0 0 10 0;");

        // Campo para o IP do servidor
        TextField ipField = new TextField("");
        ipField.setPromptText("Endereço IP do servidor");
        ipField.setStyle("-fx-font-size: 16px; -fx-background-radius: 10px; -fx-padding: 8 12 8 12; -fx-background-color: #f9f7f3; -fx-border-color: #c2a477; -fx-border-width: 1.2px;");

        // Campo para a porta
        TextField portaField = new TextField("");
        portaField.setPromptText("Porta");
        portaField.setStyle("-fx-font-size: 16px; -fx-background-radius: 10px; -fx-padding: 8 12 8 12; -fx-background-color: #f9f7f3; -fx-border-color: #c2a477; -fx-border-width: 1.2px;");

        // Campo para o nome do jogador
        TextField nomeJogadorField = new TextField();
        nomeJogadorField.setPromptText("Nome do jogador");
        nomeJogadorField.setStyle("-fx-font-size: 16px; -fx-background-radius: 10px; -fx-padding: 8 12 8 12; -fx-background-color: #f9f7f3; -fx-border-color: #c2a477; -fx-border-width: 1.2px;");

        // Botão para conectar
        Button conectarBtn = new Button("Conectar");
        conectarBtn.setStyle(
            "-fx-background-color: #8B5C2A; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; " +
            "-fx-background-radius: 12px; -fx-pref-width: 180px; -fx-pref-height: 44px; -fx-effect: dropshadow(gaussian, #b0b0b0, 2, 0, 0, 1);"
        );
        // Efeito visual ao passar o mouse
        conectarBtn.setOnMouseEntered(e -> conectarBtn.setStyle(
            "-fx-background-color: #c2a477; -fx-text-fill: #8B5C2A; -fx-font-size: 18px; -fx-font-weight: bold; " +
            "-fx-background-radius: 12px; -fx-pref-width: 180px; -fx-pref-height: 44px; -fx-effect: dropshadow(gaussian, #8B5C2A, 2, 0, 0, 1);"
        ));
        conectarBtn.setOnMouseExited(e -> conectarBtn.setStyle(
            "-fx-background-color: #8B5C2A; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; " +
            "-fx-background-radius: 12px; -fx-pref-width: 180px; -fx-pref-height: 44px; -fx-effect: dropshadow(gaussian, #b0b0b0, 2, 0, 0, 1);"
        ));

        // Label para mostrar mensagens de erro
        Label erroLabel = new Label();
        erroLabel.setTextFill(Color.web("#c0392b"));
        erroLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Adiciona todos os componentes à caixa
        caixa.getChildren().addAll(
            titulo,
            ipField,
            portaField,
            nomeJogadorField,
            conectarBtn,
            erroLabel
        );

        // Cria a cena e mostra na janela
        Scene cenaConexao = new Scene(caixa, 400, 350);
        stage.setScene(cenaConexao);
        stage.setTitle("Conectar ao Servidor");
        stage.show();

        // Ação ao clicar no botão conectar
        conectarBtn.setOnAction(e -> {
            String ip = ipField.getText().trim();
            String portaTexto = portaField.getText().trim();
            String nomeJogador = nomeJogadorField.getText().trim();

            // Verifica se todos os campos estão preenchidos
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

            // Fecha conexão antiga, se existir
            fecharSocket();

            // Tenta conectar ao servidor
            ligarAoServidor(ip, porta, nomeJogador);
        });
    }

    /**
     * Mostra uma janela de espera enquanto aguarda outro jogador conectar.
     */
    private void mostrarJanelaEspera() {
        VBox caixa = new VBox(22);
        caixa.setPadding(new Insets(48, 36, 48, 36));
        caixa.setAlignment(Pos.CENTER);
        caixa.setStyle(
            "-fx-background-color: #e9e4d9;" +
            "-fx-border-radius: 18px; -fx-background-radius: 18px;" +
            "-fx-effect: dropshadow(gaussian, #8B5C2A55, 18, 0.2, 0, 4);"
        );

        Label titulo = new Label("Aguardando outro jogador...");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #8B5C2A; -fx-padding: 0 0 10 0;");

        Label info = new Label("Assim que outro jogador se conectar,\no jogo irá começar automaticamente.");
        info.setStyle("-fx-font-size: 16px; -fx-text-fill: #444; -fx-padding: 0 0 10 0; -fx-alignment: center;");

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

    /**
     * Tenta conectar ao servidor com os dados fornecidos.
     * Faz toda a comunicação inicial e inicia a escuta de mensagens do servidor.
     * ip: Endereço IP do servidor
     * porto: Porta do servidor
     * nomeJogador: Nome do jogador local
     */
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
                                if (partes.length >= 4) { // Proteção contra mensagens mal formatadas
                                    int linha = Integer.parseInt(partes[1]);
                                    int coluna = Integer.parseInt(partes[2]);
                                    char corJogada = partes[3].charAt(0);
                                    tabuleiro.jogar(linha, coluna, corJogada);
                                    Platform.runLater(this::atualizarTabuleiro);
                                }
                            } else if (msg.equals("JOGADA_CONFIRMADA")) {
                                // Jogada confirmada pelo servidor, nada a fazer (pode usar para feedback)
                            } else if (msg.equals("JOGADA_INVALIDA")) {
                                // Jogada inválida, reativa o turno e hitbox
                                meuTurno = true;
                                podeMostrarJogadas = true;
                                Platform.runLater(() -> {
                                    mostrarAlertaBonito("Jogada inválida", "A jogada não é válida. Tente novamente.", Alert.AlertType.WARNING);
                                    atualizarTabuleiro();
                                });
                            } else if (msg.equals("FIM")) {
                                pararTemporizador();
                                Platform.runLater(() -> mostrarPopupVencedor());
                            } else if (msg.equals("SUA_VEZ")) {
                                meuTurno = true;
                                podeMostrarJogadas = true; // Permite mostrar hitbox novamente no novo turno
                                jogadaLinha = -1;
                                jogadaColuna = -1;
                                iniciarTemporizador();
                                Platform.runLater(this::atualizarTabuleiro);
                            } else if (msg.startsWith("NOME_ADVERSARIO ")) {
                                nomeJogadorAdversario = msg.substring("NOME_ADVERSARIO ".length());
                                Platform.runLater(this::atualizarCabecalhoJogadores);
                            } else if (msg.startsWith("CHAT ")) {
                                String chatMsg = msg.substring(5);
                                Platform.runLater(() -> {
                                    if (!chatMsg.startsWith(nomeJogadorLocal + ":")) {
                                        adicionarMensagemChat(chatMsg);
                                    }
                                });
                            }
                        }
                    } catch (IOException e) {
                        // Só mostra erro se o jogo não terminou normalmente
                        if (!jogoTerminou) {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setHeaderText("Erro de comunicação com o servidor");
                                alert.setContentText(e.getMessage());
                                alert.showAndWait();
                            });
                        }
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

    /**
     * Mostra a janela principal do jogo, com o tabuleiro, chat e botões.
     * Organiza todos os elementos visuais e define as ações dos botões.
     */
    private void mostrarJanelaJogo() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #e9e4d9;");

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
        String estiloBtnHover = "-fx-background-color: #c2a477; -fx-text-fill: #8B5C2A; -fx-font-weight: bold; -fx-font-size: 17px; -fx-background-radius: 10px;";

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

        // Substituir grelha por tabuleiroPane
        tabuleiroPane.setMinSize(416, 416);
        tabuleiroPane.setMaxSize(416, 416);
        tabuleiroPane.setPrefSize(416, 416);

        VBox centro = new VBox(tabuleiroPane);
        centro.setAlignment(Pos.CENTER);

        // Chat ao lado do tabuleiro
        VBox chatBox = new VBox(8);
        chatBox.setPadding(new Insets(16, 16, 16, 16));
        chatBox.setAlignment(Pos.TOP_CENTER);
        chatBox.setStyle(
            "-fx-background-color: #e9e4d9;" +
            "-fx-border-color: #8B5C2A; -fx-border-width: 2.5px; " +
            "-fx-border-radius: 16px; -fx-background-radius: 16px;" +
            "-fx-effect: dropshadow(gaussian, #8B5C2A55, 14, 0.18, 0, 3);"
        );
        chatBox.setPrefWidth(270);

        Label chatTitulo = new Label("Chat");
        chatTitulo.setStyle(
            "-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #8B5C2A;" +
            "-fx-padding: 0 0 8 0; -fx-effect: dropshadow(gaussian, #e9e4d9, 2, 0.1, 0, 1);"
        );

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefHeight(320);
        chatArea.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-control-inner-background: #f9f7f3;" +
            "-fx-background-color: #f9f7f3;" +
            "-fx-border-color: #c2a477;" +
            "-fx-border-width: 1.5px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-text-fill: #333;"
        );

        HBox chatInputBox = new HBox(8);
        chatInputBox.setAlignment(Pos.CENTER);

        chatInput = new TextField();
        chatInput.setPromptText("Escreva uma mensagem...");
        chatInput.setPrefWidth(150);
        chatInput.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-border-color: #c2a477;" +
            "-fx-border-width: 1.2px;" +
            "-fx-background-color: #f9f7f3;" +
            "-fx-text-fill: #333;"
        );

        chatSendBtn = new Button("Enviar");
        chatSendBtn.setStyle(
            "-fx-background-color: #8B5C2A; -fx-text-fill: white; -fx-font-weight: bold;" +
            "-fx-font-size: 15px; -fx-background-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, #b0b0b0, 2, 0, 0, 1);"
        );
        chatSendBtn.setOnMouseEntered(e -> chatSendBtn.setStyle(
            "-fx-background-color: #c2a477; -fx-text-fill: #8B5C2A; -fx-font-weight: bold;" +
            "-fx-font-size: 15px; -fx-background-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, #8B5C2A, 2, 0, 0, 1);"
        ));
        chatSendBtn.setOnMouseExited(e -> chatSendBtn.setStyle(
            "-fx-background-color: #8B5C2A; -fx-text-fill: white; -fx-font-weight: bold;" +
            "-fx-font-size: 15px; -fx-background-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, #b0b0b0, 2, 0, 0, 1);"
        ));

        chatInputBox.getChildren().addAll(chatInput, chatSendBtn);

        chatBox.getChildren().setAll(chatTitulo, chatArea, chatInputBox);

        // Layout principal: tabuleiro à esquerda, chat à direita, com mais espaço entre eles
        HBox conteudo = new HBox(40, new VBox(tabuleiroPane), chatBox);
        conteudo.setAlignment(Pos.CENTER);
        conteudo.setPadding(new Insets(20, 0, 20, 0));
        root.setCenter(conteudo);

        tabuleiroPane.setStyle(
            "-fx-background-color: #e9e4d9;" +
            "-fx-border-color: #8B5C2A; -fx-border-width: 3px; -fx-border-radius: 16px;" +
            "-fx-background-radius: 16px;" +
            "-fx-effect: dropshadow(gaussian, #8B5C2A55, 18, 0.2, 0, 4);"
        );

        Scene cenaJogo = new Scene(root, 760, 650);
        stage.setScene(cenaJogo);
        stage.setTitle("Jogo Reversi");
        stage.show();

        atualizarTabuleiro();

        tabuleiroPane.setOnMouseClicked(e -> {
            if (!meuTurno) return;
            double cellSize = getCellSize();
            int coluna = (int) (e.getX() / cellSize);
            int linha = (int) (e.getY() / cellSize);
            if (linha < 0 || linha > 7 || coluna < 0 || coluna > 7) return;
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
                podeMostrarJogadas = false; // Não mostra hitbox até o próximo turno
                saida.println("JOGADA " + jogadaLinha + " " + jogadaColuna);
                pararTemporizador();
                jogadaLinha = -1;
                jogadaColuna = -1;
                atualizarTabuleiro(); // hitbox desaparece imediatamente após jogar
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
            } catch (Exception ex) {
                // Ignorar erros ao sair
            }
            meuTurno = false;
            pararTemporizador();
            jogoTerminou = false;
            jogadaLinha = -1;
            jogadaColuna = -1;
            Platform.runLater(() -> {
                tabuleiro.inicializar();
                fecharSocket(); // FECHA socket ao sair para garantir nova ligação limpa
                mostrarJanelaConexao();
            });
        });

        // Chat: enviar mensagem ao clicar ou pressionar Enter
        chatSendBtn.setOnAction(e -> enviarMensagemChat());
        chatInput.setOnAction(e -> enviarMensagemChat());
    }

    /**
     * Envia a mensagem do chat para o servidor e mostra imediatamente no chat local.
     */
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

    /**
     * Adiciona uma mensagem ao chat, evitando duplicação.
     * mensagem: Mensagem a ser adicionada
     */
    private void adicionarMensagemChat(String mensagem) {
        // Evita duplicação: só adiciona se a última linha for diferente
        String conteudoAtual = chatArea.getText();
        if (!conteudoAtual.endsWith(mensagem + "\n")) {
            chatArea.appendText(mensagem + "\n");
        }
    }

    /**
     * Mostra um popup com as regras do jogo.
     */
    private void mostrarPopupRegras() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Regras do Reversi");
        alert.setHeaderText(null);

        // Título estilizado sem emoji
        Label titulo = new Label("Regras do Reversi");
        titulo.setStyle(
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #8B5C2A;" +
            "-fx-padding: 0 0 10 0;"
        );

        // Regras detalhadas do Reversi (sem emojis)
        String regras =
            "Objetivo:\n" +
            "  O objetivo do Reversi é terminar o jogo com mais peças da sua cor no tabuleiro do que o adversário.\n\n" +
            "Regras:\n" +
            "  1. O jogo começa com 4 peças no centro do tabuleiro (2 pretas e 2 brancas).\n" +
            "  2. Os jogadores jogam alternadamente, colocando uma peça da sua cor em uma casa vazia.\n" +
            "  3. Cada jogada deve capturar pelo menos uma peça do adversário. Isso acontece quando a peça colocada forma uma linha (horizontal, vertical ou diagonal) com outra peça da sua cor, tendo apenas peças do adversário entre elas.\n" +
            "  4. Todas as peças do adversário entre as duas peças da sua cor são viradas para a sua cor.\n" +
            "  5. Se um jogador não puder fazer uma jogada válida, ele passa a vez.\n" +
            "  6. O jogo termina quando nenhum dos jogadores pode jogar (tabuleiro cheio ou nenhum movimento possível).\n\n" +
            "Dicas:\n" +
            "  • Tente conquistar os cantos do tabuleiro, pois são posições estratégicas.\n" +
            "  • Evite dar ao adversário a chance de jogar nos cantos.\n" +
            "  • Planeje suas jogadas para maximizar o número de peças capturadas e limitar as opções do adversário.\n";

        Label regrasLabel = new Label(regras);
        regrasLabel.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-font-family: 'Segoe UI', sans-serif;" +
            "-fx-text-fill: #333;" +
            "-fx-padding: 0 0 0 0;"
        );
        regrasLabel.setWrapText(true);

        // Separador visual
        javafx.scene.shape.Line separador = new javafx.scene.shape.Line(0, 0, 400, 0);
        separador.setStroke(Color.web("#8B5C2A"));
        separador.setStrokeWidth(1.5);

        // Scroll para regras longas
        ScrollPane scroll = new ScrollPane(regrasLabel);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(260);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox box = new VBox(10, titulo, separador, scroll);
        box.setStyle(
            "-fx-background-color: #e9e4d9;" +
            "-fx-padding: 18px 18px 18px 18px;" +
            "-fx-border-radius: 16px; -fx-background-radius: 16px;" +
            "-fx-effect: dropshadow(gaussian, #8B5C2A55, 18, 0.2, 0, 4);"
        );

        alert.getDialogPane().setContent(box);
        alert.getDialogPane().setStyle(
            "-fx-background-color: #e9e4d9; " +
            "-fx-font-size: 15px; -fx-font-family: 'Segoe UI', sans-serif; " +
            "-fx-border-color: #8B5C2A; -fx-border-width: 2px; -fx-border-radius: 14px;" +
            "-fx-effect: dropshadow(gaussian, #8B5C2A55, 18, 0.2, 0, 4);"
        );
        alert.showAndWait();
    }

    /**
     * Mostra um popup com o resultado final do jogo (quem ganhou ou se empatou).
     */
    private void mostrarPopupVencedor() {
        jogoTerminou = true;
        int pretas = tabuleiro.contarPecas('B');
        int brancas = tabuleiro.contarPecas('W');
        String vencedor;
        String corVencedor;
        if (pretas > brancas) {
            vencedor = (minhaCor == 'B' ? nomeJogadorLocal : nomeJogadorAdversario);
            corVencedor = "Pretas";
        } else if (brancas > pretas) {
            vencedor = (minhaCor == 'W' ? nomeJogadorLocal : nomeJogadorAdversario);
            corVencedor = "Brancas";
        } else {
            vencedor = "Empate!";
            corVencedor = "";
        }

        Stage popup = new Stage();
        popup.initOwner(stage);

        VBox box = new VBox(24);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40, 54, 40, 54));
        box.setStyle(
            "-fx-background-color: #e9e4d9;" +
            "-fx-border-color: #8B5C2A; -fx-border-width: 4px; -fx-border-radius: 22px;" +
            "-fx-background-radius: 22px;" +
            "-fx-effect: dropshadow(gaussian, #8B5C2A99, 28, 0.25, 0, 8);"
        );

        Label titulo = new Label("Fim do Jogo");
        titulo.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #8B5C2A; -fx-effect: dropshadow(gaussian, #e9e4d9, 2, 0.1, 0, 1);");

        Label resultado = new Label();
        resultado.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-padding: 0 0 12 0;");

        if (pretas == brancas) {
            resultado.setText("Empate!\nAmbos terminaram com " + pretas + " peças.");
        } else {
            resultado.setText("Vencedor: " + vencedor + " (" + corVencedor + ")\n\n" +
                "Pretas: " + pretas + "   |   Brancas: " + brancas);
        }

        Button fecharBtn = new Button("Voltar ao Início");
        fecharBtn.setStyle(
            "-fx-background-color: #c2a477; -fx-text-fill: #8B5C2A; -fx-font-size: 19px; -fx-font-weight: bold;" +
            "-fx-background-radius: 16px; -fx-pref-width: 210px; -fx-pref-height: 50px; " +
            "-fx-effect: dropshadow(gaussian, #b0b0b0, 2, 0, 0, 1);"
        );
        fecharBtn.setOnMouseEntered(e -> fecharBtn.setStyle(
            "-fx-background-color: #8B5C2A; -fx-text-fill: #e9e4d9; -fx-font-size: 19px; -fx-font-weight: bold;" +
            "-fx-background-radius: 16px; -fx-pref-width: 210px; -fx-pref-height: 50px; " +
            "-fx-effect: dropshadow(gaussian, #c2a477, 2, 0, 0, 1);"
        ));
        fecharBtn.setOnMouseExited(e -> fecharBtn.setStyle(
            "-fx-background-color: #c2a477; -fx-text-fill: #8B5C2A; -fx-font-size: 19px; -fx-font-weight: bold;" +
            "-fx-background-radius: 16px; -fx-pref-width: 210px; -fx-pref-height: 50px; " +
            "-fx-effect: dropshadow(gaussian, #b0b0b0, 2, 0, 0, 1);"
        ));

        fecharBtn.setOnAction(e -> {
            try {
                if (saida != null) saida.println("SAIR");
            } catch (Exception ex) {}
            popup.close();
            jogoTerminou = false;
            jogadaLinha = -1;
            jogadaColuna = -1;
            Platform.runLater(() -> {
                tabuleiro.inicializar();
                fecharSocket(); // FECHA socket ao voltar ao início para garantir nova ligação limpa
                mostrarJanelaConexao();
            });
        });

        box.getChildren().addAll(titulo, resultado, fecharBtn);

        Scene cena = new Scene(box);
        popup.setScene(cena);
        popup.setTitle("Resultado Final");
        popup.setResizable(false);

        // Fade-in animation
        box.setOpacity(0);
        popup.show();
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), box);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /**
     * Calcula o tamanho de cada célula do tabuleiro, para desenhar corretamente.
     * return: Tamanho em pixels de cada célula
     */
    private double getCellSize() {
        double largura = tabuleiroPane.getWidth() > 0 ? tabuleiroPane.getWidth() : tabuleiroPane.getPrefWidth();
        double altura = tabuleiroPane.getHeight() > 0 ? tabuleiroPane.getHeight() : tabuleiroPane.getPrefHeight();
        return Math.min(largura, altura) / 8.0;
    }

    /**
     * Atualiza o desenho do tabuleiro, mostrando peças, jogadas possíveis e efeitos visuais.
     */
    private void atualizarTabuleiro() {
        tabuleiroPane.getChildren().clear();
        double cellSize = getCellSize();
        boolean mostrarPossiveis = meuTurno;

        // Efeito de sombra e borda mais suave no tabuleiro
        tabuleiroPane.setStyle(
            "-fx-background-color: #e9e4d9;" +
            "-fx-border-color: #8B5C2A; -fx-border-width: 3px; -fx-border-radius: 16px;" +
            "-fx-background-radius: 16px;" +
            "-fx-effect: dropshadow(gaussian, #8B5C2A55, 18, 0.2, 0, 4);"
        );

        // Desenha as casas
        for (int linha = 0; linha < 8; linha++) {
            for (int coluna = 0; coluna < 8; coluna++) {
                Rectangle r = new Rectangle(cellSize, cellSize);
                r.setX(coluna * cellSize);
                r.setY(linha * cellSize);
                // Cores suaves para o tabuleiro
                if ((linha + coluna) % 2 == 0) {
                    r.setFill(Color.web("#e9e4d9"));
                } else {
                    r.setFill(Color.web("#c2a477"));
                }
                r.setArcWidth(cellSize * 0.25);
                r.setArcHeight(cellSize * 0.25);
                r.setStroke(Color.web("#8B5C2A"));
                r.setStrokeWidth(1.2);
                tabuleiroPane.getChildren().add(r);

                char peca = tabuleiro.getPeca(linha, coluna);
                if (peca != '-') {
                    double raio = cellSize * 0.4;
                    Circle c = new Circle(r.getX() + cellSize / 2, r.getY() + cellSize / 2, raio);
                    c.setFill(peca == 'B' ? Color.web("#222") : Color.web("#fff"));
                    c.setStroke(Color.web("#8B5C2A"));
                    c.setStrokeWidth(cellSize * 0.09);
                    tabuleiroPane.getChildren().add(c);
                } else if (mostrarPossiveis && tabuleiro.jogadaValida(linha, coluna, minhaCor)) {
                    double raioHitbox = cellSize * 0.44;
                    Circle hitbox = new Circle(r.getX() + cellSize / 2, r.getY() + cellSize / 2, raioHitbox);
                    hitbox.setFill(Color.web("#FFD700", 0.33));
                    hitbox.setStroke(Color.web("#FFD700"));
                    hitbox.setStrokeWidth(cellSize * 0.06);
                    tabuleiroPane.getChildren().add(hitbox);

                    // Animação de destaque para jogada selecionada
                    if (linha == jogadaLinha && coluna == jogadaColuna) {
                        double raioFade = cellSize * 0.4;
                        Circle fadePeca = new Circle(r.getX() + cellSize / 2, r.getY() + cellSize / 2, raioFade);
                        fadePeca.setFill(minhaCor == 'B' ? Color.web("#222") : Color.web("#fff"));
                        fadePeca.setOpacity(0.55);
                        fadePeca.setStroke(Color.web("#FFD700"));
                        fadePeca.setStrokeWidth(cellSize * 0.13);
                        tabuleiroPane.getChildren().add(fadePeca);
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

    /**
     * Atualiza o cabeçalho com os nomes dos jogadores e suas cores.
     */
    private void atualizarCabecalhoJogadores() {
        // Só tenta atualizar se o label já foi criado
        if (nomesJogadoresLabel == null) return;
        String corLocal = minhaCor == 'B' ? "Pretas" : "Brancas";
        String corAdv = minhaCor == 'B' ? "Brancas" : "Pretas";
        nomesJogadoresLabel.setText(
            nomeJogadorLocal + " (" + corLocal + ")  vs  " +
            nomeJogadorAdversario + " (" + corAdv + ")"
        );
    }

    /**
     * Atualiza a contagem de peças pretas e brancas no tabuleiro.
     */
    private void atualizarContagemPecas() {
        int pretas = tabuleiro.contarPecas('B');
        int brancas = tabuleiro.contarPecas('W');
        contagemPecasLabel.setText("⚫ Pretas: " + pretas + "   |   ⚪ Brancas: " + brancas);
    }

    /**
     * Inicia o temporizador de 30 segundos para o jogador fazer sua jogada.
     */
    private void iniciarTemporizador() {
        pararTemporizador();
        tempoRestante = 30;

        temporizador = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            tempoRestante--;

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

    /**
     * Para o temporizador, se estiver rodando.
     */
    private void pararTemporizador() {
        if (temporizador != null) {
            temporizador.stop();
        }
    }

    /**
     * Mostra um alerta estilizado na tela.
     * Parâmetros:
     *  titulo   - Título do alerta
     *  mensagem - Mensagem do alerta
     *  tipo     - Tipo do alerta (informação, erro, etc)
     */
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

    /**
     * Fecha a conexão com o servidor, liberando recursos.
     */
    private void fecharSocket() {
        try {
            if (entrada != null) entrada.close();
        } catch (Exception e) {}
        try {
            if (saida != null) saida.close();
        } catch (Exception e) {}
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception e) {}
        entrada = null;
        saida = null;
        socket = null;
    }
}
