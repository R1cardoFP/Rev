package servidor;

import modelo.Tabuleiro;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServidorReversi {
    private static Tabuleiro tabuleiro = new Tabuleiro();
    private static ArrayList<PrintWriter> jogadores = new ArrayList<>();
    private static ArrayList<BufferedReader> entradas = new ArrayList<>();
    private static ArrayList<String> nomes = new ArrayList<>();
    private static char[] cores = {'B', 'W'};
    private static int jogadorAtual = 0;

    public static void main(String[] args) {
        String ipManual = "192.168.1.138"; // <- Define aqui o IP
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

                // Recebe o nome do jogador
                String nome = in.readLine();
                if (nome == null || nome.isEmpty()) nome = "Jogador" + jogadores.size();
                nomes.add(nome);

                out.println(cores[jogadores.size() - 1]); // Envia cor ao jogador

                // Se já há dois jogadores, envia o nome do adversário para cada um
                if (jogadores.size() == 2) {
                    jogadores.get(0).println(nomes.get(1));
                    jogadores.get(1).println(nomes.get(0));
                }
            }

            enviarMensagemATodos("COMEÇAR");
            tabuleiro.inicializar();
            jogadores.get(jogadorAtual).println("SUA_VEZ");

            while (true) {
                PrintWriter atual = jogadores.get(jogadorAtual);
                BufferedReader entradaAtual = entradas.get(jogadorAtual);

                String linha = entradaAtual.readLine();
                if (linha == null) break;

                if (linha.startsWith("JOGADA")) {
                    String[] partes = linha.split(" ");
                    int x = Integer.parseInt(partes[1]);
                    int y = Integer.parseInt(partes[2]);
                    char cor = cores[jogadorAtual];

                    if (tabuleiro.jogadaValida(x, y, cor)) {
                        tabuleiro.jogar(x, y, cor);
                        enviarJogadaParaJogadores(x, y, cor);

                        if (fimDeJogo()) {
                            enviarMensagemATodos("FIM");
                            break;
                        } else {
                            jogadorAtual = (jogadorAtual + 1) % 2;
                            enviarMensagemATodos("SUA_VEZ");
                        }
                    }
                } else if (linha.equals("TEMPO_ESGOTADO")) {
                    jogadorAtual = (jogadorAtual + 1) % 2;
                    enviarMensagemATodos("SUA_VEZ");
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
}
