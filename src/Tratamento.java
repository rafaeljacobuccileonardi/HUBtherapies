public class Tratamento {
    private String nome;
    private String descricao;
    private double valor;

    public Tratamento(String nome, String descricao, double valor) {
        this.nome = nome;
        this.descricao = descricao;
        this.valor = valor;
    }

    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public double getValor() { return valor; }

    public void setNome(String nome) { this.nome = nome; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setValor(double valor) { this.valor = valor; }
}
