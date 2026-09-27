import iteminfos.SuperPacient;
import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.awt.Desktop;
import java.nio.charset.StandardCharsets;
import java.io.FileWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.time.LocalDate;
import java.awt.image.BufferedImage;
/**

 Pastas: "Pacientes" e "Tratamentos" (mesmo diretório do projeto).

 Arquivo global de tratamentos: Tratamentos/tratamentos.txt (formato: nome;descricao;valor)

 Para cada paciente, tratamentos associados são listados em: Pacientes/NAME/treatments.txt

 Para cada paciente+tratamento:
      Pacientes/NAME/treatment_<safeName>_reports.txt  (balões/anotações)
      Pacientes/NAME/treatment_<safeName>_checklist.txt (checklist específico)

 Observação: mantém funcionamento existente quando nenhum tratamento está selecionado:
 Pacientes/NAME/reports.txt e Pacientes/NAME/checklist.txt continuam sendo usados.
 */
public class PacientsInterface extends JFrame {


    private static final String BASE_DIR = "Pacientes";
    private static final String TRAT_DIR = "Tratamentos";
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // layout principal
    private CardLayout cardLayout;
    private JPanel painelPrincipal;

    // cadastro do paciente
    private JTextField tfNome, tfLocal, tfEmail, tfCpf;
    private JFormattedTextField tfDataNasc;

    // pacientes list
    private DefaultListModel<String> modeloLista;
    private JList<String> listaPacientes;
    private JPanel painelInfo;

    // painel central do paciente
    private JPanel painelBaloes; // balões (relatórios) - VISUAL
    private JScrollPane scrollBaloes;

    // checklist VISUAL
    private JPanel painelChecklist;
    private JScrollPane scrollChecklist;
    private java.util.List<JCheckBox> checklistBoxes = new ArrayList<>();
    private JButton btnAddSessao;
    private JButton btnRemoverSessao;

    // TRATAMENTOS locais à UI paciente
    private JComboBox<String> cbTratamentos; // drop box abaixo da lista de pacientes
    private static final String NOVO_TRATAMENTO_LABEL = ">> Novo";
    // mantém track do tratamento selecionado para o paciente atual
    private String currentPatientSelectedTreatment = null;

    // painel Cards de tratamentos (global view)
    private JPanel painelCardsTratamentos;

    private String ultimoPacienteSelecionado = null;
    private JPanel painelDetalhesPaciente;

    // === VARIÁVEIS GLOBAIS DO RECEITUÁRIO ===
    private DefaultListModel<String> modeloReceitPacientes = new DefaultListModel<>();
    private JList<String> listaReceitPacientes = new JList<>(modeloReceitPacientes);
    private JTextArea areaReceita = new JTextArea();

    // Labels visíveis em toda a classe (globais)
    private JLabel lblNome;
    private JLabel lblEmail;
    private JLabel lblLocal;
    private JLabel lblDataNasc;
    private JLabel lblCpf;

    private String tituloTelaInicial = "HUBTerapias Harmônicas";
    private JLabel lblTituloTelaInicial;  // JLabel do título da tela inicial


    public PacientsInterface() {
        carregarTituloDoPrograma(); // carregar ANTES de criar janela

        super.setTitle(tituloTelaInicial);   // título na barra da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);

        ImageIcon icon = new ImageIcon("icon2.png");
        Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH); setIconImage(img); //ICONE



        cardLayout = new CardLayout();
        painelPrincipal = new JPanel(cardLayout);

        painelPrincipal.add(criarTelaInicial(), "inicio");
        painelPrincipal.add(criarTelaCadastro(), "cadastro");
        painelPrincipal.add(criarTelaPacientes(), "pacientes");
        painelPrincipal.add(criarTelaTratamentos(), "tratamentos");
        painelPrincipal.add(criarTelaReceituario(), "receituario");
        painelPrincipal.add(criarTelaAgenda(), "agenda");

        painelPrincipal.add(criarTelaConfiguracoes(), "configuracoes");

        add(painelPrincipal);
        cardLayout.show(painelPrincipal, "inicio");

        // garante pasta de tratamentos
        File pastaTrat = new File(TRAT_DIR);
        if (!pastaTrat.exists()) pastaTrat.mkdir();

        // garante pasta base de pacientes
        File pastaPac = new File(BASE_DIR);
        if (!pastaPac.exists()) pastaPac.mkdir();
    }



    // ---------------- TELA INICIAL ----------------
    private JPanel criarTelaInicial() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        lblTituloTelaInicial = new JLabel(tituloTelaInicial, SwingConstants.CENTER);
        lblTituloTelaInicial.setFont(new Font("Arial", Font.BOLD, 48));

        painel.add(lblTituloTelaInicial, BorderLayout.NORTH);


        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---- BOTÕES PRINCIPAIS ----
        JButton btnCadastro = new JButton("Cadastrar novo paciente");
        JButton btnPacientes = new JButton("Pacientes cadastrados");
        JButton btnTratamentos = new JButton("Tratamentos");
        JButton btnReceituario = new JButton("Receituário");
        JButton btnAgenda = new JButton("Agenda");
        JButton btnConfiguracoes = new JButton("Configurações"); // fica NO RODAPÉ

        // ---- FONTES ----
        Font fontePrincipal = new Font("SansSerif", Font.BOLD, 18);
        btnCadastro.setFont(fontePrincipal);
        btnPacientes.setFont(fontePrincipal);
        btnTratamentos.setFont(fontePrincipal);
        btnReceituario.setFont(fontePrincipal);
        btnAgenda.setFont(fontePrincipal);

        btnConfiguracoes.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // ---- TAMANHOS ----
        Dimension tamanhoBotao = new Dimension(240, 45);
        btnCadastro.setPreferredSize(tamanhoBotao);
        btnPacientes.setPreferredSize(tamanhoBotao);
        btnTratamentos.setPreferredSize(tamanhoBotao);
        btnReceituario.setPreferredSize(tamanhoBotao);
        btnAgenda.setPreferredSize(tamanhoBotao);

        btnCadastro.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPacientes.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTratamentos.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnReceituario.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAgenda.setAlignmentX(Component.CENTER_ALIGNMENT);


        centro.add(Box.createVerticalStrut(30));
        centro.add(btnCadastro);
        centro.add(Box.createVerticalStrut(9));

        centro.add(btnPacientes);
        centro.add(Box.createVerticalStrut(9));

        centro.add(btnTratamentos);
        centro.add(Box.createVerticalStrut(9));

        centro.add(btnReceituario);
        centro.add(Box.createVerticalStrut(9));

        // AGENDA LOGO ABAIXO DE RECEITUÁRIO
        centro.add(btnAgenda);

        // EMPURRA CONFIGURAÇÕES PARA O RODAPÉ
        centro.add(Box.createVerticalGlue());

        // CONFIGURAÇÕES POR ÚLTIMO, NO FUNDO DA TELA
        btnConfiguracoes.setAlignmentX(Component.CENTER_ALIGNMENT);
        centro.add(btnConfiguracoes);

        // ---- AÇÕES ----
        btnCadastro.addActionListener(e -> cardLayout.show(painelPrincipal, "cadastro"));

        btnPacientes.addActionListener(e -> {
            atualizarListaPacientes();
            cardLayout.show(painelPrincipal, "pacientes");
        });

        btnTratamentos.addActionListener(e -> {
            atualizarListaTratamentos();
            cardLayout.show(painelPrincipal, "tratamentos");
        });

        btnReceituario.addActionListener(e -> {
            atualizarListaPacientesReceituario();
            cardLayout.show(painelPrincipal, "receituario");
        });

        btnAgenda.addActionListener(e -> {
            mesAtual = LocalDate.now().withDayOfMonth(1);  // garante mês atual
            atualizarCalendario();
            cardLayout.show(painelPrincipal, "agenda");
        });

        btnConfiguracoes.addActionListener(e -> {
            cardLayout.show(painelPrincipal, "configuracoes");
        });

        painel.add(centro, BorderLayout.CENTER);
        return painel;
    }


    // ---------------- TELA CADASTRO ----------------
    private JPanel criarTelaCadastro() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        JLabel lbl = new JLabel("Cadastrar Novo Paciente", SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 26));
        painel.add(lbl, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        Dimension campoTamanho = new Dimension(340, 28);
        tfNome = new JTextField();
        tfLocal = new JTextField();
        tfEmail = new JTextField();
        tfCpf = new JTextField();

        try {
            MaskFormatter formatoData = new MaskFormatter("##/##/####");
            formatoData.setPlaceholderCharacter('_');
            tfDataNasc = new JFormattedTextField(formatoData);
            tfDataNasc.setColumns(10);
        } catch (ParseException ex) {
            tfDataNasc = new JFormattedTextField();
            System.err.println("Erro ao criar máscara para data: " + ex.getMessage());
        }

        tfNome.setPreferredSize(campoTamanho);
        tfDataNasc.setPreferredSize(campoTamanho);
        tfLocal.setPreferredSize(campoTamanho);
        tfEmail.setPreferredSize(campoTamanho);
        tfCpf.setPreferredSize(campoTamanho);

        Font fonteLabel = new Font("Arial", Font.PLAIN, 18);
        int linha = 0;
        gbc.gridx = 0; gbc.gridy = linha;
        JLabel lblNome = new JLabel("Nome:"); lblNome.setFont(fonteLabel); centro.add(lblNome, gbc);
        gbc.gridx = 1; centro.add(tfNome, gbc);

        linha++;
        gbc.gridx = 0; gbc.gridy = linha;
        JLabel lblData = new JLabel("Data de Nascimento:"); lblData.setFont(fonteLabel); centro.add(lblData, gbc);
        gbc.gridx = 1; centro.add(tfDataNasc, gbc);

        linha++;
        gbc.gridx = 0; gbc.gridy = linha;
        JLabel lblLocal = new JLabel("Localidade:"); lblLocal.setFont(fonteLabel); centro.add(lblLocal, gbc);
        gbc.gridx = 1; centro.add(tfLocal, gbc);

        linha++;
        gbc.gridx = 0; gbc.gridy = linha;
        JLabel lblEmail = new JLabel("Email:"); lblEmail.setFont(fonteLabel); centro.add(lblEmail, gbc);
        gbc.gridx = 1; centro.add(tfEmail, gbc);

        linha++;
        gbc.gridx = 0; gbc.gridy = linha;
        JLabel lblCpf = new JLabel("CPF:"); lblCpf.setFont(fonteLabel); centro.add(lblCpf, gbc);
        gbc.gridx = 1; centro.add(tfCpf, gbc);

        painel.add(centro, BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        JButton btnSalvar = new JButton("Cadastrar");
        JButton btnVoltar = new JButton("Voltar (Início)");
        btnSalvar.setPreferredSize(new Dimension(140, 35));
        btnVoltar.setPreferredSize(new Dimension(140, 35));
        botoes.add(btnSalvar); botoes.add(btnVoltar);
        painel.add(botoes, BorderLayout.SOUTH);

        btnSalvar.addActionListener(e -> cadastrarPaciente());
        btnVoltar.addActionListener(e -> cardLayout.show(painelPrincipal, "inicio"));
        return painel;
    }

    // ---------------- TELA PACIENTES ----------------
    private JPanel criarTelaPacientes() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel titulo = new JLabel("Pacientes Cadastrados");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        painel.add(titulo, BorderLayout.NORTH);

        painelDetalhesPaciente = new JPanel(new BorderLayout());


        // modelo e JList
        modeloLista = new DefaultListModel<>();
        listaPacientes = new JList<>(modeloLista);
        listaPacientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaPacientes.setVisibleRowCount(5);
        listaPacientes.setFixedCellHeight(28);
        listaPacientes.setFont(new Font("Arial", Font.PLAIN, 16));

        JScrollPane scrollLista = new JScrollPane(listaPacientes);
        scrollLista.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollLista.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollLista.setPreferredSize(new Dimension(220, 240));

// painel que contém lista + combo (combo logo abaixo da lista)
        JPanel painelLista = new JPanel();
        painelLista.setLayout(new BoxLayout(painelLista, BoxLayout.Y_AXIS));
        painelLista.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelLista.setMaximumSize(new Dimension(260, Integer.MAX_VALUE));


// --- top: área fixa que contém o scroll da lista de pacientes ---
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        topWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
//scroll com tamanho fixo não altera layout
        JScrollPane scrollWrapper = new JScrollPane(listaPacientes);
        scrollWrapper.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollWrapper.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollWrapper.setPreferredSize(new Dimension(220, 240));
        scrollWrapper.setMaximumSize(scrollWrapper.getPreferredSize());
        topWrapper.add(scrollWrapper, BorderLayout.CENTER);

// adiciona topWrapper no painelLista
        painelLista.add(topWrapper);
        painelLista.add(Box.createRigidArea(new Dimension(0, 8)));

// --- middle: combo + espaçador pequeno ---
        JPanel middle = new JPanel();
        middle.setAlignmentX(Component.LEFT_ALIGNMENT);
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        middle.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        middle.setOpaque(false);


        JLabel comboLabel = new JLabel("Tratamentos (associados):");
        comboLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        comboLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        middle.add(comboLabel);
        middle.add(Box.createRigidArea(new Dimension(0,4)));


        cbTratamentos = new JComboBox<>();
        cbTratamentos.setEnabled(false);
        cbTratamentos.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cbTratamentos.setPreferredSize(new Dimension(220, cbTratamentos.getPreferredSize().height));
        cbTratamentos.setMaximumSize(cbTratamentos.getPreferredSize());
        cbTratamentos.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbTratamentos.setAlignmentX(Component.LEFT_ALIGNMENT);
        middle.add(cbTratamentos);

// adiciona middle ao centro
        middle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        painelLista.add(middle);
        painelLista.add(Box.createRigidArea(new Dimension(0, 8)));


// --- bottom: painel de Encontros (fica abaixo do combo) ---
        JPanel encontrosWrapper = new JPanel();
        encontrosWrapper.setLayout(new BoxLayout(encontrosWrapper, BoxLayout.Y_AXIS));
        encontrosWrapper.setBorder(BorderFactory.createEmptyBorder(8, 6, 6, 6));
        encontrosWrapper.setOpaque(false);


        JLabel lblEncontrosTitle = new JLabel("Encontros do Tratamento");
        lblEncontrosTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEncontrosTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        encontrosWrapper.add(lblEncontrosTitle);
        encontrosWrapper.add(Box.createRigidArea(new Dimension(0,6)));

// Lista de encontros
        DefaultListModel<String> modeloEncontros = new DefaultListModel<>();
        JList<String> listaEncontrosLocal = new JList<>(modeloEncontros);

        // --- Adiciona listener de clique (duplo clique) ---
        listaEncontrosLocal.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    String paciente = listaPacientes.getSelectedValue();
                    String tratamento = (String) cbTratamentos.getSelectedItem();
                    String encontroExibido = listaEncontrosLocal.getSelectedValue();
                    if (paciente == null || tratamento == null || encontroExibido == null) {
                        JOptionPane.showMessageDialog(PacientsInterface.this,
                                "Selecione primeiro: paciente -> tratamento -> encontro.");
                        return;
                    }
                    // remove o sufixo " (Pago)" ou " (Não pago)" se houver, para obter o nome original
                    String encontro = encontroExibido.replaceAll("\\s*\\(.*\\)$", "").trim();

                    // chama o método diálogo de escolha (Pago / Não Pago)
                    editarStatusDoEncontro(paciente, tratamento, encontro);

                    // depois de alterar o status, recarrega a lista e os totais
                    carregarEncontrosDoTratamentoParaUI(paciente, tratamento, modeloEncontros, lblTotais);
                    // atualiza também o label de totais global/fix (caso exista)
                    atualizarTotaisEncontros(paciente, tratamento);
                }
            }
        });


        listaEncontrosLocal.setVisibleRowCount(6);
        JScrollPane scrEncontros = new JScrollPane(listaEncontrosLocal);
        scrEncontros.setPreferredSize(new Dimension(220, 140));
        scrEncontros.setMaximumSize(new Dimension(220, 140));
        scrEncontros.setAlignmentX(Component.LEFT_ALIGNMENT);
        encontrosWrapper.add(scrEncontros);
        encontrosWrapper.add(Box.createRigidArea(new Dimension(0,8)));



