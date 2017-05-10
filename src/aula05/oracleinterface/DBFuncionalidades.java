package aula05.oracleinterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author junio
 */
public class DBFuncionalidades {

    Connection connection;
    Statement stmt;
    ResultSet rs;
    JTextArea jtAreaDeStatus;
    JPanel pPainelDeExibicaoDeDados;
    JTable table;
    int currentRow = 0;
    String selectedData = null;
    JFrame imagem;
    String sel_fk;

    public DBFuncionalidades(JTextArea jtaTextArea, JFrame imagem) {
        jtAreaDeStatus = jtaTextArea;
        this.imagem = imagem;
    }

    public boolean conectar() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@grad.icmc.usp.br:15215:orcl",
                    "7696393", //usuario inicial
                    "a"); //senha inicial
            return true;
        } catch (ClassNotFoundException ex) {
            jtAreaDeStatus.setText("Problema: verifique o driver do banco de dados");
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Problema: verifique seu usuário e senha");
        }
        return false;
    }

    public void insereImagem() {
        FileInputStream input = null;
        PreparedStatement ps = null;
        CallableStatement call = null;
        String s = "";
        try {
            File[] files = new File(".").listFiles();
            File a = files[8];

//            input = new FileInputStream(a);
            s = "{ call PACKPRATICA10.upload_blob(?, ?)}";
            try (CallableStatement cstmt = connection.prepareCall(s)) {
                cstmt.setBinaryStream(1, new FileInputStream(a), (int) a.length());
                cstmt.setString(2, "F1 2013");
                cstmt.executeUpdate();
            }

//            System.out.println("Reading input file:" + a.getAbsolutePath());
//            System.out.println("\nStoring resume in database: " + a);
            System.out.println(s);
//            ps.executeUpdate();
        } catch (SQLException | FileNotFoundException ex) {
        }
    }

    public void delete(JTable jt, String selectedTabela, communicationClass comm) {
        int rows = jt.getRowCount();
        int column = jt.getColumnCount();
        int i, j;

        if (communicationClass.values.isEmpty()) {
            jtAreaDeStatus.setText("Nenhuma tupla selecionada");
            return;
        }

        ArrayList<Integer> pkIndexes = new ArrayList<>();

        for (i = 0; i < jt.getColumnCount(); i++) {
            for (j = 0; j < communicationClass.pkNames.size(); j++) {
                if (jt.getColumnName(i).equals(communicationClass.pkNames.get(j))) {
                    pkIndexes.add(i);
                }
            }
        }
        for (i = 0; i < communicationClass.pkNames.size(); i++) {
            System.out.println(communicationClass.values.get(pkIndexes.get(i)));
        }

        try {
            String s = "";
            s = "DECLARE \n"
                    + "colunaRestricao ArrayEntrada := ArrayEntrada('";

            for (i = 0; i < communicationClass.pkNames.size() - 1; i++) {
                s = s + communicationClass.pkNames.get(i) + "','";
            }
            s = s + communicationClass.pkNames.get(i) + "');\n"
                    + "valorRestricao ArrayEntrada := ArrayEntrada('";
            for (i = 0; i < communicationClass.pkNames.size() - 1; i++) {
                s = s + communicationClass.values.get(pkIndexes.get(i)) + "','";
            }
            s = s + communicationClass.values.get(pkIndexes.get(i)) + "');\n"
                    + "BEGIN \n"
                    + "PackPratica10.deleta('"
                    + selectedTabela
                    + "', colunaRestricao, valorRestricao);\n"
                    + "END;";
            System.out.println(s);

            stmt = connection.createStatement();
            stmt.executeUpdate(s);
            stmt.close();
            connection.commit();
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro ao deletar");
        }
        jtAreaDeStatus.setText("Tabela " + selectedTabela + " foi alterada");
    }

    public JPanel leblob(communicationClass comm, Integer num) {
        Statement myStmt = null;
        ResultSet result = null;
        ImageIcon img = null;
        String s = "";
        InputStream in = null;
        FileOutputStream out = null;
        Image image;

        try {
            s = "select imagem from imagem where imagemid = " + num;
            myStmt = connection.createStatement();
            result = myStmt.executeQuery(s);

            if (result.next()) {
                byte[] imageByte = result.getBytes(1);
                image = Toolkit.getDefaultToolkit().createImage(imageByte);
                img = new ImageIcon(image);
                System.out.println("readin image from db...");
                System.out.println(s);

                byte[] buffer = new byte[1024];

            }

        } catch (Exception ex) {
            System.out.print(ex);
            jtAreaDeStatus.setText("Erro na consulta: \"" + "\"");
        }
        JLabel i = new JLabel(img);
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(i);
        return p;
    }

    public void pegarNomesDeTabelas(JComboBox jc) {
        String s = "";
        try {
            s = "SELECT table_name FROM user_tables";
            stmt = connection.createStatement();
            rs = stmt.executeQuery(s);
            while (rs.next()) {
                jc.addItem(rs.getString("table_name"));
            }
            stmt.close();
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro na consulta: \"" + s + "\"");
        }
    }

    public JScrollPane exibeDados(JTable tATable, final String sTableName, final communicationClass comm) {
        String s = "";
        Vector columnNames = new Vector();
        Vector data = new Vector();


        final Vector pkColumns = new Vector();
        Vector fkColumns = new Vector();
        try {
            columnNames.removeAllElements();
            s = "SELECT * FROM " + sTableName + "";

            //pega pk
            String sel_pk = "SELECT cols.column_name \n"
                    + "FROM all_constraints cons, all_cons_columns cols\n"
                    + "WHERE cols.table_name = '" + sTableName + "'\n"
                    + "AND cons.constraint_type = 'P'\n"
                    + "AND cons.constraint_name = cols.constraint_name\n"
                    + "AND cons.owner = cols.owner\n"
                    + "ORDER BY cols.table_name, cols.position";
            sel_fk = "SELECT cols.column_name \n"
                    + "FROM all_constraints cons, all_cons_columns cols\n"
                    + "WHERE cols.table_name = '" + sTableName + "'\n"
                    + "AND cons.constraint_type = 'R'\n"
                    + "AND cons.constraint_name = cols.constraint_name\n"
                    + "AND cons.owner = cols.owner\n"
                    + "ORDER BY cols.table_name, cols.position";
            stmt = connection.createStatement();
            rs = stmt.executeQuery(s);
            ResultSetMetaData resultados = rs.getMetaData(); //cria metadados dos resultados
            int colunas = resultados.getColumnCount(); //pega quantidade de colunas

            for (int i = 1; i <= colunas; i++) {
                columnNames.addElement(resultados.getColumnName(i)); //adiciona os nomes das colunas ao vetor de nomes
            }

            while (rs.next()) {
                Vector row = new Vector(colunas);     //cria as tuplas com os dados para exibicao
                for (int i = 1; i <= colunas; i++) {
                    row.addElement(rs.getObject(i));
                }
                data.addElement(row); //adiciona no vetor de dados as tuplas
            }

            //cria vetor com colunas contendo os nomes das pks
            rs = stmt.executeQuery(sel_pk);

            int k = 0;
            while (rs.next()) {
                pkColumns.add(rs.getString(1));
                k++;
            }

            //cria vetor com colunas contendo os nomes das fks
            rs = stmt.executeQuery(sel_fk);

            while (rs.next()) {
                fkColumns.add(rs.getString(1));
            }
            rs.close();

        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro " + ex.getMessage() + " na consulta: \"" + s + "\"");
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        tATable = new JTable(model);

        DefaultTableModel d = new DefaultTableModel(data, columnNames);
        table = new JTable(d);
        table.setCellSelectionEnabled(true);
        ListSelectionModel cellSelectionModel = table.getSelectionModel();
        cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //--------------------------------------------------------------EVENTO MOUSE-----------------------------------------------
        //evento de selecionar uma celula da table
        cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();
                selectedData = table.getValueAt(row, col).toString();
                communicationClass.pkNames.removeAllElements();
                communicationClass.values.removeAllElements();
                for (int i = 0; i < table.getColumnCount(); i++) //pega os valores da row e adiciona na classe de comunicação
                {
                    communicationClass.values.add(table.getValueAt(row, i).toString());
                }
                for (Iterator it = pkColumns.iterator(); it.hasNext();) {
                    Object pkColumn = it.next();
                    communicationClass.pkNames.add(pkColumn.toString());
                }
                communicationClass.rows = row;

                if (sTableName.equals("JOGO")) {
                    if (imagem.getComponentCount() > 1) {
                        imagem.remove(imagem.getComponents()[0]);
                    }
                    JPanel a = new JPanel();
                    Random randomGenerator = new Random();
                    int randomNumber = randomGenerator.nextInt(7);
                    System.out.println(randomNumber);
                    a = leblob(comm, randomNumber);
                    imagem.add(a);
                    imagem.setVisible(true);
                    imagem.setFocusableWindowState(false);

                }

            }
        });

        //pintando as colunas que são pk de verde, fk de azul e ambas de vermelho
        for (int i = 0;
                i < model.getColumnCount();
                i++) {
            for (Object pkColumn : pkColumns) {
                for (Object fkColumn : fkColumns) {
                    //caso seja pk E fk
                    if (model.getColumnName(i).equals(pkColumn.toString()) && model.getColumnName(i).equals(fkColumn.toString())) {
                        TableColumn tm = table.getColumnModel().getColumn(i);
                        tm.setCellRenderer(new ColorColumnRenderer(Color.RED, Color.black));
                    } else if (model.getColumnName(i).equals(pkColumn.toString())) {
                        TableColumn tm = table.getColumnModel().getColumn(i);
                        tm.setCellRenderer(new ColorColumnRenderer(Color.GREEN, Color.black));
                    } else if (model.getColumnName(i).equals(fkColumn.toString())) {
                        TableColumn tm = table.getColumnModel().getColumn(i);
                        tm.setCellRenderer(new ColorColumnRenderer(Color.BLUE, Color.black));
                    }
                }
            }
        }

        table.setPreferredScrollableViewportSize(
                new Dimension(100, 200));

        JScrollPane scrollPane = new JScrollPane(table);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();

        for (int i = 0; i < table.getColumnCount(); i++) {
            sortKeys.add(new RowSorter.SortKey(i, SortOrder.DESCENDING));
        }

        sorter.setSortKeys(sortKeys);
        sorter.sort();

        scrollPane.setLocation(table.getRowCount(), table.getColumnCount());
        communicationClass.rows = table.getRowCount();
        communicationClass.columns = table.getColumnCount();
        return scrollPane;
    }

    public void createKeybindings(final JTable table, final communicationClass comm, final String tableName) {
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
        table.getActionMap().put("Enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int row = table.getSelectedRow();
                communicationClass.values.removeAllElements();
                for (int i = 0; i < table.getColumnCount(); i++) {
                    communicationClass.values.add(table.getValueAt(row, i).toString());
                }
                insereDados(communicationClass.values, tableName);
            }
        });
    }

    public Vector createCompare(String nomeJogo) {
        Vector data = new Vector();
        try {
            //dado um nome de jogo, chama a procedure que cria o resumo do jogo.
            CallableStatement statement = connection.prepareCall("{call COVARIANCIA_TEMPO(?,?,?)}");
            statement.setString(1, nomeJogo);

            for (int i = 2; i <= 12; i++) {
                statement.registerOutParameter(i, java.sql.Types.FLOAT);
            }

            statement.execute();
            data.add(nomeJogo);
            for (int i = 2; i <= 12; i++) {
                data.add(statement.getInt(i));
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return data;

    }

    public Vector createRow(String nomeJogo) {
        Vector data = new Vector();
        try {
            //dado um nome de jogo, chama a procedure que cria o resumo do jogo.
            CallableStatement statement = connection.prepareCall("{call PONTUACAO(?,?,?,?,?,?,?,?,?,?,?,?)}");
            statement.setString(1, nomeJogo);

            for (int i = 2; i <= 12; i++) {
                statement.registerOutParameter(i, java.sql.Types.FLOAT);
            }

            statement.execute();
            data.add(nomeJogo);
            for (int i = 2; i <= 12; i++) {
                data.add(statement.getInt(i));
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return data;
    }

    public double setaPontos(double nCop, double valTotal, double tTotal, double vTotal) {
        return ((nCop * 0.3) + (valTotal * 0.3) + (tTotal * 0.2) + (vTotal * 0.2));
    }

    public double pontuacao(int xi, int xf, int x) {

        return ((x - xi) * 100) / (xf - xi);
    }

    public JScrollPane exibeResumo(communicationClass commu) {
        Vector nomeJogos = new Vector();
        Vector Data = new Vector();
        try {
            String s = "SELECT nome FROM JOGO";
            stmt = connection.createStatement();
            rs = stmt.executeQuery(s);
            int i = 0;
            while (rs.next()) {
                nomeJogos.add(rs.getString(1));
                i++;
            }

            for (int j = 0; j < i - 1; j++) {
                Data.add(createRow(nomeJogos.get(j).toString()));
            }
            stmt.close();
        } catch (SQLException ex) {
        }

        Vector<String> header = new Vector<>();
        header.add("Nome do Jogo");
        header.add("Numero de Copias Vendidas");
        header.add("Valor Total de Compras do Jogo");
        header.add("Valor Total de Compras de Conteudo");
        header.add("Usuario");
        header.add("Tempo Total Jogado pelo Usuario");
        header.add("Quantidade de Conteudos");
        header.add("Media dos Itens");
        header.add("Media dos Preços dos Itens");
        header.add("Variancia do Item");
        header.add("Variancia do Preço");
        header.add("Pontuação");

        JTable tabela = new JTable(Data, header);

        int max = -1;
        int min = 9999999;
        int maxID = 0;
        for (int i = 0; i < tabela.getRowCount(); i++) {
            if ((int) tabela.getValueAt(i, 11) > max) {
                max = (int) tabela.getValueAt(i, 11);
                maxID = i;
            }
            if ((int) tabela.getValueAt(i, 11) < min) {
                min = (int) tabela.getValueAt(i, 11);
            }
        }

        communicationClass.compareRow = (Vector) Data.get(maxID);
        communicationClass.Header = header;

        for (int i = 0; i < tabela.getRowCount(); i++) {
            double ponto = pontuacao(min, max, (int) tabela.getValueAt(i, 11));
            tabela.setValueAt(ponto, i, 11);

        }

        sortAllRowsBy((DefaultTableModel) tabela.getModel(), 11, false);

        JScrollPane panelResumo = new JScrollPane(tabela);

        return panelResumo;

    }

    public void sortAllRowsBy(DefaultTableModel model, int colIndex, boolean ascending) {
        Vector data = model.getDataVector();
        Collections.sort(data, new ColumnSorter(colIndex, ascending));
        model.fireTableStructureChanged();
    }

    public class ColumnSorter implements Comparator {

        int colIndex;
        boolean ascending;

        ColumnSorter(int colIndex, boolean ascending) {
            this.colIndex = colIndex;
            this.ascending = ascending;
        }

        @Override
        public int compare(Object a, Object b) {
            Vector v1 = (Vector) a;
            Vector v2 = (Vector) b;
            Object o1 = v1.get(colIndex);
            Object o2 = v2.get(colIndex);

            if (o1 instanceof String && ((String) o1).length() == 0) {
                o1 = null;
            }
            if (o2 instanceof String && ((String) o2).length() == 0) {
                o2 = null;
            }

            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            } else if (o1 instanceof Comparable) {
                if (ascending) {
                    return ((Comparable) o1).compareTo(o2);
                } else {
                    return ((Comparable) o2).compareTo(o1);
                }
            } else {
                if (ascending) {
                    return o1.toString().compareTo(o2.toString());
                } else {
                    return o2.toString().compareTo(o1.toString());
                }
            }
        }
    }

    public void insereDados(Vector data, String sTableName) {
        try {
            int i;
            String s = "DECLARE\n"
                    + "	ins_val ArrayEntrada := ArrayEntrada(";
            for (i = 0; i < data.size() - 1; i++) {
                if (data.get(i).toString().matches("\\d{4}-\\d{2}-\\d{2}")) {
                    s = s + "to_date('" + data.get(i) + "','yyyy-mm-dd'),";
                } else {
                    s = s + "'" + data.get(i) + "',";
                }
            }
            if (data.get(i).toString().matches("\\d{4}-\\d{2}-\\d{2}")) {
                s = s + "to_date('" + data.get(i) + "','yyyy-mm-dd'));\n";
            } else {
                s = s + "'" + data.get(i) + "');\n";
            }
            s = s + "BEGIN\n"
                    + "	PackPratica10.insere('" + sTableName.toUpperCase() + "', ins_val);\n"
                    + "\n"
                    + "END;";
            stmt = connection.createStatement();
            stmt.execute(s);
            stmt.close();
            connection.commit();
        } catch (SQLException ex) {

        }
    }

    public boolean loginComOutraConta(String login, String senha) {

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection.close(); //fecha qualquer conexão aberta anteriormente
            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:15215:orcl",
                    login,
                    senha);
            jtAreaDeStatus.setText("Conectado com sucesso em: " + login + " | " + senha);
            return true;
        } catch (ClassNotFoundException ex) {
            jtAreaDeStatus.setText("Problema: verifique o driver do banco de dados");
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Problema: verifique seu usuário e senha");
        }
        return false;
    }



    public void refreshAll(JTable jt, String selectedTabela, communicationClass comm) {
        int rows = jt.getRowCount();
        int column = jt.getColumnCount();
        int i, j;

        if (communicationClass.values.isEmpty()) {
            return;
        }

        ArrayList<Integer> pkIndexes = new ArrayList<>();

        for (i = 0; i < jt.getColumnCount(); i++) {
            for (j = 0; j < communicationClass.pkNames.size(); j++) {
                if (jt.getColumnName(i).equals(communicationClass.pkNames.get(j))) {
                    pkIndexes.add(i);
                }
            }
        }

        String refresh = "DECLARE \n colUpdate ArrayEntrada := ArrayEntrada('";

        for (i = 0; i < column - 1; i++) {
            refresh = refresh + jt.getColumnName(i) + "','";
        }
        refresh = refresh + jt.getColumnName(i) + "');\n"
                + "valUpdate ArrayEntrada := ArrayEntrada(";
        for (i = 0; i < column - 1; i++) {
            if (jt.getValueAt(communicationClass.rows, i).toString().matches("\\d{4}-\\d{2}-\\d{2}")) {
                refresh = refresh + "to_date('" + jt.getValueAt(communicationClass.rows, i) + "','yyyy-mm-dd'),";
            } else {
                refresh = refresh + "'" + jt.getValueAt(communicationClass.rows, i) + "',";
            }
        }
        if (jt.getValueAt(communicationClass.rows, i).toString().matches("\\d{4}-\\d{2}-\\d{2}")) {
            refresh = refresh + "to_date('" + jt.getValueAt(communicationClass.rows, i) + "','yyyy-mm-dd'));\n";
        } else {
            refresh = refresh + "'" + jt.getValueAt(communicationClass.rows, i) + "');\n";
        }
        refresh = refresh + "colunaRestricao ArrayEntrada := ArrayEntrada('";
        for (i = 0; i < communicationClass.pkNames.size() - 1; i++) {
            refresh = refresh + communicationClass.pkNames.get(i) + "','";
        }
        refresh = refresh + communicationClass.pkNames.get(i) + "');\n"
                + "valorRestricao ArrayEntrada := ArrayEntrada(";

        for (i = 0; i < communicationClass.pkNames.size() - 1; i++) {
            if (communicationClass.values.get(pkIndexes.get(i)).matches("\\d{4}-\\d{2}-\\d{2}")) {
                refresh = refresh + "to_date('" + communicationClass.values.get(pkIndexes.get(i)) + "','yyyy-mm-dd'),";
            } else {
                refresh = refresh + "'" + communicationClass.values.get(pkIndexes.get(i)) + "',";
            }
        }

        if (communicationClass.values.get(pkIndexes.get(i)).matches("\\d{4}-\\d{2}-\\d{2}")) {
            refresh = refresh + "to_date('" + communicationClass.values.get(pkIndexes.get(i)) + "','yyyy-mm-dd'));\n";
        } else {
            refresh = refresh + "'" + communicationClass.values.get(pkIndexes.get(i)) + "');\n";
        }
        refresh = refresh + "BEGIN \n"
                + "PackPratica10.altera('"
                + selectedTabela
                + "', colUpdate, valUpdate, colunaRestricao, valorRestricao);\n"
                + "END;";
        try {
            stmt = connection.createStatement();
            stmt.executeUpdate(refresh);
            stmt.close();
        } catch (SQLException ex) {
        }

        jtAreaDeStatus.setText("Tabela " + selectedTabela + " atualizada!");

    }


    public void exibeDadosJogo(final JTable table) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

                table.setPreferredScrollableViewportSize(new Dimension(500, 70));

                JFrame frame = new JFrame("Dados do Jogo");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                JPanel panel = new JPanel();
                JScrollPane scrollPane = new JScrollPane(table);
                frame.add(panel, BorderLayout.NORTH);

                JTextArea area = new JTextArea();
                JdbcUtil.enable_dbms_output(connection, 10000);
                area.setLineWrap(true);
                //como é só da tabela jogo, sabemos que a primeira coluna é a pk de jogo (nome)
                area.setText(JdbcUtil.print_dbms_output(connection, table.getValueAt(0, 0).toString()));

                frame.add(area, BorderLayout.SOUTH);
                panel.add(scrollPane);
                panel.setVisible(true);
                frame.pack();
                frame.setVisible(true);
                frame.setResizable(true);
            }
        });
    }

//para pintar as colunas
    class ColorColumnRenderer extends DefaultTableCellRenderer {

        Color bkgndColor, fgndColor;

        public ColorColumnRenderer(Color bkgnd, Color foregnd) {
            super();
            bkgndColor = bkgnd;
            fgndColor = foregnd;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            cell.setBackground(bkgndColor);
            cell.setForeground(fgndColor);

            return cell;
        }
    }

}
