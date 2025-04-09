package com.pmsconnect.mage.kie;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class KieServer {
    private KieServices ks;
    private KieContainer kContainer;
    private KieSession kieSession;

    public KieServer(String ruleFile) {
        this.ks = KieServices.Factory.get();
        KieFileSystem kfs = this.ks.newKieFileSystem();
        kfs.write(this.ks.getResources().newClassPathResource("rules/" + ruleFile));

        KieBuilder kBuilder = this.ks.newKieBuilder(kfs);
        kBuilder.buildAll();

        Results results = kBuilder.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Error building rules: " + results.getMessages());
        }

        this.kContainer = ks.newKieContainer(this.ks.getRepository().getDefaultReleaseId());
    }

    public void startNewSession() {
        this.kieSession = this.kContainer.newKieSession();

        if (this.kieSession == null) {
            throw new RuntimeException("KieSession is null after programmatic compilation");
        }
    }

    public KieSession getKieSession() {
        return kieSession;
    }
}
