package modelo;

import modelo.Casa;
import modelo.Peca;

public class Tabuleiro {
    private final Casa[][] casas;

    public Tabuleiro() {
        casas = new Casa[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                casas[i][j] = new Casa(i, j);
        inicializar();
    }

    public void inicializar() {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                casas[i][j].setPeca(null);

        casas[3][3].setPeca(new Peca('W'));
        casas[3][4].setPeca(new Peca('B'));
        casas[4][3].setPeca(new Peca('B'));
        casas[4][4].setPeca(new Peca('W'));
    }

    public char getPeca(int linha, int coluna) {
        Peca p = casas[linha][coluna].getPeca();
        return p == null ? '-' : p.getCor();
    }

    public void setPeca(int linha, int coluna, char cor) {
        casas[linha][coluna].setPeca(new Peca(cor));
    }

    public boolean jogadaValida(int linha, int coluna, char cor) {
        if (!casas[linha][coluna].estaVazia()) return false;
        return existeCaptura(linha, coluna, cor);
    }

    private boolean existeCaptura(int linha, int coluna, char cor) {
        // Direções para procurar capturas
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        char adversario = (cor == 'B') ? 'W' : 'B';

        for (int dir = 0; dir < 8; dir++) {
            int x = linha + dx[dir], y = coluna + dy[dir];
            boolean encontrouAdversario = false;

            while (x >= 0 && x < 8 && y >= 0 && y < 8) {
                char c = getPeca(x, y);
                if (c == adversario) {
                    encontrouAdversario = true;
                    x += dx[dir];
                    y += dy[dir];
                } else if (c == cor) {
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

    public void jogar(int linha, int coluna, char cor) {
        if (!jogadaValida(linha, coluna, cor)) return;
        casas[linha][coluna].setPeca(new Peca(cor));
        virarPecas(linha, coluna, cor);
    }

    private void virarPecas(int linha, int coluna, char cor) {
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        char adversario = (cor == 'B') ? 'W' : 'B';

        for (int dir = 0; dir < 8; dir++) {
            int x = linha + dx[dir], y = coluna + dy[dir];
            boolean encontrouAdversario = false;
            int passos = 0;

            while (x >= 0 && x < 8 && y >= 0 && y < 8) {
                char c = getPeca(x, y);
                if (c == adversario) {
                    encontrouAdversario = true;
                    x += dx[dir];
                    y += dy[dir];
                    passos++;
                } else if (c == cor) {
                    if (encontrouAdversario) {
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

    public int contarPecas(char cor) {
        int cont = 0;
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (getPeca(i, j) == cor)
                    cont++;
        return cont;
    }
}
