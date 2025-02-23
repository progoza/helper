package io.github.progoza.helper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import io.github.progoza.helper.model.Settings;
import io.github.progoza.helper.rentcalc.Calculator;
import io.github.progoza.helper.rentcalc.LoadSave;
import io.github.progoza.helper.rentcalc.MarkdownExport;
import io.github.progoza.helper.rentcalc.model.Apartament;
import io.github.progoza.helper.rentcalc.model.FixedCost;
import io.github.progoza.helper.rentcalc.model.Meter;
import io.github.progoza.helper.rentcalc.model.MeteredCost;
import io.github.progoza.helper.rentcalc.model.Statement;

public class App {

    private static FixedCost readFixedCost(Scanner s, long fixedCostId) {
        System.out.print("Podaj nazwę kosztu stałego (lub pozostaw puste jeśli juz nie chcesz więcej dodawać):\n> ");
        String fixedCostName = s.nextLine();
        if (fixedCostName.equals("")) {
            return null;
        }
        BigDecimal fixedCostValue = readBigDecimal(s, "Podaj wysokość miesięczną koszty stałego%s:\n> ", null);

        FixedCost fixedCost = new FixedCost();
        fixedCost.setChanged(true);
        fixedCost.setDescription(fixedCostName);
        fixedCost.setId(fixedCostId);
        fixedCost.setPayAmount(fixedCostValue);
        return fixedCost;
    }

    private static MeteredCost readMeteredCost(Scanner s, long meteredCostId) {
        System.out.print("Podaj nazwę kosztu zmeinnego (lub pozostaw puste jeśli juz nie chcesz więcej dodawać):\n> ");
        String meteredCostName = s.nextLine();
        if (meteredCostName.equals("")) {
            return null;
        }
        BigDecimal meteredCostPayPerUnit = readBigDecimal(s, "Podaj wysokość ceny jednostki kosztu mierzonego%s:\n> ", null);
        BigDecimal meteredCostAnticipatedUsage = readBigDecimal(s, "Podaj wysokość planowanego zuzycia miesięcznego%s:\n> ", null);
        System.out.print("Podaj nazwę jednostki:\n> ");
        String meteredCostUnitName = s.nextLine();

        MeteredCost meteredCost = new MeteredCost();
        meteredCost.setPayPerUnit(meteredCostPayPerUnit);
        meteredCost.setAnticipatedUsage(meteredCostAnticipatedUsage);
        meteredCost.setChanged(true);
        meteredCost.setId(meteredCostId++);
        meteredCost.setName(meteredCostName);
        meteredCost.setUnitName(meteredCostUnitName);
        return meteredCost;
    }

    private static String readOldStatementFileName(Scanner s, List<String> files) {
        if (files.isEmpty()) {
            System.out.println("Na razie nie odnaleziono zadnych rozliczeń, trzeba wprowadzić pierwsze rozliczenie.");
            return null;
        } else {
            System.out.println("W bazie znajdują się następujące rozliczenia:");
            for (int i=0; i<files.size(); i++) {
                System.out.println("  " + (i+1) + " - " + files.get(i));
            }
            int oldStatementId = readInt(s, "Wybierz poprzednie (referencyjne) rozliczenie, lub 0 jeśli chcesz zrobić nowe%s:\n> ", 1);    
            return files.get(oldStatementId - 1);
        }
    }

    private static Apartament readApartment(Scanner s, Apartament oldApartament) {
        oldApartament = oldApartament == null ? new Apartament() : oldApartament;
        String apartementAddress = readStringSilent(s, "Podaj adres mieszkania%s:\n> ", oldApartament.getAddress());
        BigDecimal apartmentRent = readBigDecimalSilent(s, "Podaj wysokość czynszu%s:\n> ", oldApartament.getRentAmount());
        Apartament apartament = new Apartament();
        apartament.setAddress(apartementAddress);
        apartament.setRentAmount(apartmentRent);
        return apartament;
    }

    private static LocalDate readCreationDate(Scanner s) {
        System.out.print( "Podaj datę stworzenia rozliczenia w formacie YYYY-MM-DD (domyślnie - bieżąca data)):\n> ");
        String dateOfCreationStr = s.nextLine();
        LocalDate dateOfcreation = LocalDate.now();
        if (!dateOfCreationStr.isBlank()) {
            String[] dateOfCreatiponArray = dateOfCreationStr.split("-");
            dateOfcreation = LocalDate.of(
                Integer.parseInt(dateOfCreatiponArray[0]), 
                Integer.parseInt(dateOfCreatiponArray[1]), 
                Integer.parseInt(dateOfCreatiponArray[2]));
        }
        return dateOfcreation;
    }

