package innards.namespace.loaders.indirecty;

import innards.math.linalg.Quaternion;
import innards.math.linalg.Vec3;

public class IndirectyMultiDispatcher implements IndirectyDispatcher
{
    private IndirectyDispatcher[] dispatchers = null;

    public IndirectyMultiDispatcher()
    {
    }
    public IndirectyMultiDispatcher(IndirectyDispatcher one, IndirectyDispatcher two)
    {
        dispatchers = new IndirectyDispatcher[]{one,two};
    }
    public IndirectyMultiDispatcher(IndirectyDispatcher[] d)
    {
        dispatchers = d;
    }

    public void setDispatchers(IndirectyDispatcher[] d)
    {
        dispatchers = d;
    }

    public void handleNodeCount(int nodeCount)
    {
        for (int i = 0; i < dispatchers.length; i++)
        {
            dispatchers[i].handleNodeCount(nodeCount);
        }
    }
    public void handleRootNode(String nodeName)
    {
        for (int i = 0; i < dispatchers.length; i++)
        {
            dispatchers[i].handleRootNode(nodeName);
        }
    }
    public void handleNodeAndParent(String nodeName, String parentName)
    {
        for (int i = 0; i < dispatchers.length; i++)
        {
            dispatchers[i].handleNodeAndParent(nodeName, parentName);
        }
    }
    public void handleSampleCount(int sampleCount)
    {
        for (int i = 0; i < dispatchers.length; i++)
        {
            dispatchers[i].handleSampleCount(sampleCount);
        }
    }
    public void handleTimeIndex(double timeIndex)
    {
        for (int i = 0; i < dispatchers.length; i++)
        {
            dispatchers[i].handleTimeIndex(timeIndex);
        }
    }
    public void handleRotationData(Quaternion rotation)
    {
        for (int i = 0; i < dispatchers.length; i++)
        {
            dispatchers[i].handleRotationData(rotation);
        }
    }
    public void handleTranslationData(Vec3 translation)
    {
        for (int i = 0; i < dispatchers.length; i++)
        {
            dispatchers[i].handleTranslationData(translation);
        }
    }
    public void handleNodeAndParentWithTranslation(String nodeName, String parentName)
    {
        for (int i = 0; i < dispatchers.length; i++)
        {
            dispatchers[i].handleNodeAndParentWithTranslation(nodeName, parentName);
        }
    }
    public IndirectyDispatcher construct()
    {
        return new IndirectyMultiDispatcher();
    }
}