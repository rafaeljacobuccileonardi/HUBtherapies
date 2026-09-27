package iteminfos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.Period;

public class SuperPacient {

    private String nome;
    private String local;
    private String email;
    private String cpf;
    private LocalDate dataNascimento;

        private static final DateTimeFormatter FORMATADOR_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            public SuperPacient(String nome, String local, String email, String cpf, String dataNascimento) {
                this.nome = nome;
                this.local = local;
                this.email = email;
                this.cpf = cpf;
                this.dataNascimento = LocalDate.parse(dataNascimento, FORMATADOR_BR);
            }

                // Getter formato da data
                public String getDataNascimentoFormatada() {
                    return dataNascimento.format(FORMATADOR_BR);
                }

                // Cálculo automático da idade
                public int getIdade() {
                    return Period.between(dataNascimento, LocalDate.now()).getYears();
                }

                // Getters e Setters básicos
                public String getNome() { return nome; }
                public void setNome(String nome) { this.nome = nome; }

                public String getLocal() { return local; }
                public void setLocal(String local) { this.local = local; }

                public String getEmail() { return email; }
                public void setEmail(String email) { this.email = email; }

                public String getCpf() { return cpf; }
                public void setCpf(String cpf) { this.cpf = cpf; }

                public LocalDate getDataNascimento() { return dataNascimento; }
                public void setDataNascimento(String dataNascimento) {
                this.dataNascimento = LocalDate.parse(dataNascimento, FORMATADOR_BR);
                }


    @Override
    public String toString() {
        return String.format(
                "Nome: %s%nLocalidade: %s%nEmail: %s%nData de Nascimento: %s%nIdade: %d anos%nCPF: %s%n",
                nome,
                local,
                email,
                getDataNascimentoFormatada(), // %s
                getIdade(),                   // %d
                cpf                           // %s
        );
    }

}