    private static boolean silentDefaults = false;
    public static void main( String[] args )
    {
        System.out.println( "Na razie wspieram tylko rentcalc!");
        
        silentDefaults = List.of(args).stream().filter(x -> x.equals("-silentDefaults") || x.equals("-S")).findFirst().isPresent();
        if (silentDefaults) {
            System.out.println("Uzywam trybu silent - będę pobierał tylko konieczne dane - pozostałe pozostaną domyślne.");
        }
        Settings settings = new Settings();
        Scanner s = new Scanner(System.in);
        Calculator calc = new Calculator();
        LoadSave loaderSaver = new LoadSave();
        boolean hasReferenceStatement = false;
        Statement oldStatement = new Statement();
        String filePrefix = null;

        List<String> files = loaderSaver.listStaments(settings.getStatementDir());
        String oldStatementFileName = readOldStatementFileName(s, files);

        if (oldStatementFileName != null && !oldStatementFileName.isBlank())  {
            oldStatement = loaderSaver.loadStatement(settings.getStatementDir(), oldStatementFileName);
            filePrefix = oldStatementFileName.split("-")[0];
            hasReferenceStatement = true;
        }

        if (!hasReferenceStatement) {
            filePrefix = readString(s, "Podaj prefix pliku dla nowego rozliczenia (np. nazwe ulicy, etc):\n> ", null);
            loaderSaver.setFilePrefix(filePrefix);
        } 

        Apartament apartament = readApartment(s, oldStatement.getApartament());
        LocalDate dateOfCreation = readCreationDate(s);

        Statement newStatement = calc.getNewStatement();   
        newStatement.setApartament(apartament);
        newStatement.setCreationDate(dateOfCreation);

        long fixedCostId = 1;
        boolean addNewFixedCosts = false;

        if (hasReferenceStatement && !silentDefaults) {
            System.out.println("Lista kosztów stałych z referencyjnego rozliczenia:");
            for (FixedCost oldFixedCost : oldStatement.getFixedCosts()) {
                if (oldFixedCost.isRemoved()) {
                    continue;
                }
                System.out.println("  " + oldFixedCost.getId() + " - " + oldFixedCost.getDescription() + " : " + oldFixedCost.getPayAmount());
                String option = readString(s, "  Czy chcesz je (Z)mienic, (U)sunąć czy (N)ie zmieniać%s?\n> ", "N");
                if (option.equalsIgnoreCase("Z")) {
                    FixedCost fixedCost = readFixedCost(s, oldFixedCost.getId());
                    newStatement.getFixedCosts().add(fixedCost);
                } else {
                    FixedCost fixedCost = new FixedCost();
                    fixedCost.setId(oldFixedCost.getId());
                    fixedCost.setDescription(oldFixedCost.getDescription());
                    fixedCost.setPayAmount(oldFixedCost.getPayAmount());
                    if (option.equalsIgnoreCase("U")) {
                        fixedCost.setRemoved(true);
                    }
                    newStatement.getFixedCosts().add(fixedCost);
                }
            }
            fixedCostId = oldStatement.getFixedCosts().stream().mapToLong(x -> x.getId()).max().orElse(0L) + 1;
            String option2 = readString(s, "  Czy chcesz chcesz dodać nowe koszty stałę (T)/(N)%s?\n> ", "N");
            if (option2.equalsIgnoreCase("Y")) {
                addNewFixedCosts = true;
            }
        }

        while (addNewFixedCosts || !hasReferenceStatement) {
            FixedCost fixedCost = readFixedCost(s, fixedCostId++);
            if (fixedCost == null) {
                break;
            }
            newStatement.getFixedCosts().add(fixedCost);
        }

        long meteredCostId = 1;
        boolean addNewMeteredCosts = false;
        if (hasReferenceStatement && !silentDefaults) {
            System.out.println("Lista kosztów licznikowych z referencyjnego rozliczenia:");
            for (MeteredCost oldMeteredCost : oldStatement.getMeteredCosts()) {
                if (oldMeteredCost.isRemoved()) {
                    continue;
                }
                System.out.println("  " + oldMeteredCost.getId() + " - " + oldMeteredCost.getName() + " : " + oldMeteredCost.getPayPerUnit() + "/" + oldMeteredCost.getUnitName());
                String option = readString(s, "  Czy chcesz je (Z)mienic, (U)sunąć czy (N)ie zmieniać%s?\n> ", "N");
                if (option.equalsIgnoreCase("Z")) {
                    MeteredCost meteredCost = readMeteredCost(s, oldMeteredCost.getId());
                    newStatement.getMeteredCosts().add(meteredCost);
                } else {
                    MeteredCost meteredCost = new MeteredCost();
                    meteredCost.setId(oldMeteredCost.getId());
                    meteredCost.setName(oldMeteredCost.getName());
                    meteredCost.setPayPerUnit(oldMeteredCost.getPayPerUnit());
                    meteredCost.setAnticipatedUsage(oldMeteredCost.getAnticipatedUsage());
                    meteredCost.setUnitName(oldMeteredCost.getUnitName());
                    if (option.equalsIgnoreCase("U")) {
                        meteredCost.setRemoved(true);                                                
                    } 
                    newStatement.getMeteredCosts().add(meteredCost);
                }
            }
            meteredCostId = oldStatement.getMeteredCosts().stream().mapToLong(x -> x.getId()).max().orElse(0L) + 1;
            String option2 = readString(s, "  Czy chcesz chcesz dodać nowe koszty licznikowe (T)/(N)%s?\n> ", "N");
            if (option2.equalsIgnoreCase("Y")) {
                addNewMeteredCosts = true;
            }
        }        
        while (addNewMeteredCosts || !hasReferenceStatement) {
            MeteredCost meteredCost = readMeteredCost(s, meteredCostId++);
            if (meteredCost == null) {
                break;
            }
            newStatement.getMeteredCosts().add(meteredCost);
        }

        long meterId = 1;
        boolean addNewMeters = false;
        if (hasReferenceStatement) {
            System.out.println("Podaj odczyty liczników zdefiniowanych w poprzednim rozliczeniu.");
            for (Meter oldMeter : oldStatement.getMeters()) {
                if (oldMeter.isRemoved()) {
                    continue;
                }
                BigDecimal newReading = readBigDecimal(s, 
                                           "  " + oldMeter.getId() + " - " + oldMeter.getName() + "(poprzedni odczyt: " + oldMeter.getReading() + ")\n> ",
                                           oldMeter.getUsage());
                Meter meter = new Meter();
                String option = readStringSilent(s, "Oznaczyć ten licznik jako usunięty? (T)/(N)%s?\n> ", "N");
                if (option.equalsIgnoreCase("Y")) {
                    meter.setRemoved(true);
                }
                meter.setId(oldMeter.getId());
                meter.setMeteredCosts(new ArrayList<>());
                for (long l : oldMeter.getMeteredCosts()) { 
                    meter.getMeteredCosts().add(l);
                }
                meter.setName(oldMeter.getName());
                meter.setReading(newReading);
                newStatement.getMeters().add(meter);
            }
            meterId = oldStatement.getMeters().stream().mapToLong(x -> x.getId()).max().orElse(0L) + 1;
            String option2 = readStringSilent(s, "  Czy chcesz chcesz dodać nowe liczniki (T)/(N)%s?\n> ", "N");
            if (option2.equalsIgnoreCase("Y")) {
                addNewMeters = true;
            }
        }
        while (addNewMeters || !hasReferenceStatement) {
            System.out.print("Podaj nazwę licznika (lub pozostaw puste jeśli juz nie chcesz więcej dodawać):\n> ");
            String meterName = s.nextLine();
            if (meterName.equals("")) {
                break;
            }

            BigDecimal meteredReading = readBigDecimal(s, "Podaj odczyt licznika%s:\n> ", null);
            System.out.println("Zdefiniuj jakie zmienne koszty mierzy licznik (podaj ID oddzielone przecnikiem):");
            for (MeteredCost mc : newStatement.getMeteredCosts()) {
                System.out.println("  " + mc.getId() + " - " + mc.getName());
            }
            System.out.print("> ");
            String meterPaymentsIdsStr = s.nextLine();
            String[] meterPaymentsIdArr = meterPaymentsIdsStr.split(",");
            Meter meter = new Meter();
            meter.setName(meterName);
            meter.setReading(meteredReading);
            meter.setId(meterId++);
            meter.setMeteredCosts(new ArrayList<>());
            for (String paymentIdStr : meterPaymentsIdArr) {
                long paymentId = Long.parseLong(paymentIdStr.trim());
                Optional<MeteredCost> mc = newStatement.getMeteredCosts().stream().filter(x -> x.getId() == paymentId).findFirst();
                if (mc.isPresent()) {
                    meter.getMeteredCosts().add(paymentId);
                }
            }
            newStatement.getMeters().add(meter);
        }

        BigDecimal dueAmount = readBigDecimalSilent(s, "Podaj kwote zaległej płatności (lub pozostaw puste, jeśli nie ma zaległości):\n> ", 
                                            BigDecimal.ZERO);
        newStatement.setDueAmount(dueAmount);
  
        if (hasReferenceStatement) {
            int numberOfFuturePayments = readIntSilent(s, "Podaj na ile miesięcy do produ zrobić rozliczenie%s:\n> ", 3);
            newStatement.setNumberOfFuturePayments(numberOfFuturePayments);
            
            while (true) {
                String freeComment = readStringSilent(s, "Podaj wolny komentarz do rozliczenia (pusty aby pominąć):\n> ", null);                    
                if (freeComment != null && !freeComment.isBlank()) {
                    newStatement.getFreeComments().add(freeComment);
                } else {
                    break;
                }
            }
        }
        s.close();

        if (hasReferenceStatement) {
            calc.setOldStatement(oldStatement);
            calc.calculate();
            MarkdownExport.exportMarkdown(settings.getMdDir(), filePrefix, newStatement);
        }
        loaderSaver.saveStatement(newStatement, settings.getStatementDir());
    }