// botão adicionar encontro (funcional)
        JButton btnAdicionarEncontroLocal = new JButton("Adicionar Encontro");
        btnAdicionarEncontroLocal.setAlignmentX(Component.LEFT_ALIGNMENT);

// Botão NOVO para gerar planilha
        JButton btnGerarPlanilha = new JButton("Gerar planilha de pagamentos");
        btnGerarPlanilha.setEnabled(false);
        btnGerarPlanilha.setAlignmentX(Component.LEFT_ALIGNMENT);

// Adiciona botões ao layout correto
        encontrosWrapper.add(btnAdicionarEncontroLocal);
        encontrosWrapper.add(Box.createRigidArea(new Dimension(0,6)));
        encontrosWrapper.add(btnGerarPlanilha);
        encontrosWrapper.add(Box.createRigidArea(new Dimension(0,6)));

        encontrosWrapper.add(lblTotais);
        lblTotais.setAlignmentX(Component.LEFT_ALIGNMENT);

// Listener do botão Gerar Planilha
        btnGerarPlanilha.addActionListener(e -> {
            String paciente = listaPacientes.getSelectedValue();
            String tratamento = (String) cbTratamentos.getSelectedItem();

            if (paciente == null || tratamento == null ||
                    tratamento.equals("-- Selecione --") ||
                    tratamento.equals(NOVO_TRATAMENTO_LABEL)) {

                JOptionPane.showMessageDialog(this,
                        "Selecione um paciente e um tratamento válido.");
                return;
            }

            gerarPlanilhaPagamentos(paciente, tratamento);
        });


// adiciona encontrosWrapper ao sul do painelLista (fica logo abaixo do centro)
        JScrollPane scrollEncontrosWrapper = new JScrollPane(
                encontrosWrapper,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollEncontrosWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollEncontrosWrapper.setMaximumSize(new Dimension(260, 300));

        scrollEncontrosWrapper.setBorder(null);
        painelLista.add(scrollEncontrosWrapper);



// --- wiring: listeners básicos para manter comportamento anterior do combo ---
        cbTratamentos.addActionListener(e -> {


            if (cbTratamentos.getSelectedItem() == null) return;
            String sel = (String) cbTratamentos.getSelectedItem();

            btnGerarPlanilha.setEnabled(
                    sel != null &&
                            !sel.equals("-- Selecione --") &&
                            !sel.equals(NOVO_TRATAMENTO_LABEL)
            );


            if (listaPacientes.getSelectedValue() == null) return;
            String nomePaciente = listaPacientes.getSelectedValue();

            if (sel.equals(NOVO_TRATAMENTO_LABEL)) {
                abrirDialogAssociarTratamento(nomePaciente);
                return;
            }
            if (sel.equals("-- Selecione --")) {
                currentPatientSelectedTreatment = null;
                carregarRelatorios(nomePaciente);
                carregarChecklist(nomePaciente);
                btnGerarPlanilha.setEnabled(true);


                if (!sel.equals("-- Selecione --") && !sel.equals(NOVO_TRATAMENTO_LABEL)) {
                    btnGerarPlanilha.setEnabled(true);
                }

                // limpa encontros visuais
                modeloEncontros.clear();
                lblTotais.setText("");

                return;
            }
            // tratamento selecionado -> carrega encontros do arquivo correspondente
            currentPatientSelectedTreatment = sel;
            carregarRelatorios(nomePaciente);
            carregarChecklist(nomePaciente);

            // chama método que lê encontros do arquivo e popula modeloEncontros & atualiza totais
            carregarEncontrosDoTratamentoParaUI(nomePaciente, sel, modeloEncontros, lblTotais);
        });

// listener do botão Adicionar Encontro (grava no arquivo específico e recarrega)
        btnAdicionarEncontroLocal.addActionListener(e -> {
            String paciente = listaPacientes.getSelectedValue();
            String tratamento = (String) cbTratamentos.getSelectedItem();
            if (paciente == null) { JOptionPane.showMessageDialog(this, "Selecione um paciente primeiro."); return; }
            if (tratamento == null || tratamento.equals("-- Selecione --") || tratamento.equals(NOVO_TRATAMENTO_LABEL)) {
                JOptionPane.showMessageDialog(this, "Selecione um tratamento associado para adicionar encontro.");
                return;
            }


            // sugere nome automático: "Encontro N"
            int prox = modeloEncontros.getSize() + 1;
            String nomePadrao = "Encontro " + prox;
            String entrada = JOptionPane.showInputDialog(this, "Nome do Encontro:", nomePadrao);
            if (entrada == null || entrada.trim().isEmpty()) return;
            // salva no arquivo de encontros do paciente+tratamento
            File f = getEncontrosFileForPatientTreatment(paciente, tratamento);
            try (FileWriter fw = new FileWriter(f, true); BufferedWriter bw = new BufferedWriter(fw)) {
                // formato simples: nome|pago(false)
                bw.write(entrada.trim() + "|false");

                bw.newLine();
            } catch (IOException ioex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar encontro: " + ioex.getMessage());
                return;
            }
            // recarrega.
            carregarEncontrosDoTratamentoParaUI(paciente, tratamento, modeloEncontros, lblTotais);
        });

        JPanel painelCentro = new JPanel(new BorderLayout(10,10));
        painelInfo = new JPanel(new GridLayout(4, 1));
        painelInfo.setBorder(new CompoundBorder(new LineBorder(Color.LIGHT_GRAY, 1, true), BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        painelInfo.setBackground(new Color(250, 250, 250));
        painelCentro.add(painelInfo, BorderLayout.NORTH);

        painelBaloes = new JPanel();
        painelBaloes.setLayout(new BoxLayout(painelBaloes, BoxLayout.Y_AXIS));
        scrollBaloes = new JScrollPane(painelBaloes);
        painelCentro.add(scrollBaloes, BorderLayout.CENTER);

        // checklist (visual) - será carregado por carregarChecklist (usa currentPatientSelectedTreatment)
        painelChecklist = new JPanel(new GridLayout(0, 3, 6, 6));
        painelChecklist.setBorder(BorderFactory.createTitledBorder("Sessões (checklist)"));
        painelChecklist.setBackground(Color.WHITE);
        scrollChecklist = new JScrollPane(painelChecklist);
        scrollChecklist.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollChecklist.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollChecklist.getVerticalScrollBar().setUnitIncrement(16);
        scrollChecklist.setPreferredSize(new Dimension(480, 100));
        JPanel painelAcoesChecklist = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        btnAddSessao = new JButton("Adicionar Sessão");
        btnRemoverSessao = new JButton("Remover Selecionadas");
        painelAcoesChecklist.add(btnAddSessao);
        painelAcoesChecklist.add(btnRemoverSessao);
        JPanel checklistWrapper = new JPanel(new BorderLayout());
        checklistWrapper.add(scrollChecklist, BorderLayout.CENTER);
        checklistWrapper.add(painelAcoesChecklist, BorderLayout.SOUTH);
        painelCentro.add(checklistWrapper, BorderLayout.SOUTH);

        // Split
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelLista, painelCentro);
        split.setDividerLocation(260);
        split.setResizeWeight(0);
        painel.add(split, BorderLayout.CENTER);

        // Botões inferiores
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnNovoRelatorio = new JButton("Novo Relatório");
        JButton btnAtualizar = new JButton("Atualizar Lista");
        JButton btnVoltar = new JButton("Voltar ao Cadastro");
        JButton btnExcluirTratPac = new JButton("Remover Tratamento (do paciente)");
        botoes.add(btnNovoRelatorio);
        botoes.add(btnAtualizar);
        botoes.add(btnExcluirTratPac);
        botoes.add(btnVoltar);
        painel.add(botoes, BorderLayout.SOUTH);



        // Listeners
        btnAtualizar.addActionListener(e -> atualizarListaPacientes());
        btnVoltar.addActionListener(e -> cardLayout.show(painelPrincipal, "cadastro"));

        btnNovoRelatorio.addActionListener(e -> {
            String sel = listaPacientes.getSelectedValue();
            if (sel == null) {
                JOptionPane.showMessageDialog(this, "Selecione um paciente antes de adicionar relatório.");
                return;
            }
            abrirDialogNovoRelatorio(sel);
        });

        btnExcluirTratPac.addActionListener(e -> {
            String paciente = listaPacientes.getSelectedValue();
            String tratado = (String) cbTratamentos.getSelectedItem();
            if (paciente == null) {
                JOptionPane.showMessageDialog(this, "Selecione um paciente.");
                return;
            }
            if (tratado == null || tratado.equals("-- Selecione --")) {
                JOptionPane.showMessageDialog(this, "Selecione o tratamento do paciente que deseja remover.");
                return;
            }
            int conf = JOptionPane.showConfirmDialog(this, "Remover associação do tratamento \"" + tratado + "\" deste paciente?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (conf != JOptionPane.YES_OPTION) return;
            removerAssociacaoTratamentoDoPaciente(paciente, tratado);
            // recarrega combo
            popularComboTratamentosParaPaciente(paciente);
            // força reload geral
            carregarInfoPaciente(paciente);
            currentPatientSelectedTreatment = null;
            carregarRelatorios(paciente);
            carregarChecklist(paciente);
            JOptionPane.showMessageDialog(this, "Associação removida.");
        });

        listaPacientes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String sel = listaPacientes.getSelectedValue();
                currentPatientSelectedTreatment = null; // reset
                if (sel != null) {
                    carregarInfoPaciente(sel);
                    // popula combo de tratamentos desse paciente
                    popularComboTratamentosParaPaciente(sel);
                    // carregar relatórios e checklist gerais (por padrão)
                    carregarRelatorios(sel);
                    carregarChecklist(sel);
                } else {
                    painelInfo.removeAll();
                    painelBaloes.removeAll();
                    painelChecklist.removeAll();
                    painelInfo.revalidate();
                    painelBaloes.revalidate();
                    painelChecklist.revalidate();
                    cbTratamentos.setEnabled(false);
                }
            }
        });

        btnAddSessao.addActionListener(e -> {
            String sel = listaPacientes.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(this, "Selecione um paciente para adicionar sessão."); return; }
            String nomeSessao = JOptionPane.showInputDialog(this, "Nome da sessão:");
            if (nomeSessao != null && !nomeSessao.trim().isEmpty()) {
                adicionarSessaoUI(nomeSessao.trim(), false);
                saveChecklist(sel); // save will detect selected treatment and save to the proper file
            }
        });

        btnRemoverSessao.addActionListener(e -> {
            String sel = listaPacientes.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(this, "Selecione um paciente para remover sessões."); return; }
            removerSessoesSelecionadas(sel);
        });


        // Carrega lista ao abrir a tela
        atualizarListaPacientes();
        return painel;
    }

    // --------------------------------------
