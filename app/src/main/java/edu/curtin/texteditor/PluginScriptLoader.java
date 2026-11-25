package edu.curtin.texteditor;

import edu.curtin.texteditor.api.EditorAPI;
import edu.curtin.texteditor.api.EditorPlugin;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import java.io.File;
import java.io.IOException;

// Loads java plugins and groovy scripts
public class PluginScriptLoader
{
    private final FileIO fileIO;

    public PluginScriptLoader(FileIO fileIO)
    {
        this.fileIO = fileIO;
    }

    // Loads a java plugin by its class name starts it using the EditorAPI
    public void loadPlugin(String className, EditorAPI api)
        throws ReflectiveOperationException, ClassCastException
    {
        Class<?> cls = Class.forName(className.trim());
        EditorPlugin plugin = (EditorPlugin) cls.getConstructor().newInstance();
        plugin.start(api);
    }

    // Reads a UTF-8 script, provides access to the EditorAPI, and runs the script using GroovyShell 
    public void loadScript(File scriptFile, EditorAPI api)
        throws IOException, GroovyRuntimeException
    {
        String scriptCode = fileIO.load(scriptFile, "UTF-8");
        var shell = new GroovyShell();
        shell.setVariable("api", api);
        shell.evaluate(scriptCode);
    }
}