api.addTextModificationCallback({
    String text = api.getText()
    int caret = api.getCaretPosition()

    if(caret >= 3 && text.substring(caret - 3, caret) == ":-)")
    {
        String smiley = "\uD83D\uDE0A" 
        String updated = text.substring(0, caret - 3) + smiley + text.substring(caret)
        api.setText(updated)
        api.setCaretPosition(caret - 3 + smiley.length())
    }
})