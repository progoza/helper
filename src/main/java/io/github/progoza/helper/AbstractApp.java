package io.github.progoza.helper;

import io.github.progoza.helper.model.Settings;

public abstract class AbstractApp {

    protected final Utilities utils;
    protected final Settings settings;

    public AbstractApp(boolean silentDefaults, Settings settings) {
        this.utils = new Utilities(silentDefaults);
        this.settings = settings;
    }

    public abstract void run();
}
