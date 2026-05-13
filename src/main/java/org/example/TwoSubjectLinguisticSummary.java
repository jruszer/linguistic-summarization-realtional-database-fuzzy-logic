package org.example;

import java.util.Map;
import java.util.Set;

public class TwoSubjectLinguisticSummary {

    private String subject1;
    private String subject2;
    private Map<String, LinguisticVariable<Double>> linguisticVariables;
    private Quantifier quantifier;
    private Map.Entry<String, String> summarizer;
    private Map.Entry<String, String> qualifier1;
    private Map.Entry<String, String> qualifier2;

    private boolean isForm4;


    public TwoSubjectLinguisticSummary(String subject1, String subject2, Map<String, LinguisticVariable<Double>> linguisticVariables,
                                       Quantifier quantifier, Map.Entry<String, String> summarizer) {
        this(subject1, subject2, linguisticVariables, quantifier);
        setSummarizer(summarizer);
        this.qualifier1 = null;
        this.qualifier2 = null;
        this.isForm4 = false;
    }


    public TwoSubjectLinguisticSummary(String subject1, String subject2, Map<String, LinguisticVariable<Double>> linguisticVariables, Quantifier quantifier,
                                       Map.Entry<String, String> qualifier, Map.Entry<String, String> summarizer, boolean isForm2) {
        this(subject1, subject2, linguisticVariables, quantifier);
        setSummarizer(summarizer);

        if (isForm2) {
            setQualifier2(qualifier);
            this.qualifier1 = null;
        } else {
            setQualifier1(qualifier);
            this.qualifier2 = null;
        }
        this.isForm4 = false;
    }

    public TwoSubjectLinguisticSummary(String subject1, String subject2, Map<String, LinguisticVariable<Double>> linguisticVariables, Map.Entry<String, String> summarizer) {
        this(subject1, subject2, linguisticVariables, (Quantifier) null);
        setSummarizer(summarizer);
        this.qualifier1 = null;
        this.qualifier2 = null;
        this.isForm4 = true;
    }


    private TwoSubjectLinguisticSummary(String subject1, String subject2, Map<String, LinguisticVariable<Double>> linguisticVariables, Quantifier quantifier) {
        this.subject1 = subject1;
        this.subject2 = subject2;

        this.linguisticVariables = linguisticVariables;

        this.quantifier = quantifier;
    }


    private void setSummarizer(Map.Entry<String, String> info) {
        this.summarizer = info;
    }

    private void setQualifier1(Map.Entry<String, String> info) {
        this.qualifier1 = info;
    }

    private void setQualifier2(Map.Entry<String, String> info) {
        this.qualifier2 = info;
    }


    private double getMembership(Match record, Map.Entry<String, String> predicateInfo) {
        if (predicateInfo == null) {
            return 1.0;
        }

        String variableName = predicateInfo.getKey();
        String labelName = predicateInfo.getValue();

        LinguisticVariable<Double> lv = linguisticVariables.get(variableName);
        if (lv == null) {
            return 0.0;
        }
        FuzzySet<Double> fuzzySet = lv.getLabel(labelName);
        if (fuzzySet == null) {
            return 0.0;
        }
        Double recordValue = null;
        switch (variableName) {
            case "wzrost zwycięzcy":
                recordValue = record.getNumericValue("winner_ht");
                break;
            case "wzrost przegranego":
                recordValue = record.getNumericValue("loser_ht");
                break;
            case "wiek zwycięzcy":
                recordValue = record.getNumericValue("winner_age");
                break;
            case "wiek przegranego":
                recordValue = record.getNumericValue("loser_age");
                break;
            case "czas trwania meczu":
                recordValue = record.getNumericValue("minutes");
                break;
            case "ranking zwycięzcy":
                recordValue = record.getNumericValue("winner_rank");
                break;
            case "ranking przegranego":
                recordValue = record.getNumericValue("loser_rank");
                break;
            case "liczba asów":
                recordValue = record.getNumericValue("aces");
                break;
            case "liczba podwójnych błędów serwisowych":
                recordValue = record.getNumericValue("df");
                break;
            case "liczba gemów":
                recordValue = record.getNumericValue("games");
                break;
            default:
                recordValue = record.getNumericValue(variableName);
                break;
        }

        if (recordValue != null) {
            return fuzzySet.getMembershipX(recordValue);
        } else {
            return 0.0;
        }
    }

