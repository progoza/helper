package io.github.progoza.helper;

import java.util.List;
import io.github.progoza.helper.model.Settings;


public class App {

    private static Settings settings;

    public static void main( String[] args )
    {
        boolean silentDefaults = List.of(args).stream().filter(x -> x.equals("-silent") || x.equals("-S")).findFirst().isPresent();
        if (silentDefaults) {
            System.out.println("Uzywam trybu silent - będę pobierał tylko konieczne dane - pozostałe pozostaną domyślne.");
        }
        settings = new Settings();

        AbstractApp app = null;
        if (args.length > 0) {
            switch (args[0]) {
                case "rentcalc":
                    app = new RentcalcApp(silentDefaults, settings);
                    break;
                default:
                    System.out.println( "Wybierz aplikację do uruchomienia jako pierwszy argument: rentcalc");
                    System.exit(1);
                    break;
            }
        }
        app.run();
    }
}