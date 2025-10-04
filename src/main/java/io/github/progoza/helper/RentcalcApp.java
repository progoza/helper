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

public class RentcalcApp extends AbstractApp{

    public RentcalcApp(boolean silentDefaults, Settings settings) {
        super(silentDefaults, settings);
    }

    private FixedCost readFixedCost(Scanner s, long fixedCostId) {
        System.out.print("Podaj nazwę kosztu stałego (lub pozostaw puste jeśli juz nie chcesz więcej dodawać):\n> ");
        String fixedCostName = s.nextLine();
        if (fixedCostName.equals("")) {
            return null;
        }
        BigDecimal fixedCostValue = utils.readBigDecimal(s, "Podaj wysokość miesięczną koszty stałego%s:\n> ", null);

        FixedCost fixedCost = new FixedCost();
        fixedCost.setChanged(true);
        fixedCost.setDescription(fixedCostName);
        fixedCost.setId(fixedCostId);
        fixedCost.setAmount(fixedCostValue);
        return fixedCost;
    }

    private MeteredCost readMeteredCost(Scanner s, long meteredCostId) {
        System.out.print("Podaj nazwę kosztu zmeinnego (lub pozostaw puste jeśli juz nie chcesz więcej dodawać):\n> ");
        String meteredCostName = s.nextLine();
        if (meteredCostName.equals("")) {
            return null;
        }
        BigDecimal meteredCostPayPerUnit = utils.readBigDecimal(s, "Podaj wysokość ceny jednostki kosztu mierzonego%s:\n> ", null);
        BigDecimal meteredCostAnticipatedUsage = utils.readBigDecimal(s, "Podaj wysokość planowanego zuzycia miesięcznego%s:\n> ", null);
        System.out.print("Podaj nazwę jednostki:\n> ");
        String meteredCostUnitName = s.nextLine();

        MeteredCost meteredCost = new MeteredCost();
        meteredCost.setPayPerUnit(meteredCostPayPerUnit);
        meteredCost.setAnticipatedUsage(meteredCostAnticipatedUsage);
        meteredCost.setChanged(true);
        meteredCost.setId(meteredCostId);
        meteredCost.setDescription(meteredCostName);
        meteredCost.setUnitName(meteredCostUnitName);
        return meteredCost;
    }

    private String readOldStatementFileName(Scanner s, List<String> files) {
        if (files.isEmpty()) {
            System.out.println("Na razie nie odnaleziono zadnych rozliczeń, trzeba wprowadzić pierwsze rozliczenie.");
            return null;
        } else {
            System.out.println("W bazie znajdują się następujące rozliczenia:");
            for (int i=0; i<files.size(); i++) {
                System.out.println("  " + (i+1) + " - " + files.get(i));
            }
            int oldStatementId = utils.readInt(s, "Wybierz poprzednie (referencyjne) rozliczenie, lub 0 jeśli chcesz zrobić nowe%s:\n> ", 1);
            return files.get(oldStatementId - 1);
        }
    }

    private Apartament readApartment(Scanner s, Apartament oldApartament) {
        oldApartament = oldApartament == null ? new Apartament() : oldApartament;
        String apartementAddress = utils.readStringSilent(s, "Podaj adres mieszkania%s:\n> ", oldApartament.getAddress());
        BigDecimal apartmentRent = utils.readBigDecimalSilent(s, "Podaj wysokość czynszu%s:\n> ", oldApartament.getRentAmount());
        Apartament apartament = new Apartament();
        apartament.setAddress(apartementAddress);
        apartament.setRentAmount(apartmentRent);
        return apartament;
    }

