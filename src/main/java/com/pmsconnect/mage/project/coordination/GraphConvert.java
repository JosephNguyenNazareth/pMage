package com.pmsconnect.mage.project.coordination;

import com.pmsconnect.mage.utils.lock.GraphForPMage;

import java.util.List;

public interface GraphConvert {
    public void convert(List<String> processFragments);
    public GraphForPMage getGraph();
}
