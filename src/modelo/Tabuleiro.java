package modelo;

import modelo.Casa;
import modelo.Peca;

/**
 * Esta classe representa o tabuleiro do jogo Reversi.
 * É composta por uma grelha 8x8 de casas, e controla toda a lógica das jogadas.
 */
public class Tabuleiro {
    // Matriz 8x8 que representa as casas do tabuleiro
    private final Casa[][] casas;

    /**
     * Construtor do tabuleiro. Cria uma grelha 8x8 e inicializa o estado inicial do jogo.
     */
    public Tabuleiro() {
        casas = new Casa[8][8];
        // Inicializa todas as casas da grelha
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                casas[i][j] = new Casa(i, j);

        // Define as peças iniciais no centro do tabuleiro
        inicializar();
    }

    /**
     * Define o estado inicial do tabuleiro:
     * - Limpa todas as casas
     * - Coloca as 4 peças centrais (2 brancas e 2 pretas)
     */
    public void inicializar() {
        // Remove todas as peças do tabuleiro (casas ficam vazias)
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                casas[i][j].setPeca(null);

        // Coloca as peças iniciais no centro do tabuleiro (posição padrão do Reversi)
        casas[3][3].setPeca(new Peca('W')); // W = Branco
        casas[3][4].setPeca(new Peca('B')); // B = Preto
        casas[4][3].setPeca(new Peca('B'));
        casas[4][4].setPeca(new Peca('W'));
    }

    /**
     * Retorna a peça presente numa determinada posição do tabuleiro.
     * Se não houver peça, devolve '-'.
     */
    public char getPeca(int linha, int coluna) {
        Peca p = casas[linha][coluna].getPeca();
        return p == null ? '-' : p.getCor();
    }

    /**
     * Coloca uma peça de uma cor específica numa dada posição do tabuleiro.
     */
    public void setPeca(int linha, int coluna, char cor) {
        casas[linha][coluna].setPeca(new Peca(cor));
    }

    /**
     * Verifica se uma jogada é válida.
     * Uma jogada é válida se a casa estiver vazia e se for possível capturar peças do adversário.
     */
    public boolean jogadaValida(int linha, int coluna, char cor) {
        if (!casas[linha][coluna].estaVazia()) return false;
        return existeCaptura(linha, coluna, cor);
    }

    /**
     * Verifica se, ao colocar uma peça na posição dada, é possível capturar peças do adversário.
     * Esta função percorre todas as direções possíveis à procura de peças do adversário.
     */
    private boolean existeCaptura(int linha, int coluna, char cor) {
        // Direções em que se pode capturar (horizontal, vertical e diagonal)
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        char adversario = (cor == 'B') ? 'W' : 'B'; // Define a cor do adversário

        // Percorre todas as 8 direções
        for (int dir = 0; dir < 8; dir++) {
            int x = linha + dx[dir], y = coluna + dy[dir];
            boolean encontrouAdversario = false;

            // Percorre casas na direção atual até sair do tabuleiro ou encontrar fim de jogada
            while (x >= 0 && x < 8 && y >= 0 && y < 8) {
                char c = getPeca(x, y);
                if (c == adversario) {
                    encontrouAdversario = true;
                    x += dx[dir];
                    y += dy[dir];
                } else if (c == cor) {
                    // Só é válido se pelo menos uma peça do adversário for capturada
                    if (encontrouAdversario)
                        return true;
                    else
                        break;
                } else {
                    break;
                }
            }
        }
        return false;
    }

    /**
     * Executa uma jogada válida no tabuleiro.
     * Coloca a peça e vira as peças do adversário capturadas.
     */
    public void jogar(int linha, int coluna, char cor) {
        if (!jogadaValida(linha, coluna, cor)) return;
        casas[linha][coluna].setPeca(new Peca(cor)); // Coloca a peça
        virarPecas(linha, coluna, cor); // Vira as peças do adversário
    }

    /**
     * Vira as peças do adversário que foram capturadas numa jogada.
     * Esta função percorre todas as direções e altera as peças capturadas.
     */
    private void virarPecas(int linha, int coluna, char cor) {
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        char adversario = (cor == 'B') ? 'W' : 'B';

        // Percorre todas as 8 direções
        for (int dir = 0; dir < 8; dir++) {
            int x = linha + dx[dir], y = coluna + dy[dir];
            boolean encontrouAdversario = false;
            int passos = 0; // Conta quantas peças do adversário foram encontradas

            // Vai percorrendo enquanto estiver dentro dos limites
            while (x >= 0 && x < 8 && y >= 0 && y < 8) {
                char c = getPeca(x, y);
                if (c == adversario) {
                    encontrouAdversario = true;
                    x += dx[dir];
                    y += dy[dir];
                    passos++;
                } else if (c == cor) {
                    if (encontrouAdversario) {
                        // Vira todas as peças do adversário que estavam entre as peças do jogador
                        int vx = linha + dx[dir], vy = coluna + dy[dir];
                        for (int i = 0; i < passos; i++) {
                            casas[vx][vy].setPeca(new Peca(cor));
                            vx += dx[dir];
                            vy += dy[dir];
                        }
                    }
                    break;
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Conta o número de peças de uma determinada cor no tabuleiro.
     */
    public int contarPecas(char cor) {
        int cont = 0;
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (getPeca(i, j) == cor)
                    cont++;
        return cont;
    }
}
