package edu.curtin.texteditor.api;

import java.util.Locale;

public interface EditorAPI
{
    String getText();
    void setText(String text);

    int getCaretPosition();
    void setCaretPosition(int position);

    int getSelectionStart();
    int getSelectionEnd();
    String getSelectedText();
    void selectText(int start, int end);

    String promptForInput(String title, String promptText);
    void addButton(String label, ButtonCallback callback);

    void addTextModificationCallback(TextModificationCallback callback);
    void addFunctionKeyCallback(String functionKey, FunctionKeyCallback callback);

    Locale getLocale();
    String getTranslation(String key);
}