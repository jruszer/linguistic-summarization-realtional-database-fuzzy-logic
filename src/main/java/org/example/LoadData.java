package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LoadData {

    private Set<Match> allRecords;
    private Map<String, LinguisticVariable<Double>> allLVs;
    private List<Quantifier> relativeQuantifiers;
    private List<Quantifier> absoluteQuantifiers;

    public LoadData(String csvFilePath) {
        allRecords = new HashSet<>();
        allLVs = new HashMap<>();
        relativeQuantifiers = new ArrayList<>();
        absoluteQuantifiers = new ArrayList<>();

        loadCsvData(csvFilePath);
        defineLinguisticVariables();
        defineQuantifiers();
    }

    private void loadCsvData(String csvFilePath) {
        List<String> numericColumns = Arrays.asList("winner_ht", "winner_age", "loser_ht", "loser_age", "minutes", "winner_rank", "loser_rank", "aces", "df", "games");

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            String[] headers = br.readLine().split(",");

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1);
                Match record = new Match();
                for (int i = 0; i < headers.length; i++) {
                    String header = headers[i].trim();
                    String value = values[i].trim();

                    if (numericColumns.contains(header)) {
                        try {
                            if (!value.isEmpty()) {
                                record.addNumericValue(header, Double.parseDouble(value));
                            } else {
                                record.addNumericValue(header, null);
                            }
                        } catch (NumberFormatException e) {
                            record.addNumericValue(header, null);
                        }
                    } else {
                        record.addStringValue(header, value);
                    }
                }
                allRecords.add(record);
            }
        } catch (IOException e) {
            System.err.println("Błąd podczas wczytywania pliku CSV: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Nie można wczytać danych CSV", e);
        }
        System.out.println("Wczytano " + allRecords.size() + " rekordów z pliku CSV.");
    }

    private void defineLinguisticVariables() {
        double minUniverseHeight = 160.0;
        double maxUniverseHeight = 215.0;
        UniverseOfDiscourse<Double> heightUniverse = new UniverseOfDiscourse<>(minUniverseHeight, maxUniverseHeight);

        MembershipFunction mfOkolo170cm = new TrapezoidalFun(165.0, 165.0, 170.0, 180.0);
        //MembershipFunction mfOkolo180cm = new TriangularFun(170.0, 180.0, 190.0);
        MembershipFunction mfOkolo180cm = new GaussFun(180, 3);
        //MembershipFunction mfOkolo190cm = new TrapezoidalFun(180.0, 190.0, 199.0, 200.0);
        MembershipFunction mfOkolo190cm = new TriangularFun(180.0, 190.0, 200.0);
        MembershipFunction mfPowyzej2m = new TrapezoidalFun(190.0, 200.0, maxUniverseHeight, maxUniverseHeight);

        LinguisticVariable<Double> winnerHeightLV = new LinguisticVariable<>("wzrost zwycięzcy", heightUniverse);
        winnerHeightLV.addLabel("około 170 cm", new FuzzySet<>(heightUniverse, mfOkolo170cm));
        winnerHeightLV.addLabel("około 180 cm", new FuzzySet<>(heightUniverse, mfOkolo180cm));
        winnerHeightLV.addLabel("około 190 cm", new FuzzySet<>(heightUniverse, mfOkolo190cm));
        winnerHeightLV.addLabel("powyżej 2 m", new FuzzySet<>(heightUniverse, mfPowyzej2m));
        allLVs.put(winnerHeightLV.getName(), winnerHeightLV);

        LinguisticVariable<Double> loserHeightLV = new LinguisticVariable<>("wzrost przegranego", heightUniverse);
        loserHeightLV.addLabel("około 170 cm", new FuzzySet<>(heightUniverse, mfOkolo170cm));
        loserHeightLV.addLabel("około 180 cm", new FuzzySet<>(heightUniverse, mfOkolo180cm));
        loserHeightLV.addLabel("około 190 cm", new FuzzySet<>(heightUniverse, mfOkolo190cm));
        loserHeightLV.addLabel("powyżej 2 m", new FuzzySet<>(heightUniverse, mfPowyzej2m));
        allLVs.put(loserHeightLV.getName(), loserHeightLV);


        double minUniverseAge = 14.0;
        double maxUniverseAge = 45.0;
        UniverseOfDiscourse<Double> ageUniverse = new UniverseOfDiscourse<>(minUniverseAge, maxUniverseAge);

        MembershipFunction mfMniejNiz18 = new TrapezoidalFun(minUniverseAge, minUniverseAge, 17.0, 20.0);
        MembershipFunction mfOkolo20 = new TrapezoidalFun(17.0, 20.0, 24.0, 25.0);
        MembershipFunction mfOkolo30 = new TrapezoidalFun(24.0, 25.0, 34.0, 35.0);
        MembershipFunction mfPowyzej35 = new TrapezoidalFun(34.0, 35.0, maxUniverseAge, maxUniverseAge);

        LinguisticVariable<Double> winnerAgeLV = new LinguisticVariable<>("wiek zwycięzcy", ageUniverse);
        winnerAgeLV.addLabel("mniej niż 18", new FuzzySet<>(ageUniverse, mfMniejNiz18));
        winnerAgeLV.addLabel("około 20", new FuzzySet<>(ageUniverse, mfOkolo20));
        winnerAgeLV.addLabel("około 30", new FuzzySet<>(ageUniverse, mfOkolo30));
        winnerAgeLV.addLabel("powyżej 35", new FuzzySet<>(ageUniverse, mfPowyzej35));
        allLVs.put(winnerAgeLV.getName(), winnerAgeLV);

        LinguisticVariable<Double> loserAgeLV = new LinguisticVariable<>("wiek przegranego", ageUniverse);
        loserAgeLV.addLabel("mniej niż 18", new FuzzySet<>(ageUniverse, mfMniejNiz18));
        loserAgeLV.addLabel("około 20", new FuzzySet<>(ageUniverse, mfOkolo20));
        loserAgeLV.addLabel("około 30", new FuzzySet<>(ageUniverse, mfOkolo30));
        loserAgeLV.addLabel("powyżej 35", new FuzzySet<>(ageUniverse, mfPowyzej35));
        allLVs.put(loserAgeLV.getName(), loserAgeLV);

        UniverseOfDiscourse<Double> durationUniverse = new UniverseOfDiscourse<>(0.0, 420.0);
        LinguisticVariable<Double> durationLV = new LinguisticVariable<>("czas trwania meczu", durationUniverse);

        durationLV.addLabel("krótszy niż 40 min", new FuzzySet<>(durationUniverse, new TrapezoidalFun(0.0, 0.0, 30.0, 40.0)));
        durationLV.addLabel("około godziny", new FuzzySet<>(durationUniverse, new TrapezoidalFun(30.0, 40.0, 80.0, 120.0)));
//        durationLV.addLabel("około 2 godzin", new FuzzySet<>(durationUniverse, new TrapezoidalFun(80.0, 120.0, 120.0, 160.0)));
        durationLV.addLabel("około 2 godzin", new FuzzySet<>(durationUniverse, new GaussFun(120,10)));
        durationLV.addLabel("niecałe 3 godziny", new FuzzySet<>(durationUniverse, new TriangularFun(120.0, 150.0,  180.0)));
        durationLV.addLabel("ponad 3 godziny", new FuzzySet<>(durationUniverse, new TrapezoidalFun(150.0, 180.0, 420.0, 420.0)));
        allLVs.put(durationLV.getName(), durationLV);

        UniverseOfDiscourse<Double> rankUniverse = new UniverseOfDiscourse<>(1.0, 2000.0);

        MembershipFunction mfTop10 = new TrapezoidalFun(1.0, 1.0, 10.0, 11.0);
        MembershipFunction mfTop20 = new TrapezoidalFun(10.0, 11.0, 20.0, 21.0);
        MembershipFunction mfTop50 = new TrapezoidalFun(20.0, 21.0, 45.0, 50.0);
        MembershipFunction mfTop100 = new TrapezoidalFun(45.0, 50.0, 90.0, 100.0);
        MembershipFunction mfTop200 = new TrapezoidalFun(90.0, 100.0, 190.0, 200.0);
        MembershipFunction mfPozaTop200 = new TrapezoidalFun(190.0, 200.0, 950.0, 1000.0);
        MembershipFunction mfPozaTop1000 = new TrapezoidalFun(950.0, 1000.0, 2000.0, 2000.0);


        LinguisticVariable<Double> winnerRankLV = new LinguisticVariable<>("ranking zwycięzcy", rankUniverse);
        winnerRankLV.addLabel("top 10", new FuzzySet<>(rankUniverse, mfTop10));
        winnerRankLV.addLabel("top 20", new FuzzySet<>(rankUniverse, mfTop20));
        winnerRankLV.addLabel("top 50", new FuzzySet<>(rankUniverse, mfTop50));
        winnerRankLV.addLabel("top 100", new FuzzySet<>(rankUniverse, mfTop100));
        winnerRankLV.addLabel("top 200", new FuzzySet<>(rankUniverse, mfTop200));
        winnerRankLV.addLabel("poza top 200", new FuzzySet<>(rankUniverse, mfPozaTop200));
        winnerRankLV.addLabel("poza top 1000", new FuzzySet<>(rankUniverse, mfPozaTop1000));
        allLVs.put(winnerRankLV.getName(), winnerRankLV);


        LinguisticVariable<Double> loserRankLV = new LinguisticVariable<>("ranking przegranego", rankUniverse);
        loserRankLV.addLabel("top 10", new FuzzySet<>(rankUniverse, mfTop10));
        loserRankLV.addLabel("top 20", new FuzzySet<>(rankUniverse, mfTop20));
        loserRankLV.addLabel("top 50", new FuzzySet<>(rankUniverse, mfTop50));
        loserRankLV.addLabel("top 100", new FuzzySet<>(rankUniverse, mfTop100));
        loserRankLV.addLabel("top 200", new FuzzySet<>(rankUniverse, mfTop200));
        loserRankLV.addLabel("poza top 200", new FuzzySet<>(rankUniverse, mfPozaTop200));
        loserRankLV.addLabel("poza top 1000", new FuzzySet<>(rankUniverse, mfPozaTop1000));
        allLVs.put(loserRankLV.getName(), loserRankLV);

        UniverseOfDiscourse<Double> acesUniverse = new UniverseOfDiscourse<>(0.0, 120.0);
        LinguisticVariable<Double> acesLV = new LinguisticVariable<>("liczba asów", acesUniverse);

        acesLV.addLabel("mniej niż 10", new FuzzySet<>(acesUniverse, new TrapezoidalFun(0.0, 0.0, 9.0, 10.0)));
        acesLV.addLabel("około 20", new FuzzySet<>(acesUniverse, new TrapezoidalFun(9.0, 10.0, 30.0, 40.0)));
        acesLV.addLabel("około 40", new FuzzySet<>(acesUniverse, new TriangularFun(30.0, 40.0, 50.0)));
        acesLV.addLabel("ponad 50", new FuzzySet<>(acesUniverse, new TrapezoidalFun(40.0, 50.0, 60.0, 70.0)));
        acesLV.addLabel("niecałe 100", new FuzzySet<>(acesUniverse, new TrapezoidalFun(60.0, 70.0, 90.0, 100.0)));
        acesLV.addLabel("ponad 100", new FuzzySet<>(acesUniverse, new TrapezoidalFun(90.0, 100.0, 120.0, 120.0)));
        allLVs.put(acesLV.getName(), acesLV);


        UniverseOfDiscourse<Double> dfUniverse = new UniverseOfDiscourse<>(0.0, 50.0);
        LinguisticVariable<Double> dfLV = new LinguisticVariable<>("liczba podwójnych błędów serwisowych", dfUniverse);

        dfLV.addLabel("mniej niż 5", new FuzzySet<>(dfUniverse, new TrapezoidalFun(0.0, 0.0, 4.0, 5.0)));
        dfLV.addLabel("mniej niż 10", new FuzzySet<>(dfUniverse, new TrapezoidalFun(4.0, 5.0, 9.0, 10.0)));
        dfLV.addLabel("mniej niż 20", new FuzzySet<>(dfUniverse, new TrapezoidalFun(9.0, 10.0, 19.0, 20.0)));
        dfLV.addLabel("ponad 20", new FuzzySet<>(dfUniverse, new TrapezoidalFun(19.0, 20.0, 29.0, 30.0)));
        dfLV.addLabel("około 40", new FuzzySet<>(dfUniverse, new TrapezoidalFun(29.0, 30.0, 49.0, 49.0)));
        allLVs.put(dfLV.getName(), dfLV);

        UniverseOfDiscourse<Double> gamesUniverse = new UniverseOfDiscourse<>(0.0, 90.0);
        LinguisticVariable<Double> gamesLV = new LinguisticVariable<>("liczba gemów", gamesUniverse);

        gamesLV.addLabel("mniej niż 12", new FuzzySet<>(gamesUniverse, new TrapezoidalFun(0.0, 0.0, 11.0, 12.0)));
        gamesLV.addLabel("około 20", new FuzzySet<>(gamesUniverse, new TrapezoidalFun(11.0, 12.0, 23.0, 25.0)));
        gamesLV.addLabel("około 30", new FuzzySet<>(gamesUniverse, new TrapezoidalFun(23.0, 25.0, 33.0, 35.0)));
        gamesLV.addLabel("około 40", new FuzzySet<>(gamesUniverse, new TrapezoidalFun(33.0, 35.0, 47.0, 50.0)));
        gamesLV.addLabel("mniej niż 60", new FuzzySet<>(gamesUniverse, new TrapezoidalFun(47.0, 50.0, 59.0, 60.0)));
        gamesLV.addLabel("ponad 60", new FuzzySet<>(gamesUniverse, new TrapezoidalFun(59.0, 60.0, 90.0, 90.0)));
        allLVs.put(gamesLV.getName(), gamesLV);
    }

    private void defineQuantifiers() {
        UniverseOfDiscourse<Double> relativeQuantifierUoD = new UniverseOfDiscourse<>(0.0, 1.0);

        relativeQuantifiers.add(new RelativeQuantifier("niewiele", new FuzzySet<>(relativeQuantifierUoD, new TrapezoidalFun(0.0, 0.0, 0.0, 0.25))));
        relativeQuantifiers.add(new RelativeQuantifier("około 1/4", new FuzzySet<>(relativeQuantifierUoD, new TriangularFun(0.0, 0.25, 0.5))));
        relativeQuantifiers.add(new RelativeQuantifier("około połowa", new FuzzySet<>(relativeQuantifierUoD, new TriangularFun(0.25, 0.5, 0.75))));
        relativeQuantifiers.add(new RelativeQuantifier("około 3/4", new FuzzySet<>(relativeQuantifierUoD, new TriangularFun(0.5, 0.75, 1.0))));
        relativeQuantifiers.add(new RelativeQuantifier("prawie wszystkie", new FuzzySet<>(relativeQuantifierUoD, new TrapezoidalFun(0.75, 1.0, 1.0, 1.0))));

        UniverseOfDiscourse<Double> absoluteQuantifierUoD = new UniverseOfDiscourse<>(0.0, 13000.0);

        absoluteQuantifiers.add(new AbsoluteQuantifier("mniej niż 1000", new FuzzySet<>(absoluteQuantifierUoD, new TrapezoidalFun(0.0, 0.0, 0.0, 1000.0))));
        absoluteQuantifiers.add(new AbsoluteQuantifier("około 1000", new FuzzySet<>(absoluteQuantifierUoD, new GaussFun(1000,600))));
        absoluteQuantifiers.add(new AbsoluteQuantifier("około 3000", new FuzzySet<>(absoluteQuantifierUoD, new TriangularFun(1000.0, 3000.0, 6000.0))));
        absoluteQuantifiers.add(new AbsoluteQuantifier("około 6000", new FuzzySet<>(absoluteQuantifierUoD, new TriangularFun(3000.0, 6000.0, 9000.0))));
        absoluteQuantifiers.add(new AbsoluteQuantifier("około 9000", new FuzzySet<>(absoluteQuantifierUoD, new GaussFun(9000,900))));
        absoluteQuantifiers.add(new AbsoluteQuantifier("więcej niż 11000", new FuzzySet<>(absoluteQuantifierUoD, new TrapezoidalFun(9500, 11000.0, 12000.0, 12000.0))));
    }

    public Set<Match> getAllRecords() {
        return allRecords;
    }

    public Map<String, LinguisticVariable<Double>> getAllLVs() {
        return allLVs;
    }

    public List<Quantifier> getRelativeQuantifiers() {
        return relativeQuantifiers;
    }

    public List<Quantifier> getAbsoluteQuantifiers() {
        return absoluteQuantifiers;
    }

    public Set<Match> getRecordsByYear(int year) {
        System.out.println("All records size: " + allRecords.size());
        Set<Match> matchesInYear = new HashSet<>();

        for (Match match : allRecords) {
            String dateString = match.getStringValue("tourney_date");
            System.out.println("Processing date: " + dateString);

            if (dateString != null && dateString.length() >= 4) {
                try {
                    int matchYear = Integer.parseInt(dateString.substring(0, 4));
                    System.out.println("Extracted year: " + matchYear);

                    if (matchYear == year) {
                        matchesInYear.add(match);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Number format exception for: " + dateString);
                }
            }
        }

        return matchesInYear;
    }

    public List<Map.Entry<String, String>> getAllPredicates() {
        return allLVs.entrySet().stream()
                .flatMap(lvEntry -> lvEntry.getValue().getLabelNames().stream()
                        .map(labelName -> new AbstractMap.SimpleEntry<>(lvEntry.getKey(), labelName)))
                .collect(Collectors.toList());
    }
}