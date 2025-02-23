package io.github.progoza.helper.rentcalc;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import io.github.progoza.helper.rentcalc.model.FixedCost;
import io.github.progoza.helper.rentcalc.model.Meter;
import io.github.progoza.helper.rentcalc.model.MeteredCost;
import io.github.progoza.helper.rentcalc.model.Statement;

public class MarkdownExport {

    private static String[] lines = 
    new String[] {
        "---\n" + //
        "header-includes:\n" + //
        "  - \\usepackage{tabularx}\n" +
        "  - \\usepackage{float}\n"+
        "  - \\newcolumntype{R}{>{\\raggedleft\\arraybackslash}X}\n" +
        "  - \\newcolumntype{S}{>{\\raggedleft\\arraybackslash}m{2.5cm}}\n" +   
        "  - \\newcolumntype{M}{>{\\raggedleft\\arraybackslash}m{5.5cm}}\n" +                          
        "fontsize: 12pt\n" + //
        "mainfont: /home/pawel/workdir/statements/pdf/DejaVuSerif.ttf\n" + //
        "sansfont: /home/pawel/workdir/statements/pdf/DejaVuSans.ttf\n" + //
        "monofont: /home/pawel/workdir/statements/pdf/DejaVuSansMono.ttf \n" + //
        "geometry: [top=2cm, bottom=2.5cm, left=1.8cm, right=1.8cm]\n" + //
        "---\n"+
        "# Rozliczenie kosztów opłat eksploatacyjnych dla mieszkania {address} \n" + 
        "\n" + 
        "Za okres {title}\n" +
        "\n" +
        "#### Koszty stałe\n  " +
        "\n" + //
        "- Opłaty stałe: {fixedCostPerMonth} zł/miesiąc x {monthsCount} = {fixedCostsAmount}\n" +
        "\n" + //
        "#### Koszty zmienne\n  " +
        "\n" +
        "- Wskazania liczników\n" + 
        "\n",
        
        "\\begin{table}[H]\n" + //
        "\\centering\n" + //
        "\\ttfamily\n" +
        "\\begin{tabularx}{\\textwidth}{|X|R|R|R|}\n" + //
        "\\hline\n" + //
        "\\multicolumn{1}{|c|}{\\textbf{Nazwa}} & \\multicolumn{1}{|c|}{\\textbf{{refStatementDate}}} & \\multicolumn{1}{|c|}{\\textbf{{statementDate}}} & \\multicolumn{1}{|c|}{\\textbf{Zużycie}} \\\\\n" + //
        "\\hline\n" ,

        "{meterName}   & {meterOldReading} & {meterNewReading} & {meterUsage}  \\\\\n" + //
        "\\hline\n",

        "\\end{tabularx}\n" + //
        "\\end{table}" +
        "\n" + //
        "-  Opłaty według wskazań liczników\n" + //
        "\n",

        "\\begin{table}[H]\n" + //
        "\\centering\n" + //
        "\\ttfamily\n" +        
        "\\begin{tabularx}{\\textwidth}{|R|M|}\n" + //
        "\\hline\n" + //
        "\\multicolumn{1}{|c|}{\\textbf{Opłata w/g zużycia}} & \\multicolumn{1}{|c|}{\\textbf{Koszt}} \\\\\n" + //
        "\\hline\n",

        "{meteredCostName}: {pricePerUnit} x {actualUsage} = & {actualCost}  \\\\\n" + //
        "\\hline\n",

        "\\end{tabularx}\n" + //
        "\\end{table}\n  \n"+
        "#### Podsumowanie okresu {title}\n" + //
        "\n" + //
        "\n- Zestawienie pobranych zaliczek i rzeczywistych kosztów" + //        
        "\n" + //
        "\\begin{table}[H]\n" + //
        "\\centering\n" + //
        "\\ttfamily\n" + //
        "\\begin{tabularx}{\\textwidth}{|R|M|}\n" + //
        "\\hline\n" + //
        "Pobrane zaliczki: {monthsCount} x {totalAnticipatedCostPerMonth} = & {totalAnticipatedCost} \\\\\n" + //
        "\\hline\n" + //
        "Rzeczywiste koszty: {fixedCostsValue} + {meteredActualCostsValue} = & {totalActualCosts} \\\\\n" + //
        "\\hline\n" + //
        "{diffName}: & {diff} \\\\\n" + //
        "\\hline\n" + //
        "\\end{tabularx}\n" + //
        "\\end{table}\n  \n" + //
        "##### Opłaty za okres {nextPeriodTitle}\n" + //
        " \n" + //
        " - Proszę odliczyć/doliczyć nadpłatę/niedopłatę do czynszu za {nextMonthName}.\n",

        " - {freeComment}\n",

        "\n" + //
        "\\begin{table}[H]\n" + //
        "\\centering\n" + //
        "\\ttfamily\n" + //
        "\\begin{tabularx}{\\textwidth}{|X|S|S|S|S|}\n" + //
        "\\hline\n" + //
        "\\multicolumn{1}{|c|}{\\textbf{Data}} & \\multicolumn{1}{|c|}{\\textbf{Czynsz}} & \\multicolumn{1}{|c|}{\\textbf{Opłaty}} & \\multicolumn{1}{|c|}{\\textbf{{diffName}}} & \\multicolumn{1}{|c|}{\\textbf{Razem}} \\\\\n" + //
        "\\hline\n",

        "{futurePaymentDate} & {rentAmount} & {futureTotalAnticipatedCosts} & {diff} & {futurePaymentAmount} \\\\\n" + //
        "\\hline\n",

        "\\end{tabularx}\n" + //
        "\\end{table}\n\n  " + //
        "\\newpage \n" + //
        "\n" + //
        "### Wykaz kosztów eksploatacyjnych obciążających Najemcę\n" + //
        "\n" + //
        "1. Opłaty eksploatacyjne stałe:\n" + //
        "\n",

        "\\begin{table}[H]\n" + //
        "\\centering\n" + //
        "\\ttfamily\n" + //
        "\\begin{tabularx}{\\textwidth}{|X|M|}\n" + //
        "\\hline\n" + //
        "\\multicolumn{1}{|c|}{\\textbf{Opłata stała}} & \\multicolumn{1}{|c|}{\\textbf{Koszt}} \\\\\n" + //
        "\\hline\n",

        "{futureFixedCostName} & {futureFixedCostAmount} zł / mc \\\\\n" + //
        "\\hline\n",

        "\\end{tabularx}\n" + //
        "\\end{table}\n  \n" + //
        "2. Opłaty eksploatacyjne w/g zużycia:\n" + //
        "\n",

        "\\begin{table}[H]\n" + //
        "\\centering\n" + //
        "\\ttfamily\n" + //
        "\\begin{tabularx}{\\textwidth}{|X|X|X|}\n" + //
        "\\hline\n" + //
        "\\multicolumn{3}{|c|}{\\textbf{Opłata w/g zużycia:}}  \\\\\n" + //
        "\\hline\n",

        "{meteredCostName} & opłata za 1 {meteredCostUnit}: &  {payPerUnit} zł / {meteredCostUnit} \\\\\n" + //
        "   &  planowane zużycie / mc zł & {anticipatedUsage} {meteredCostUnit} \\\\\n" + //
        "   &  planowany koszy / mc & {anticipatedCost} zł \\\\\n" + //
        "\\hline\n",

        "\\end{tabularx}\n" + //
        "\\end{table}\n  \n" + //
        "#### Suma opłat: {futureTotalFixCosts} + {futureTotalMeteredCosts} = {futureTotalCosts}\n"
    };

