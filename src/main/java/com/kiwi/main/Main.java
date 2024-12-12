package com.kiwi.main;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.kiwi.main.KiwiUtils.readFileToList;


public class Main implements BurpExtension
{
    public static MontoyaApi api;
    public static List<String> backList;

    @Override
    public void initialize(MontoyaApi api)
    {
        backList = readFileToList("./BackList.txt"); // 加载黑名单
        Main.api = api;
        api.extension().setName("ParamScanner");
        TableModel tableModel = new TableModel();
        api.http().registerHttpHandler(new HttpHandler(tableModel));
        api.userInterface().registerSuiteTab("ParamScanner", tableModel.constructLoggerTab(api.userInterface()));
        api.userInterface().registerContextMenuItemsProvider(new ContextMenu(api));
        // 检测是否存在BackList.txt文件，如果不存在则创建
        if (!new File("./BackList.txt").exists()) {
            try {
                new File("./BackList.txt").createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        api.logging().logToOutput("install success!!!");
        api.logging().logToOutput("Good Luck!!!");
    }
}