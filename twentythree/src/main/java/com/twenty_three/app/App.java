package com.twenty_three.app;

import java.io.IOException;

import com.twenty_three.app.Indexers.FBISIndex;
import com.twenty_three.app.Indexers.FTIndex;
import com.twenty_three.app.Indexers.Fr94Index;
import com.twenty_three.app.Indexers.LATimesIndex;
import com.twenty_three.app.Searcher.MultiDocSearcher;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        try {
            new FBISIndex().createFBISIndex(Constants.corpus_fbis);
            new Fr94Index().createFRIndex(Constants.corpus_fr94);
            new FTIndex().createFTIndex(Constants.corpus_ft);
            new LATimesIndex().createLAIndex(Constants.corpus_latimes);

            MultiDocSearcher.query();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
