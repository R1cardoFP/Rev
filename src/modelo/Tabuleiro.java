package modelo;

public class Tabuleiro {
    private final char[][] tabuleiro;

    public Tabuleiro() {
        tabuleiro = new char[8][8];
        inicializar();
    }

    public void inicializar() {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                tabuleiro[i][j] = '-';

        tabuleiro[3][3] = 'W';
        tabuleiro[3][4] = 'B';
        tabuleiro[4][3] = 'B';
        tabuleiro[4][4] = 'W';
    }

    public char getPeca(int linha, int coluna) {
        return tabuleiro[linha][coluna];
    }

    public void setPeca(int linha, int coluna, char cor) {
        tabuleiro[linha][coluna] = cor;
    }

    public boolean jogadaValida(int linha, int coluna, char cor) {
        if (tabuleiro[linha][coluna] != '-') return false;
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
                if (tabuleiro[x][y] == adversario) {
                    encontrouAdversario = true;
                    x += dx[dir];
                    y += dy[dir];
                } else if (tabuleiro[x][y] == cor) {
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
        tabuleiro[linha][coluna] = cor;
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
                if (tabuleiro[x][y] == adversario) {
                    encontrouAdversario = true;
                    x += dx[dir];
                    y += dy[dir];
                    passos++;
                } else if (tabuleiro[x][y] == cor) {
                    if (encontrouAdversario) {
                        // virar as peças entre linha,coluna e x,y
                        int vx = linha + dx[dir], vy = coluna + dy[dir];
                        for (int i = 0; i < passos; i++) {
                            tabuleiro[vx][vy] = cor;
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
                if (tabuleiro[i][j] == cor)
                    cont++;
        return cont;
    }
}