    public static void exportMarkdown(String dir, String filePrefix, Statement s) {
        if (filePrefix.isBlank()) {
            throw new IllegalStateException("Attempt to save statement without assigning the file prefix.");
        }
        if (dir.isBlank()) {
            throw new IllegalArgumentException("Attempt to save statement without providing directory");
        }
        if (dir.endsWith("/")) {
            dir = dir.substring(0, dir.length() - 1);
        }

        String fileName = dir + "/" + filePrefix + "-" + s.getCreationDate().getYear() + "-" + s.getCreationDate().getMonthValue() + ".md";    

        DecimalFormat df2 = new DecimalFormat("#0.00");
        DecimalFormat df3 = new DecimalFormat("#0.000");

        try (FileWriter writer = new FileWriter(fileName, false)) { // 'false' overwrites file
        
            String line0 = lines[0]
                .replace("{address}", s.getApartament().getAddress())
                .replace("{title}", s.getFrontPageTitle())
                .replace("{fixedCostPerMonth}", df2.format(s.getFixedCostsPerMonth()))
                .replace("{monthsCount}", "" + s.getCountOfMonthsCovered())
                .replace("{fixedCostsAmount}", df2.format(s.getFixedCostsAmount()));
            writer.write(line0);

            String line1;
            if (s.getMeteredCosts().isEmpty()) {
                line1 = "Brak zarejestrowanych liczników.";
            } else {
                line1 = lines[1]
                    .replace("{refStatementDate}", s.getLastStatementDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)))
                    .replace("{statementDate}", s.getCreationDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)));
            }
            writer.write(line1);

            for (Meter m : s.getMeters()) {
                String line2 = lines[2]
                    .replace("{meterName}", m.getName())
                    .replace("{meterOldReading}", df3.format(m.getOldMeterReading()))
                    .replace("{meterNewReading}", df3.format(m.getReading()))
                    .replace("{meterUsage}", df3.format(m.getUsage()));
                writer.write(line2);
            }

            String line3 = lines[3];
            writer.write(line3);

            String line4;
            if (s.getMeteredCosts().isEmpty()) {
                line4 = "Brak kosztów w/g zuycia.";
            } else {
                line4 = lines[4];
            }
            writer.write(line4);

            for (MeteredCost mc : s.getMeteredCosts()) {
                String line5 = lines[5]
                .replace("{meteredCostName}", mc.getName())
                .replace("{pricePerUnit}", df2.format(mc.getPayPerUnit()))
                .replace("{actualUsage}", df3.format(mc.getActualUsage()))
                .replace("{actualCost}", df2.format(mc.getActualCost()));
                writer.write(line5);
            }

            String line6 = lines[6]
                .replace("{title}", s.getFrontPageTitle())
                .replace("{monthsCount}", "" + s.getCountOfMonthsCovered())
                .replace("{totalAnticipatedCostPerMonth}", df2.format(s.getTotalAnticipatedCostsPerMonth()))
                .replace("{totalAnticipatedCost}", df2.format(s.getTotalAnticipatedCosts()))
                .replace("{fixedCostsValue}", df2.format(s.getFixedCostsAmount()))
                .replace("{meteredActualCostsValue}", df2.format(s.getActualMeteredCostsAmount()))
                .replace("{totalActualCosts}", df2.format(s.getTotalActualCosts()))
                .replace("{diffName}", s.getDiffDescription())
                .replace("{diff}", df2.format(s.getDiffToPayOrReturn()))
                .replace("{nextPeriodTitle}", s.getNextPeriodTitle())
                .replace("{nextMonthName}", s.getNextMonthName());
            writer.write(line6);

            for (String freeComment : s.getFreeComments()) {
                String line7 = lines[7]
                    .replace("{freeComment}", freeComment);
                writer.write(line7);
            }

            String line8 = lines[8].replace("{diffName}", s.getDiffDescription());
            writer.write(line8);

            for (int i=0; i<s.getFuturePayments().size(); i++) {
                String line9 = lines[9]
                    .replace("{futurePaymentDate}", s.getFuturePaymentsDeadline().get(i).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)))
                    .replace("{rentAmount}", df2.format(s.getApartament().getRentAmount()))
                    .replace("{futureTotalAnticipatedCosts}", df2.format(s.getFutureTotalCostsPerMonth()))
                    .replace("{futurePaymentAmount}", df2.format(s.getFuturePayments().get(i)))
                    .replace("{diff}", i == 0 ? df2.format(s.getDiffToPayOrReturn()) : " ") ;    
                writer.write(line9);
            }

            writer.write(lines[10]);   

            long futureFixedCostsCount = s.getFixedCosts().stream().filter(x -> !x.isRemoved()).count();
            String line11;
            if (futureFixedCostsCount == 0) {
                line11 = "Brak zdefiniowanych kosztów stałych.";
            } else {
                line11 = lines[11];
            }
            writer.write(line11);   

            for (FixedCost fc : s.getFixedCosts()) {
                if (fc.isRemoved()) {
                    continue;
                }
                String line12 = lines[12]
                        .replace("{futureFixedCostName}", fc.getDescription())
                        .replace("{futureFixedCostAmount}", df2.format(fc.getPayAmount()));
                writer.write(line12);
            }

            writer.write(lines[13]);

            long futureMeteredCostsCount = s.getMeteredCosts().stream().filter(x -> !x.isRemoved()).count();
            String line14;
            if (futureMeteredCostsCount == 0) {
                line14 = "Brak definiowanych kosztów w/g zuycia.";
            } else {
                line14 = lines[14];
            }
            writer.write(line14);

            for (MeteredCost meteredCost : s.getMeteredCosts()) {
                if (meteredCost.isRemoved()) {
                    continue;
                }
                String line15 = lines[15]
                    .replace("{meteredCostName}", meteredCost.getName())
                    .replace("{meteredCostUnit}", meteredCost.getUnitName())
                    .replace("{payPerUnit}", df2.format(meteredCost.getPayPerUnit()))
                    .replace("{anticipatedUsage}", df3.format(meteredCost.getAnticipatedUsage()))
                    .replace("{anticipatedCost}", df2.format(meteredCost.getAnticipatedCost()));
                writer.write(line15);
            }

            String line16 = lines[16]
                .replace("{futureTotalFixCosts}", s.getFutureFixedCostPerMonth().toString())
                .replace("{futureTotalMeteredCosts}", s.getFutureMeteredCostsPerMonth().toString())
                .replace("{futureTotalCosts}", s.getFutureTotalCostsPerMonth().toString())
                ;
            writer.write(line16);
            System.out.println("Plik markdown " + fileName + " zapisany pomyślnie.");
        } catch (IOException e) {
            throw new RuntimeException("Exception while exporting markdown file.", e);
        }

        try (FileWriter writer = new FileWriter("/tmp/last-rentcalc-file.txt", false)) {
            writer.write(fileName);
        } catch (IOException e) {
            throw new RuntimeException("Exception while exporting markdown file.", e);
        }
    }
}
