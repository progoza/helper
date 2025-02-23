package io.github.progoza.helper.model;

public class Settings {

    public String getStatementDir() {
        return System.getenv("HOME") + "/workdir/statements";
    }

    public String getPdfDir() {
        return System.getenv("HOME") + "/workdir/statements/pdf";
    }
}