//          TELA RECEITUÁRIO
// --------------------------------------




    private JPanel criarTelaReceituario() {



        JPanel painel = new JPanel(new BorderLayout(10,10));
        painel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Painel de informações no topo
        JPanel painelInfoTopo = new JPanel(new GridLayout(4, 2, 5, 5));  // agora suporta CPF corretamente
        painelInfoTopo.setBorder(BorderFactory.createTitledBorder("Dados do Paciente"));

        lblNome = new JLabel("Nome: ");
        lblEmail = new JLabel("Email: ");
        lblLocal = new JLabel("Localidade: ");
        lblDataNasc = new JLabel("Data de Nascimento: ");
        lblCpf = new JLabel("CPF: ");


        painelInfoTopo.add(lblNome);
        painelInfoTopo.add(lblEmail);
        painelInfoTopo.add(lblLocal);
        painelInfoTopo.add(lblDataNasc);
        painelInfoTopo.add(lblCpf);

        // Lista de pacientes
        JScrollPane scrLista = new JScrollPane(listaReceitPacientes);
        scrLista.setPreferredSize(new Dimension(240, 250));

        // Área de texto para o receituário
        JTextArea areaReceita = new JTextArea();
        areaReceita.setLineWrap(true);
        areaReceita.setWrapStyleWord(true);
        JScrollPane scrTexto = new JScrollPane(areaReceita);

        // Split para divisão lateral
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrLista, scrTexto);
        split.setDividerLocation(260);

        // Botões inferiores
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSalvar = new JButton("Salvar");
        JButton btnVoltar = new JButton("Voltar");
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnVoltar);

        // Montagem final
        painel.add(painelInfoTopo, BorderLayout.NORTH);
        painel.add(split, BorderLayout.CENTER);
        painel.add(painelBotoes, BorderLayout.SOUTH);

        // ----- Atualiza cabeçalho ao selecionar paciente -----
        listaReceitPacientes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String nomePac = listaReceitPacientes.getSelectedValue();
                if (nomePac != null) {
                    carregarInfoTopoReceituario(nomePac, lblNome, lblEmail, lblLocal, lblDataNasc, areaReceita, lblCpf);
                }
            }
        });

        // ----- Botão SALVAR (.docx Word) -----
        btnSalvar.addActionListener(e -> {
            String nomePac = listaReceitPacientes.getSelectedValue();
            if (nomePac == null) {
                JOptionPane.showMessageDialog(this, "Selecione um paciente.");
                return;
            }
            salvarReceituarioComoRTF(nomePac, areaReceita.getText()); // CHAMA A FUNÇÃO NOVA
        });

        // ----- Botão VOLTAR -----
        btnVoltar.addActionListener(e -> cardLayout.show(painelPrincipal, "pacientes"));

