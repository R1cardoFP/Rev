package servidor;

// Importações das classes do modelo do jogo
import modelo.Tabuleiro;
import modelo.Casa;
import modelo.Peca;

// Importações para comunicação de rede e listas
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Classe principal do servidor do jogo Reversi.
 * Responsável por aceitar ligações dos jogadores, gerir o jogo e comunicar com os clientes.
 *
 * Comentários detalhados para facilitar a compreensão de quem não tem experiência com programação.
 */
public class ServidorReversi {

    // Tabuleiro do jogo (mantém o estado das peças)
    private static final Tabuleiro tabuleiro = new Tabuleiro();

    // Listas para guardar a comunicação e dados dos jogadores
    private static final ArrayList<PrintWriter> jogadores = new ArrayList<>(); // Para enviar mensagens
    private static final ArrayList<BufferedReader> entradas = new ArrayList<>(); // Para receber mensagens
    private static final ArrayList<String> nomes = new ArrayList<>(); // Nomes dos jogadores
    private static final ArrayList<Socket> sockets = new ArrayList<>(); // Sockets de cada jogador

    // Cores atribuídas aos jogadores: 'B' para preto, 'W' para branco
    private static final char[] cores = {'B', 'W'};
    private static int jogadorAtual = 0; // Índice do jogador que está a jogar

