package com.kiwi.main;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ContextMenu implements ContextMenuItemsProvider {
    private final MontoyaApi api;
    private TableModel tableModel;

    public ContextMenu(MontoyaApi api)
    {
        this.api = api;
    }

    @Override
    public java.util.List<Component> provideMenuItems(ContextMenuEvent event) {
        if (event.isFromTool(ToolType.PROXY, ToolType.TARGET, ToolType.LOGGER)) {
            List<Component> menuItemList = new ArrayList<>();
            JMenuItem retrieveRequestItem = new JMenuItem("Find the Parameters");
            JMenuItem retrieveResponseItem = new JMenuItem("Test");
            retrieveRequestItem.addActionListener(e -> {
                Optional<MessageEditorHttpRequestResponse> messageEditorHttpRequestResponse = event.messageEditorRequestResponse();
                TableModel.inputField.setText(messageEditorHttpRequestResponse.get().requestResponse().request().url().toString());
                });
            menuItemList.add(retrieveRequestItem);
            return menuItemList;
        }
        return null;
    }
}
