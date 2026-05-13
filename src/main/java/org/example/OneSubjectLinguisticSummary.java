package org.example;

import java.util.*;

public class OneSubjectLinguisticSummary {

    private String subject;
    private Map<String, LinguisticVariable<Double>> linguisticVariables;
    private List<Map.Entry<String, String>> summarizes;
    private Map.Entry<String, String> qualifier;
    private Quantifier quantifier;

    public OneSubjectLinguisticSummary(String subject, Map<String, LinguisticVariable<Double>> linguisticVariables,
                                       Quantifier quantifier,
                                       Map.Entry<String, String> qualifier, List<Map.Entry<String, String>> summarizes) {

        Objects.requireNonNull(subject, "Podmiot nie może być null.");
        if (subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Podmiot nie może być pusty.");
        }
        Objects.requireNonNull(linguisticVariables, "Mapa zmiennych lingwistycznych nie może być null.");
        if (linguisticVariables.isEmpty()) {
            throw new IllegalArgumentException("Mapa zmiennych lingwistycznych nie może być pusta.");
        }
        Objects.requireNonNull(quantifier, "Kwantyfikator nie może być null.");
        Objects.requireNonNull(summarizes, "Lista sumaryzatorów nie może być null.");
        if (summarizes.isEmpty()) {
            throw new IllegalArgumentException("Lista sumaryzatorów nie może być pusta.");
        }
        if (summarizes.size() > 4) {
            throw new IllegalArgumentException("Maksymalnie 4 sumaryzatory są dozwolone.");
        }

        this.subject = subject;
        this.linguisticVariables = linguisticVariables;
        this.quantifier = quantifier;
        this.summarizes = new ArrayList<>(summarizes);
        this.qualifier = qualifier;

        for (Map.Entry<String, String> s : summarizes) {
            validatePredicateInfo(s, "sumaryzatora");
        }
        if (qualifier != null) {
            validatePredicateInfo(qualifier, "kwalifikatora");
        }
    }

    public OneSubjectLinguisticSummary(String subject, Map<String, LinguisticVariable<Double>> linguisticVariables,
                                       Quantifier quantifier, // TYLKO JEDEN KWANTYFIKATOR
                                       List<Map.Entry<String, String>> summarizes) {
        this(subject, linguisticVariables, quantifier, null, summarizes);
    }

    private void validatePredicateInfo(Map.Entry<String, String> info, String type) {
        if (info == null || info.getKey() == null || info.getKey().trim().isEmpty() ||
                info.getValue() == null || info.getValue().trim().isEmpty()) {
            throw new IllegalArgumentException("Informacje o " + type + " (kolumna lub etykieta) są niepoprawne.");
        }
        LinguisticVariable<Double> lv = linguisticVariables.get(info.getKey());
        if (lv == null) {
            throw new IllegalArgumentException("Brak zmiennej lingwistycznej dla kolumny " + type + " '" + info.getKey() + "'.");
        }
        if (!lv.getLabelNames().contains(info.getValue())) {
            throw new IllegalArgumentException("Etykieta " + type + " '" + info.getValue() + "' nie istnieje w zmiennej lingwistycznej dla kolumny '" + info.getKey() + "'.");
        }
    }


    public String writeSummary() {
        StringBuilder summarizersSb = new StringBuilder();
        for (int i = 0; i < summarizes.size(); i++) {
            Map.Entry<String, String> predicateInfo = summarizes.get(i);
            String variableName = predicateInfo.getKey();
            String labelName = predicateInfo.getValue();
            summarizersSb.append(variableName).append(" ").append(labelName);
            if (i < summarizes.size() - 1) {
                summarizersSb.append(" i ");
            }
        }
        String summarizersJoined = summarizersSb.toString();
        StringBuilder sb = new StringBuilder();

        sb.append(quantifier.getLabel());
        if(subject.equals("mecze"))
            sb.append(" ").append(subject);
        else
            sb.append(" mecze z ").append(subject);

        if (qualifier != null) {
            sb.append(" będące/mające ");
            String variableName = qualifier.getKey();
            String labelName = qualifier.getValue();
            sb.append(variableName).append(" ").append(labelName);
        }
        sb.append(" są/mają ");
        sb.append(summarizersJoined);
        sb.append(".");

        return sb.toString();
    }

