package edu.curtin.texteditor;

import edu.curtin.texteditor.api.EditorAPI;
import edu.curtin.texteditor.api.EditorPlugin;
import java.text.BreakIterator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

// Adss a find button and F3 key to search and select the matching text
public class FindPlugin implements EditorPlugin
{
    public FindPlugin() {}

    @Override
    public void start(EditorAPI api)
    {
        String buttonLabel = api.getTranslation("find_btn");
        api.addButton(buttonLabel, () -> performFind(api));
        api.addFunctionKeyCallback("F3", () -> performFind(api));
    }

    private void performFind(EditorAPI api)
    {
        String title = api.getTranslation("find_prompt_title");
        String header = api.getTranslation("find_prompt_header");
        String searchTerm = api.promptForInput(title, header);

        if(searchTerm == null || searchTerm.isEmpty())
        {
            return;
        }

        String normQuery = Normalizer.normalize(searchTerm, Normalizer.Form.NFKC);
        if(normQuery.isEmpty())
        {
            return;
        }

        String text = api.getText();
        int caret = Math.max(0, Math.min(api.getCaretPosition(), text.length()));

        int[] matchRange = findNormalizedMatch(text, caret, normQuery, api);
        if(matchRange != null)
        {
            api.selectText(matchRange[0], matchRange[1]);
        }
    }

    private int[] findNormalizedMatch(String text, int startPos, String normQuery, EditorAPI api)
    {
        List<Integer> boundaries = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getCharacterInstance(api.getLocale());
        iterator.setText(text.substring(startPos));

        int boundary = iterator.first();
        while(boundary != BreakIterator.DONE)
        {
            boundaries.add(startPos + boundary);
            boundary = iterator.next();
        }

        int numBoundaries = boundaries.size();
        for(int i = 0; i < numBoundaries - 1; i++)
        {
            int candidateStart = boundaries.get(i);
            for(int j = i + 1; j < numBoundaries; j++)
            {
                int candidateEnd = boundaries.get(j);
                String candidate = text.substring(candidateStart, candidateEnd);
                String normCandidate = Normalizer.normalize(candidate, Normalizer.Form.NFKC);

                if(normCandidate.equals(normQuery))
                {
                    return new int[] { candidateStart, candidateEnd };
                }

                if(normCandidate.length() >= normQuery.length()
                    && !normQuery.startsWith(normCandidate))
                {
                    break;
                }
            }
        }
        return null;
    }
}