// Carregar lista de pacientes
        atualizarListaPacientesReceituario();  // agora sem argumentos

        return painel;
    }

    private void carregarInfoTopoReceituario(String nomePac,
                                             JLabel lblNome, JLabel lblEmail,
                                             JLabel lblLocal, JLabel lblDataNasc,
                                             JTextArea areaReceita, JLabel lblCpf) {

        File info = new File(BASE_DIR, nomePac.replace(" ", "_") + "/info.txt");
        if (!info.exists()) return;

        try {
            java.util.List<String> linhas = Files.readAllLines(info.toPath(), java.nio.charset.StandardCharsets.UTF_8);

            // Garantir tamanho mínimo
            String nome      = (linhas.size() > 0) ? linhas.get(0) : "Não informado";
            String email     = (linhas.size() > 1) ? linhas.get(1) : "Não informado";
            String local     = (linhas.size() > 2) ? linhas.get(2) : "Não informado";
            String dataNasc  = (linhas.size() > 3) ? linhas.get(3) : "Não informado";
            String cpf       = (linhas.size() > 4) ? linhas.get(4) : "Não informado";

            // ✔ ORGANIZAÇÃO PROFISSIONAL NO LAYOUT
            lblNome.setText("<html><b></b> " + nome + "</html>");
            lblLocal.setText("<html><b></b> " + local + "</html>");
            lblDataNasc.setText("<html><b></b> " + dataNasc + "</html>");
            lblEmail.setText("<html><b></b> " + email + "</html>");
            lblCpf.setText("<html><b></b> " + cpf + "</html>");
            for (String linha : linhas) {
                if (linha.toLowerCase().contains("cpf")) {
                    cpf = linha.replace("CPF:", "").trim();
                    break;
                }
            }
            lblCpf.setText("<html><b>CPF:</b> " + cpf + "</html>");
            for (String linha : linhas) {
                if (linha.toLowerCase().contains("cpf")) {
                    cpf = linha.replace("CPF:", "").trim();
                    break;
                }
            }
            lblCpf.setText("<html><b>CPF:</b> " + cpf + "</html>");

            // ✔ Carregar receituário salvo (editável)
            File r = new File(BASE_DIR, nomePac.replace(" ", "_") + "/receituario.txt");
            if (r.exists()) {
                areaReceita.setText(String.join("\n", Files.readAllLines(r.toPath(), java.nio.charset.StandardCharsets.UTF_8)));
            } else {
                areaReceita.setText("");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao ler dados do paciente:\n" + ex.getMessage());
        }
    }


    // === ATUALIZA A LISTA DE PACIENTES NO RECEITUÁRIO ===
    private void atualizarListaPacientesReceituario() {
        modeloReceitPacientes.clear();

        File pastaPac = new File(BASE_DIR);
        if (pastaPac.exists() && pastaPac.isDirectory()) {
            File[] pacientes = pastaPac.listFiles(File::isDirectory);
            if (pacientes != null) {
                for (File p : pacientes) {

                    String nome = p.getName();

                    // para IGNORAR a agenda
                    if (nome.equalsIgnoreCase("agenda")) continue;

                    modeloReceitPacientes.addElement(p.getName());


                }
            }
        }
    }




    private void salvarReceituarioComoRTF(String paciente, String conteudoReceituario) {
        try {
            File pastaPac = new File(BASE_DIR, paciente.replace(" ", "_"));
            File info = new File(pastaPac, "info.txt");
            if (!info.exists()) {
                JOptionPane.showMessageDialog(this, "Arquivo info.txt do paciente não encontrado!");
                return;
            }

            // Carregar dados
            java.util.List<String> linhas = Files.readAllLines(info.toPath(), StandardCharsets.UTF_8);
            String nome     = linhas.get(0);
            String email    = linhas.get(1);
            String local    = linhas.get(2);
            String dataNasc = linhas.get(3);
            String cpf      = (linhas.size() >= 5) ? linhas.get(4) : "Não informado";

            // Onde salvar
            File file = new File(pastaPac, "receituario.rtf");

            // MELHOR ENCODING: ANSI + UTF-8
            String cabecalho = "{\\rtf1\\ansi\\deff0\n" +
                    "\\qc{\\b\\fs34 RECEITU\\'c1RIO}\\par\n" +
                    "\\par" +
                    "\\ql ---------------------------------------------\\par\n" +
                    "{\\b\\fs24 IDENTIFICA\\'c7\\'c3O DO EMITENTE}\\par\n" +
                    "\\ql Terapeuta\\par\n" +
                    "\\ql CRTH\\par\n" +
                    "\\ql CRT/Fenate\\par\n" +
                    "\\ql CBO\\par\n" +
                    "\\par" +
                    "\\ql ---------------------------------------------\\par\n" +
                    "\\ql " + lblNome.getText().replace("<html><b>", "").replace("</b>", "").replace("</html>", "") + "\\par\n" +
                    "\\ql " + lblLocal.getText().replace("<html><b>", "").replace("</b>", "").replace("</html>", "") + "\\par\n" +
                    "\\ql " + lblDataNasc.getText().replace("<html><b>", "").replace("</b>", "").replace("</html>", "") + "\\par\n" +
                    "\\ql " + lblCpf.getText().replace("<html><b>", "").replace("</b>", "").replace("</html>", "") + "\\par\n" +
                    "\\ql ---------------------------------------------\\par\n";




            // Escapar acentos corretamente
            String textoCorrigido = conteudoReceituario
                    .replace("\\", "\\\\")
                    .replace("\n", "\\par ") // Quebra de linha correta
                    .replace("á", "\\'e1").replace("é", "\\'e9")
                    .replace("í", "\\'ed").replace("ó", "\\'f3")
                    .replace("ú", "\\'fa").replace("ã", "\\'e3")
                    .replace("õ", "\\'f5").replace("ç", "\\'e7")
                    .replace("Â", "\\'c2").replace("Ê", "\\'ca");

            String finalText = cabecalho + textoCorrigido + "}";

            try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
                fw.write(finalText);
            }

            JOptionPane.showMessageDialog(this, "Receituário salvo com sucesso em:\n" + file.getAbsolutePath());
            Desktop.getDesktop().open(file);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar receituário: " + e.getMessage());
        }
    }




    // ---------------- TELA TRATAMENTOS ----------------
    private JPanel criarTelaTratamentos() {
        JPanel painel = new JPanel(new BorderLayout(15, 15));
        painel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel titulo = new JLabel("Tratamentos", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 26));
        painel.add(titulo, BorderLayout.NORTH);

        // Topo com botões Cadastrar e Voltar
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        JButton btnCadastrar = new JButton("Cadastrar Tratamento");
        JButton btnVoltar = new JButton("Voltar ao Início");
        topo.add(btnCadastrar);
        topo.add(btnVoltar);
        painel.add(topo, BorderLayout.SOUTH);

        // Painel central apenas com cards em Grid 3 colunas
        painelCardsTratamentos = new JPanel();
        painelCardsTratamentos.setLayout(new GridLayout(0, 3, 12, 12));
        painelCardsTratamentos.setBackground(new Color(250,250,250));
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(250,250,250));
        wrapper.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        wrapper.add(painelCardsTratamentos, BorderLayout.NORTH);
        JScrollPane scrollCards = new JScrollPane(wrapper, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollCards.getVerticalScrollBar().setUnitIncrement(16);

        painel.add(scrollCards, BorderLayout.CENTER);

        btnVoltar.addActionListener(e -> cardLayout.show(painelPrincipal, "inicio"));
        btnCadastrar.addActionListener(e -> abrirDialogCadastroTratamento(painelCardsTratamentos));

        atualizarListaTratamentos();
        return painel;
    }

    // Atualiza cards e também (indiretamente) a fonte quando paciente abrir combo
    private void atualizarListaTratamentos() {
        File file = new File(TRAT_DIR, "tratamentos.txt");

        if (painelCardsTratamentos != null) {
            painelCardsTratamentos.removeAll();
            if (file.exists()) {
                try {
                    java.util.List<String> linhas = Files.readAllLines(file.toPath());
                    boolean any = false;
                    for (String l : linhas) {
                        if (l.trim().isEmpty()) continue;
                        String[] partes = l.split(";", 3);
                        String nome = partes.length > 0 ? partes[0] : "";
                        String desc = partes.length > 1 ? partes[1] : "";
                        String valorStr = partes.length > 2 ? partes[2] : "0";
                        double valor = 0;
                        try { valor = Double.parseDouble(valorStr); } catch (Exception ex) {}
                        JPanel card = criarCardTratamento(nome, desc, valor);
                        painelCardsTratamentos.add(card);
                        any = true;
                    }
                    if (!any) {
                        JLabel vazio = new JLabel("Nenhum tratamento cadastrado.");
                        vazio.setFont(new Font("SansSerif", Font.ITALIC, 14));
                        painelCardsTratamentos.add(vazio);
                    }
                } catch (IOException e) {
                    painelCardsTratamentos.add(new JLabel("Erro ao carregar tratamentos: " + e.getMessage()));
                }
            } else {
                JLabel vazio = new JLabel("Nenhum tratamento cadastrado.");
                vazio.setFont(new Font("SansSerif", Font.ITALIC, 14));
                painelCardsTratamentos.add(vazio);
            }
            painelCardsTratamentos.revalidate();
            painelCardsTratamentos.repaint();
        }
    }

    private JPanel criarCardTratamento(String nome, String descricao, double valor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(260, 140));
        card.setBackground(new Color(220, 240, 255));
        card.setBorder(new CompoundBorder(new LineBorder(new Color(180, 220, 170), 1, true), BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JLabel lblNome = new JLabel(nome);
        lblNome.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblNome.setForeground(new Color(0, 80, 200));

        JTextArea ta = new JTextArea(descricao);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        ta.setOpaque(false);
        ta.setFont(new Font("SansSerif", Font.PLAIN, 14));
        ta.setBorder(null);

        JLabel lblValor = new JLabel(String.format("Sessão: R$ %.2f", valor));
        lblValor.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblValor.setForeground(new Color(20, 120, 20));

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        JScrollPane taScroll = new JScrollPane(ta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        taScroll.setBorder(null);
        taScroll.setOpaque(false);
        taScroll.getViewport().setOpaque(false);
        centro.add(taScroll, BorderLayout.CENTER);
        centro.add(lblValor, BorderLayout.SOUTH);

        card.add(lblNome, BorderLayout.NORTH);
        card.add(centro, BorderLayout.CENTER);

        card.setToolTipText(nome + " — " + String.format("R$ %.2f", valor));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // popup excluir (global)
        JPopupMenu popup = new JPopupMenu();
        JMenuItem miExcluir = new JMenuItem("Excluir Tratamento (global)");
        popup.add(miExcluir);
        miExcluir.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Excluir tratamento global \"" + nome + "\"?\n(associações a pacientes não serão removidas automaticamente)",
                    "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                excluirTratamentoPorNome(nome);
            }
        });
        card.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { if (e.isPopupTrigger()) popup.show(card, e.getX(), e.getY()); }
            @Override public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) popup.show(card, e.getX(), e.getY()); }
        });

        return card;
    }

    private void abrirDialogCadastroTratamento(JPanel painelCards) {
        JDialog dlg = new JDialog(this, "Cadastrar Tratamento", true);
        dlg.setSize(560, 360);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(8,8));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nomeField = new JTextField();
        JTextArea descField = new JTextArea(4, 30);
        descField.setLineWrap(true);
        descField.setWrapStyleWord(true);
        JTextField valorField = new JTextField();

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Nome do Tratamento:"), gbc);
        gbc.gridx = 1; form.add(nomeField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 1; form.add(new JScrollPane(descField), gbc);
        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Valor por sessão (R$):"), gbc);
        gbc.gridx = 1; form.add(valorField, gbc);

        dlg.add(form, BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        botoes.add(btnSalvar); botoes.add(btnCancelar);
        dlg.add(botoes, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dlg.dispose());

        btnSalvar.addActionListener(e -> {
            String nome = nomeField.getText().trim();
            String desc = descField.getText().trim();
            String valor = valorField.getText().trim();
            if (nome.isEmpty() || desc.isEmpty() || valor.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Preencha todos os campos!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try { Double.parseDouble(valor.replace(",", ".")); } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Valor inválido. Use somente números (ex: 150.00).", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            File file = new File(TRAT_DIR, "tratamentos.txt");
            try (FileWriter fw = new FileWriter(file, true); BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(nome + ";" + desc.replace("\n", " ") + ";" + valor.replace(",", "."));
                bw.newLine();
            } catch (IOException ioex) {
                JOptionPane.showMessageDialog(dlg, "Erro ao salvar: " + ioex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            atualizarListaTratamentos();
            dlg.dispose();
            JOptionPane.showMessageDialog(this, "Tratamento cadastrado com sucesso!");
        });

        dlg.setVisible(true);
    }

    private void excluirTratamentoPorNome(String nomeSel) {
        File file = new File(TRAT_DIR, "tratamentos.txt");
        if (!file.exists()) return;
        try {
            java.util.List<String> linhas = Files.readAllLines(file.toPath());
            java.util.List<String> novas = new ArrayList<>();
            for (String l : linhas) {
                if (!l.startsWith(nomeSel + ";")) { novas.add(l); }
            }
            Files.write(file.toPath(), novas);
            atualizarListaTratamentos();
            JOptionPane.showMessageDialog(this, "Tratamento excluído!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + e.getMessage());
        }
    }

    // ------------- Ações de cadastro paciente -------------
    private void cadastrarPaciente() {
        String nome = tfNome.getText().trim();
        String data = tfDataNasc.getText().trim();
        String local = tfLocal.getText().trim();
        String email = tfEmail.getText().trim();
        String cpf = tfCpf.getText().trim();
        if (nome.isEmpty() || data.isEmpty() || local.isEmpty() || email.isEmpty() || cpf.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SuperPacient paciente;
        try {
            paciente = new SuperPacient(nome, local, email, cpf, data);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Data inválida ou erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        File pasta = new File(BASE_DIR, nome.replace(" ", "_"));
        if (!pasta.exists()) {
            pasta.mkdirs();
        } else {
            JOptionPane.showMessageDialog(this, "Paciente já existe!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File info = new File(pasta, "info.txt");
        try (PrintWriter pw = new PrintWriter(new FileWriter(info))) {
            pw.print(paciente.toString());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar info: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            new File(pasta, "reports.txt").createNewFile();
            new File(pasta, "checklist.txt").createNewFile();
            new File(pasta, "treatments.txt").createNewFile(); // lista de tratamentos associados
        } catch (IOException ignored) {}
        tfNome.setText("");
        tfDataNasc.setText("");
        tfLocal.setText("");
        tfEmail.setText("");
        tfCpf.setText("");
        atualizarListaPacientes();
        JOptionPane.showMessageDialog(this, "Paciente cadastrado com sucesso!");
    }

    // ------------- Listagem pacientes -------------
    private void atualizarListaPacientes() {
        modeloLista.clear();
        File base = new File(BASE_DIR);
        if (!base.exists()) base.mkdirs();

        File[] pastas = base.listFiles(File::isDirectory);
        if (pastas != null) {

            Arrays.sort(pastas,
                    Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

            for (File p : pastas) {

                // 🔥 IGNORA A PASTA DA AGENDA (CORREÇÃO DO BUG)
                if (p.getName().equalsIgnoreCase("agenda")) continue;

                modeloLista.addElement(p.getName().replace("_", " "));
            }
        }
    }


    // ------------- Perfil paciente -------------
    private void carregarInfoPaciente(String nomePaciente) {
        painelInfo.removeAll();
        File info = new File(BASE_DIR, nomePaciente.replace(" ", "_") + "/info.txt");
        if (info.exists()) {
            try {
                java.util.List<String> linhas = Files.readAllLines(info.toPath());
                for (String l : linhas) {
                    JLabel lbl = new JLabel(l);
                    lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    painelInfo.add(lbl);
                }
            } catch (IOException ex) {
                painelInfo.add(new JLabel("Erro ao ler info: " + ex.getMessage()));
            }
        } else {
            painelInfo.add(new JLabel("Sem informações do paciente."));
        }
        painelInfo.revalidate();
        painelInfo.repaint();
    }

    // ---------------- Relatórios (balões) ----------------
    // Agora respeita currentPatientSelectedTreatment: escreve/le os arquivos por tratamento quando setado.
    private void carregarRelatorios(String nomePaciente) {
        painelBaloes.removeAll();
        File reports;
        if (currentPatientSelectedTreatment != null) {
            reports = getReportsFileForPatientTreatment(nomePaciente, currentPatientSelectedTreatment);
        } else {
            reports = new File(BASE_DIR, nomePaciente.replace(" ", "_") + "/reports.txt");
        }
        if (reports.exists()) {
            try {
                java.util.List<String> linhas = Files.readAllLines(reports.toPath());
                for (String linha : linhas) {
                    if (linha.trim().isEmpty()) continue;
                    int sep = linha.indexOf("::");
                    String ts = "";
                    String texto = linha;
                    if (sep > 0) {
                        ts = linha.substring(0, sep);
                        texto = linha.substring(sep + 2);
                        texto = texto.replace("\\n", "\n");

                    }
                    JPanel balao = criarBalao(texto, ts);
                    painelBaloes.add(balao);
                    painelBaloes.add(Box.createVerticalStrut(6));
                }
            } catch (IOException ex) {
                painelBaloes.add(new JLabel("Erro ao ler relatórios: " + ex.getMessage()));
            }
        }
        painelBaloes.revalidate();
        painelBaloes.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar v = scrollBaloes.getVerticalScrollBar();
            v.setValue(v.getMaximum());
        });
    }

    private JPanel criarBalao(String texto, String ts){
        // Painel do balão
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBackground(new Color(220, 248, 198));
        bubble.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 220, 170), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        bubble.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Área de texto
        JTextArea textArea = new JTextArea(texto);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setOpaque(false);

        // Define largura máxima antes de calcular preferred size
        int maxWidth = 450; // largura máxima do balão
        textArea.setSize(new Dimension(maxWidth, Short.MAX_VALUE));

        // Pega tamanho final após line wrap
        Dimension d = textArea.getPreferredSize();
        textArea.setPreferredSize(d);
        textArea.setMaximumSize(d);
        textArea.setMinimumSize(d);

        bubble.add(textArea);

        // Ajusta tamanho do painel do balão para o texto + padding
        bubble.setPreferredSize(new Dimension(d.width + 20, d.height + 20));
        bubble.setMaximumSize(new Dimension(d.width + 20, d.height + 20));
        bubble.setMinimumSize(new Dimension(d.width + 20, d.height + 20));

        // Timestamp
        JLabel tsLabel = new JLabel(ts);
        tsLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        tsLabel.setForeground(Color.GRAY);
        tsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Container final empilhado
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setOpaque(false);
        wrap.add(tsLabel);
        wrap.add(Box.createVerticalStrut(2));
        wrap.add(bubble);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        return wrap;
    }




    private void abrirDialogNovoRelatorio(String nomePaciente) {
        JDialog dialog = new JDialog(this, "Novo Relatório - " + nomePaciente, true);
        dialog.setSize(600, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));
        JTextArea ta = new JTextArea();
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        dialog.add(new JScrollPane(ta), BorderLayout.CENTER);
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bSalvar = new JButton("Salvar");
        JButton bCancelar = new JButton("Cancelar");
        botoes.add(bSalvar);
        botoes.add(bCancelar);
        dialog.add(botoes, BorderLayout.SOUTH);
        bSalvar.addActionListener(e -> {
            String texto = ta.getText().trim();
            if (texto.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Digite o conteúdo do relatório!");
                return;
            }
            salvarRelatorio(nomePaciente, texto);
            dialog.dispose();
        });
        bCancelar.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void salvarRelatorio(String paciente, String texto) {
        String ts = LocalDateTime.now().format(TS_FORMAT);

        String textoCompactado = texto.replace("\n", "\\n");
        String linha = ts + "::" + textoCompactado;

        File reports;
        if (currentPatientSelectedTreatment != null) {
            reports = getReportsFileForPatientTreatment(paciente, currentPatientSelectedTreatment);
        } else {
            reports = new File(BASE_DIR, paciente.replace(" ", "_") + "/reports.txt");
        }
        try (FileWriter fw = new FileWriter(reports, true); BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(linha);
            bw.newLine();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar relatório: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        carregarRelatorios(paciente);
    }

    // ---------------- CHECKLIST por paciente/tratamento ----------------
    private void carregarChecklist(String nomePaciente) {
        painelChecklist.removeAll();
        checklistBoxes.clear();
        painelChecklist.setLayout(new GridLayout(0, 3, 6, 6));

        File file;
        if (currentPatientSelectedTreatment != null) {
            file = getChecklistFileForPatientTreatment(nomePaciente, currentPatientSelectedTreatment);
        } else {
            file = new File(BASE_DIR, nomePaciente.replace(" ", "_") + "/checklist.txt");
        }

        if (file.exists()) {
            try {
                java.util.List<String> linhas = Files.readAllLines(file.toPath());
                for (String l : linhas) {
                    if (l.trim().isEmpty()) continue;
                    int sep = l.indexOf('|');
                    boolean checked = false;
                    String label = l;
                    if (sep >= 0) {
                        checked = Boolean.parseBoolean(l.substring(0, sep));
                        label = l.substring(sep + 1);
                    }
                    JCheckBox cb = new JCheckBox(label, checked);
                    cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
                    cb.addItemListener(ev -> saveChecklist(nomePaciente)); // salva ao marcar/desmarcar
                    checklistBoxes.add(cb);
                    painelChecklist.add(cb);
                }
            } catch (IOException ex) {
                painelChecklist.add(new JLabel("Erro ao ler checklist: " + ex.getMessage()));
            }
        }
        if (checklistBoxes.isEmpty()) {
            JLabel hint = new JLabel("Sem sessões. Use 'Adicionar Sessão' para criar.");
            hint.setFont(new Font("SansSerif", Font.ITALIC, 12));
            hint.setForeground(Color.DARK_GRAY);
            painelChecklist.add(hint);
        }
        painelChecklist.revalidate();
        painelChecklist.repaint();
        SwingUtilities.invokeLater(() -> {
            if (scrollChecklist != null) {
                scrollChecklist.setPreferredSize(new Dimension(scrollChecklist.getPreferredSize().width, 100));
                scrollChecklist.revalidate();
                scrollChecklist.repaint();
            }
        });
    }

    private void adicionarSessaoUI(String nomeSessao, boolean marcado) {
        JCheckBox cb = new JCheckBox(nomeSessao, marcado);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        String current = listaPacientes.getSelectedValue();
        cb.addItemListener(e -> { if (current != null) saveChecklist(current); });
        checklistBoxes.add(cb);
        painelChecklist.add(cb);
        painelChecklist.revalidate();
        painelChecklist.repaint();
        SwingUtilities.invokeLater(() -> {
            if (scrollChecklist != null) {
                scrollChecklist.setPreferredSize(new Dimension(scrollChecklist.getPreferredSize().width, 100));
                scrollChecklist.revalidate();
                scrollChecklist.repaint();
            }
        });
    }

    private void removerSessoesSelecionadas(String nomePaciente) {
        java.util.List<JCheckBox> removidos = new ArrayList<>();
        for (JCheckBox cb : new ArrayList<>(checklistBoxes)) {
            if (cb.isSelected()) {
                removidos.add(cb);
                painelChecklist.remove(cb);
                checklistBoxes.remove(cb);
            }
        }
        if (removidos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Marque as sessões que deseja remover e clique em Remover Selecionadas.");
            return;
        }
        painelChecklist.revalidate();
        painelChecklist.repaint();
        saveChecklist(nomePaciente);
        SwingUtilities.invokeLater(() -> {
            if (scrollChecklist != null) {
                scrollChecklist.setPreferredSize(new Dimension(scrollChecklist.getPreferredSize().width, 100));
                scrollChecklist.revalidate();
                scrollChecklist.repaint();
            }
        });
    }

    // salva checklist - respeita tratamento selecionado via currentPatientSelectedTreatment
    private void saveChecklist(String nomePaciente) {
        File file;
        if (currentPatientSelectedTreatment != null) {
            file = getChecklistFileForPatientTreatment(nomePaciente, currentPatientSelectedTreatment);
        } else {
            file = new File(BASE_DIR, nomePaciente.replace(" ", "_") + "/checklist.txt");
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (JCheckBox cb : checklistBoxes) {
                pw.println(cb.isSelected() + "|" + cb.getText());
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar checklist: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ----------------------- ASSOCIAR TRATAMENTO A PACIENTE -----------------------
    // abre diálogo que lista tratamentos globais e permite associar 1 ou mais ao paciente
    // ----------------------- ASSOCIAR TRATAMENTO A PACIENTE (com balões corrigido) -----------------------
    private void abrirDialogAssociarTratamento(String nomePaciente) {
        java.util.List<Tratamento> tratamentos = lerTratamentosGlobais();
        if (tratamentos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Não há tratamentos cadastrados na tela de Tratamentos.");
            return;
        }

        JDialog dlg = new JDialog(this, "Selecionar Tratamento para: " + nomePaciente, true);
        dlg.setSize(600, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel painelCards = new JPanel(new GridLayout(0, 2, 10, 10)); // 2 colunas
        JScrollPane scroll = new JScrollPane(painelCards);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Loop de criação dos cartões de tratamento
        for (Tratamento t : tratamentos) {
            final Tratamento tt = t;
            final String pacienteNome = nomePaciente;

            JPanel card = new JPanel(new BorderLayout(5, 5));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(150, 150, 150), 1, true),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            card.setBackground(Color.WHITE);

            JLabel lblNome = new JLabel("<html><b>" + escapeHtml(tt.getNome()) + "</b></html>");
            JLabel lblDesc = new JLabel("<html><i>" + escapeHtml(tt.getDescricao()) + "</i></html>");
            JLabel lblValor = new JLabel(String.format("Valor por sessão: R$ %.2f", tt.getValor()));

            JPanel info = new JPanel(new GridLayout(3, 1));
            info.setBackground(Color.WHITE);
            info.add(lblNome);
            info.add(lblDesc);
            info.add(lblValor);

            card.add(info, BorderLayout.CENTER);

            // Interação do clique no cartão
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    File fileTratPac = new File(BASE_DIR, pacienteNome.replace(" ", "_") + "/treatments.txt");
                    boolean jaAssociado = false;

                    // Verifica se o tratamento já está vinculado
                    if (fileTratPac.exists()) {
                        try {
                            java.util.List<String> linhas = Files.readAllLines(fileTratPac.toPath());
                            for (String l : linhas) {
                                if (l.trim().equalsIgnoreCase(tt.getNome().trim())) {
                                    jaAssociado = true;
                                    break;
                                }
                            }
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(dlg, "Erro ao verificar tratamentos: " + ex.getMessage());
                            return;
                        }
                    }

                    if (jaAssociado) {
                        JOptionPane.showMessageDialog(
                                dlg,
                                "⚠️ Esse tratamento já está vinculado ao paciente.",
                                "Atenção",
                                JOptionPane.WARNING_MESSAGE
                        );
                        // não fecha o diálogo — permite escolher outro
                        return;
                    }

                    // Confirmação de associação
                    int op = JOptionPane.showConfirmDialog(
                            dlg,
                            "Associar o tratamento \"" + tt.getNome() + "\" a " + pacienteNome + "?",
                            "Confirmar associação",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (op == JOptionPane.YES_OPTION) {
                        associarTratamentoAoPaciente(pacienteNome, tt.getNome());
                        dlg.dispose();
                    }
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    card.setBackground(new Color(220, 240, 255));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    card.setBackground(Color.WHITE);
                }
            });

            painelCards.add(card);
        }

        dlg.add(scroll, BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dlg.dispose());
        botoes.add(btnFechar);
        dlg.add(botoes, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }


    // Lê os tratamentos completos (nome, descrição, valor)
    private java.util.List<Tratamento> lerTratamentosGlobais() {
        java.util.List<Tratamento> lista = new ArrayList<>();
        File file = new File(TRAT_DIR, "tratamentos.txt");
        if (!file.exists()) return lista;
        try {
            java.util.List<String> linhas = Files.readAllLines(file.toPath());
            for (String l : linhas) {
                if (l.trim().isEmpty()) continue;
                String[] partes = l.split(";", 3);
                if (partes.length >= 3) {
                    String nome = partes[0];
                    String descricao = partes[1];
                    double valor = Double.parseDouble(partes[2]);
                    lista.add(new Tratamento(nome, descricao, valor));
                }
            }
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Erro ao ler tratamentos: " + e.getMessage());
        }
        return lista;
    }

    private double obterValorDoTratamento(String nome){
        java.util.List<Tratamento> lista = lerTratamentosGlobais();
        for (Tratamento t : lista){
            if(t.getNome().equals(nome)){
                return t.getValor();
            }
        }
        return 0.0;
    }


    // Salva a associação do tratamento ao paciente
    private void associarTratamentoAoPaciente(String nomePaciente, String nomeTratamento) {
        File fileTratPac = new File(BASE_DIR, nomePaciente.replace(" ", "_") + "/treatments.txt");
        try {
            java.util.List<String> linhas = new ArrayList<>();
            if (fileTratPac.exists()) linhas = Files.readAllLines(fileTratPac.toPath());
            if (!linhas.contains(nomeTratamento)) {
                try (FileWriter fw = new FileWriter(fileTratPac, true);
                     BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write(nomeTratamento);
                    bw.newLine();
                }
                File reports = getReportsFileForPatientTreatment(nomePaciente, nomeTratamento);
                File checklist = getChecklistFileForPatientTreatment(nomePaciente, nomeTratamento);
                reports.createNewFile();
                checklist.createNewFile();
                JOptionPane.showMessageDialog(this, "Tratamento associado com sucesso!");
            } else {
                JOptionPane.showMessageDialog(this, "Este tratamento já está associado a este paciente.");
            }
            popularComboTratamentosParaPaciente(nomePaciente);
            cbTratamentos.setSelectedItem(nomeTratamento);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao associar tratamento: " + ex.getMessage());
        }
    }


    // popula a combo com tratamentos associados ao paciente + opções
    private void popularComboTratamentosParaPaciente(String nomePaciente) {
        cbTratamentos.removeAllItems();
        cbTratamentos.addItem("-- Selecione --");
        File fileTratPac = new File(BASE_DIR, nomePaciente.replace(" ", "_") + "/treatments.txt");
        if (fileTratPac.exists()) {
            try {
                java.util.List<String> linhas = Files.readAllLines(fileTratPac.toPath());
                for (String l : linhas) {
                    if (l.trim().isEmpty()) continue;
                    cbTratamentos.addItem(l.trim());
                }
            } catch (IOException ex) {
                // ignora e continua
            }
        }
        cbTratamentos.addItem(NOVO_TRATAMENTO_LABEL);
        cbTratamentos.setSelectedIndex(0);
        cbTratamentos.setEnabled(true);
        currentPatientSelectedTreatment = null;
    }

    // ---------------------- PAINEL DE ENCONTROS DO TRATAMENTO ----------------------
    private JPanel painelEncontros = new JPanel();
    private DefaultListModel<String> modeloEncontros = new DefaultListModel<>();
    private JList<String> listaEncontros = new JList<>(modeloEncontros);
    private JButton btnNovoEncontro = new JButton("Adicionar Encontro");
    private JLabel lblTotais = new JLabel("");




    // inicialização visual do painel (CHAMADA UMA ÚNICA VEZ NO CONSTRUTOR)
    private void inicializarPainelEncontros(){
        painelEncontros.setLayout(new BorderLayout(5,5));
        painelEncontros.setBorder(BorderFactory.createTitledBorder("Encontros do Tratamento"));

        JScrollPane scr = new JScrollPane(listaEncontros);
        painelEncontros.add(scr, BorderLayout.CENTER);


        // adiciona listener de duplo-clique na lista de encontros
        listaEncontros.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    String paciente = listaPacientes.getSelectedValue();
                    String tratamento = currentPatientSelectedTreatment;
                    String exibicao = listaEncontros.getSelectedValue(); // ex: "Encontro 1 (Não pago)"
                    if (paciente == null || tratamento == null || exibicao == null) {
                        JOptionPane.showMessageDialog(PacientsInterface.this,
                                "Selecione primeiro: paciente -> tratamento -> encontro.");
                        return;
                    }
                    // extrai o nome puro (remove " (Pago)" ou " (Não pago)" se presentes)
                    String nomeEncontro = exibicao.replaceAll("\\s*\\(.*\\)$", "").trim();

                    // abre diálogo para marcar pago / não pago
                    abrirDialogPagamentoEncontro(nomeEncontro);

                    // recarrega lista e totais depois da edição
                    carregarEncontrosDoTratamento(paciente, tratamento);
                }
            }
        });


        painelDetalhesPaciente.add(painelEncontros, BorderLayout.SOUTH);
    }




    // ---------------------- CRIAR NOVO ENCONTRO ----------------------
    private void criarNovoEncontro(){
        if(ultimoPacienteSelecionado == null || currentPatientSelectedTreatment == null){
            JOptionPane.showMessageDialog(this, "Selecione um tratamento primeiro.");
            return;
        }

        String paciente = ultimoPacienteSelecionado;
        String tratamento = currentPatientSelectedTreatment;

        int numero = modeloEncontros.size() + 1;

        String nomeEncontro = "Encontro " + numero;

        // cada encontro será salvo no mesmo arquivo checklist do tratamento
        File arquivo = getChecklistFileForPatientTreatment(paciente, tratamento);

        try(FileWriter fw = new FileWriter(arquivo, true);
            BufferedWriter bw = new BufferedWriter(fw)){
            bw.write(nomeEncontro + ";NAO_PAGO");
            bw.newLine();
        } catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Erro ao salvar encontro: " + ex.getMessage());
            return;
        }

        carregarEncontrosDoTratamento(paciente, tratamento);
    }

    // ---------------------- CARREGAR ENCONTROS ----------------------
    // Substitua o método existente por este
    private void carregarEncontrosDoTratamento(String paciente, String tratamento){
        modeloEncontros.clear();

        File arquivo = getEncontrosFileForPatientTreatment(paciente, tratamento);
        if(!arquivo.exists()){
            atualizarTotaisEncontros(paciente, tratamento);
            return;
        }

        double valor = obterValorDoTratamento(tratamento);
        double pago = 0;
        double naoPago = 0;

        try{
            java.util.List<String> linhas = Files.readAllLines(arquivo.toPath());
            for(String l : linhas){
                if(l.trim().isEmpty()) continue;

                // agora aceita 3 campos: nome|true|data
                String[] p = l.split("\\|");
                String nome = p[0].trim();
                String flag = (p.length >= 2 ? p[1].trim().toLowerCase() : "false");

                boolean isPago = flag.equals("true");

                String exibicao = nome + (isPago ? " (Pago)" : " (Não pago)");
                modeloEncontros.addElement(exibicao);

                if(isPago) pago += valor;
                else naoPago += valor;
            }
        } catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Erro ao carregar encontros: " + ex.getMessage());
        }

        atualizarTotaisEncontros(paciente, tratamento);
        lblTotais.revalidate();
        lblTotais.repaint();
    }




    // ---------------------- DIALOGO PAGO / NÃO PAGO ----------------------
    private void abrirDialogPagamentoEncontro(String nomeEncontroExibido){

        String nomeEncontro = nomeEncontroExibido.replaceAll("\\s*\\(.*\\)$", "").trim();

        String paciente = ultimoPacienteSelecionado;
        String tratamento = currentPatientSelectedTreatment;

        if (paciente == null || tratamento == null) {
            JOptionPane.showMessageDialog(this, "Selecione primeiro: paciente -> tratamento -> encontro.");
            return;
        }

        File arquivo = getEncontrosFileForPatientTreatment(paciente, tratamento);
        if (!arquivo.exists()) {
            JOptionPane.showMessageDialog(this, "Arquivo de encontros não encontrado para este paciente/tratamento.");
            return;
        }

        // Lê status atual do encontro (se houver) para pré-selecionar
        String statusAtual = "false";
        try {
            java.util.List<String> linhas = Files.readAllLines(arquivo.toPath());
            for (String l : linhas) {
                if (l.startsWith(nomeEncontro + "|")) {
                    String[] parts = l.split("\\|", 2);
                    if (parts.length >= 2) statusAtual = parts[1].trim();
                    break;
                }
            }
        } catch (IOException ex) {
            // segue com padrão false
        }

        // opções radio (mutuamente exclusivas)
        JRadioButton rbPago = new JRadioButton("Pago");
        JRadioButton rbNaoPago = new JRadioButton("Não pago");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbPago);
        bg.add(rbNaoPago);

        if ("true".equalsIgnoreCase(statusAtual) || "pago".equalsIgnoreCase(statusAtual)) rbPago.setSelected(true);
        else rbNaoPago.setSelected(true);

        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.add(new JLabel("Status do encontro: " + nomeEncontro));
        painel.add(Box.createVerticalStrut(8));
        painel.add(rbPago);
        painel.add(rbNaoPago);

        int opc = JOptionPane.showConfirmDialog(this, painel, "Marcar pagamento", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opc != JOptionPane.OK_OPTION) return;

        String novoFlag = rbPago.isSelected() ? "true" : "false";

        // atualiza o arquivo: substitui a linha correspondente ou adiciona caso não exista
        try {
            java.util.List<String> linhas = Files.readAllLines(arquivo.toPath());
            java.util.List<String> novas = new ArrayList<>();
            boolean encontrou = false;
            for (String l : linhas) {
                if (l.startsWith(nomeEncontro + "|")) {
                    novas.add(nomeEncontro + "|" + novoFlag);
                    encontrou = true;
                } else {
                    novas.add(l);
                }
            }
            if (!encontrou) {
                novas.add(nomeEncontro + "|" + novoFlag);
            }
            Files.write(arquivo.toPath(), novas);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar status: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // recarrega UI e totais
        carregarEncontrosDoTratamento(paciente, tratamento);
    }






    private void removerAssociacaoTratamentoDoPaciente(String paciente, String tratado) {
        File fileTratPac = new File(BASE_DIR, paciente.replace(" ", "_") + "/treatments.txt");
        if (!fileTratPac.exists()) return;
        try {
            java.util.List<String> linhas = Files.readAllLines(fileTratPac.toPath());
            java.util.List<String> novas = new ArrayList<>();
            for (String l : linhas) {
                if (!l.trim().equals(tratado)) novas.add(l);
            }
            Files.write(fileTratPac.toPath(), novas);
            // opcional: deletar arquivos específicos do paciente+tratamento
            File r = getReportsFileForPatientTreatment(paciente, tratado);
            File c = getChecklistFileForPatientTreatment(paciente, tratado);
            if (r.exists()) r.delete();
            if (c.exists()) c.delete();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao remover associação: " + ex.getMessage());
        }
    }

    // -------------------- util: leitura tratamentos globais --------------------
    // retorna apenas nomes (ordem do arquivo)
    private java.util.List<String> lerTratamentosGlobaisNomes() {
        java.util.List<String> nomes = new ArrayList<>();
        File file = new File(TRAT_DIR, "tratamentos.txt");
        if (!file.exists()) return nomes;
        try {
            java.util.List<String> linhas = Files.readAllLines(file.toPath());
            for (String l : linhas) {
                if (l.trim().isEmpty()) continue;
                String[] partes = l.split(";", 3);
                if (partes.length >= 1) nomes.add(partes[0]);
            }
        } catch (IOException e) {
            // ignora
        }
        return nomes;
    }

    // -------------------- util: arquivos por paciente+tratamento --------------------
    private File getReportsFileForPatientTreatment(String paciente, String tratamento) {
        String safe = tratamento.replaceAll("\\s+", "_");
        return new File(BASE_DIR, paciente.replace(" ", "_") + "/treatment_" + safe + "_reports.txt");
    }
    private File getChecklistFileForPatientTreatment(String paciente, String tratamento) {
        String safe = tratamento.replaceAll("\\s+", "_");
        return new File(BASE_DIR, paciente.replace(" ", "_") + "/treatment_" + safe + "_checklist.txt");
    }

    // obtenha arquivo de encontros para paciente+tratamento
    // -------------------- util: arquivo de ENCONTROS por paciente+tratamento --------------------
    /**
     * Retorna o arquivo onde são gravados os encontros (encontros = sessões reservadas para o tratamento)
     * Ex.: Pacientes/Rafael_X/treatment_Harmonização_foco_encontros.txt
     */
    private File getEncontrosFileForPatientTreatment(String paciente, String tratamento) {
        // mantém convenção: espaços -> underscore, usa mesmo base dir dos outros
        String safeTrat = tratamento.replaceAll("\\s+", "_");
        String pastaPaciente = paciente.replace(" ", "_");
        File pasta = new File(BASE_DIR, pastaPaciente);
        // garante que a pasta do paciente exista (não cria dados extras além da pasta)
        if (!pasta.exists()) pasta.mkdirs();
        String nomeArquivo = "treatment_" + safeTrat + "_encontros.txt";
        return new File(pasta, nomeArquivo);
    }


    // carrega encontros do arquivo e popula a UI (modelo e label de totais)
    private void carregarEncontrosDoTratamentoParaUI(String paciente, String tratamento,
                                                     DefaultListModel<String> modeloEncontros, JLabel lblTotais){
        modeloEncontros.clear();
        File f = getEncontrosFileForPatientTreatment(paciente, tratamento);

        double valorTrat = 0;
        try {
            java.util.List<String> glob = Files.readAllLines(new File(TRAT_DIR, "tratamentos.txt").toPath());
            for (String l : glob) {
                if (l.trim().isEmpty()) continue;
                String[] p = l.split(";", 3);
                if (p.length >= 3 && p[0].equals(tratamento)) {
                    try { valorTrat = Double.parseDouble(p[2]); } catch (Exception ex) { valorTrat = 0; }
                    break;
                }
            }
        } catch (Exception ignored) {}

        double totalPago = 0, totalNaoPago = 0;

        if (f.exists()) {
            try {
                java.util.List<String> linhas = Files.readAllLines(f.toPath());
                for (String ln : linhas) {
                    if (ln.trim().isEmpty()) continue;

                    // agora aceita nome|true|data
                    String[] partes = ln.split("\\|");

                    String nome = partes[0];
                    boolean pago = (partes.length >= 2) && partes[1].trim().equalsIgnoreCase("true");

                    modeloEncontros.addElement(nome + (pago ? " (Pago)" : " (Não pago)"));

                    if (pago) totalPago += valorTrat;
                    else totalNaoPago += valorTrat;
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao ler encontros: " + ex.getMessage());
            }
        }

        atualizarTotaisEncontros(paciente, tratamento);
    }


    private void editarStatusDoEncontro(String nomeEncontro) {
        if (currentPatientSelectedTreatment == null || ultimoPacienteSelecionado == null) {
            return;
        }

        String paciente = ultimoPacienteSelecionado;
        String tratamento = currentPatientSelectedTreatment;

        File fileChecklist = getChecklistFileForPatientTreatment(paciente, tratamento);

        try {
            java.util.List<String> linhas = Files.readAllLines(fileChecklist.toPath());
            boolean encontrado = false;

            for (int i = 0; i < linhas.size(); i++) {
                String linha = linhas.get(i).trim();
                if (linha.startsWith(nomeEncontro)) {

                    String[] partes = linha.split(";");
                    String pagoStatus = partes.length > 1 ? partes[1] : "NAO";

                    String[] opcoes = {"Pago", "Não Pago"};
                    int escolha = JOptionPane.showOptionDialog(
                            this,
                            "Defina o status do encontro:",
                            nomeEncontro,
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            opcoes,
                            pagoStatus.equals("SIM") ? opcoes[0] : opcoes[1]
                    );

                    if (escolha == 0) {
                        linhas.set(i, nomeEncontro + ";SIM");
                    } else if (escolha == 1) {
                        linhas.set(i, nomeEncontro + ";NAO");
                    }

                    encontrado = true;
                    break;
                }
            }

            if (encontrado) {
                Files.write(fileChecklist.toPath(), linhas);
                carregarEncontrosDoTratamento(paciente, tratamento);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // abre diálogo Pago / Não Pago e salva a escolha
    private void editarStatusDoEncontro(String paciente, String tratamento, String encontro) {
        String[] opcoes = {"Pago", "Não Pago", "Cancelar"};
        int escolha = JOptionPane.showOptionDialog(
                this,
                "Marcar \"" + encontro + "\" como:",
                "Status do Encontro",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoes,
                opcoes[0]
        );

        if (escolha == 0) { // Pago
            salvarStatusEncontro(paciente, tratamento, encontro, true);
        } else if (escolha == 1) { // Não Pago
            salvarStatusEncontro(paciente, tratamento, encontro, false);
        } else {
            // cancelar -> não faz nada
        }
    }

    // grava (ou atualiza) o status do encontro no arquivo do paciente+tratamento
    private void salvarStatusEncontro(String paciente, String tratamento, String encontro, boolean pago) {
        try {
            File arquivo = getEncontrosFileForPatientTreatment(paciente, tratamento);

            java.util.List<String> linhas = Files.readAllLines(arquivo.toPath());
            java.util.List<String> novas = new ArrayList<>();

            for (String l : linhas) {
                if (l.trim().isEmpty()) continue;

                String[] p = l.split("\\|");
                String nome = p[0].trim();

                if (nome.equals(encontro)) {

                    if (pago) {
                        String dataAgora = LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                        novas.add(nome + "|true|" + dataAgora);

                    } else {
                        novas.add(nome + "|false|");
                    }

                } else {
                    // mantém como está
                    novas.add(l);
                }
            }

            Files.write(arquivo.toPath(), novas);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // calcula totais (Pago / Não pago) e atualiza lblTotais (rodapé)
    private void atualizarTotaisEncontros(String paciente, String tratamento){
        File file = getEncontrosFileForPatientTreatment(paciente, tratamento);
        double valorTrat = obterValorDoTratamentoPorNome(tratamento);
        double totalPago = 0.0;
        double totalNaoPago = 0.0;

        if (!file.exists()) {
            if (lblTotais != null) {
                lblTotais.setText(String.format(
                        "<html>"
                                + "<span style='color:green; font-size:9px;'>Pago: R$ %.2f</span><br>"
                                + "<span style='color:red; font-size:9px;'>Não pago: R$ %.2f</span>"
                                + "</html>",
                        0.0, 0.0
                ));
            }
            return;
        }

        try {
            java.util.List<String> linhas = Files.readAllLines(file.toPath());
            for (String l : linhas) {
                if (l.trim().isEmpty()) continue;

                // NOW: accepts 2 OR 3 fields (compatível com dados antigos e novos)
                String[] p = l.split("\\|");

                // campo p[1] sempre é "true" ou "false"
                boolean pago = (p.length >= 2 && p[1].trim().equalsIgnoreCase("true"));

                if (pago) totalPago += valorTrat;
                else totalNaoPago += valorTrat;
            }

            if (lblTotais != null) {
                lblTotais.setText(String.format(
                        "<html>"
                                + "<span style='color:green; font-size:9px;'>Pago: R$ %.2f</span><br>"
                                + "<span style='color:red; font-size:9px;'>Não pago: R$ %.2f</span>"
                                + "</html>",
                        totalPago, totalNaoPago
                ));
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao calcular totais: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    // util: caminho do arquivo de status de encontros do paciente+tratamento

    // util: tenta ler o valor do tratamento pelo nome (do tratamentos.txt). retorna 0.0 se não achar.
    private double obterValorDoTratamentoPorNome(String nomeTratamento) {
        File file = new File(TRAT_DIR, "tratamentos.txt");
        if (!file.exists()) return 0.0;
        try {
            java.util.List<String> linhas = Files.readAllLines(file.toPath());
            for (String l : linhas) {
                if (l.trim().isEmpty()) continue;
                String[] partes = l.split(";", 3);
                if (partes.length >= 3 && partes[0].trim().equals(nomeTratamento)) {
                    try {
                        return Double.parseDouble(partes[2]);
                    } catch (NumberFormatException ex) { return 0.0; }
                }
            }
        } catch (IOException ignored) {}
        return 0.0;
    }



    private void carregarTituloDoPrograma() {
        File config = new File(BASE_DIR, "config.txt");
        if (!config.exists()) return;

        try {
            java.util.List<String> linhas = Files.readAllLines(config.toPath());
            if (!linhas.isEmpty()) {
                tituloTelaInicial = linhas.get(0);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar título: " + e.getMessage());
        }
    }


    private void salvarTituloPrograma(String novoTitulo) {
        try {
            File config = new File(BASE_DIR, "config.txt");
            try (FileWriter fw = new FileWriter(config)) {
                fw.write(novoTitulo);
            }
            JOptionPane.showMessageDialog(this, "Título salvo com sucesso!");
            setTitle(novoTitulo); // Atualiza o título imediatamente
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + e.getMessage());
        }
    }

    private JPanel criarTelaConfiguracoes() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS)); // layout vertical
        painel.setBorder(BorderFactory.createEmptyBorder(100, 300, 100, 300)); // CENTRALIZAÇÃO

        // Label
        JLabel lblNovoTitulo = new JLabel("Novo título da tela inicial:");
        lblNovoTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblNovoTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Campo de texto (tamanho controlado)
        JTextField txtTitulo = new JTextField();
        txtTitulo.setMaximumSize(new Dimension(300, 35)); // TAMANHO PROFISSIONAL
        txtTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtTitulo.setFont(new Font("SansSerif", Font.PLAIN, 20));  // AUMENTA O TAMANHO DA FONTE

        // Botão SALVAR
        JButton btnSalvarTitulo = new JButton("Salvar");
        btnSalvarTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSalvarTitulo.addActionListener(e -> {
            String novoTitulo = txtTitulo.getText().trim();
            if (novoTitulo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Digite um título válido!");
                return;
            }

            salvarTituloPrograma(novoTitulo);   // SALVA arquivo
            setTitle(novoTitulo);                // ALTERA barra da janela
            lblTituloTelaInicial.setText(novoTitulo); // ALTERA título da TELA (agora FUNCIONA)

            JOptionPane.showMessageDialog(this, "Título atualizado com sucesso!");
        });


        // Botão VOLTAR
        JButton btnVoltar = new JButton("Voltar");
        btnVoltar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVoltar.addActionListener(e -> cardLayout.show(painelPrincipal, "inicio"));

        // --- Montagem central ---
        painel.add(lblNovoTitulo);
        painel.add(Box.createVerticalStrut(15)); // espaço
        painel.add(txtTitulo);
        painel.add(Box.createVerticalStrut(20));
        painel.add(btnSalvarTitulo);
        painel.add(Box.createVerticalStrut(10));
        painel.add(btnVoltar);

        return painel;
    }

    private void atualizarTelaInicialComNovoTitulo() {
        // Remove tela antiga e cria uma nova com o novo título
        painelPrincipal.remove(criarTelaInicial());
        painelPrincipal.add(criarTelaInicial(), "inicio");
        cardLayout.show(painelPrincipal, "inicio");
    }

// ================== AGENDA ======================

    private JPanel painelAgenda;
    private JTextArea areaAnotacoes;
    private JLabel lblMesAno;
    private LocalDate dataSelecionada = LocalDate.now();

    private JPanel criarTelaAgenda() {

        painelAgenda = new JPanel(new BorderLayout(10,10));
        painelAgenda.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel topo = new JPanel(new BorderLayout());

        JButton btnAnterior = new JButton("◀");
        JButton btnProximo = new JButton("▶");

        lblMesAno = new JLabel("", SwingConstants.CENTER);
        lblMesAno.setFont(new Font("SansSerif", Font.BOLD, 26));

        topo.add(btnAnterior, BorderLayout.WEST);
        topo.add(lblMesAno, BorderLayout.CENTER);
        topo.add(btnProximo, BorderLayout.EAST);

        painelAgenda.add(topo, BorderLayout.NORTH);

        // ---- GRADE DO CALENDÁRIO ----
        JPanel grade = new JPanel(new GridLayout(7, 7, 5, 5));
        grade.setBorder(BorderFactory.createTitledBorder("Calendário"));

        String[] dias = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
        for (String d : dias) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(new Font("SansSerif", Font.BOLD, 14));
            grade.add(l);
        }

        // Botões dos dias
        for (int i = 0; i < 42; i++) {
            JButton dia = new JButton("");
            dia.setFocusPainted(false);

            dia.addActionListener(e -> {
                if (!dia.getText().isEmpty()) {
                    int diaNum = Integer.parseInt(dia.getText());
                    dataSelecionada = LocalDate.of(mesAtual.getYear(), mesAtual.getMonth(), diaNum);
                    carregarAnotacoesDia(dataSelecionada);
                }
            });

            grade.add(dia);
        }

        painelAgenda.add(grade, BorderLayout.CENTER);

        // ---- ANOTAÇÕES ----
        areaAnotacoes = new JTextArea();
        areaAnotacoes.setLineWrap(true);
        areaAnotacoes.setWrapStyleWord(true);

        JScrollPane scr = new JScrollPane(areaAnotacoes);
        scr.setPreferredSize(new Dimension(280, 400));

        JButton btnSalvar = new JButton("Salvar Anotações");
        btnSalvar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSalvar.addActionListener(e -> salvarAnotacoesDia());

        btnSalvar.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnSalvar.getPreferredSize().height));

        // ---- BOTÃO VOLTAR ----
        JButton btnVoltarAgenda = new JButton("Voltar");
        btnVoltarAgenda.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnVoltarAgenda.addActionListener(e ->
                cardLayout.show(painelPrincipal, "inicio")
        );


        JPanel direita = new JPanel(new BorderLayout());
        direita.add(scr, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel();
        painelBotoes.setLayout(new BoxLayout(painelBotoes, BoxLayout.Y_AXIS));
        painelBotoes.add(btnSalvar);
        painelBotoes.add(Box.createVerticalStrut(6));  // espaçamento
        painelBotoes.add(btnVoltarAgenda);

// agora sim adiciona o painel empilhado no SOUTH
        direita.add(painelBotoes, BorderLayout.SOUTH);

        painelAgenda.add(direita, BorderLayout.EAST);

        // ---- EVENTOS DOS BOTÕES DE NAVEGAÇÃO ----
        btnAnterior.addActionListener(e -> {
            mesAtual = mesAtual.minusMonths(1);
            atualizarCalendario();
        });

        btnProximo.addActionListener(e -> {
            mesAtual = mesAtual.plusMonths(1);
            atualizarCalendario();
        });

        // ---- CHAMAR O CALENDÁRIO AO CRIAR A TELA ----
        atualizarCalendario();

        return painelAgenda;
    }

    private LocalDate mesAtual = LocalDate.now().withDayOfMonth(1);

    private void atualizarCalendario() {

        lblMesAno.setText(
                mesAtual.getMonth().toString().toUpperCase() + " / " + mesAtual.getYear()
        );

        JPanel grade = (JPanel) painelAgenda.getComponent(1);

        Component[] botoes = grade.getComponents();

        // Limpa dias antigos
        for (int i = 7; i < 49; i++) {
            JButton btn = (JButton) botoes[i];
            btn.setText("");

// reset VISUAL COMPLETO
            btn.setOpaque(false);
            btn.setContentAreaFilled(true);
            btn.setBackground(UIManager.getColor("Button.background"));
            btn.setBorder(UIManager.getBorder("Button.border"));
            btn.setForeground(UIManager.getColor("Button.foreground"));
            btn.setFont(new Font("SansSerif", Font.PLAIN, 14));

// Evita herança de alinhamento do mês anterior
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            btn.setVerticalAlignment(SwingConstants.CENTER);

        }

        int primeiroDiaSemana = mesAtual.getDayOfWeek().getValue(); // 1=Seg … 7=Dom
        if (primeiroDiaSemana == 7) primeiroDiaSemana = 0;

        int diasNoMes = mesAtual.lengthOfMonth();

        int idx = 7 + primeiroDiaSemana;

        for (int dia = 1; dia <= diasNoMes; dia++) {

            JButton btn = (JButton) botoes[idx];
            btn.setText(String.valueOf(dia));

            // ---- VERIFICA SE EXISTE ANOTAÇÃO E PINTA DE VERDE ----
            File pasta = new File(BASE_DIR, "agenda");
            File arquivo = new File(pasta, LocalDate.of(mesAtual.getYear(), mesAtual.getMonth(), dia) + ".txt");

            if (arquivo.exists()) {
                btn.setOpaque(true);
                btn.setContentAreaFilled(true);

// fundo azul claro
                btn.setBackground(new Color(150, 180, 255));

// borda azul escuro
                btn.setBorder(BorderFactory.createLineBorder(new Color(50, 90, 200), 1));

// centraliza o número
                btn.setHorizontalAlignment(SwingConstants.CENTER);
                btn.setVerticalAlignment(SwingConstants.CENTER);

// deixa o número azul escuro
                btn.setForeground(new Color(20, 40, 120));

// opcional: melhora a fonte
                btn.setFont(new Font("SansSerif", Font.BOLD, 15));

            }

            idx++;
        }
    }


    private void carregarAnotacoesDia(LocalDate data) {
        try {
            File pasta = new File(BASE_DIR, "agenda");
            if (!pasta.exists()) pasta.mkdirs();

            File file = new File(pasta, data + ".txt");

            if (file.exists()) {
                areaAnotacoes.setText(String.join("\n", Files.readAllLines(file.toPath())));
            } else {
                areaAnotacoes.setText("");
            }

        } catch (Exception ignored) {}
    }

    private void salvarAnotacoesDia() {
        try {
            File pasta = new File(BASE_DIR, "agenda");
            if (!pasta.exists()) pasta.mkdirs();

            File file = new File(pasta, dataSelecionada + ".txt");

            try (FileWriter fw = new FileWriter(file)) {
                fw.write(areaAnotacoes.getText());
            }

            JOptionPane.showMessageDialog(this, "Anotações salvas com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + e.getMessage());
        }
    }

    private void gerarPlanilhaPagamentos(String paciente, String tratamento) {
        try {
            // Pasta do paciente
            File pastaPac = new File(BASE_DIR, paciente.replace(" ", "_"));
            if (!pastaPac.exists()) pastaPac.mkdirs();

            // Arquivo de encontros
            File arquivoEncontros = getEncontrosFileForPatientTreatment(paciente, tratamento);
            if (!arquivoEncontros.exists()) {
                JOptionPane.showMessageDialog(this,
                        "Nenhum encontro encontrado para este tratamento.");
                return;
            }

            // Nome do CSV
            File csv = new File(pastaPac, "planilha_pagamentos_" +
                    tratamento.replace(" ", "_") + ".csv");

            // Valor do encontro (valor do tratamento)
            double valor = obterValorDoTratamentoPorNome(tratamento);

            // Lê encontros
            java.util.List<String> linhas = Files.readAllLines(arquivoEncontros.toPath());

            StringBuilder sb = new StringBuilder();

            // CABEÇALHO
            sb.append("Paciente;Tratamento;Encontro;Status;Valor;Data do Pagamento\n");

            for (String l : linhas) {
                if (l.trim().isEmpty()) continue;

                String[] p = l.split("\\|");
                if (p.length < 2) continue;

                String nomeEncontro = p[0];
                boolean pago = p[1].trim().equalsIgnoreCase("true");
                String dataPagamento = (p.length >= 3) ? p[2] : "";


                if (pago) {
                    sb.append(paciente).append(";")
                            .append(tratamento).append(";")
                            .append(nomeEncontro).append(";")
                            .append("PAGO").append(";")
                            .append(String.format("R$ %.2f", valor)).append(";")
                            .append(dataPagamento)
                            .append("\n");
                }
            }

            // Salva CSV
            try (FileWriter fw = new FileWriter(csv, false)) {
                fw.write(sb.toString());
            }

            JOptionPane.showMessageDialog(this,
                    "Planilha gerada com sucesso!\n" + csv.getAbsolutePath());

            // Abre no Excel
            Desktop.getDesktop().open(csv);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao gerar planilha: " + e.getMessage());
        }
    }



    // ------------- util geral -------------
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    // ------------- main -------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            PacientsInterface app = new PacientsInterface();
            app.setVisible(true);
        });
    }
}