    private double calculateNormalizedValue(Set<Match> data) {
        double sumForS = 0.0;
        double sumForW = 0.0;
        double sumInt = 0.0;
        boolean hasQualifier = (qualifier != null);

        for (Match record : data) {
            double membershipS = 1.0;
            for (Map.Entry<String, String> s : summarizes) {
                LinguisticVariable<Double> lv = linguisticVariables.get(s.getKey());
                if (lv == null || !lv.getLabelNames().contains(s.getValue())) {
                    membershipS = 0.0;
                    break;
                }
                FuzzySet<Double> fuzzy = lv.getLabel(s.getValue());

                Double colValue = switch (s.getKey()) {
                    case "wzrost zwycięzcy" -> record.getNumericValue("winner_ht");
                    case "wzrost przegranego" -> record.getNumericValue("loser_ht");
                    case "wiek zwycięzcy" -> record.getNumericValue("winner_age");
                    case "wiek przegranego" -> record.getNumericValue("loser_age");
                    case "czas trwania meczu" -> record.getNumericValue("minutes");
                    case "ranking zwycięzcy" -> record.getNumericValue("winner_rank");
                    case "ranking przegranego" -> record.getNumericValue("loser_rank");
                    case "liczba asów" -> record.getNumericValue("aces");
                    case "liczba podwójnych błędów serwisowych" -> record.getNumericValue("df");
                    case "liczba gemów" -> record.getNumericValue("games");
                    default -> {
                        membershipS = 0.0;
                        yield null;
                    }
                };
                if (colValue != null) {
                    membershipS = Math.min(membershipS, fuzzy.getMembershipX(colValue));
                } else {
                    membershipS = 0.0;
                }

            }
            sumForS += membershipS;

            double membershipW = 1.0;
            if (hasQualifier) {
                LinguisticVariable<Double> lvQ = linguisticVariables.get(qualifier.getKey());
                FuzzySet<Double> fsQ = lvQ.getLabel(qualifier.getValue());

                Double valueFromMatchQ = switch (qualifier.getKey()) {
                    case "wzrost zwycięzcy" -> record.getNumericValue("winner_ht");
                    case "wzrost przegranego" -> record.getNumericValue("loser_ht");
                    case "wiek zwycięzcy" -> record.getNumericValue("winner_age");
                    case "wiek przegranego" -> record.getNumericValue("loser_age");
                    case "czas trwania meczu" -> record.getNumericValue("minutes");
                    case "ranking zwycięzcy" -> record.getNumericValue("winner_rank");
                    case "ranking przegranego" -> record.getNumericValue("loser_rank");
                    case "liczba asów" -> record.getNumericValue("aces");
                    case "liczba podwójnych błędów serwisowych" -> record.getNumericValue("df");
                    case "liczba gemów" -> record.getNumericValue("games");
                    default -> {
                        membershipW = 0.0;
                        yield null;
                    }
                };
                membershipW = fsQ.getMembershipX(valueFromMatchQ);

            }
            sumForW += membershipW;
            if (hasQualifier) {
                sumInt += Math.min(membershipS, membershipW);
            }
        }

        if (quantifier instanceof RelativeQuantifier) {
            if (hasQualifier) {
                return sumInt / sumForW;
            } else {
                return sumForS / data.size();
            }
        } else {
            return sumForS;
        }
    }


    public double calculateT1(Set<Match> data) {
        double normalizedValue = calculateNormalizedValue(data);
        return quantifier.calculateT(normalizedValue);
    }

