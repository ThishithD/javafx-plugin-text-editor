package edu.curtin.texteditor;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// Manages the file open/save and encoding options 
public class LoadSaveUI
{
    private static final int SPACING = 8;

    private final Stage stage;
    private final ResourceBundle bundle;
    private final FileIO fileIO;
    private final FileChooser fileDialog = new FileChooser();

    public LoadSaveUI(Stage stage, ResourceBundle bundle, FileIO fileIO)
    {
        this.stage = stage;
        this.bundle = bundle;
        this.fileIO = fileIO;
        this.fileDialog.setInitialDirectory(new File("."));
    }

    // Shows a dialog to choose UTF-8, UTF-16, or UTF-32
    private String getEncoding()
    {
        var encodingComboBox = new ComboBox<String>();
        encodingComboBox.getItems().setAll("UTF-8", "UTF-16", "UTF-32");
        encodingComboBox.setValue("UTF-8");

        var content = new FlowPane();
        content.setHgap(SPACING);
        content.getChildren().setAll(new Label(bundle.getString("encoding_label")),
            encodingComboBox
        );

        var dialog = new Dialog<String>();
        dialog.setTitle(bundle.getString("encoding_dialog_title"));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> (btn == ButtonType.OK) ? encodingComboBox.getValue() : null
        );
        return dialog.showAndWait().orElse(null);
    }

    // Asks the user to choose a file and it's encoding, then loads its text
    public String load()
    {
        fileDialog.setTitle(bundle.getString("open_file_title"));
        File f = fileDialog.showOpenDialog(stage);
        if(f != null)
        {
            String encoding = getEncoding();
            if(encoding != null)
            {
                try
                {
                    return fileIO.load(f, encoding);
                }
                catch(IOException e)
                {
                    var alert = new Alert(
                        Alert.AlertType.ERROR,
                        e.getMessage(),
                        ButtonType.CLOSE
                    );
                    alert.setTitle(bundle.getString("error_open_title"));
                    alert.setHeaderText(bundle.getString("error_open_title"));
                    alert.showAndWait();
                }
            }
        }
        return null;
    }

    // Asks the user to choose where to save the file and it's encoding, then saves the text
    public void save(String text)
    {
        fileDialog.setTitle(bundle.getString("save_file_title"));
        File f = fileDialog.showSaveDialog(stage);
        if(f != null)
        {
            String encoding = getEncoding();
            if(encoding != null)
            {
                try
                {
                    fileIO.save(f, text, encoding);
                }
                catch(IOException e)
                {
                    var alert = new Alert(Alert.AlertType.ERROR, e.getMessage(),
                        ButtonType.CLOSE
                    );
                    alert.setTitle(bundle.getString("error_save_title"));
                    alert.setHeaderText(bundle.getString("error_save_title"));
                    alert.showAndWait();
                }
            }
        }
    }
}