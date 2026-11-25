package edu.curtin.texteditor;

import edu.curtin.texteditor.api.ButtonCallback;
import edu.curtin.texteditor.api.EditorAPI;
import edu.curtin.texteditor.api.FunctionKeyCallback;
import edu.curtin.texteditor.api.TextModificationCallback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

// Manaes the main editor text window and its core functions
public class MainUI implements EditorAPI
{
    private static final int FONT_SIZE = 20;
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;

    private final Stage stage;
    private final Locale locale;
    private final ResourceBundle bundle;
    private final LoadSaveUI loadSaveUI;
    private final PluginsUI pluginsUI;
    private final Map<KeyChord, List<KeymapAction>> keymap;

    private final TextArea textArea = new TextArea();
    private final ToolBar toolBar = new ToolBar();

    private final List<TextModificationCallback> textCallbacks = new ArrayList<>();
    private final Map<String, List<FunctionKeyCallback>> functionKeyCallbacks = new HashMap<>();

    public MainUI(Stage stage, Locale locale, ResourceBundle bundle, LoadSaveUI loadSaveUI, PluginsUI pluginsUI, Map<KeyChord, List<KeymapAction>> keymap)
    {
        this.stage = stage;
        this.locale = locale;
        this.bundle = bundle;
        this.loadSaveUI = loadSaveUI;
        this.pluginsUI = pluginsUI;
        this.keymap = new HashMap<>(keymap);
    }

    public void display()
    {
        stage.setTitle(bundle.getString("window_title"));
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);

        var openBtn = new Button(bundle.getString("open_btn"));
        var saveBtn = new Button(bundle.getString("save_btn"));
        var pluginsBtn = new Button(bundle.getString("plugins_btn"));

        toolBar.getItems().addAll(openBtn, saveBtn, pluginsBtn, new Separator());
        toolBar.setFocusTraversable(false);
        toolBar.getItems().forEach(item -> item.setFocusTraversable(false));

        textArea.setStyle(String.format(
            "-fx-font-family: 'monospace'; -fx-font-size: %d;",
            FONT_SIZE
        ));

        openBtn.setOnAction(event ->
        {
            String loadedText = loadSaveUI.load();
            if(loadedText != null)
            {
                setText(loadedText);
            }
        });

        saveBtn.setOnAction(event -> loadSaveUI.save(textArea.getText()));
        pluginsBtn.setOnAction(event -> pluginsUI.showDialog(this));

        textArea.textProperty().addListener((obs, oldVal, newVal) -> notifyTextModified());

        var mainBox = new BorderPane();
        mainBox.setTop(toolBar);
        mainBox.setCenter(textArea);

        var scene = new Scene(mainBox);
        scene.setOnKeyPressed(this::handleKeyPress);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    private void notifyTextModified()
    {
        List<TextModificationCallback> snapshot = new ArrayList<>(textCallbacks);
        Platform.runLater(() ->
        {
            for(TextModificationCallback cb : snapshot)
            {
                cb.onTextModified();
            }
        });
    }

    private void handleKeyPress(KeyEvent keyEvent)
    {
        KeyCode code = keyEvent.getCode();
        String keyName = code.getName().toUpperCase(Locale.ROOT);

        // Notify registered F1-F12 callbacks
        List<FunctionKeyCallback> callbacks = functionKeyCallbacks.get(keyName);
        if(callbacks != null && !callbacks.isEmpty())
        {
            for(FunctionKeyCallback cb : new ArrayList<>(callbacks))
            {
                cb.onFunctionKeyPressed();
            }
            keyEvent.consume();
            return;
        }

        // Check keyboard shortcuts in keymap.txt
        if(code.isLetterKey() && keyName.length() == 1)
        {
            char letter = Character.toLowerCase(keyName.charAt(0));
            var chord = new KeyChord(keyEvent.isAltDown(), keyEvent.isControlDown(), keyEvent.isShiftDown(), letter
            );

            List<KeymapAction> actions = keymap.get(chord);
            if(actions != null && !actions.isEmpty())
            {
                for(KeymapAction action : actions)
                {
                    executeKeymapAction(action);
                }
                keyEvent.consume();
            }
        }
    }

