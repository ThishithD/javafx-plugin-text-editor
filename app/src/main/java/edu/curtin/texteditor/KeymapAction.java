package edu.curtin.texteditor;

// Represent an edit like insert or delete text  triggered by a KeyChord 
public class KeymapAction
{
    public enum ActionType
    {
        INSERT, DELETE
    }
    public enum LocationType
    {
        START_OF_LINE, AT_CARET
    }

    private final ActionType actionType;
    private final String text;
    private final LocationType locationType;

    public KeymapAction(ActionType actionType, String text, LocationType locationType)
    {
        this.actionType = actionType;
        this.text = text;
        this.locationType = locationType;
    }
    public ActionType getActionType()
    {
        return actionType;
    }
    public String getText()
    {
        return text;
    }
    public LocationType getLocationType()
    {
        return locationType;
    }
}