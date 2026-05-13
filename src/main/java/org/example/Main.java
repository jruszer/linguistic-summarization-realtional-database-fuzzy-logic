//package org.example;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.*;
//
//// --- Tutaj powinny znajdować się Twoje implementacje klas: ---
//// MembershipFunction.java
//// TrapezoidalFun.java
//// TriangularFun.java
//// FuzzySet.java
//// UniverseOfDiscourse.java
//// Quantifier.java
//// RelativeQuantifier.java
//// AbsoluteQuantifier.java
//// SingleSubjectLinguisticSummary.java (zmodyfikowany jak w poprzedniej odpowiedzi)
//// Match.java
//// -------------------------------------------------------------
//
//public class Main {
//
//    public static void main(String[] args) {
//
//        // --- 1. Wczytywanie danych z CSV ---
//        Set<Match> allRecords = new HashSet<>();
//        String csvFilePath = "src/main/resources/matches.csv"; // Upewnij się, że plik jest w odpowiednim miejscu
//        List<String> numericColumns = Arrays.asList("winner_ht", "winner_age", "loser_ht", "loser_age", "minutes", "winner_rank", "loser_rank", "aces", "df", "games");
//
//        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
//            String line;
//            String[] headers = br.readLine().split(","); // Czytaj nagłówki
//
//            while ((line = br.readLine()) != null) {
//                String[] values = line.split(",");
//                Match record = new Match();
//                for (int i = 0; i < headers.length; i++) {
//                    String header = headers[i].trim();
//                    String value = values[i].trim();
//                    if (numericColumns.contains(header)) {
//                        try {
//                            record.addNumericValue(header, Double.parseDouble(value));
//                        } catch (NumberFormatException e) {
//                            // Obsłuż błąd konwersji, np. loguj lub ustaw null
//                            record.addNumericValue(header, null); // Ustaw null dla niepoprawnych liczb
//                        }
//                    } else {
//                        record.addStringValue(header, value);
//                    }
//                }
//                allRecords.add(record);
//            }
//        } catch (IOException e) {
//            System.err.println("Błąd podczas wczytywania pliku CSV: " + e.getMessage());
//            e.printStackTrace();
//            return; // Przerwij działanie, jeśli nie można wczytać danych
//        }
//
//        if (allRecords.isEmpty()) {
//            System.out.println("Brak danych do analizy. Sprawdź plik CSV.");
//            return;
//        }
//        System.out.println("Wczytano " + allRecords.size() + " rekordów z pliku CSV.");
//
//        // ======================================================================
//        // 2. Definicje zmiennych lingwistycznych
//        // ======================================================================
//
//        Map<String, LinguisticVariable<Double>> allLVs = new HashMap<>();
//
//        // --- Zmienne lingwistyczne dla wzrostu zwycięzcy/przegranego ---
//        double minUniverseHeight = 160.0;
//        double maxUniverseHeight = 215.0;
//        UniverseOfDiscourse<Double> heightUniverse = new UniverseOfDiscourse<>(minUniverseHeight, maxUniverseHeight);
//
//        // Etykiety dla wzrostu
//        MembershipFunction mfOkolo170cm = new TrapezoidalFun(165.0, 165.0, 170.0, 180.0);
//        MembershipFunction mfOkolo180cm = new TriangularFun(170.0, 180.0, 190.0);
//        MembershipFunction mfOkolo190cm = new TrapezoidalFun(180.0, 190.0, 199.0, 200.0);
//        MembershipFunction mfPowyzej2m = new TrapezoidalFun(199.0, 200.0, maxUniverseHeight, maxUniverseHeight);
//
//        LinguisticVariable<Double> winnerHeightLV = new LinguisticVariable<>("wzrost zwycięzcy", heightUniverse);
//        winnerHeightLV.addLabel("około 170 cm", new FuzzySet<>(heightUniverse, mfOkolo170cm));
//        winnerHeightLV.addLabel("około 180 cm", new FuzzySet<>(heightUniverse, mfOkolo180cm));
//        winnerHeightLV.addLabel("około 190 cm", new FuzzySet<>(heightUniverse, mfOkolo190cm));
//        winnerHeightLV.addLabel("powyżej 2 m", new FuzzySet<>(heightUniverse, mfPowyzej2m));
//        allLVs.put(winnerHeightLV.getName(), winnerHeightLV);
//
//        LinguisticVariable<Double> loserHeightLV = new LinguisticVariable<>("wzrost przegranego", heightUniverse);
//        loserHeightLV.addLabel("około 170 cm", new FuzzySet<>(heightUniverse, mfOkolo170cm));
//        loserHeightLV.addLabel("około 180 cm", new FuzzySet<>(heightUniverse, mfOkolo180cm));
//        loserHeightLV.addLabel("około 190 cm", new FuzzySet<>(heightUniverse, mfOkolo190cm));
//        loserHeightLV.addLabel("powyżej 2 m", new FuzzySet<>(heightUniverse, mfPowyzej2m));
//        allLVs.put(loserHeightLV.getName(), loserHeightLV);
//
//
//        // --- Zmienne lingwistyczne dla wieku zwycięzcy/przegranego ---
//        double minUniverseAge = 14.0;
//        double maxUniverseAge = 45.0;
//        UniverseOfDiscourse<Double> ageUniverse = new UniverseOfDiscourse<>(minUniverseAge, maxUniverseAge);
//
//        // Etykiety dla wieku
//        MembershipFunction mfMniejNiz18 = new TrapezoidalFun(minUniverseAge, minUniverseAge, 17.0, 20.0);
//        MembershipFunction mfOkolo20 = new TrapezoidalFun(17.0, 20.0, 24.0, 25.0);
//        MembershipFunction mfOkolo30 = new TrapezoidalFun(24.0, 25.0, 34.0, 35.0);
//        MembershipFunction mfPowyzej35 = new TrapezoidalFun(34.0, 35.0, maxUniverseAge, maxUniverseAge);
//
//        LinguisticVariable<Double> winnerAgeLV = new LinguisticVariable<>("wiek zwycięzcy", ageUniverse);
//        winnerAgeLV.addLabel("mniej niż 18", new FuzzySet<>(ageUniverse, mfMniejNiz18));
//        winnerAgeLV.addLabel("około 20", new FuzzySet<>(ageUniverse, mfOkolo20));
//        winnerAgeLV.addLabel("około 30", new FuzzySet<>(ageUniverse, mfOkolo30));
//        winnerAgeLV.addLabel("powyżej 35", new FuzzySet<>(ageUniverse, mfPowyzej35));
//        allLVs.put(winnerAgeLV.getName(), winnerAgeLV);
//
//        LinguisticVariable<Double> loserAgeLV = new LinguisticVariable<>("wiek przegranego", ageUniverse);
//        loserAgeLV.addLabel("mniej niż 18", new FuzzySet<>(ageUniverse, mfMniejNiz18));
//        loserAgeLV.addLabel("około 20", new FuzzySet<>(ageUniverse, mfOkolo20));
//        loserAgeLV.addLabel("około 30", new FuzzySet<>(ageUniverse, mfOkolo30));
//        loserAgeLV.addLabel("powyżej 35", new FuzzySet<>(ageUniverse, mfPowyzej35));
//        allLVs.put(loserAgeLV.getName(), loserAgeLV);
//
//        // --- Zmienna lingwistyczna: Czas trwania meczu (minutes) ---
//        UniverseOfDiscourse<Double> durationUniverse = new UniverseOfDiscourse<>(0.0, 420.0);
//        LinguisticVariable<Double> durationLV = new LinguisticVariable<>("czas trwania meczu", durationUniverse);
//
//        // Etykiety dla czasu trwania meczu:
//        durationLV.addLabel("krótszy niż 40 min", new FuzzySet<>(durationUniverse, new TrapezoidalFun(0.0, 0.0, 30.0, 40.0)));
//        durationLV.addLabel("około godziny", new FuzzySet<>(durationUniverse, new TrapezoidalFun(30.0, 40.0, 80.0, 120.0)));
//        durationLV.addLabel("około 2 godzin", new FuzzySet<>(durationUniverse, new TrapezoidalFun(80.0, 120.0, 120.0, 160.0)));
//        durationLV.addLabel("niecałe 3 godziny", new FuzzySet<>(durationUniverse, new TrapezoidalFun(120.0, 160.0, 160.0, 180.0)));
//        durationLV.addLabel("ponad 3 godziny", new FuzzySet<>(durationUniverse, new TrapezoidalFun(160.0, 180.0, 420.0, 420.0)));
//        allLVs.put(durationLV.getName(), durationLV);
//
//        // --- Zmienne lingwistyczne: Ranking zwycięzcy/przegranego ---
//        UniverseOfDiscourse<Double> rankUniverse = new UniverseOfDiscourse<>(1.0, 2000.0);
//
//        // Etykiety dla rankingu:
//        MembershipFunction mfTop10 = new TrapezoidalFun(1.0, 1.0, 10.0, 11.0);
//        MembershipFunction mfTop20 = new TrapezoidalFun(10.0, 11.0, 20.0, 21.0);
//        MembershipFunction mfTop50 = new TrapezoidalFun(20.0, 21.0, 45.0, 50.0);
//        MembershipFunction mfTop100 = new TrapezoidalFun(45.0, 50.0, 90.0, 100.0);
//        MembershipFunction mfTop200 = new TrapezoidalFun(90.0, 100.0, 190.0, 200.0);
//        MembershipFunction mfPozaTop200 = new TrapezoidalFun(190.0, 200.0, 950.0, 1000.0);
//        MembershipFunction mfPozaTop1000 = new TrapezoidalFun(950.0, 1000.0, 2000.0, 2000.0);
//
//
//        LinguisticVariable<Double> winnerRankLV = new LinguisticVariable<>("ranking zwycięzcy", rankUniverse);
//        winnerRankLV.addLabel("top 10", new FuzzySet<>(rankUniverse, mfTop10));
//        winnerRankLV.addLabel("top 20", new FuzzySet<>(rankUniverse, mfTop20));
//        winnerRankLV.addLabel("top 50", new FuzzySet<>(rankUniverse, mfTop50));
//        winnerRankLV.addLabel("top 100", new FuzzySet<>(rankUniverse, mfTop100));
//        winnerRankLV.addLabel("top 200", new FuzzySet<>(rankUniverse, mfTop200));
//        winnerRankLV.addLabel("poza top 200", new FuzzySet<>(rankUniverse, mfPozaTop200));
//        winnerRankLV.addLabel("poza top 1000", new FuzzySet<>(rankUniverse, mfPozaTop1000));
//        allLVs.put(winnerRankLV.getName(), winnerRankLV);
//
//
//        LinguisticVariable<Double> loserRankLV = new LinguisticVariable<>("ranking przegranego", rankUniverse);
//        loserRankLV.addLabel("top 10", new FuzzySet<>(rankUniverse, mfTop10));
//        loserRankLV.addLabel("top 20", new FuzzySet<>(rankUniverse, mfTop20));
//        loserRankLV.addLabel("top 50", new FuzzySet<>(rankUniverse, mfTop50));
//        loserRankLV.addLabel("top 100", new FuzzySet<>(rankUniverse, mfTop100));
//        loserRankLV.addLabel("top 200", new FuzzySet<>(rankUniverse, mfTop200));
//        loserRankLV.addLabel("poza top 200", new FuzzySet<>(rankUniverse, mfPozaTop200));
//        loserRankLV.addLabel("poza top 1000", new FuzzySet<>(rankUniverse, mfPozaTop1000));
//        allLVs.put(loserRankLV.getName(), loserRankLV);
//
//        // --- ZMIENNA LINGWISTYCZNA: Liczba asów (aces) ---
//        UniverseOfDiscourse<Double> acesUniverse = new UniverseOfDiscourse<>(0.0, 120.0);
//        LinguisticVariable<Double> acesLV = new LinguisticVariable<>("liczba asów", acesUniverse);
//
//        // Etykiety dla liczby asów:
//        // μ_mniej niż 10(x) - zmodyfikowana, aby od 0 do 9 była przynależność 1
//        acesLV.addLabel("mniej niż 10", new FuzzySet<>(acesUniverse, new TrapezoidalFun(0.0, 0.0, 9.0, 10.0)));
//
//        // μ_około 20(x)
//        acesLV.addLabel("około 20", new FuzzySet<>(acesUniverse, new TrapezoidalFun(9.0, 10.0, 30.0, 40.0)));
//
//        // μ_około 40(x) (Funkcja Gaussa z obrazka, jeśli masz implementację GaussianFun)
//        // Jeśli nie masz GaussianFun, użyj np. TriangularFun(30.0, 40.0, 50.0) lub TrapezoidalFun(30.0, 35.0, 45.0, 50.0)
//        acesLV.addLabel("około 40", new FuzzySet<>(acesUniverse, new TriangularFun(30.0, 40.0, 50.0))); // Używam TriangularFun jako zamiennik
//        // acesLV.addLabel("około 40", new FuzzySet<>(acesUniverse, new GaussFun(50.0, 10.0))); // Jeśli masz GaussFun, odkomentuj
//
//        // μ_ponad 50(x)
//        acesLV.addLabel("ponad 50", new FuzzySet<>(acesUniverse, new TrapezoidalFun(40.0, 50.0, 60.0, 70.0)));
//
//        // μ_niecałe 100(x)
//        acesLV.addLabel("niecałe 100", new FuzzySet<>(acesUniverse, new TrapezoidalFun(60.0, 70.0, 90.0, 100.0)));
//
//        // μ_ponad 100(x)
//        acesLV.addLabel("ponad 100", new FuzzySet<>(acesUniverse, new TrapezoidalFun(90.0, 100.0, 120.0, 120.0)));
//        allLVs.put(acesLV.getName(), acesLV);
//
//
//        // --- ZMIENNA LINGWISTYCZNA: Liczba podwójnych błędów serwisowych (df) ---
//        // Ustalamy uniwersum dla podwójnych błędów, np. od 0 do 50 (maksymalna wartość z definicji "około 40")
//        UniverseOfDiscourse<Double> dfUniverse = new UniverseOfDiscourse<>(0.0, 50.0);
//        LinguisticVariable<Double> dfLV = new LinguisticVariable<>("liczba podwójnych błędów serwisowych", dfUniverse);
//
//        // Etykiety dla liczby podwójnych błędów:
//        // μ_mniej niż 5(x) - zmodyfikowana, aby od 0 do 4 była przynależność 1
//        dfLV.addLabel("mniej niż 5", new FuzzySet<>(dfUniverse, new TrapezoidalFun(0.0, 0.0, 4.0, 5.0)));
//
//        // μ_mniej niż 10(x)
//        dfLV.addLabel("mniej niż 10", new FuzzySet<>(dfUniverse, new TrapezoidalFun(4.0, 5.0, 9.0, 10.0)));
//
//        // μ_mniej niż 20(x)
//        dfLV.addLabel("mniej niż 20", new FuzzySet<>(dfUniverse, new TrapezoidalFun(9.0, 10.0, 19.0, 20.0)));
//
//        // μ_ponad 20(x)
//        dfLV.addLabel("ponad 20", new FuzzySet<>(dfUniverse, new TrapezoidalFun(19.0, 20.0, 29.0, 30.0)));
//
//        // μ_około 40(x)
//        dfLV.addLabel("około 40", new FuzzySet<>(dfUniverse, new TrapezoidalFun(29.0, 30.0, 49.0, 49.0)));
//        allLVs.put(dfLV.getName(), dfLV);
//
//        // --- ZMIENNA LINGWISTYCZNA: Liczba gier (games) ---
//        // Ustalamy uniwersum dla gier, np. od 0 do 90 (maksymalna wartość z definicji "ponad 60")
//        UniverseOfDiscourse<Double> gamesUniverse = new UniverseOfDiscourse<>(0.0, 90.0);
//        LinguisticVariable<Double> gamesLV = new LinguisticVariable<>("liczba gemów", gamesUniverse);
//
//        // Etykiety dla liczby gier:
//        // μ_mniej niż 12(x)
//        gamesLV.addLabel("mniej niż 12", new FuzzySet<>(gamesUniverse, new TrapezoidalFun(0.0, 0.0, 11.0, 12.0)));
//
//        // μ_około 20(x)
//        gamesLV.addLabel("około 20", new FuzzySet<>(gamesUniverse, new TrapezoidalFun(11.0, 12.0, 23.0, 25.0)));
//
//        // μ_około 30(x)
//        gamesLV.addLabel("około 30", new FuzzySet<>(gamesUniverse, new TrapezoidalFun(23.0, 25.0, 33.0, 35.0)));
//
//        // μ_około 40(x)
//        gamesLV.addLabel("około 40", new FuzzySet<>(gamesUniverse, new TrapezoidalFun(33.0, 35.0, 47.0, 50.0)));
//
//        // μ_mniej niż 60(x)
//        gamesLV.addLabel("mniej niż 60", new FuzzySet<>(gamesUniverse, new TrapezoidalFun(47.0, 50.0, 59.0, 60.0)));
//
//        // μ_ponad 60(x)
//        gamesLV.addLabel("ponad 60", new FuzzySet<>(gamesUniverse, new TrapezoidalFun(59.0, 60.0, 90.0, 90.0)));
//        allLVs.put(gamesLV.getName(), gamesLV);
//
//
//        // ======================================================================
//        // 3. Deklaracja Kwantyfikatorów
//        // ======================================================================
//
//        // Uniwersum dla kwantyfikatorów względnych (zakres [0, 1])
//        UniverseOfDiscourse<Double> relativeQuantifierUoD = new UniverseOfDiscourse<>(0.0, 1.0);
//
//        // Kwantyfikatory względne
//        FuzzySet<Double> fsNiewiele = new FuzzySet<>(relativeQuantifierUoD, new TrapezoidalFun(0.0, 0.0, 0.0, 0.25));
//        Quantifier niewieleQuantifier = new RelativeQuantifier("niewiele", fsNiewiele);
//
//        FuzzySet<Double> fsOkolo1_4 = new FuzzySet<>(relativeQuantifierUoD, new TriangularFun(0.0, 0.25, 0.5));
//        Quantifier okolo1_4Quantifier = new RelativeQuantifier("około 1/4", fsOkolo1_4);
//
//        FuzzySet<Double> fsOkoloPolowa = new FuzzySet<>(relativeQuantifierUoD, new TriangularFun(0.25, 0.5, 0.75));
//        Quantifier okoloPolowaQuantifier = new RelativeQuantifier("około połowa", fsOkoloPolowa);
//
//        FuzzySet<Double> fsOkolo3_4 = new FuzzySet<>(relativeQuantifierUoD, new TriangularFun(0.5, 0.75, 1.0));
//        Quantifier okolo3_4Quantifier = new RelativeQuantifier("około 3/4", fsOkolo3_4);
//
//        FuzzySet<Double> fsPrawieWszystkie = new FuzzySet<>(relativeQuantifierUoD, new TrapezoidalFun(0.75, 1.0, 1.0, 1.0));
//        Quantifier prawieWszystkieQuantifier = new RelativeQuantifier("prawie wszystkie", fsPrawieWszystkie);
//
//        // Lista kwantyfikatorów względnych
//        List<Quantifier> relativeQuantifiers = Arrays.asList(
//                niewieleQuantifier,
//                okolo1_4Quantifier,
//                okoloPolowaQuantifier,
//                okolo3_4Quantifier,
//                prawieWszystkieQuantifier
//        );
//
//
//        // Kwantyfikatory bezwzględne
//        UniverseOfDiscourse<Double> absoluteQuantifierUoD = new UniverseOfDiscourse<>(0.0, 12000.0); // Przykładowe uniwersum
//
//        FuzzySet<Double> fsMniejNiz1000 = new FuzzySet<>(absoluteQuantifierUoD, new TrapezoidalFun(0.0, 0.0, 0.0, 1000.0));
//        Quantifier mniejNiz1000Quantifier = new AbsoluteQuantifier("mniej niż 1000", fsMniejNiz1000);
//
//        FuzzySet<Double> fsOkolo1000 = new FuzzySet<>(absoluteQuantifierUoD, new TriangularFun(0.0, 1000.0, 2000.0));
//        Quantifier okolo1000Quantifier = new AbsoluteQuantifier("około 1000", fsOkolo1000);
//
//        FuzzySet<Double> fsOkolo3000 = new FuzzySet<>(absoluteQuantifierUoD, new TriangularFun(1000.0, 3000.0, 6000.0));
//        Quantifier okolo3000Quantifier = new AbsoluteQuantifier("około 3000", fsOkolo3000);
//
//        FuzzySet<Double> fsOkolo6000 = new FuzzySet<>(absoluteQuantifierUoD, new TriangularFun(3000.0, 6000.0, 9000.0));
//        Quantifier okolo6000Quantifier = new AbsoluteQuantifier("około 6000", fsOkolo6000);
//
//        FuzzySet<Double> fsOkolo9000 = new FuzzySet<>(absoluteQuantifierUoD, new TriangularFun(6000.0, 9000.0, 12000.0));
//        Quantifier okolo9000Quantifier = new AbsoluteQuantifier("około 9000", fsOkolo9000);
//
//        FuzzySet<Double> fsWiecejNiz11000 = new FuzzySet<>(absoluteQuantifierUoD, new TrapezoidalFun(10000.0, 11000.0, 12000.0, 12000.0));
//        Quantifier wiecejNiz11000Quantifier = new AbsoluteQuantifier("więcej niż 11000", fsWiecejNiz11000);
//
//        // Lista kwantyfikatorów bezwzględnych
//        List<Quantifier> absoluteQuantifiers = Arrays.asList(
//                mniejNiz1000Quantifier,
//                okolo1000Quantifier,
//                okolo3000Quantifier,
//                okolo6000Quantifier,
//                okolo9000Quantifier,
//                wiecejNiz11000Quantifier
//        );
//
//
//        // ======================================================================
//        // 4. Generowanie przykładowych podsumowań jednopodmiotowych (Podmiot: MECZE)
//        // ======================================================================
//
//
//        System.out.println("--- PRZYKŁADOWE PODSUMOWANIA JEDNOPODMIOTOWE (PODMIOT: MECZE) Z PODANYM KWANTYFIKATOREM ---");
//
//        // Podsumowanie 1: "Około 1/4 meczów miało zwycięzcę o wzroście około 190 cm."
//        List<Map.Entry<String, String>> s1Summarizers = Arrays.asList(
//                new AbstractMap.SimpleEntry<>("liczba gemów", "około 30")
//        );
//        // Przekazujemy TERAZ konkretny kwantyfikator
//        OneSubjectLinguisticSummary summary1 = new OneSubjectLinguisticSummary(
//                "meczów", allLVs, okolo1_4Quantifier, s1Summarizers // Zmienione: okolo1_4Quantifier zamiast relativeQuantifiers
//        );
//        double truthDegree1 = summary1.calculateT1(allRecords); // Zmieniona nazwa metody
//        System.out.println(summary1.writeSummary());
//        System.out.printf("  Stopień prawdziwości (T1): %.2f%n", truthDegree1);
//        System.out.printf("  T2: %.2f%n", summary1.calculateT2());
//        System.out.printf("  T3: %.2f%n", summary1.calculateT3(allRecords));
//        System.out.printf("  T4: %.2f%n", summary1.calculateT4(allRecords));
//        System.out.printf("  T5: %.2f%n", summary1.calculateT5());
//        System.out.printf("  T6: %.2f%n", summary1.calculateT6(allRecords));
//        System.out.printf("  T7: %.2f%n", summary1.calculateT7());
//        System.out.printf("  T8: %.2f%n", summary1.calculateT8());
//        System.out.printf("  T9: %.2f%n", summary1.calculateT9());
//        System.out.printf("  T10: %.2f%n", summary1.calculateT10());
//        System.out.printf("  T11: %.2f%n", summary1.calculateT11());
//        System.out.printf("  Całkowity stopień prawdziwości: %.2f%n", summary1.calculateT(allRecords));
//
//
//        // Podsumowanie 2: "Niewiele meczów miało przegranego w wieku mniej niż 18 lat."
//        List<Map.Entry<String, String>> s2Summarizers = Arrays.asList(
//                new AbstractMap.SimpleEntry<>("wiek przegranego", "mniej niż 18")
//        );
//        OneSubjectLinguisticSummary summary2 = new OneSubjectLinguisticSummary(
//                "meczów", allLVs, niewieleQuantifier, s2Summarizers // Zmienione
//        );
//        double truthDegree2 = summary2.calculateT1(allRecords); // Zmieniona nazwa metody
//        System.out.println(summary2.writeSummary());
//        System.out.printf("  Stopień prawdziwości (T1): %.2f%n", truthDegree2);
//        System.out.printf("  T2: %.2f%n", summary2.calculateT2());
//        System.out.printf("  T3: %.2f%n", summary2.calculateT3(allRecords));
//        System.out.printf("  T4: %.2f%n", summary2.calculateT4(allRecords));
//        System.out.printf("  T5: %.2f%n", summary2.calculateT5());
//        System.out.printf("  T6: %.2f%n", summary2.calculateT6(allRecords));
//        System.out.printf("  T7: %.2f%n", summary2.calculateT7());
//        System.out.printf("  T8: %.2f%n", summary2.calculateT8());
//        System.out.printf("  T9: %.2f%n", summary2.calculateT9());
//        System.out.printf("  T10: %.2f%n", summary2.calculateT10());
//        System.out.printf("  T11: %.2f%n", summary2.calculateT11());
//        System.out.printf("  Całkowity stopień prawdziwości: %.2f%n", summary2.calculateT(allRecords));
//
//
//        // Podsumowanie 3: "Około połowa meczów, w których zwycięzca miał powyżej 35 lat, miała zwycięzcę o wzroście powyżej 2 m."
//        List<Map.Entry<String, String>> s3Summarizers = Arrays.asList(
//                new AbstractMap.SimpleEntry<>("wzrost zwycięzcy", "powyżej 2 m")
//        );
//        Map.Entry<String, String> q3Qualifier = new AbstractMap.SimpleEntry<>("wiek zwycięzcy", "powyżej 35");
//        OneSubjectLinguisticSummary summary3 = new OneSubjectLinguisticSummary(
//                "meczów", allLVs, okoloPolowaQuantifier, q3Qualifier, s3Summarizers // Zmienione
//        );
//        double truthDegree3 = summary3.calculateT1(allRecords); // Zmieniona nazwa metody
//        System.out.println(summary3.writeSummary());
//        System.out.printf("  Stopień prawdziwości (T1): %.2f%n", truthDegree3);
//        System.out.printf("  T2: %.2f%n", summary3.calculateT2());
//        System.out.printf("  T3: %.2f%n", summary3.calculateT3(allRecords));
//        System.out.printf("  T4: %.2f%n", summary3.calculateT4(allRecords));
//        System.out.printf("  T5: %.2f%n", summary3.calculateT5());
//        System.out.printf("  T6: %.2f%n", summary3.calculateT6(allRecords));
//        System.out.printf("  T7: %.2f%n", summary3.calculateT7());
//        System.out.printf("  T8: %.2f%n", summary3.calculateT8());
//        System.out.printf("  T9: %.2f%n", summary3.calculateT9());
//        System.out.printf("  T10: %.2f%n", summary3.calculateT10());
//        System.out.printf("  T11: %.2f%n", summary3.calculateT11());
//        System.out.printf("  Całkowity stopień prawdziwości: %.2f%n", summary3.calculateT(allRecords));
//
//
//        // Podsumowanie 4: "Prawie wszystkie mecze miały zwycięzcę o wzroście około 180 cm i wieku około 20 lat."
//        List<Map.Entry<String, String>> s4Summarizers = Arrays.asList(
//                new AbstractMap.SimpleEntry<>("wzrost zwycięzcy", "około 180 cm"),
//                new AbstractMap.SimpleEntry<>("wiek zwycięzcy", "około 20")
//        );
//        OneSubjectLinguisticSummary summary4 = new OneSubjectLinguisticSummary(
//                "meczów", allLVs, prawieWszystkieQuantifier, s4Summarizers // Zmienione
//        );
//        double truthDegree4 = summary4.calculateT1(allRecords); // Zmieniona nazwa metody
//        System.out.println(summary4.writeSummary());
//        System.out.printf("  Stopień prawdziwości (T1): %.2f%n", truthDegree4);
//        System.out.printf("  T2: %.2f%n", summary4.calculateT2());
//        System.out.printf("  T3: %.2f%n", summary4.calculateT3(allRecords));
//        System.out.printf("  T4: %.2f%n", summary4.calculateT4(allRecords));
//        System.out.printf("  T5: %.2f%n", summary4.calculateT5());
//        System.out.printf("  T6: %.2f%n", summary4.calculateT6(allRecords));
//        System.out.printf("  T7: %.2f%n", summary4.calculateT7());
//        System.out.printf("  T8: %.2f%n", summary4.calculateT8());
//        System.out.printf("  T9: %.2f%n", summary4.calculateT9());
//        System.out.printf("  T10: %.2f%n", summary4.calculateT10());
//        System.out.printf("  T11: %.2f%n", summary4.calculateT11());
//        System.out.printf("  Całkowity stopień prawdziwości: %.2f%n", summary4.calculateT(allRecords));
//
//
//        // Podsumowanie 5: "Około 3/4 meczów, w których przegrany miał około 30 lat, miało przegranego o wzroście około 170 cm."
//        List<Map.Entry<String, String>> s5Summarizers = Arrays.asList(
//                new AbstractMap.SimpleEntry<>("wzrost przegranego", "około 170 cm")
//        );
//        Map.Entry<String, String> q5Qualifier = new AbstractMap.SimpleEntry<>("wiek przegranego", "około 30");
//        OneSubjectLinguisticSummary summary5 = new OneSubjectLinguisticSummary(
//                "meczów", allLVs, okolo3_4Quantifier, q5Qualifier, s5Summarizers // Zmienione
//        );
//        double truthDegree5 = summary5.calculateT1(allRecords); // Zmieniona nazwa metody
//        System.out.println(summary5.writeSummary());
//        System.out.printf("  Stopień prawdziwości (T1): %.2f%n", truthDegree5);
//        System.out.printf("  T2: %.2f%n", summary5.calculateT2());
//        System.out.printf("  T3: %.2f%n", summary5.calculateT3(allRecords));
//        System.out.printf("  T4: %.2f%n", summary5.calculateT4(allRecords));
//        System.out.printf("  T5: %.2f%n", summary5.calculateT5());
//        System.out.printf("  T6: %.2f%n", summary5.calculateT6(allRecords));
//        System.out.printf("  T7: %.2f%n", summary5.calculateT7());
//        System.out.printf("  T8: %.2f%n", summary5.calculateT8());
//        System.out.printf("  T9: %.2f%n", summary5.calculateT9());
//        System.out.printf("  T10: %.2f%n", summary5.calculateT10());
//        System.out.printf("  T11: %.2f%n", summary5.calculateT11());
//        System.out.printf("  Całkowity stopień prawdziwości: %.2f%n", summary5.calculateT(allRecords));
//
//        // Podsumowanie 6: "Niewielu meczów, w których zwycięzca miał wzrost około 180 cm, miało zwycięzcę o wieku mniej niż 18 lat i około 20 lat."
//        List<Map.Entry<String, String>> s6Summarizers = Arrays.asList(
//                new AbstractMap.SimpleEntry<>("wiek zwycięzcy", "mniej niż 18"),
//                new AbstractMap.SimpleEntry<>("wiek zwycięzcy", "około 20")
//        );
//        Map.Entry<String, String> q6Qualifier = new AbstractMap.SimpleEntry<>("wzrost zwycięzcy", "około 180 cm");
//        OneSubjectLinguisticSummary summary6 = new OneSubjectLinguisticSummary(
//                "meczów", allLVs, niewieleQuantifier, q6Qualifier, s6Summarizers // Zmienione
//        );
//        double truthDegree6 = summary6.calculateT1(allRecords); // Zmieniona nazwa metody
//        System.out.println(summary6.writeSummary());
//        System.out.printf("  Stopień prawdziwości (T1): %.2f%n", truthDegree6);
//        System.out.printf("  T2: %.2f%n", summary6.calculateT2());
//        System.out.printf("  T3: %.2f%n", summary6.calculateT3(allRecords));
//        System.out.printf("  T4: %.2f%n", summary6.calculateT4(allRecords));
//        System.out.printf("  T5: %.2f%n", summary6.calculateT5());
//        System.out.printf("  T6: %.2f%n", summary6.calculateT6(allRecords));
//        System.out.printf("  T7: %.2f%n", summary6.calculateT7());
//        System.out.printf("  T8: %.2f%n", summary6.calculateT8());
//        System.out.printf("  T9: %.2f%n", summary6.calculateT9());
//        System.out.printf("  T10: %.2f%n", summary6.calculateT10());
//        System.out.printf("  T11: %.2f%n", summary6.calculateT11());
//        System.out.printf("  Całkowity stopień prawdziwości: %.2f%n", summary6.calculateT(allRecords));
//
//
//        // --- Przykłady z kwantyfikatorami bezwzględnymi ---
//        System.out.println("\n--- PRZYKŁADY PODSUMOWAŃ Z KWANTYFIKATORAMI BEZWZGLĘDNYMI (PODMIOT: MECZE) Z PODANYM KWANTYFIKATOREM ---");
//
//        // Podsumowanie 7: "Mniej niż 1000 meczów miało zwycięzcę o wzroście około 180 cm."
//        List<Map.Entry<String, String>> s7Summarizers = Arrays.asList(
//                new AbstractMap.SimpleEntry<>("wiek zwycięzcy", "mniej niż 18")
//        );
//        OneSubjectLinguisticSummary summary7 = new OneSubjectLinguisticSummary(
//                "meczów", allLVs, mniejNiz1000Quantifier, s7Summarizers // Zmienione: konkretny kwantyfikator
//        );
//        double truthDegree7 = summary7.calculateT1(allRecords); // Zmieniona nazwa metody
//        System.out.println(summary7.writeSummary());
//        System.out.printf("  Stopień prawdziwości (T1): %.2f%n", truthDegree7);
//        System.out.printf("  T2: %.2f%n", summary7.calculateT2());
//        System.out.printf("  T3: %.2f%n", summary7.calculateT3(allRecords));
//        System.out.printf("  T4: %.2f%n", summary7.calculateT4(allRecords));
//        System.out.printf("  T5: %.2f%n", summary7.calculateT5());
//        System.out.printf("  T6: %.2f%n", summary7.calculateT6(allRecords));
//        System.out.printf("  T7: %.2f%n", summary7.calculateT7());
//        System.out.printf("  T8: %.2f%n", summary7.calculateT8());
//        System.out.printf("  T9: %.2f%n", summary7.calculateT9());
//        System.out.printf("  T10: %.2f%n", summary7.calculateT10());
//        System.out.printf("  T11: %.2f%n", summary7.calculateT11());
//        System.out.printf("  Całkowity stopień prawdziwości: %.2f%n", summary7.calculateT(allRecords));
//
//        // Podsumowanie 8: "Około 3000 meczów, w których zwycięzca miał około 20 lat, miało zwycięzcę o wzroście około 190 cm."
//        List<Map.Entry<String, String>> s8Summarizers = Arrays.asList(
//                new AbstractMap.SimpleEntry<>("wzrost zwycięzcy", "około 190 cm")
//        );
//        Map.Entry<String, String> q8Qualifier = new AbstractMap.SimpleEntry<>("wiek zwycięzcy", "około 20");
//        OneSubjectLinguisticSummary summary8 = new OneSubjectLinguisticSummary(
//                "meczów", allLVs, okolo3000Quantifier, q8Qualifier, s8Summarizers // Zmienione
//        );
//        double truthDegree8 = summary8.calculateT1(allRecords); // Zmieniona nazwa metody
//        System.out.println(summary8.writeSummary());
//        System.out.printf("  Stopień prawdziwości (T1): %.2f%n", truthDegree8);
//        System.out.printf("  T2: %.2f%n", summary8.calculateT2());
//        System.out.printf("  T3: %.2f%n", summary8.calculateT3(allRecords));
//        System.out.printf("  T4: %.2f%n", summary8.calculateT4(allRecords));
//        System.out.printf("  T5: %.2f%n", summary8.calculateT5());
//        System.out.printf("  T6: %.2f%n", summary8.calculateT6(allRecords));
//        System.out.printf("  T7: %.2f%n", summary8.calculateT7());
//        System.out.printf("  T8: %.2f%n", summary8.calculateT8());
//        System.out.printf("  T9: %.2f%n", summary8.calculateT9());
//        System.out.printf("  T10: %.2f%n", summary8.calculateT10());
//        System.out.printf("  T11: %.2f%n", summary8.calculateT11());
//        System.out.printf("  Całkowity stopień prawdziwości: %.2f%n", summary8.calculateT(allRecords));
//
//        // Podsumowanie 9: "Więcej niż 11000 meczów miało przegranego o wieku około 30 lat."
//        List<Map.Entry<String, String>> s9Summarizers = Arrays.asList(
//                new AbstractMap.SimpleEntry<>("wiek przegranego", "około 30")
//        );
//        OneSubjectLinguisticSummary summary9 = new OneSubjectLinguisticSummary(
//                "meczów", allLVs, wiecejNiz11000Quantifier, s9Summarizers // Zmienione
//        );
//        double truthDegree9 = summary9.calculateT1(allRecords); // Zmieniona nazwa metody
//        System.out.println(summary9.writeSummary());
//        System.out.printf("  Stopień prawdziwości (T1): %.2f%n", truthDegree9);
//        System.out.printf("  T2: %.2f%n", summary9.calculateT2());
//        System.out.printf("  T3: %.2f%n", summary9.calculateT3(allRecords));
//        System.out.printf("  T4: %.2f%n", summary9.calculateT4(allRecords));
//        System.out.printf("  T5: %.2f%n", summary9.calculateT5());
//        System.out.printf("  T6: %.2f%n", summary9.calculateT6(allRecords));
//        System.out.printf("  T7: %.2f%n", summary9.calculateT7());
//        System.out.printf("  T8: %.2f%n", summary9.calculateT8());
//        System.out.printf("  T9: %.2f%n", summary9.calculateT9());
//        System.out.printf("  T10: %.2f%n", summary9.calculateT10());
//        System.out.printf("  T11: %.2f%n", summary9.calculateT11());
//        System.out.printf("  Całkowity stopień prawdziwości: %.2f%n", summary9.calculateT(allRecords));
//
//
//        // --- Nowe podsumowania z czasem trwania meczu ---
//        System.out.println("\n--- NOWE PODSUMOWANIA Z CZASEM TRWANIA MECZU (PODMIOT: MECZE) Z PODANYM KWANTYFIKATOREM ---");
//
//        // Podsumowanie 10: "Prawie wszystkie mecze trwały ponad 3 godziny."
//        List<Map.Entry<String, String>> s10Summarizers = Arrays.asList(
//                new AbstractMap.SimpleEntry<>("czas trwania meczu", "ponad 3 godziny")
//        );
//        OneSubjectLinguisticSummary summary10 = new OneSubjectLinguisticSummary(
//                "meczów", allLVs, prawieWszystkieQuantifier, s10Summarizers // Zmienione
//        );
//        double truthDegree10 = summary10.calculateT1(allRecords); // Zmieniona nazwa metody
//        System.out.println(summary10.writeSummary());
//        System.out.printf("  Stopień prawdziwości (T1): %.2f%n", truthDegree10);
//        System.out.printf("  T2: %.2f%n", summary10.calculateT2());
//        System.out.printf("  T3: %.2f%n", summary10.calculateT3(allRecords));
//        System.out.printf("  T4: %.2f%n", summary10.calculateT4(allRecords));
//        System.out.printf("  T5: %.2f%n", summary10.calculateT5());
//        System.out.printf("  T6: %.2f%n", summary10.calculateT6(allRecords));
//        System.out.printf("  T7: %.2f%n", summary10.calculateT7());
//        System.out.printf("  T8: %.2f%n", summary10.calculateT8());
//        System.out.printf("  T9: %.2f%n", summary10.calculateT9());
//        System.out.printf("  T10: %.2f%n", summary10.calculateT10());
//        System.out.printf("  T11: %.2f%n", summary10.calculateT11());
//        System.out.printf("  Całkowity stopień prawdziwości: %.2f%n", summary10.calculateT(allRecords));
//
//
//// ======================================================================
//        // 5. Segregacja danych według lat
//        // ======================================================================
//
//        Map<String, Set<Match>> recordsByYear = new HashMap<>();
//        for (Match record : allRecords) {
//            String tourneyDate = record.getStringValue("tourney_date");
//            if (tourneyDate != null && tourneyDate.length() >= 4) {
//                String year = tourneyDate.substring(0, 4);
//                // Filtrujemy lata, jeśli chcemy tylko 2015-2020
//                if (Integer.parseInt(year) >= 2015 && Integer.parseInt(year) <= 2020) {
//                    recordsByYear.computeIfAbsent(year, k -> new HashSet<>()).add(record);
//                }
//            }
//        }
//
//        System.out.println("\n--- LICZBA REKORDÓW NA ROK ---");
//        recordsByYear.forEach((year, records) ->
//                System.out.println("Rok " + year + ": " + records.size() + " meczów")
//        );
//
//        // Upewnijmy się, że mamy dane dla lat, które chcemy porównywać
//        Set<Match> data2015 = recordsByYear.getOrDefault("2015", new HashSet<>());
//        Set<Match> data2016 = recordsByYear.getOrDefault("2016", new HashSet<>());
//        Set<Match> data2017 = recordsByYear.getOrDefault("2017", new HashSet<>());
//        Set<Match> data2018 = recordsByYear.getOrDefault("2018", new HashSet<>());
//        Set<Match> data2019 = recordsByYear.getOrDefault("2019", new HashSet<>());
//        Set<Match> data2020 = recordsByYear.getOrDefault("2020", new HashSet<>());
//
//
//        // ======================================================================
//        // 6. Generowanie przykładowych podsumowań dwupodmiotowych (Podmiot 1: MECZE Z ROKU X, Podmiot 2: MECZE Z ROKU Y)
//        // ======================================================================
//
//        System.out.println("\n--- PRZYKŁADOWE PODSUMOWANIA DWUPODMIOTOWE (PORÓWNANIE LAT) ---");
//
//        // Kwantyfikatory do użycia (możemy użyć tych samych względnych)
//        // Kwantyfikatory niewieleQuantifier, okoloPolowaQuantifier, prawieWszystkieQuantifier są już zdefiniowane.
//
//
//        // --- Podsumowanie dwupupodmiotowe - Forma 1 ---
//        // Q meczy z roku 2018 w porównaniu do meczy z roku 2017 jest/są długich.
//        // SUMARYZATOR JEST TERAZ POJEDYNCZY
//        Map.Entry<String, String> form1Summarizer = new AbstractMap.SimpleEntry<>("czas trwania meczu", "ponad 3 godziny");
//
//        for(Quantifier quantifier : relativeQuantifiers) {
//            TwoSubjectLinguisticSummary twoSubjectSummaryYear1 = new TwoSubjectLinguisticSummary(
//                    "meczy z roku 2018", "meczy z roku 2017",
//                    allLVs,
//                    quantifier, // Podany kwantyfikator
//                    form1Summarizer // Przekazujemy POJEDYNCZY sumaryzator
//            );
//
//            double truthDegreeFormYear1 = twoSubjectSummaryYear1.calculateTruthDegreeForForm1(data2018, data2017);
//            System.out.println("\nPodsumowanie dwupodmiotowe (Forma 1 - por. lat):");
//            System.out.println(twoSubjectSummaryYear1.generateSummaryTextForm1());
//            System.out.printf("  Stopień prawdziwości: %.2f%n", truthDegreeFormYear1);
//        }
//
//        // To było powtórzone wywołanie dla niewieleQuantifier, usunąłem pętlę i zostawiłem jedno wywołanie dla przykładu.
//        // Jeśli potrzebujesz powtórzyć, wrzuć to w pętlę.
//        TwoSubjectLinguisticSummary twoSubjectSummaryYear1Single = new TwoSubjectLinguisticSummary(
//                "meczy z roku 2018", "meczy z roku 2017",
//                allLVs,
//                niewieleQuantifier, // Podany kwantyfikator
//                form1Summarizer // Przekazujemy POJEDYNCZY sumaryzator
//        );
//
//        double truthDegreeFormYear1Single = twoSubjectSummaryYear1Single.calculateTruthDegreeForForm1(data2018, data2017);
//        System.out.println("\nPodsumowanie dwupodmiotowe (Forma 1 - por. lat, pojedyncze wywołanie):");
//        System.out.println(twoSubjectSummaryYear1Single.generateSummaryTextForm1());
//        System.out.printf("  Stopień prawdziwości: %.2f%n", truthDegreeFormYear1Single);
//
//
//        // --- Podsumowanie dwupodmiotowe - Forma 2 ---
//        // Q meczy z roku 2019 w porównaniu do tych meczy z roku 2020, które miały około 20 asów, jest/są z niewielu błędami.
//        // SUMARYZATOR JEST TERAZ POJEDYNCZY
//        Map.Entry<String, String> form2Summarizer = new AbstractMap.SimpleEntry<>("liczba podwójnych błędów serwisowych", "mniej niż 5");
//        Map.Entry<String, String> form2Qualifier2 = new AbstractMap.SimpleEntry<>("liczba asów", "około 20");
//
//        for (Quantifier quantifier : relativeQuantifiers) {
//            TwoSubjectLinguisticSummary twoSubjectSummaryYear2 = new TwoSubjectLinguisticSummary(
//                    "meczy z roku 2019", "meczy z roku 2020",
//                    allLVs,
//                    quantifier, // Podany kwantyfikator
//                    form2Qualifier2, // Kwalifikator dla P2
//                    form2Summarizer, // Pojedynczy sumaryzator
//                    true // Flaga: to jest Forma 2
//            );
//
//            double truthDegreeFormYear2 = twoSubjectSummaryYear2.calculateTruthDegreeForForm2(data2019, data2020);
//            System.out.println("\nPodsumowanie dwupodmiotowe (Forma 2 - por. lat):");
//            System.out.println(twoSubjectSummaryYear2.generateSummaryTextForm2());
//            System.out.printf("  Stopień prawdziwości: %.2f%n", truthDegreeFormYear2);
//        }
//
//        TwoSubjectLinguisticSummary twoSubjectSummaryYear2 = new TwoSubjectLinguisticSummary(
//                "meczy z roku 2019", "meczy z roku 2020",
//                allLVs,
//                okoloPolowaQuantifier, // Podany kwantyfikator
//                form2Qualifier2, // Kwalifikator dla P2
//                form2Summarizer, // Pojedynczy sumaryzator
//                true // Flaga: to jest Forma 2
//        );
//
//        double truthDegreeFormYear2 = twoSubjectSummaryYear2.calculateTruthDegreeForForm2(data2019, data2020);
//        System.out.println("\nPodsumowanie dwupodmiotowe (Forma 2 - por. lat):");
//        System.out.println(twoSubjectSummaryYear2.generateSummaryTextForm2());
//        System.out.printf("  Stopień prawdziwości: %.2f%n", truthDegreeFormYear2);
//
//
//        // --- Podsumowanie dwupodmiotowe - Forma 3 ---
//        // Q meczy z roku 2015, które miały zwycięzcę o wzroście około 180 cm, w porównaniu do meczy z roku 2016 jest/są krótkich.
//        // SUMARYZATOR JEST TERAZ POJEDYNCZY
//        Map.Entry<String, String> form3Summarizer = new AbstractMap.SimpleEntry<>("czas trwania meczu", "krótszy niż 40 min");
//        Map.Entry<String, String> form3Qualifier1 = new AbstractMap.SimpleEntry<>("wzrost zwycięzcy", "około 180 cm");
//
//        for(Quantifier quantifier : relativeQuantifiers) {
//            TwoSubjectLinguisticSummary twoSubjectSummaryYear3 = new TwoSubjectLinguisticSummary(
//                    "meczy z roku 2015", "meczy z roku 2016",
//                    allLVs,
//                    quantifier, // Podany kwantyfikator
//                    form3Qualifier1, // Kwalifikator dla P1 (pamiętaj, że w konstruktorze jest pierwszy)
//                    form3Summarizer, // Pojedynczy sumaryzator
//                    false // Flaga: to jest Forma 3 (czyli NIE Forma 2)
//            );
//
//            double truthDegreeFormYear3 = twoSubjectSummaryYear3.calculateTruthDegreeForForm3(data2015, data2016);
//            System.out.println("\nPodsumowanie dwupodmiotowe (Forma 3 - por. lat):");
//            System.out.println(twoSubjectSummaryYear3.generateSummaryTextForm3());
//            System.out.printf("  Stopień prawdziwości: %.2f%n", truthDegreeFormYear3);
//        }
//
//        TwoSubjectLinguisticSummary twoSubjectSummaryYear3 = new TwoSubjectLinguisticSummary(
//                "meczy z roku 2015", "meczy z roku 2016",
//                allLVs,
//                prawieWszystkieQuantifier, // Podany kwantyfikator
//                form3Qualifier1, // Kwalifikator dla P1 (pamiętaj, że w konstruktorze jest pierwszy)
//                form3Summarizer, // Pojedynczy sumaryzator
//                false // Flaga: to jest Forma 3 (czyli NIE Forma 2)
//        );
//
//        double truthDegreeFormYear3 = twoSubjectSummaryYear3.calculateTruthDegreeForForm3(data2015, data2016);
//        System.out.println("\nPodsumowanie dwupodmiotowe (Forma 3 - por. lat):");
//        System.out.println(twoSubjectSummaryYear3.generateSummaryTextForm3());
//        System.out.printf("  Stopień prawdziwości: %.2f%n", truthDegreeFormYear3);
//
//
//        // --- Podsumowanie dwupodmiotowe - Forma 4 ---
//        // Q meczy z roku 2018 niż meczy z roku 2019 jest/są z wieloma asami i około 40 gemami.
//        // SUMARYZATOR JEST TERAZ POJEDYNCZY
//        // To jest przykład z oryginalnego kodu, który miał wiele sumaryzatorów.
//        // Zgodnie z nową logiką, możesz mieć TYLKO JEDEN sumaryzator.
//        // Wybieram jeden z nich jako przykład, musisz zdecydować, który jest właściwy.
//        // --- Podsumowanie dwupodmiotowe - Forma 4 (SPECJALNA) ---
//        // Q meczy z roku 2018 niż meczy z roku 2019 jest/są z wieloma asami i około 40 gemami.
//        // WAŻNE: Forma 4 teraz przyjmuje tylko jeden sumaryzator, BEZ KWANTYFIKATORA
//        Map.Entry<String, String> form4Summarizer = new AbstractMap.SimpleEntry<>("liczba asów", "około 20");
//        // Jeśli nadal potrzebujesz złożonych sumaryzatorów (np. "liczba asów I liczba gemów"),
//        // musisz zaimplementować to w logice "getMembership" lub w predykacie.
//        // Obecnie obsługiwany jest tylko jeden predykat jako sumaryzator dla Formy 4.
//
//        TwoSubjectLinguisticSummary twoSubjectSummaryYear4 = new TwoSubjectLinguisticSummary(
//                "meczy z roku 2019", "meczy z roku 2018",
//                allLVs,
//                form4Summarizer // Tylko sumaryzator, BEZ KWANTYFIKATORA
//        );
//
//        double truthDegreeFormYear4 = twoSubjectSummaryYear4.calculateTruthDegreeForForm4(allRecords, data2018, data2019);
//        System.out.println("\nPodsumowanie dwupodmiotowe (Forma 4 - por. lat):");
//        System.out.println(twoSubjectSummaryYear4.generateSummaryTextForm4()); // Użyj nowej metody
//        System.out.printf("  Stopień prawdziwości (Forma 4 - spec.): %.2f%n", truthDegreeFormYear4);
//    }
//}