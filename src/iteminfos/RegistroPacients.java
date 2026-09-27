package iteminfos;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RegistroPacients {

    private static final String BASE_DIR = "Pacientes";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm");

    static {
        File base = new File(BASE_DIR);
        if (!base.exists()) base.mkdir();
    }

    // 🔹 Verifica se paciente já possui pasta registrada
    public static boolean pacienteJaExiste(String nome) {
        File pasta = new File(BASE_DIR + "/" + nome.replace(" ", "_"));
        return pasta.exists();
    }

    // 🔹 Cria pasta e salva CSV (somente se ainda não existir)
    public static void salvarFichaCSV(SuperPacient paciente) {
        try {
            String nomePasta = BASE_DIR + "/" + paciente.getNome().replace(" ", "_");
            Files.createDirectories(Paths.get(nomePasta));

            File arquivoCSV = new File(nomePasta + "/dados.csv");
            if (arquivoCSV.exists()) {
                System.out.println("⚠️ Paciente já registrado. Nenhum novo dado foi adicionado.");
                return;
            }

            try (FileWriter fw = new FileWriter(arquivoCSV)) {
                fw.write("Nome,Localidade,Email,Cpf,Data de Nascimento,Idade\n");
                fw.write(String.format("%s,%s,%s,%s,%s,%d\n",
                        paciente.getNome(),
                        paciente.getLocal(),
                        paciente.getEmail(),
                        paciente.getCpf(),
                        paciente.getDataNascimentoFormatada(),
                        Period.between(paciente.getDataNascimento(), LocalDate.now()).getYears()));
            }

        } catch (IOException e) {
            System.err.println("Erro ao salvar dados: " + e.getMessage());
        }
    }

    // 🔹 Lista todos os pacientes (nomes das pastas)
    public static void listarPacientes() {
        File base = new File(BASE_DIR);
        File[] pastas = base.listFiles(File::isDirectory);

        if (pastas == null || pastas.length == 0) {
            System.out.println("📭 Nenhum paciente registrado ainda.");
            return;
        }

        for (File pasta : pastas) {
            System.out.println(" - " + pasta.getName().replace("_", " "));
        }
    }

    // 🔹 Carrega paciente a partir do CSV
    public static SuperPacient carregarPaciente(String nome) {
        File arquivoCSV = new File(BASE_DIR + "/" + nome.replace(" ", "_") + "/dados.csv");

        if (!arquivoCSV.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(arquivoCSV))) {
            br.readLine(); // pula o cabeçalho
            String linha = br.readLine();
            if (linha != null) {
                String[] dados = linha.split(",");
                return new SuperPacient(dados[0], dados[1], dados[2], dados[3], dados[4]);
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar paciente: " + e.getMessage());
        }
        return null;
    }

    // 🔹 Salva relatório com data/hora embutida
    public static void salvarRelatorio(SuperPacient paciente, String conteudoRelatorio) {
        try {
            String nomePasta = BASE_DIR + "/" + paciente.getNome().replace(" ", "_");
            Files.createDirectories(Paths.get(nomePasta));

            String nomeArquivo = "Relatorio_" + LocalDateTime.now().format(FORMATO_DATA) + ".txt";
            Path caminho = Paths.get(nomePasta, nomeArquivo);

            String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String conteudoFinal = "[" + dataHora + "] " + conteudoRelatorio;

            Files.write(caminho, conteudoFinal.getBytes());

            System.out.println("✅ Relatório salvo: " + nomeArquivo);
        } catch (IOException e) {
            System.err.println("Erro ao salvar relatório: " + e.getMessage());
        }
    }

    // 🔹 Retorna lista de relatórios (para exibir na interface)
    public static List<String> listarRelatorios(SuperPacient paciente) {
        List<String> relatorios = new ArrayList<>();
        String nomePasta = BASE_DIR + "/" + paciente.getNome().replace(" ", "_");
        File pasta = new File(nomePasta);

        if (!pasta.exists()) {
            System.out.println("⚠️ Nenhuma pasta encontrada para este paciente.");
            return relatorios;
        }

        File[] arquivos = pasta.listFiles((dir, nome) -> nome.startsWith("Relatorio_") && nome.endsWith(".txt"));
        if (arquivos == null || arquivos.length == 0) {
            System.out.println("📭 Nenhum relatório encontrado.");
            return relatorios;
        }

        Arrays.sort(arquivos, Comparator.comparingLong(File::lastModified));

        for (File arquivo : arquivos) {
            try {
                String conteudo = Files.readString(arquivo.toPath());
                relatorios.add(conteudo);
            } catch (IOException e) {
                System.err.println("Erro ao ler relatório: " + e.getMessage());
            }
        }

        return relatorios;
    }
}