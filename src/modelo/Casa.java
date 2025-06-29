package modelo;

/**
 * Esta classe representa uma casa (ou célula) no tabuleiro de um jogo como o Reversi.
 * Cada casa sabe:
 * - onde está (linha e coluna)
 * - se tem uma peça colocada ou não
 */
public class Casa {

    // A linha onde esta casa está localizada (entre 0 e 7 no tabuleiro)
    private int linha;

    // A coluna onde esta casa está localizada (entre 0 e 7 no tabuleiro)
    private int coluna;

    // A peça que está nesta casa (pode ser preta 'B', branca 'W' ou nenhuma)
    private Peca peca;

    /**
     * Construtor da classe Casa.
     * Este método é chamado quando se quer criar uma nova casa no tabuleiro.
     *
     * @param linha A linha onde a casa está (0 a 7)
     * @param coluna A coluna onde a casa está (0 a 7)
     */
    public Casa(int linha, int coluna) {
        this.linha = linha;         // Guarda a linha fornecida
        this.coluna = coluna;       // Guarda a coluna fornecida
        this.peca = null;           // Ao criar a casa, ela começa vazia (sem peça)
    }

    /**
     * Método para obter a linha da casa.
     *
     * @return O número da linha
     */
    public int getLinha() {
        return linha;
    }

    /**
     * Método para obter a coluna da casa.
     *
     * @return O número da coluna
     */
    public int getColuna() {
        return coluna;
    }

    /**
     * Método para obter a peça atual que está nesta casa.
     *
     * @return A peça presente (ou null se a casa estiver vazia)
     */
    public Peca getPeca() {
        return peca;
    }

    /**
     * Método para colocar (ou remover) uma peça nesta casa.
     *
     * @param peca A peça a colocar (ou null para remover a peça)
     */
    public void setPeca(Peca peca) {
        this.peca = peca;
    }

    /**
     * Método para verificar se a casa está vazia (ou seja, sem nenhuma peça).
     *
     * @return true se não houver peça na casa, false se tiver alguma peça
     */
    public boolean estaVazia() {
        return peca == null;
    }
}