    private void executeKeymapAction(KeymapAction action)
    {
        String target = action.getText();
        if(target.isEmpty())
        {
            return;
        }

        String doc = textArea.getText();
        int caret = textArea.getCaretPosition();

        if(action.getLocationType() == KeymapAction.LocationType.AT_CARET)
        {
            if(action.getActionType() == KeymapAction.ActionType.INSERT)
            {
                textArea.insertText(caret, target);
            }
            else
            {
                if(doc.startsWith(target, caret))
                {
                    textArea.deleteText(caret, caret + target.length());
                }
                else if(caret >= target.length() && doc.startsWith(target, caret - target.length()))
                {
                    textArea.deleteText(caret - target.length(), caret);
                }
            }
        }
        else
        {
            int lastNewline = doc.lastIndexOf('\n', caret - 1);
            int lineStart = (lastNewline == -1) ? 0 : lastNewline + 1;

            if(action.getActionType() == KeymapAction.ActionType.INSERT)
            {
                textArea.insertText(lineStart, target);
            }
            else if(doc.startsWith(target, lineStart))
            {
                textArea.deleteText(lineStart, lineStart + target.length());
            }
        }
    }

    // EditorAPI Implementation
    @Override
    public String getText()
    {
        return textArea.getText();
    }

    @Override
    public void setText(String text)
    {
        if(!text.equals(textArea.getText()))
        {
            textArea.setText(text);
        }
    }

    @Override
    public int getCaretPosition()
    {
        return textArea.getCaretPosition();
    }
    @Override
    public void setCaretPosition(int position)
    {
        int clamped = Math.max(0, Math.min(position, textArea.getLength()));
        textArea.positionCaret(clamped);
    }
    @Override
    public int getSelectionStart()
    {
        IndexRange selection = textArea.getSelection();
        return selection.getStart();
    }
    @Override
    public int getSelectionEnd()
    {
        IndexRange selection = textArea.getSelection();
        return selection.getEnd();
    }
    @Override
    public String getSelectedText()
    {
        return textArea.getSelectedText();
    }
    @Override
    public void selectText(int start, int end)
    {
        int len = textArea.getLength();
        int clampedStart = Math.max(0, Math.min(start, len));
        int clampedEnd = Math.max(0, Math.min(end, len));
        textArea.selectRange(clampedStart, clampedEnd);
    }
    @Override
    public String promptForInput(String title, String promptText)
    {
        var dialog = new TextInputDialog();
        dialog.initOwner(stage);
        dialog.setTitle(title);
        dialog.setHeaderText(promptText);
        return dialog.showAndWait().orElse(null);
    }
    @Override
    public void addButton(String label, ButtonCallback callback)
    {
        var button = new Button(label);
        button.setFocusTraversable(false);
        button.setOnAction(event -> callback.onButtonPressed());
        toolBar.getItems().add(button);
    }
    @Override
    public void addTextModificationCallback(TextModificationCallback callback)
    {
        textCallbacks.add(callback);
    }
    @Override
    public void addFunctionKeyCallback(String functionKey, FunctionKeyCallback callback)
    {
        if(functionKey != null)
        {
            String normalisedKey = functionKey.trim().toUpperCase(Locale.ROOT);
            functionKeyCallbacks.computeIfAbsent(normalisedKey, k -> new ArrayList<>()).add(callback);
        }
    }
    @Override
    public Locale getLocale()
    {
        return locale;
    }
    @Override
    public String getTranslation(String key)
    {
        if(bundle.containsKey(key))
        {
            return bundle.getString(key);
        }
        return key;
    }
}