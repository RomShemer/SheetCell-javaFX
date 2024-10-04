package controller.skinStyle;

import java.util.ArrayList;
import java.util.List;

public class SkinManager {
    private static SkinOption currentSkin = SkinOption.DEFAULT;
    private final List<SkinChangeListener> listeners = new ArrayList<>();

    public SkinManager() {
        this.currentSkin = SkinOption.DEFAULT;
    }

    public SkinManager(SkinOption skinOption) {
        this.currentSkin = skinOption;
    }

    public static SkinOption getCurrentSkinMode(){
        return currentSkin;
    }

    public void addSkinChangeListener(SkinChangeListener listener) {
        listeners.add(listener);
    }

    public void changeSkin(SkinOption newSkin) {
        if (newSkin != currentSkin) {
            this.currentSkin = newSkin;
            notifyListeners(newSkin);
        }
    }

    private void notifyListeners(SkinOption newSkin) {
        for (SkinChangeListener listener : listeners) {
            listener.onSkinChange(newSkin);
        }
    }

    public String getCurrentSkinName() {
        return currentSkin.getModeName();
    }
}
