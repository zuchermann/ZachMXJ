package innards.namespace.loaders.indirecty;

import innards.math.linalg.*;
import innards.util.*;

public class IndirectyParser extends SimpleParser
{
    private IndirectyDispatcher dispatcher;

    public IndirectyParser(String dataFileName, IndirectyDispatcher d)
    {
        super(dataFileName);
        dispatcher = d;
    }

    public IndirectyParser(String fileNameOrString, IndirectyDispatcher d, boolean bUseAStringBuffer)
    {
        super(fileNameOrString, bUseAStringBuffer);
        dispatcher = d;
    }


    public void parse() throws Parser.ParseException
    {
        int nodeCount = (int) getNumber();
        dispatcher.handleNodeCount(nodeCount);
        for (int i=0; i<nodeCount; i++)
        {
            parseNode();
        }

        requireEndOfFileNow();
    }

    private void parseNode() throws Parser.ParseException
    {
        boolean isRootNode = false;

        String firstWord = getWord();
       // System.out.println("\n \n this is the first word: " + firstWord);
        if (firstWord.equals("<ROOT_NODE>"))
        {
            isRootNode = true;
            dispatcher.handleRootNode(getWord());
        }
        else
        {
            String secondWord = getWord();
            if (secondWord.equals("<IS_CHILD_OF>"))
            {
                dispatcher.handleNodeAndParent(firstWord, getWord());
            }
            else if (secondWord.equals("<IS_CHILD_OF_WITH_TRANSLATION>"))
            {
                dispatcher.handleNodeAndParentWithTranslation(firstWord, getWord());
                isRootNode = true;
            }
            else
            {
                throw new Parser.ParseException("At line " + tokenizer.lineno() +
                                                ", the parser expected <IS_CHILD_OF> but read: " + tokenizer.toString());
            }
        }

        int sampleCount = (int) getNumber();
        dispatcher.handleSampleCount(sampleCount);
        for (int i=0; i<sampleCount; i++)
        {
            dispatcher.handleTimeIndex(getNumber());

            double w = getNumber();
            double x = getNumber();
            double y = getNumber();
            double z = getNumber();
            dispatcher.handleRotationData(new Quaternion(w,x,y,z));

            if (isRootNode == true)
            {
                x = getNumber();
                y = getNumber();
                z = getNumber();
                dispatcher.handleTranslationData(new Vec3(x,y,z));
            }
        }
    }
}
