package edu.curtin.texteditor;

import edu.curtin.texteditor.api.EditorAPI;
import groovy.lang.GroovyRuntimeException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// Manages the loaded plugins and scripts and allows the user to load new ones
public class PluginsUI
{
    private final Stage stage;
    private final ResourceBundle bundle;
    private final PluginScriptLoader loader;
    private final ObservableList<String> loadedModules = FXCollections.observableArrayList();
    private final FileChooser scriptChooser = new FileChooser();

    public PluginsUI(Stage stage, ResourceBundle bundle, PluginScriptLoader loader)
    {
        this.stage = stage;
        this.bundle = bundle;
        this.loader = loader;

        // Uses the root project directory by default if running from inside the app/ subproject
        File parentDir = new File("..").getAbsoluteFile();
        if(parentDir.exists() && parentDir.isDirectory())
        {
            this.scriptChooser.setInitialDirectory(parentDir);
        }
        else
        {
            this.scriptChooser.setInitialDirectory(new File("."));
        }
    }

    public void showDialog(EditorAPI api)
    {
        var dialog = new Dialog<Void>();
        dialog.initOwner(stage);
        dialog.setTitle(bundle.getString("plugins_dialog_title"));
        dialog.setHeaderText(bundle.getString("plugins_dialog_header"));

        var addPluginBtn = new Button(bundle.getString("add_plugin_btn"));
        var addScriptBtn = new Button(bundle.getString("add_script_btn"));
        var toolBar = new ToolBar(addPluginBtn, addScriptBtn);

        addPluginBtn.setOnAction(event -> promptAndLoadPlugin(api));
        addScriptBtn.setOnAction(event -> promptAndLoadScript(api));

        var listView = new ListView<>(loadedModules);
        var box = new BorderPane();
        box.setTop(toolBar);
        box.setCenter(new ScrollPane(listView));

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }

    private void promptAndLoadPlugin(EditorAPI api)
    {
        var inputDialog = new TextInputDialog();
        inputDialog.initOwner(stage);
        inputDialog.setTitle(bundle.getString("plugin_prompt_title"));
        inputDialog.setHeaderText(bundle.getString("plugin_prompt_header"));

        String className = inputDialog.showAndWait().orElse(null);
        if(className != null && !className.isBlank())
        {
            String trimmedName = className.trim();
            try
            {
                loader.loadPlugin(trimmedName, api);
                loadedModules.add("Plugin: " + trimmedName);
            }
            catch(InvocationTargetException e)
            {
                Throwable cause = (e.getCause() != null) ? e.getCause() : e;
                showError(bundle.getString("error_plugin_title"), cause.getClass().getSimpleName() + ": " + cause.getMessage());
            }
            catch(ReflectiveOperationException | ClassCastException e)
            {
                showError(bundle.getString("error_plugin_title"), e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    private void promptAndLoadScript(EditorAPI api)
    {
        scriptChooser.setTitle(bundle.getString("load_script_title"));
        File scriptFile = scriptChooser.showOpenDialog(stage);
        if(scriptFile != null)
        {
            try
            {
                loader.loadScript(scriptFile, api);
                loadedModules.add("Script: " + scriptFile.getName());
            }
            catch(IOException | GroovyRuntimeException e)
            {
                showError(bundle.getString("error_script_title"), e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    private void showError(String title, String message)
    {
        var alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.CLOSE);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.showAndWait();
    }
}