package servidor;

import modelo.Tabuleiro;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class ServidorReversi {
    private static final Tabuleiro tabuleiro = new Tabuleiro();
    private static final ArrayList<PrintWriter> jogadores = new ArrayList<>();
    private static final ArrayList<BufferedReader> entradas = new ArrayList<>();
    private static final ArrayList<String> nomes = new ArrayList<>();
    private static final ArrayList<Socket> sockets = new ArrayList<>(); // Para fechar conexões
    private static final char[] cores = {'B', 'W'};
    private static int jogadorAtual = 0;

    public static void main(String[] args) {

        String ipManual = "192.168.1.144"; // <- Define aqui o IP
        int porta = 2000;


        try (ServerSocket serverSocket = new ServerSocket(porta, 0, InetAddress.getByName(ipManual))) {
            System.out.println("Servidor Reversi a correr em " + ipManual + ":" + porta);

            while (true) {
                // Limpa listas para novo jogo
                jogadores.clear();
                entradas.clear();
                nomes.clear();
                sockets.clear();
                jogadorAtual = 0;

                System.out.println("À escuta de clientes...");

                // Espera por dois jogadores
                while (jogadores.size() < 2) {
                    Socket cliente = serverSocket.accept();
                    System.out.println("Jogador ligado.");
                    PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                    jogadores.add(out);
                    entradas.add(in);
                    sockets.add(cliente);

                    String nome = in.readLine();
                    if (nome == null || nome.isEmpty()) nome = "Jogador" + jogadores.size();
                    nomes.add(nome);

                    out.println(cores[jogadores.size() - 1]); // Envia cor
                }

                jogadores.get(0).println("NOME_ADVERSARIO " + nomes.get(1));
                jogadores.get(1).println("NOME_ADVERSARIO " + nomes.get(0));

                enviarMensagemATodos("COMEÇAR");
                tabuleiro.inicializar();
                jogadores.get(jogadorAtual).println("SUA_VEZ");

                boolean jogoAtivo = true;
                while (jogoAtivo) {
                    boolean mensagemProcessada = false;

                    // --- INÍCIO DO CONTROLE DE TEMPO ---
                    int tempoTurno = 30; // segundos
                    long inicioTurno = System.currentTimeMillis();
                    try {
                        sockets.get(jogadorAtual).setSoTimeout(tempoTurno * 1000);
                    } catch (IOException ex) { }
                    jogadores.get(jogadorAtual).println("TEMPO " + tempoTurno);

                    while (!mensagemProcessada) {
                        for (int i = 0; i < entradas.size(); i++) {
                            if (i != jogadorAtual) continue;

                            BufferedReader entrada = entradas.get(i);
                            try {
                                String linha = null;
                                try {
                                    if (entrada.ready()) {
                                        linha = entrada.readLine();
                                    } else {
                                        // Espera até o tempo acabar ou receber mensagem
                                        long tempoRestante = tempoTurno * 1000 - (System.currentTimeMillis() - inicioTurno);
                                        if (tempoRestante > 0) {
                                            sockets.get(jogadorAtual).setSoTimeout((int) tempoRestante);
                                            linha = entrada.readLine();
                                        }
                                    }
                                } catch (SocketTimeoutException ste) {
                                    // Tempo esgotado
                                    jogadores.get(jogadorAtual).println("JOGADA_INVALIDA");
                                    jogadorAtual = (jogadorAtual + 1) % 2;
                                    jogadores.get(jogadorAtual).println("SUA_VEZ");
                                    mensagemProcessada = true;
                                    break;
                                }

                                if (linha == null) continue;

                                PrintWriter atual = jogadores.get(jogadorAtual);

                                if (linha.startsWith("JOGADA")) {
                                    String[] partes = linha.split(" ");
                                    int x = Integer.parseInt(partes[1]);
                                    int y = Integer.parseInt(partes[2]);
                                    char cor = cores[jogadorAtual];

                                    if (tabuleiro.jogadaValida(x, y, cor)) {
                                        tabuleiro.jogar(x, y, cor);
                                        enviarJogadaParaJogadores(x, y, cor);
                                        atual.println("JOGADA_CONFIRMADA");

                                        if (fimDeJogo()) {
                                            enviarMensagemATodos("FIM");
                                            jogoAtivo = false;
                                            break;
                                        } else {
                                            jogadorAtual = (jogadorAtual + 1) % 2;
                                            jogadores.get(jogadorAtual).println("SUA_VEZ");
                                        }
                                    } else {
                                        atual.println("JOGADA_INVALIDA");
                                    }
                                    mensagemProcessada = true;
                                    break;

                                } else if (linha.equals("TEMPO_ESGOTADO")) {
                                    jogadorAtual = (jogadorAtual + 1) % 2;
                                    jogadores.get(jogadorAtual).println("SUA_VEZ");
                                    mensagemProcessada = true;
                                    break;

                                } else if (linha.startsWith("CHAT ")) {
                                    for (PrintWriter p : jogadores) {
                                        p.println(linha);
                                    }

                                } else if (linha.startsWith("SAIR")) {
                                    System.out.println("Jogador " + nomes.get(jogadorAtual) + " saiu do jogo.");
                                    jogadores.get(jogadorAtual).println("SAIU");
                                    removerJogador(jogadorAtual);
                                    jogoAtivo = false;
                                    break;
                                }
                            } catch (SocketTimeoutException ste) {
                                // Tempo esgotado
                                jogadores.get(jogadorAtual).println("JOGADA_INVALIDA");
                                jogadorAtual = (jogadorAtual + 1) % 2;
                                jogadores.get(jogadorAtual).println("SUA_VEZ");
                                mensagemProcessada = true;
                                break;
                            } catch (IOException ex) {
                                // Erro de leitura: trata como desconexão
                                removerJogador(i);
                                jogoAtivo = false;
                                break;
                            }
                        }
                        try { Thread.sleep(20); } catch (InterruptedException e) { }
                    }
                    // --- FIM DO CONTROLE DE TEMPO ---
                }

                // Fecha todas as conexões restantes
                for (Socket s : sockets) {
                    try { s.close(); } catch (IOException ex) { }
                }
                System.out.println("Jogo terminado ou jogador saiu. Reiniciando espera por jogadores...");
            }

        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void removerJogador(int idx) {
        try {
            if (sockets.size() > idx) sockets.get(idx).close();
        } catch (IOException ex) { }
        if (jogadores.size() > idx) jogadores.remove(idx);
        if (entradas.size() > idx) entradas.remove(idx);
        if (nomes.size() > idx) nomes.remove(idx);
        if (sockets.size() > idx) sockets.remove(idx);
    }

    private static void enviarMensagemATodos(String msg) {
        for (PrintWriter p : jogadores) {
            p.println(msg);
        }
    }

    private static void enviarJogadaParaJogadores(int linha, int coluna, char cor) {
        for (PrintWriter p : jogadores) {
            p.println("JOGADA " + linha + " " + coluna + " " + cor);
        }
    }

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

    private static boolean jogadorTemJogada(int jogador) {
        char cor = cores[jogador];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (tabuleiro.jogadaValida(i, j, cor))
                    return true;
        return false;
    }
}
