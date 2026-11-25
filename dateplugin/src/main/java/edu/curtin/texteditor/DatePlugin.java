package edu.curtin.texteditor;

import edu.curtin.texteditor.api.EditorAPI;
import edu.curtin.texteditor.api.EditorPlugin;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

// Adds a date button to insert the current date and time
public class DatePlugin implements EditorPlugin
{
    public DatePlugin() {}

    @Override
    public void start(EditorAPI api)
    {
        String buttonLabel = api.getTranslation("date_btn");
        api.addButton(buttonLabel, () -> insertDateAtCaret(api));
    }

    private void insertDateAtCaret(EditorAPI api)
    {
        DateTimeFormatter formatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(api.getLocale());

        String formattedDate = formatter.format(ZonedDateTime.now());
        String currentText = api.getText();
        int caret = Math.max(0, Math.min(api.getCaretPosition(), currentText.length()));

        String updatedText = currentText.substring(0, caret) + formattedDate + currentText.substring(caret);

        api.setText(updatedText);
        api.setCaretPosition(caret + formattedDate.length());
    }
}