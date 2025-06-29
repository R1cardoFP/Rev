package modelo;

public class Jogador {
    private String nome;
    private char cor; // 'B' ou 'W'

    public Jogador(String nome, char cor) {
        this.nome = nome;
        this.cor = cor;
    }

    public String getNome() {
        return nome;
    }

    public char getCor() {
        return cor;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCor(char cor) {
        this.cor = cor;
    }
}
