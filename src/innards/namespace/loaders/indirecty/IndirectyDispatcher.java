package innards.namespace.loaders.indirecty;

import innards.math.linalg.*;

public interface IndirectyDispatcher
{
    public void handleNodeCount(int nodeCount);
    public void handleRootNode(String nodeName);
    public void handleNodeAndParent(String nodeName, String parentName);
    public void handleSampleCount(int sampleCount);
    public void handleTimeIndex(double timeIndex);
    public void handleRotationData(Quaternion rotation);
    public void handleTranslationData(Vec3 translation);
    public void handleNodeAndParentWithTranslation(String nodeName, String parentName);

    public IndirectyDispatcher construct();
}
