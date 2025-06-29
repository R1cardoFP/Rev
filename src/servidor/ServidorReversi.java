package servidor;

import modelo.Tabuleiro;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServidorReversi {
    private static final Tabuleiro tabuleiro = new Tabuleiro();
    private static final ArrayList<PrintWriter> jogadores = new ArrayList<>();
    private static final ArrayList<BufferedReader> entradas = new ArrayList<>();
    private static final ArrayList<String> nomes = new ArrayList<>();
    private static final char[] cores = {'B', 'W'};
    private static int jogadorAtual = 0;

    public static void main(String[] args) {
        String ipManual = "192.168.1.144"; // <- Define aqui o IP
        int porta = 12345;

        try (ServerSocket serverSocket = new ServerSocket(porta, 0, InetAddress.getByName(ipManual))) {
            System.out.println("Servidor Reversi a correr em " + ipManual + ":" + porta);
            System.out.println("À escuta de clientes...");

            while (jogadores.size() < 2) {
                Socket cliente = serverSocket.accept();
                System.out.println("Jogador ligado.");
                PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                jogadores.add(out);
                entradas.add(in);

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

            while (true) {
                boolean mensagemProcessada = false;

                while (!mensagemProcessada) {
                    for (int i = 0; i < entradas.size(); i++) {
                        if (i != jogadorAtual) continue; // Ignora quem não for o da vez

                        BufferedReader entrada = entradas.get(i);
                        if (entrada.ready()) {
                            String linha = entrada.readLine();
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
                                        return;
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
                                jogadores.remove(jogadorAtual);
                                entradas.remove(jogadorAtual);
                                nomes.remove(jogadorAtual);

                                if (jogadores.size() == 1) {
                                    jogadores.get(0).println("ESPERANDO");
                                }

                                if (jogadores.size() > 0) {
                                    jogadorAtual = jogadorAtual % jogadores.size();
                                }
                                mensagemProcessada = true;
                                break;
                            }
                        }
                    }

                    try { Thread.sleep(20); } catch (InterruptedException e) { }
                }
            }

        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        }
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