    private LocalDate readCreationDate(Scanner s) {
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

    @Override
    public void run() {
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
            filePrefix = utils.readString(s, "Podaj prefix pliku dla nowego rozliczenia (np. nazwe ulicy, etc):\n> ", null);
            loaderSaver.setFilePrefix(filePrefix);
        } 

        Apartament apartament = readApartment(s, oldStatement.getApartament());
        LocalDate dateOfCreation = readCreationDate(s);

        Statement newStatement = calc.getNewStatement();   
        newStatement.setApartament(apartament);
        newStatement.setCreationDate(dateOfCreation);

        long fixedCostId = 1;
        boolean addNewFixedCosts = false;

        if (hasReferenceStatement) {
            System.out.println("Lista kosztów stałych z referencyjnego rozliczenia:");
            for (FixedCost oldFixedCost : oldStatement.getFixedCosts()) {
                if (oldFixedCost.isRemoved()) {
                    continue;
                }
                System.out.println("  " + oldFixedCost.getId() + " - " + oldFixedCost.getDescription() + " : " + oldFixedCost.getAmount());
                String option = utils.readStringSilent(s, "  Czy chcesz je (Z)mienic, (U)sunąć czy (N)ie zmieniać%s?\n> ", "N");
                if (option.equalsIgnoreCase("Z")) {
                    FixedCost fixedCost = readFixedCost(s, oldFixedCost.getId());
                    newStatement.getFixedCosts().add(fixedCost);
                } else {
                    FixedCost fixedCost = new FixedCost();
                    fixedCost.setId(oldFixedCost.getId());
                    fixedCost.setDescription(oldFixedCost.getDescription());
                    fixedCost.setAmount(oldFixedCost.getAmount());
                    if (option.equalsIgnoreCase("U")) {
                        fixedCost.setRemoved(true);
                    }
                    newStatement.getFixedCosts().add(fixedCost);
                }
            }
            fixedCostId = oldStatement.getFixedCosts().stream().mapToLong(x -> x.getId()).max().orElse(0L) + 1;
            String option2 = utils.readStringSilent(s, "  Czy chcesz chcesz dodać nowe koszty stałę (T)/(N)%s?\n> ", "N");
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
        if (hasReferenceStatement) {
            System.out.println("Lista kosztów licznikowych z referencyjnego rozliczenia:");
            for (MeteredCost oldMeteredCost : oldStatement.getMeteredCosts()) {
                if (oldMeteredCost.isRemoved()) {
                    continue;
                }
                System.out.println("  " + oldMeteredCost.getId() + " - " + oldMeteredCost.getDescription() + " : " + oldMeteredCost.getPayPerUnit() + "/" + oldMeteredCost.getUnitName());
                String option = utils.readStringSilent(s, "  Czy chcesz je (Z)mienic, (U)sunąć czy (N)ie zmieniać%s?\n> ", "N");
                if (option.equalsIgnoreCase("Z")) {
                    MeteredCost meteredCost = readMeteredCost(s, oldMeteredCost.getId());
                    newStatement.getMeteredCosts().add(meteredCost);
                } else {
                    MeteredCost meteredCost = new MeteredCost();
                    meteredCost.setId(oldMeteredCost.getId());
                    meteredCost.setDescription(oldMeteredCost.getDescription());
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
            String option2 = utils.readStringSilent(s, "  Czy chcesz chcesz dodać nowe koszty licznikowe (T)/(N)%s?\n> ", "N");
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
                BigDecimal newReading = utils.readBigDecimal(s, 
                                           "  " + oldMeter.getId() + " - " + oldMeter.getName() + "(poprzedni odczyt: " + oldMeter.getReading() + ")\n> ",
                                           oldMeter.getUsage());
                Meter meter = new Meter();
                String option = utils.readStringSilent(s, "Oznaczyć ten licznik jako usunięty? (T)/(N)%s?\n> ", "N");
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
            String option2 = utils.readStringSilent(s, "  Czy chcesz chcesz dodać nowe liczniki (T)/(N)%s?\n> ", "N");
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

            BigDecimal meteredReading = utils.readBigDecimal(s, "Podaj odczyt licznika%s:\n> ", null);
            System.out.println("Zdefiniuj jakie zmienne koszty mierzy licznik (podaj ID oddzielone przecnikiem):");
            for (MeteredCost mc : newStatement.getMeteredCosts()) {
                System.out.println("  " + mc.getId() + " - " + mc.getDescription());
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

        BigDecimal dueAmount = utils.readBigDecimalSilent(s, "Podaj kwote zaległej płatności (lub pozostaw puste, jeśli nie ma zaległości):\n> ", 
                                            BigDecimal.ZERO);
        newStatement.setDueAmount(dueAmount);
  
        if (hasReferenceStatement) {
            int numberOfFuturePayments = utils.readIntSilent(s, "Podaj na ile miesięcy do produ zrobić rozliczenie%s:\n> ", 3);
            newStatement.setNumberOfFuturePayments(numberOfFuturePayments);
            
            while (true) {
                String freeComment = utils.readStringSilent(s, "Podaj wolny komentarz do rozliczenia (pusty aby pominąć):\n> ", null);                    
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
}
