package edu.curtin.texteditor;

import edu.curtin.texteditor.dsl.KeymapLexer;
import edu.curtin.texteditor.dsl.KeymapParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

// Reads keymap.txt in UTF-8 and extracts keyboard shotcuts 
public class KeymapLoader
{
    public KeymapLoader() {}
    public Map<KeyChord, List<KeymapAction>> load(File file) throws IOException, KeymapParseException
    {
        try(InputStream is = new FileInputStream(file);
            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8))
        {
            return parse(reader);
        }
    }

    public Map<KeyChord, List<KeymapAction>> parse(Reader reader) throws IOException, KeymapParseException
    {
        var lexer = new KeymapLexer(CharStreams.fromReader(reader));
        var parser = new KeymapParser(new CommonTokenStream(lexer));

        List<String> syntaxErrors = new ArrayList<>();
        var errorListener = new BaseErrorListener()
        {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
            {
                syntaxErrors.add(String.format("Line %d:%d - %s", line, charPositionInLine, msg));
            }
        };

        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        parser.addErrorListener(errorListener);

        KeymapParser.KeymapFileContext rootCtx = parser.keymapFile();
        if(!syntaxErrors.isEmpty())
        {
            throw new KeymapParseException(String.join("\n", syntaxErrors));
        }

        Map<KeyChord, List<KeymapAction>> keymap = new HashMap<>();
        for(KeymapParser.BindingContext bindingCtx : rootCtx.binding())
        {
            KeyChord chord = extractKeyChord(bindingCtx.keyCombo());
            KeymapAction action = extractAction(bindingCtx);
            keymap.computeIfAbsent(chord, k -> new ArrayList<>()).add(action);
        }
        return keymap;
    }

    private KeyChord extractKeyChord(KeymapParser.KeyComboContext comboCtx) throws KeymapParseException
    {
        boolean alt = false;
        boolean ctrl = false;
        boolean shift = false;

        for(Token modToken : comboCtx.modifiers)
        {
            String modText = modToken.getText();
            switch(modText)
            {
                case "alt":
                    if(alt)
                    {
                        throw new KeymapParseException("Duplicate 'alt' modifier on line " + modToken.getLine());
                    }
                    alt = true;
                    break;
                case "ctrl":
                    if(ctrl)
                    {
                        throw new KeymapParseException("Duplicate 'ctrl' modifier on line " + modToken.getLine());
                    }
                    ctrl = true;
                    break;
                case "shift":
                    if(shift)
                    {
                        throw new KeymapParseException("Duplicate 'shift' modifier on line " + modToken.getLine());
                    }
                    shift = true;
                    break;
                default:
                    throw new KeymapParseException("Unknown modifier: " + modText);
            }
        }

        char letter = comboCtx.key.getText().charAt(0);
        return new KeyChord(alt, ctrl, shift, letter);
    }

    private KeymapAction extractAction(KeymapParser.BindingContext bindingCtx)
    {
        KeymapAction.ActionType actionType =
            (bindingCtx.action().INSERT() != null)
                ? KeymapAction.ActionType.INSERT
                : KeymapAction.ActionType.DELETE;

        KeymapAction.LocationType locationType =
            (bindingCtx.location().CARET() != null)
                ? KeymapAction.LocationType.AT_CARET
                : KeymapAction.LocationType.START_OF_LINE;

        String rawString = bindingCtx.target.getText();
        String unquoted = rawString.substring(1, rawString.length() - 1);

        return new KeymapAction(actionType, unquoted, locationType);
    }
}