    public static BigDecimal readBigDecimal(Scanner s, String prompt, BigDecimal def, boolean silentDefault) {
        if (silentDefault && App.silentDefaults) {
            return def;
        }
        System.out.print( String.format(prompt, def != null ? " (domyśln.: " + def + ")" : ""));
        String strVal = s.nextLine();
        if (strVal.isBlank()) {
            return def;
        }
        return new BigDecimal(strVal);
    }

    public static BigDecimal readBigDecimal(Scanner s, String prompt, BigDecimal def) {
        return readBigDecimal(s, prompt, def, false);
    }

    public static BigDecimal readBigDecimalSilent(Scanner s, String prompt, BigDecimal def) {
        return readBigDecimal(s, prompt, def, true);
    }

    public static long readLong(Scanner s, String prompt, Long def, boolean silentDefault) {
        if (silentDefault && App.silentDefaults) {
            return def;
        }
        System.out.print( String.format(prompt, def != null ? " (domyśln.: " + def + ")" : ""));
        String strVal = s.nextLine();
        if (strVal.isBlank()) {
            return def == null ? 0L : def;
        }
        return Long.parseLong(strVal);
    }

    public static long readLong(Scanner s, String prompt, Long def) {
        return readLong(s, prompt, def, false);
    }

    public static long readLongSilent(Scanner s, String prompt, Long def) {
        return readLong(s, prompt, def, true);
    }

    public static int readInt(Scanner s, String prompt, Integer def, boolean silentDefault) {
        if (silentDefault && App.silentDefaults) {
            return def == null ? 0 : def;
        }
        System.out.print( String.format(prompt, def != null ? " (domyśln.: " + def + ")" : ""));
        String strVal = s.nextLine();
        if (strVal.isBlank()) {
            return def == null ? 0 : def;
        }
        return Integer.parseInt(strVal);
    }

    public static int readInt(Scanner s, String prompt, Integer def) {
        return readInt(s, prompt, def, false);
    }

    public static int readIntSilent(Scanner s, String prompt, Integer def) {
        return readInt(s, prompt, def, true);
    }

    public static String readString(Scanner s, String prompt, String def, boolean silentDefault) {
        if (silentDefault && App.silentDefaults) {
            return def;
        }
        System.out.print( String.format(prompt, def != null ? " (domyśln.: " + def + ")" : ""));
        String strVal = s.nextLine();
        if (strVal.isBlank()) {
            return def;
        }
        return strVal;
    }

    public static String readString(Scanner s, String prompt, String def) {
        return readString(s, prompt, def, false);
    }

    public static String readStringSilent(Scanner s, String prompt, String def) {
        return readString(s, prompt, def, true);
    }

}