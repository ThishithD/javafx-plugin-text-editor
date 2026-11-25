package edu.curtin.texteditor;

import java.util.Objects;

// Represents a keyboard shortcut using modifier keys like alt, ctrl, shift and and a lowercase letter
public final class KeyChord
{
    private final boolean alt;
    private final boolean ctrl;
    private final boolean shift;
    private final char letter;

    public KeyChord(boolean alt, boolean ctrl, boolean shift, char letter)
    {
        this.alt = alt;
        this.ctrl = ctrl;
        this.shift = shift;
        this.letter = Character.toLowerCase(letter);
    }

    public boolean isAlt()
    {
        return alt;
    }
    public boolean isCtrl()
    {
        return ctrl;
    }
    public boolean isShift()
    {
        return shift;
    }
    public char getLetter()
    {
        return letter;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(this == obj)
        {
            return true;
        }
        if(!(obj instanceof KeyChord other))
        {
            return false;
        }
        return alt == other.alt && ctrl == other.ctrl && shift == other.shift && letter == other.letter;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(alt, ctrl, shift, letter);
    }
}