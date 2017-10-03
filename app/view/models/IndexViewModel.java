package view.models;

import api.entity.MizContent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class IndexViewModel {
    public IndexViewModel(List<Pair<String, MizContent>> mizes) {
        this.mizes = mizes;
    }

    public List<Pair<String, MizContent>> getMizes() {
        return mizes;
    }

    private List<Pair<String, MizContent>> mizes;
}
