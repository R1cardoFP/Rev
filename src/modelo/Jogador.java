package modelo;

/**
 * Esta classe representa um jogador do jogo Reversi.
 * Cada jogador tem um nome e uma cor ('B' para preto, 'W' para branco).
 */
public class Jogador {

    // Variável que guarda o nome do jogador (ex: "Maria", "João", etc.)
    private String nome;

    // Variável que guarda a cor atribuída ao jogador
    // 'B' significa que o jogador joga com as peças pretas
    // 'W' significa que o jogador joga com as peças brancas
    private char cor;

    /**
     * Construtor da classe Jogador.
     * Este método é chamado quando se cria um novo jogador.
     *
     * @param nome Nome do jogador (pode ser escolhido pelo utilizador)
     * @param cor Cor atribuída ao jogador ('B' ou 'W')
     */
    public Jogador(String nome, char cor) {
        this.nome = nome; // Guarda o nome fornecido
        this.cor = cor;   // Guarda a cor atribuída
    }

    /**
     * Método para obter (ver) o nome do jogador.
     *
     * @return O nome do jogador
     */
    public String getNome() {
        return nome;
    }

    /**
     * Método para obter (ver) a cor do jogador.
     *
     * @return 'B' ou 'W'
     */
    public char getCor() {
        return cor;
    }

    /**
     * Método para alterar o nome do jogador.
     *
     * @param nome Novo nome a atribuir
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Método para alterar a cor do jogador.
     * Isto raramente é usado, já que a cor normalmente é fixa.
     *
     * @param cor Nova cor a atribuir ('B' ou 'W')
     */
    public void setCor(char cor) {
        this.cor = cor;
    }
}
