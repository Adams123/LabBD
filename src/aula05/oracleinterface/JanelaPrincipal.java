
package aula05.oracleinterface;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.table.DefaultTableModel;

public class JanelaPrincipal {

    JFrame j;
    JFrame imagem;
    JPanel pPainelDeCima;
    JButton botaoUpdate;
    JButton botaoDelete;
    JPanel pPainelDeBaixo;
    JComboBox jc;
    JTextArea jtAreaDeStatus;
    JTabbedPane tabbedPane;
    JPanel pPainelDeExibicaoDeDados;
    JTable jt;
    JScrollPane scrollPaneResumo;
    JPanel pPanelDeResumo;
    DBFuncionalidades bd;
    JTextArea jAreaDeDDL;
    JButton botaoInsert;
    JScrollPane scrollPane;
    JButton jBotaoAtualizar;
    JPanel painelUpdate;
    JButton botaoExibeJogo;
    communicationClass comm = new communicationClass();

    String selectedTabela = null;

    public void ExibeJanelaPrincipal() {
        /*Janela*/
        j = new JFrame("ICMC-USP - SCC0541 - Projeto Final");
        j.setSize(700, 500);
        j.setLayout(new BorderLayout());
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*Painel da parte superior (north) - com combobox e outras informações*/
        pPainelDeCima = new JPanel();
        j.add(pPainelDeCima, BorderLayout.NORTH);
        jc = new JComboBox();
        pPainelDeCima.add(jc);

        imagem = new JFrame("imagem");
        imagem.setSize(700, 500);
        imagem.setLayout(new BorderLayout());
        imagem.setLocation(700, 0);

        /*Painel da parte inferior (south) - com área de status*/
        pPainelDeBaixo = new JPanel();
        j.add(pPainelDeBaixo, BorderLayout.SOUTH);
        jtAreaDeStatus = new JTextArea();
        jtAreaDeStatus.setText("Aqui é sua área de status");
        pPainelDeBaixo.add(jtAreaDeStatus);

        botaoDelete = new JButton("Remover");
        pPainelDeBaixo.add(botaoDelete);

        /*Painel tabulado na parte central (CENTER)*/
        tabbedPane = new JTabbedPane();
        j.add(tabbedPane, BorderLayout.CENTER);

        /*Tab de exibicao*/
        pPainelDeExibicaoDeDados = new JPanel();
        pPainelDeExibicaoDeDados.setLayout(new GridLayout(1, 2));
        botaoUpdate = new JButton("Update");

        tabbedPane.add(pPainelDeExibicaoDeDados, "Exibição");

        /*Table de exibição*/
        jt = new JTable();

        botaoUpdate = new JButton("Atualizar");
        pPainelDeBaixo.add(botaoUpdate);

        botaoInsert = new JButton("Inserir");
        pPainelDeBaixo.add(botaoInsert);

        botaoExibeJogo = new JButton("Exibir dados");

        pPanelDeResumo = new JPanel();
        pPanelDeResumo.setLayout(new GridLayout(1, 1));
        tabbedPane.add(pPanelDeResumo, "Resumo");

        bd = new DBFuncionalidades(jtAreaDeStatus, imagem);
        if (bd.conectar()) {
            bd.pegarNomesDeTabelas(jc);
        }
        this.DefineEventos();
        scrollPaneResumo = bd.exibeResumo(comm);
        pPanelDeResumo.add(scrollPaneResumo);
        j.setVisible(true);

    }

    private void DefineEventos() {
        jc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /* ------Exibir Dados--------*/
                JComboBox jcTemp = (JComboBox) e.getSource();
                jtAreaDeStatus.setText((String) jcTemp.getSelectedItem());
                selectedTabela = jcTemp.getSelectedItem().toString();
                scrollPane = bd.exibeDados(jt, (String) jcTemp.getSelectedItem(), comm);
                pPainelDeExibicaoDeDados.add(scrollPane);
                //verifica se já existe um scrollPane dentro do painel de exibicao de dados. se existir, remove o anterior
                if (pPainelDeExibicaoDeDados.getComponentCount() > 1) {
                    pPainelDeExibicaoDeDados.remove(pPainelDeExibicaoDeDados.getComponents()[0]);
                }
                if (pPainelDeCima.getComponentCount() > 1) {
                    pPainelDeCima.remove(pPainelDeCima.getComponents()[1]);
                }
                if ("JOGO".equals(selectedTabela)) {
                    pPainelDeCima.add(botaoExibeJogo);
                    imagem.setVisible(true);
                    JPanel a = new JPanel();
                    a = bd.leblob(comm, 7);
                    imagem.add(a);
                    if (imagem.getComponentCount() > 1) {
                        imagem.remove(imagem.getComponents()[0]);
                    }
                } else {
                    imagem.setVisible(false);
                    imagem.setFocusableWindowState(false);

                }
            }
        });
        //atualizar a tabela
        botaoUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedTabela == null) {
                    return;
                }
                JViewport viewport = scrollPane.getViewport();
                JTable mytable = (JTable) viewport.getView();
                bd.refreshAll(mytable, selectedTabela, comm);
            }
        });
        //adicionar uma linha para inserção
        botaoInsert.addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("empty-statement")
            public void actionPerformed(ActionEvent e) {
                if (selectedTabela == null) {
                    return;
                }
                JViewport viewport = scrollPane.getViewport();
                JTable mytable = (JTable) viewport.getView();
                DefaultTableModel model = (DefaultTableModel) mytable.getModel();;
                Vector<String> Data = new Vector<>();
                for (int i = 0; i < mytable.getColumnCount(); i++) {
                    Data.add("");
                }
                
                model.insertRow(mytable.getRowCount(), Data);

                bd.createKeybindings(mytable, comm, selectedTabela);
            }
        });

        botaoExibeJogo.addActionListener(new ActionListener() {
            @Override

            public void actionPerformed(ActionEvent e) {
                JViewport viewport = scrollPane.getViewport();
                JTable mytable = (JTable) viewport.getView();
                if (mytable.getSelectedRow() == -1) {
                    return;
                }
                Vector data = new Vector();
                Vector names = new Vector();

                int row = mytable.getSelectedRow();
                String nomeJogo = mytable.getValueAt(row, 0).toString();
                Vector tupla = new Vector(communicationClass.compareRow.size());
//                tupla.addElement();
                data.addElement(bd.createRow(nomeJogo));
                data.addElement(communicationClass.compareRow);
                JTable jogoTable = new JTable(data, communicationClass.Header);

                bd.exibeDadosJogo(jogoTable);
            }
        });

        botaoDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedTabela == null) {
                    return;
                }
                JViewport viewport = scrollPane.getViewport();
                JTable mytable = (JTable) viewport.getView();
                bd.delete(mytable, selectedTabela, comm);
                pPainelDeExibicaoDeDados.repaint();
            }
        });
    }

}