    public double calculateT2() {
        double in = -1.0;
        int n = 0;

        for (Map.Entry<String, String> sInfo : summarizes) {
            String columnName = sInfo.getKey();
            String label = sInfo.getValue();

            LinguisticVariable<Double> lv = linguisticVariables.get(columnName);
            if (in == -1) {
                //System.out.println("dasdasdasdas" + lv.getLabel(label).getSupp().getElements().size());
                in = lv.getLabel(label).getSupp().getElements().size() / ((lv.getUoD().getMaxX() - lv.getUoD().getMinX())*100);
                n = 1;
                       // (double) lv.getUniverse().getElements().size();
            } else {
                in *= lv.getLabel(label).getSupp().getElements().size() / ((lv.getUoD().getMaxX() - lv.getUoD().getMinX())*100);
                n++;
                //(double) lv.getUniverse().getElements().size();
            }
        }

        return 1 - Math.pow(in, 1.0 / n);
    }

    public double calculateT3(Set<Match> data) {
        int countIntersection = 0;
        int countQualifier = data.size();

        for (Match record : data) {
            boolean satisfiesQualifier = (qualifier == null) || checkSatisfiesPredicate(record, qualifier);

            if (satisfiesQualifier) {
                boolean satisfiesAllSummarizers = true;
                for (Map.Entry<String, String> s : summarizes) {
                    if (!checkSatisfiesPredicate(record, s)) {
                        satisfiesAllSummarizers = false;
                        break;
                    }
                }

                if (satisfiesAllSummarizers) {
                    countIntersection++;
                }
            } else {
                countQualifier--;
            }
        }

        if (countQualifier == 0) {
            return 0.0;
        }

        return (double) countIntersection / countQualifier;
    }

    private boolean checkSatisfiesPredicate(Match record, Map.Entry<String, String> predicate) {
        String columnName = predicate.getKey();
        String labelName = predicate.getValue();

        LinguisticVariable<Double> lv = linguisticVariables.get(columnName);
        if (lv == null) {
            return false;
        }

        FuzzySet<Double> fuzzySet = lv.getLabel(labelName);
        if (fuzzySet == null) {
            return false;
        }

        Double value = getValueFromRecord(record, columnName);
        if (value == null) {
            return false;
        }

        return fuzzySet.getMembershipX(value) > 0;
    }

    private Double getValueFromRecord(Match record, String columnName) {
        return switch (columnName) {
            case "wzrost zwycięzcy" -> record.getNumericValue("winner_ht");
            case "wzrost przegranego" -> record.getNumericValue("loser_ht");
            case "wiek zwycięzcy" -> record.getNumericValue("winner_age");
            case "wiek przegranego" -> record.getNumericValue("loser_age");
            case "czas trwania meczu" -> record.getNumericValue("minutes");
            case "ranking zwycięzcy" -> record.getNumericValue("winner_rank");
            case "ranking przegranego" -> record.getNumericValue("loser_rank");
            case "liczba asów" -> record.getNumericValue("aces");
            case "liczba podwójnych błędów serwisowych" -> record.getNumericValue("df");
            case "liczba gemów" -> record.getNumericValue("games");
            default -> null;
        };
    }

    public double calculateT4(Set<Match> data) {
//        if(qualifierPredicateInfo == null) {
//            return -1;
//        }
        if (summarizes.size() == 1) {
            return 0.00;
        }
        ClassicSet<Double> supp;
        double r = -1;
        for (Map.Entry<String, String> sInfo : summarizes) {
            String columnName = sInfo.getKey();
            String label = sInfo.getValue();
            LinguisticVariable<Double> lv = linguisticVariables.get(columnName);

            supp = lv.getLabel(label).getSupp();
            if (r == -1) {
                r = (double) supp.getElements().size()
                        / ((lv.getUoD().getMaxX() - lv.getUoD().getMinX())*100);
            } else {
                r *= (double) supp.getElements().size() / ((lv.getUoD().getMaxX() - lv.getUoD().getMinX())*100);
            }
        }

        return Math.abs(r - calculateT3(data));
    }

