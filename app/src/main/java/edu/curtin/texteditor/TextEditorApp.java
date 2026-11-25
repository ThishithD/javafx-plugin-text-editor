package edu.curtin.texteditor;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

//Main entry point for the text editor app

public class TextEditorApp extends Application
{
    private static final String KEYMAP_FILENAME = "keymap.txt";
    public static void main(String[] args)
    {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage)
    {
        Locale locale = resolveLocale(getParameters());
        ResourceBundle bundle = ResourceBundle.getBundle("bundle", locale);

        Map<KeyChord, List<KeymapAction>> keymap = loadKeymap(bundle);

        var fileIO = new FileIO();
        var loadSaveUI = new LoadSaveUI(stage, bundle, fileIO);
        var pluginScriptLoader = new PluginScriptLoader(fileIO);
        var pluginsUI = new PluginsUI(stage, bundle, pluginScriptLoader);
        var mainUI = new MainUI(stage, locale, bundle, loadSaveUI, pluginsUI, keymap);
        mainUI.display();
    }

    private Map<KeyChord, List<KeymapAction>> loadKeymap(ResourceBundle bundle)
    {
        File keymapFile = new File(KEYMAP_FILENAME);
        if(!keymapFile.exists())
        {
            File fallback = new File("app", KEYMAP_FILENAME);
            if(fallback.exists())
            {
                keymapFile = fallback;
            }
        }

        if(!keymapFile.exists())
        {
            return Collections.emptyMap();
        }

        try
        {
            return new KeymapLoader().load(keymapFile);
        }
        catch(IOException | KeymapParseException e)
        {
            var alert = new Alert(
                Alert.AlertType.WARNING,
                e.getMessage(),
                ButtonType.OK
            );
            alert.setTitle(bundle.getString("error_keymap_title"));
            alert.setHeaderText(bundle.getString("error_keymap_title"));
            alert.showAndWait();
            return Collections.emptyMap();
        }
    }

    // Gets the locale from command line arguments or uses the default if none is provided
    private Locale resolveLocale(Parameters params)
    {
        Map<String, String> named = params.getNamed();
        String localeTag = named.get("locale");

        if(localeTag == null)
        {
            List<String> raw = params.getRaw();
            if(!raw.isEmpty())
            {
                String firstArg = raw.get(0).trim();
                if(firstArg.startsWith("--locale="))
                {
                    localeTag = firstArg.substring("--locale=".length()).trim();
                }
                else
                {
                    localeTag = firstArg;
                }
            }
        }

        if(localeTag != null && !localeTag.isEmpty())
        {
            return Locale.forLanguageTag(localeTag);
        }
        return Locale.getDefault();
    }
}