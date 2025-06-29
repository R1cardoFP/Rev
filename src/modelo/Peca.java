package modelo;

public class Peca {
    private char cor; // 'B' ou 'W'

    public Peca(char cor) {
        this.cor = cor;
    }

    public char getCor() {
        return cor;
    }

    public void setCor(char cor) {
        this.cor = cor;
    }
}