    /**
     * Método principal. Inicia o servidor e gere o ciclo de espera e jogo.
     */
    public static void main(String[] args) {

        String ipManual = "10.20.10.100"; // IP onde o servidor vai escutar (mude para o IP da sua máquina)
        int porta = 2025; // Porta onde o servidor vai escutar

        try (ServerSocket serverSocket = new ServerSocket(porta, 0, InetAddress.getByName(ipManual))) {
            System.out.println("Servidor Reversi a correr em " + ipManual + ":" + porta);

            // Ciclo infinito: o servidor nunca termina, fica sempre à espera de novos jogos
            while (true) {
                // Limpa listas para preparar um novo jogo
                jogadores.clear();
                entradas.clear();
                nomes.clear();
                sockets.clear();
                jogadorAtual = 0;

                System.out.println("À escuta de clientes...");

                // Aceita ligações até ter dois jogadores
                while (jogadores.size() < 2) {
                    Socket cliente = serverSocket.accept(); // Espera ligação
                    System.out.println("Jogador ligado.");

                    // Prepara canais de comunicação com o cliente
                    PrintWriter out = new PrintWriter(cliente.getOutputStream(), true); // Para enviar
                    BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream())); // Para receber

                    jogadores.add(out);
                    entradas.add(in);
                    sockets.add(cliente);

                    // Lê o nome do jogador enviado pelo cliente, ou atribui um nome padrão
                    String nome = in.readLine();
                    if (nome == null || nome.isEmpty()) nome = "Jogador" + jogadores.size();
                    nomes.add(nome);

                    // Envia ao cliente a cor que lhe foi atribuída ('B' ou 'W')
                    out.println(cores[jogadores.size() - 1]);
                }

                // Informa cada jogador do nome do adversário
                jogadores.get(0).println("NOME_ADVERSARIO " + nomes.get(1));
                jogadores.get(1).println("NOME_ADVERSARIO " + nomes.get(0));

                // Início do jogo: envia mensagem para ambos e inicializa o tabuleiro
                // Aqui começa o jogo de verdade. O servidor avisa os dois jogadores que o jogo vai começar,
                // inicializa o tabuleiro (coloca as peças iniciais) e diz a quem começa que é a sua vez.
                enviarMensagemATodos("COMEÇAR");
                tabuleiro.inicializar();
                jogadores.get(jogadorAtual).println("SUA_VEZ"); // Diz a quem começa que é a sua vez

                boolean jogoAtivo = true; // Controla se o jogo está a decorrer

                // Ciclo principal do jogo (enquanto não terminar)
                // Este ciclo fica a correr enquanto o jogo não acabar. Ele vai alternando entre os jogadores,
                // esperando que cada um faça a sua jogada, controlando o tempo e tratando as mensagens recebidas.
                while (jogoAtivo) {
                    boolean mensagemProcessada = false; // Controla se já tratou a mensagem do turno

                    // --- INÍCIO: controlo de tempo ---
                    // Cada jogador tem 30 segundos para jogar. Se não jogar a tempo, perde a vez.
                    int tempoTurno = 30; // Tempo limite por jogada (segundos)
                    long inicioTurno = System.currentTimeMillis(); // Marca o início do turno
                    try {
                        sockets.get(jogadorAtual).setSoTimeout(tempoTurno * 1000); // Define timeout no socket
                    } catch (IOException ex) {}

                    jogadores.get(jogadorAtual).println("TEMPO " + tempoTurno); // Informa o tempo ao cliente

                    // Espera e processa a mensagem do jogador da vez
                    // Aqui o servidor fica à espera de uma mensagem do jogador da vez. Só processa mensagens desse jogador.
                    while (!mensagemProcessada) {
                        for (int i = 0; i < entradas.size(); i++) {
                            if (i != jogadorAtual) continue; // Só processa o jogador da vez

                            BufferedReader entrada = entradas.get(i);
                            try {
                                String linha = null;

                                try {
                                    if (entrada.ready()) {
                                        // Se já há mensagem, lê imediatamente
                                        linha = entrada.readLine();
                                    } else {
                                        // Espera pelo tempo restante
                                        // Se o jogador ainda não enviou nada, espera até acabar o tempo do turno
                                        long tempoRestante = tempoTurno * 1000 - (System.currentTimeMillis() - inicioTurno);
                                        if (tempoRestante > 0) {
                                            sockets.get(jogadorAtual).setSoTimeout((int) tempoRestante);
                                            linha = entrada.readLine();
                                        }
                                    }
                                } catch (SocketTimeoutException ste) {
                                    // Se o tempo esgotou, passa a vez
                                    jogadores.get(jogadorAtual).println("JOGADA_INVALIDA");
                                    jogadorAtual = (jogadorAtual + 1) % 2;
                                    jogadores.get(jogadorAtual).println("SUA_VEZ");
                                    mensagemProcessada = true;
                                    break;
                                }

                                if (linha == null) continue; // Se não recebeu nada, volta ao início

                                PrintWriter atual = jogadores.get(jogadorAtual);

                                // --- Tratamento das mensagens recebidas ---
                                if (linha.startsWith("JOGADA")) {
                                    // Jogador fez uma jogada
                                    String[] partes = linha.split(" ");
                                    int x = Integer.parseInt(partes[1]);
                                    int y = Integer.parseInt(partes[2]);
                                    char cor = cores[jogadorAtual];

                                    if (tabuleiro.jogadaValida(x, y, cor)) {
                                        // Se a jogada é válida, atualiza o tabuleiro
                                        tabuleiro.jogar(x, y, cor);
                                        enviarJogadaParaJogadores(x, y, cor); // Informa ambos
                                        atual.println("JOGADA_CONFIRMADA");

                                        // Verifica se o jogo terminou
                                        if (fimDeJogo()) {
                                            enviarMensagemATodos("FIM");
                                            jogoAtivo = false;
                                            break;
                                        } else {
                                            // Passa a vez ao outro jogador
                                            jogadorAtual = (jogadorAtual + 1) % 2;
                                            jogadores.get(jogadorAtual).println("SUA_VEZ");
                                        }
                                    } else {
                                        // Jogada inválida
                                        atual.println("JOGADA_INVALIDA");
                                    }
                                    mensagemProcessada = true;
                                    break;

                                } else if (linha.equals("TEMPO_ESGOTADO")) {
                                    // Jogador informou que perdeu o tempo
                                    jogadorAtual = (jogadorAtual + 1) % 2;
                                    jogadores.get(jogadorAtual).println("SUA_VEZ");
                                    mensagemProcessada = true;
                                    break;

                                } else if (linha.startsWith("CHAT ")) {
                                    // Mensagem de chat enviada a todos
                                    for (PrintWriter p : jogadores) {
                                        p.println(linha);
                                    }

                                } else if (linha.startsWith("SAIR")) {
                                    // Jogador saiu do jogo
                                    System.out.println("Jogador " + nomes.get(jogadorAtual) + " saiu do jogo.");
                                    try {
                                        int outro = (jogadorAtual == 0) ? 1 : 0;
                                        if (jogadores.size() > outro) {
                                            jogadores.get(outro).println("SAIU");
                                        }
                                    } catch (Exception ex) {}
                                    removerJogador(jogadorAtual);
                                    jogoAtivo = false;
                                    break;
                                }
                            } catch (SocketTimeoutException ste) {
                                // Tempo esgotado, mudar jogador
                                jogadores.get(jogadorAtual).println("JOGADA_INVALIDA");
                                jogadorAtual = (jogadorAtual + 1) % 2;
                                jogadores.get(jogadorAtual).println("SUA_VEZ");
                                mensagemProcessada = true;
                                break;

                            } catch (IOException ex) {
                                // Jogador desconectado
                                System.out.println("Jogador desconectado.");
                                try {
                                    // Notifica o outro jogador, se ainda estiver conectado
                                    int outro = (i == 0) ? 1 : 0;
                                    if (jogadores.size() > outro) {
                                        jogadores.get(outro).println("SAIU");
                                    }
                                } catch (Exception ignore) {}
                                removerJogador(i);
                                jogoAtivo = false;
                                break;
                            }
                        }
                        try { Thread.sleep(20); } catch (InterruptedException e) {}
                    }
                    // --- FIM: controlo de tempo ---
                }

                // Após o jogo, fecha todas as conexões
                for (Socket s : sockets) {
                    try { s.close(); } catch (IOException ex) {}
                }

                // Limpa listas para próximo jogo
                jogadores.clear();
                entradas.clear();
                nomes.clear();
                sockets.clear();
                jogadorAtual = 0;
                System.out.println("Jogo terminado ou jogador saiu. Reiniciando espera por jogadores...");
            }

        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Remove todas as referências a um jogador e fecha o seu socket.
     * idx: índice do jogador a remover
     */
    private static void removerJogador(int idx) {
        try {
            if (sockets.size() > idx) sockets.get(idx).close();
        } catch (IOException ex) {}
        if (jogadores.size() > idx) jogadores.remove(idx);
        if (entradas.size() > idx) entradas.remove(idx);
        if (nomes.size() > idx) nomes.remove(idx);
        if (sockets.size() > idx) sockets.remove(idx);
    }