    private String singleInfoForText(Map.Entry<String, String> info) {
        return info.getKey() + " " + info.getValue();
    }


    public String generateSummaryTextForm1() {
        return this.quantifier.getLabel() + " meczy z " + subject1 + " w porównaniu do meczy z " + subject2 + " jest/są " +
                singleInfoForText(summarizer) + ".";
    }

    public String generateSummaryTextForm2() {
        return this.quantifier.getLabel() + " meczy z " + subject1 + " w porównaniu do tych meczy z " + subject2 + ", które są " +
                singleInfoForText(qualifier2) + ", jest/są " +
                singleInfoForText(summarizer) + ".";
    }

    public String generateSummaryTextForm3() {
        return this.quantifier.getLabel() + " meczy z " + subject1 + ", które są " +
                singleInfoForText(qualifier1) + ", w porównaniu do meczy z " + subject2 + " jest/są " +
                singleInfoForText(summarizer) + ".";
    }

    public String generateSummaryTextForm4() {
        return "Więcej meczy z " + subject1 + " niż meczy z " + subject2 + " jest " +
                singleInfoForText(summarizer) + ".";
    }

    public double calculateTfor1(Set<Match> data1, Set<Match> data2) {
        double sumS_P1 = 0.0;
        for (Match record : data1) {
            sumS_P1 += getMembership(record, summarizer);
        }
        //System.out.println("Membership for record in data1: " + sumS_P1);
        double sumS_P2 = 0.0;
        for (Match record : data2) {
            sumS_P2 += getMembership(record, summarizer);
        }
        //System.out.println("Membership for record in data2: " + sumS_P2);
        return this.quantifier.calculateT((sumS_P1 / data1.size())/(sumS_P1/ data1.size() + sumS_P2 / data2.size()));
    }

    public double calculateTfor2(Set<Match> data1, Set<Match> data2) {
        double sumS_P1 = 0.0;
        for (Match record : data1) {
            sumS_P1 += getMembership(record, summarizer);
        }

        double sumIntersectionS_W_P2 = 0.0;
        for (Match record : data2) {
            double membershipW = getMembership(record, qualifier2);
            double membershipS = getMembership(record, summarizer);
            sumIntersectionS_W_P2 += Math.min(membershipW, membershipS);
        }

        return this.quantifier.calculateT((sumS_P1 / data1.size())/(sumS_P1/ data1.size() + sumIntersectionS_W_P2 / data2.size()));
    }

    public double calculateTfor3(Set<Match> data1, Set<Match> data2) {
        double sumIntersectionS_W_P1 = 0.0;
        for (Match record : data1) {
            double membershipW = getMembership(record, qualifier1);
            double membershipS = getMembership(record, summarizer);
            sumIntersectionS_W_P1 += Math.min(membershipW, membershipS);
        }

        double sumS_P2 = 0.0;
        for (Match record : data2) {
            sumS_P2 += getMembership(record, summarizer);
        }

        return this.quantifier.calculateT((sumIntersectionS_W_P1 / data1.size())/(sumIntersectionS_W_P1/ data1.size() + sumS_P2 / data2.size()));
    }

    public double calculateTfor4(Set<Match> allRecords, Set<Match> dataP1, Set<Match> dataP2) {

        if (allRecords.isEmpty()) {
            return 0.0;
        }

        double sumImplications = 0.0;

        for (Match r : allRecords) {
            double implication = 0.0;

            double muP1 ;
            if (dataP1.contains(r)) {
                muP1 = getMembership(r, summarizer);
            }
            else {
                muP1 = 0.0;
            }

            double muP2 ;
            if (dataP2.contains(r)) {
                muP2 = getMembership(r, summarizer);
            }
            else {
                muP2 = 0.0;
            }

            if(dataP1.contains(r) || dataP2.contains(r)) {
                implication = Math.min(1.0, 1 - muP2 + muP1);
            }

            sumImplications += implication;
        }
        double m = sumImplications / (dataP1.size() + dataP2.size());

        return 1.0 - m;
    }
}