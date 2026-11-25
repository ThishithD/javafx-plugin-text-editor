package edu.curtin.texteditor;

// Thrown when keymap.txt has errors
public class KeymapParseException extends Exception
{
    public KeymapParseException(String message)
    {
        super(message);
    }

    public KeymapParseException(String message, Throwable cause)
    {
        super(message, cause);
    }
}