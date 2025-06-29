package modelo;

/**
 * Esta classe representa uma peça do jogo Reversi (também conhecido como Othello).
 * Cada peça tem uma cor: 'B' para preto ou 'W' para branco.
 */
public class Peca {
    // Atributo que guarda a cor da peça
    // Pode ser 'B' (para peça preta) ou 'W' (para peça branca)
    private char cor;

    /**
     * Construtor da peça.
     * Este método é chamado quando se cria uma nova peça.
     *
     * @param cor A cor da peça, deve ser 'B' (preto) ou 'W' (branco)
     */
    public Peca(char cor) {
        this.cor = cor; // Guarda a cor recebida na variável da peça
    }

    /**
     * Método que devolve a cor atual da peça.
     *
     * @return 'B' ou 'W'
     */
    public char getCor() {
        return cor;
    }

    /**
     * Método que altera a cor da peça.
     * Nota: no Reversi, uma peça pode mudar de cor quando é capturada.
     *
     * @param cor Nova cor da peça ('B' ou 'W')
     */
    public void setCor(char cor) {
        this.cor = cor;
    }
}