    /**
     * Envia uma mensagem de texto a todos os jogadores.
     * msg: mensagem a enviar
     */
    private static void enviarMensagemATodos(String msg) {
        for (PrintWriter p : jogadores) {
            p.println(msg);
        }
    }

    /**
     * Envia uma jogada efetuada a ambos os jogadores.
     * linha, coluna: posição da jogada
     * cor: cor da peça jogada
     */
    private static void enviarJogadaParaJogadores(int linha, int coluna, char cor) {
        for (PrintWriter p : jogadores) {
            p.println("JOGADA " + linha + " " + coluna + " " + cor);
        }
    }

    /**
     * Verifica se o jogo terminou (sem jogadas válidas para nenhum jogador).
     * return: true se terminou, false caso contrário
     */
    private static boolean fimDeJogo() {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
                if (tabuleiro.getPeca(i, j) == '-') {
                    if (tabuleiro.jogadaValida(i, j, 'B') || tabuleiro.jogadaValida(i, j, 'W'))
                        return false;
                }
            }
        return true;
    }

    /**
     * Verifica se o jogador tem alguma jogada válida disponível.
     * jogador: índice do jogador
     * return: true se tem jogada, false caso contrário
     */
    private static boolean jogadorTemJogada(int jogador) {
        char cor = cores[jogador];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (tabuleiro.jogadaValida(i, j, cor))
                    return true;
        return false;
    }
}
