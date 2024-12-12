package com.kiwi.main;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.UserInterface;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static burp.api.montoya.ui.editor.EditorOptions.READ_ONLY;
import static com.kiwi.main.Main.api;

public class TableModel extends AbstractTableModel {
    private final List<HttpRequestResponse> log;
    public static JTextField inputField;
    public static JButton sendButton;
    public static JPanel mainPanel;
    static JPanel inputPanel;
    JComboBox<String> methodComboBox;
    public static String selectedMethod;
    public static JTable table;

    public TableModel() {
        this.log = new ArrayList<>();
        sendButton = new JButton("Attack");
        inputField = new JTextField(90);
        mainPanel = new JPanel(new BorderLayout());
        inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        methodComboBox = new JComboBox<>(new String[]{"GET", "POST"});
    }

    @Override
    public synchronized int getRowCount() {
        return log.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case 0 -> "URL";
            case 1 -> "Method";
            case 2 -> "Status Code";
            case 3 -> "Length";
            case 4 -> "Title";
            default -> "";
        };
    }

    @Override
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
        HttpRequestResponse httpResponse = log.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> httpResponse.request().url();
            case 1 -> httpResponse.request().method();
            case 2 -> httpResponse.response().statusCode();
            case 3 -> httpResponse.response().bodyToString().length();
            case 4 -> KiwiUtils.getTitleFromResponse(httpResponse);
            default -> "";
        };
    }

    public synchronized void add(HttpRequestResponse httpRequestResponse) {
        int index = log.size();
        log.add(httpRequestResponse);
        fireTableRowsInserted(index, index);
    }

    public synchronized HttpRequestResponse get(int rowIndex) {
        return log.get(rowIndex);
    }


    public Component constructLoggerTab(UserInterface userInterface) {
        // 主分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // 请求/响应查看器
        JTabbedPane tabs = new JTabbedPane();

        HttpRequestEditor requestViewer = userInterface.createHttpRequestEditor(READ_ONLY);
        HttpResponseEditor responseViewer = userInterface.createHttpResponseEditor(READ_ONLY);

        tabs.addTab("Request", requestViewer.uiComponent());
        tabs.addTab("Response", responseViewer.uiComponent());

        splitPane.setRightComponent(tabs);
        // 日志表格
         table = new JTable(this) {
            @Override
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                // 显示选中行的请求/响应内容
                HttpRequestResponse httpRequestResponse = get(rowIndex);
                requestViewer.setRequest(httpRequestResponse.request());
                responseViewer.setResponse(httpRequestResponse.response());
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
            }
        };

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(this);
        // 为特定列设置比较器
        sorter.setComparator(2, Comparator.comparingInt(o -> Integer.parseInt(o.toString())));
        sorter.setComparator(3, Comparator.comparingInt(o -> Integer.parseInt(o.toString())));

        // 将排序器绑定到表格
        table.setRowSorter(sorter);

        // 添加表格到滚动面板
        JScrollPane scrollPane = new JScrollPane(table);
        splitPane.setLeftComponent(scrollPane);

        // 添加顶部输入框和按钮
        JButton sendButton = getSendButton();
        inputPanel.add(new JLabel("Target:"));
        inputPanel.add(inputField);
        inputPanel.add(sendButton);
        methodComboBox.addActionListener(e -> {
            selectedMethod = methodComboBox.getSelectedItem().toString();
        });
        inputPanel.add(methodComboBox, BorderLayout.EAST);

        // 主面板，包含输入框和分割面板
        mainPanel.add(inputPanel, BorderLayout.NORTH); // 输入框放在顶部
        mainPanel.add(splitPane, BorderLayout.CENTER); // 分割面板放在中心

        return mainPanel;
    }

    private static JButton getSendButton() {
        JButton sendButton = new JButton("Send Request");
        sendButton.addActionListener(e -> {
            // 清除表格中的旧数据
            TableModel tableModel = (TableModel) table.getModel();
            tableModel.clear();
            try {
                URLParse urlParse = new URLParse(new URL(inputField.getText()));
                api.logging().logToOutput(inputField.getText());
                if (inputField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(mainPanel, "Please enter a valid URL.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                KiwiUtils.sendHttpRequestAsync(urlParse.getUrl(),
                        HttpHandler.httpDataStructure.getParamFromUrl(urlParse.getUrl()),
                        new ResponseCallBack(),selectedMethod);
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
        });
        return sendButton;
    }

    public synchronized void clear() {
        int size = log.size();
        if (size > 0) {
            log.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }

}