    public double calculateT5() {
        int numOfS = 0;
        for (Map.Entry<String, String> ignored : summarizes) {
            numOfS++;
        }
        return 2 * Math.pow(0.5, numOfS);
    }

    public double calculateT6(Set<Match> data) { //TU CHYBA POPRAWIĆ to data size????
        if (quantifier instanceof AbsoluteQuantifier) {
            return 1 - quantifier.getFuzzySet().getCard() / 12046;
        }
        return 1 - quantifier.getFuzzySet().getCard();
    }

    public double calculateT7() {
//        double sum = 0.0;
//        for (Map.Entry<String, String> sInfo : summarizes) {
//            String columnName = sInfo.getKey();
//            String label = sInfo.getValue();
//            LinguisticVariable<Double> lv = linguisticVariables.get(columnName);
//            sum += lv.getLabel(label).getDegOfFuzz();
//        }
        return 1 - quantifier.getFuzzySet().getCard() / ((quantifier.getFuzzySet().getUoD().getMaxX() - quantifier.getFuzzySet().getUoD().getMinX()));
        //return sum / summarizes.size();
    }

    public double calculateT8() {
        double gora = 1.0;
        double dol = 1.0;
        for (Map.Entry<String, String> sInfo : summarizes) {
            String columnName = sInfo.getKey();
            String label = sInfo.getValue();
            LinguisticVariable<Double> lv = linguisticVariables.get(columnName);
            gora *= lv.getLabel(label).getCard();
            dol *= lv.getLabel(label).getUoD().getMaxX() - lv.getLabel(label).getUoD().getMinX();
        }
        return 1 - Math.pow(gora / dol, 1.0 / summarizes.size());
    }


    public double calculateT9() {
            if(qualifier == null) {
                return 0;
            }
        LinguisticVariable<Double> lv = linguisticVariables.get(qualifier.getKey()); //kolumna
        FuzzySet<Double> fuzzySet = lv.getLabel(qualifier.getValue()); //label
        return 1 - fuzzySet.getSupp().getElements().size() / (double) ((lv.getUoD().getMaxX() - lv.getUoD().getMinX())*100);
    }

    public double calculateT10() {
            if(qualifier == null) {
                return 0;
            }
        LinguisticVariable<Double> lv = linguisticVariables.get(qualifier.getKey()); //kolumna
        FuzzySet<Double> fuzzySet = lv.getLabel(qualifier.getValue()); //label
        //System.out.println("FuzzySet: " + fuzzySet.getCard() + " UoD: " + (lv.getUoD().getMaxX() - lv.getUoD().getMinX())*100);
        return 1 - fuzzySet.getCard() /((lv.getUoD().getMaxX() - lv.getUoD().getMinX()));
    }

    public double calculateT11() {
            if(qualifier == null) {
                return 1;
            }
        return 2 * Math.pow(0.5, 1);
    }

    public double calculateT(Set<Match> data, double w1, double w2, double w3, double w4, double w5, double w6, double w7, double w8, double w9, double w10, double w11) {
            double T1 = calculateT1(data);
            double T2 = calculateT2();
            double T3 = calculateT3(data);
            double T4 = calculateT4(data);
            double T5 = calculateT5();
            double T6 = calculateT6(data);
            double T7 = calculateT7();
            double T8 = calculateT8();
            double T9 = calculateT9();
            double T10 = calculateT10();
            double T11 = calculateT11();
            return (T1 * w1 + T2 * w2 + T3 * w3 + T4 * w4 + T5 * w5 + T6 * w6 + T7 * w7 + T8 * w8 + T9 * w9 + T10 * w10 + T11 * w11);
            //return (T1 * 0.8 + T2 * 0.02 + T3 * 0.02 + T4 * 0.02 + T5 * 0.02 + T6 * 0.02 + T7 * 0.02 + T8 * 0.02 + T9 * 0.02 + T10 * 0.02 + T11 * 0.02);
    }

}