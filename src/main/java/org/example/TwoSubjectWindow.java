package org.example;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TwoSubjectWindow extends VBox {

    private final Map<String, Set<Match>> allDataSets;
    private final Map<String, LinguisticVariable<Double>> allLV;
    private List<Quantifier> allRelativeQuantifiers;
    private final List<Map.Entry<String, String>> allSume;

    private final ComboBox<String> subject1ComboBox = new ComboBox<>();
    private final ComboBox<String> subject2ComboBox = new ComboBox<>();
    private final ComboBox<Map.Entry<String, String>> qualifierComboBox = new ComboBox<>();
    private final ComboBox<Map.Entry<String, String>> summarizerComboBox = new ComboBox<>();
    private final Button generateButton = new Button("Generuj podsumowania");
    private final Button saveButton = new Button("Zapisz");
    private final TextField saveCountField = new TextField("10");
    private final TextArea resultTextArea = new TextArea();

    private List<SummaryResult> currentResults;
    private final Stage primaryStage;

    public TwoSubjectWindow(Map<String, Set<Match>> allDataSets, Map<String, LinguisticVariable<Double>> allLV, List<Quantifier> allRelativeQuantifiers,
                            List<Map.Entry<String, String>> allSume, Stage primaryStage) {
        this.allDataSets = allDataSets;
        this.allLV = allLV;
        this.allRelativeQuantifiers = allRelativeQuantifiers;
        this.allSume = allSume;
        this.primaryStage = primaryStage;
        this.currentResults = new ArrayList<>();

        init();
        layoutComponents();
        setupEventHandlers();
    }

    private void init() {
        List<String> years = new ArrayList<>(allDataSets.keySet());
        years.remove("mecze ze wszystkich lat");

        subject1ComboBox.setItems(FXCollections.observableArrayList(years));
        subject2ComboBox.setItems(FXCollections.observableArrayList(years));

        if (!years.isEmpty()) {
            subject1ComboBox.getSelectionModel().selectFirst();
            subject2ComboBox.getSelectionModel().select(years.size() > 1 ? 1 : 0);
        }

        List<Map.Entry<String, String>> qualifierOptions = new ArrayList<>();
        qualifierOptions.add(new AbstractMap.SimpleEntry<>("Nie", ""));
        qualifierOptions.addAll(allSume);

        qualifierComboBox.setItems(FXCollections.observableArrayList(qualifierOptions));
        qualifierComboBox.setConverter(new SummString());
        qualifierComboBox.getSelectionModel().selectFirst();

        summarizerComboBox.setItems(FXCollections.observableArrayList(allSume));
        summarizerComboBox.setConverter(new SummString());
        summarizerComboBox.setPromptText("Wybierz");

        resultTextArea.setEditable(false);
        resultTextArea.setWrapText(true);
        resultTextArea.setPrefHeight(400);

        saveButton.setDisable(true);
        saveCountField.setPrefWidth(50);
    }

    private void layoutComponents() {
        VBox mainContainer = new VBox(10);
        mainContainer.setAlignment(Pos.TOP_LEFT);
        mainContainer.setPadding(new Insets(15));

        HBox subjectsBox = new HBox(20);
        VBox subject1Box = new VBox(5, new Label("Z których lat chcesz porównać mecze?"), subject1ComboBox);
        VBox subject2Box = new VBox(5, new Label(""), subject2ComboBox);
        subjectsBox.getChildren().addAll(subject1Box, subject2Box);


        VBox qualifierBox = new VBox(10);
        qualifierBox.setAlignment(Pos.TOP_LEFT);
        qualifierBox.getChildren().addAll(
                new VBox(5, new Label("Czy chcesz dodatkowo ograniczyć mecze?"), qualifierComboBox)
        );

        VBox summarizerBox = new VBox(5, new Label("Jakie mecze cię interesują?"), summarizerComboBox);
        summarizerBox.setAlignment(Pos.TOP_LEFT);

        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(Pos.CENTER_LEFT);
        buttonsBox.getChildren().addAll(generateButton,new HBox(10, new Label("Zapisz:"), saveCountField),saveButton);

        VBox resultsBox = new VBox(5);
        resultsBox.setAlignment(Pos.TOP_LEFT);
//        ScrollPane scrollPane = new ScrollPane(resultTextArea);
//        scrollPane.setFitToWidth(true);
//        scrollPane.setPrefHeight(300);
        resultsBox.getChildren().addAll(new Label("Wyniki:"), resultTextArea);

        mainContainer.getChildren().addAll(
                subjectsBox,
                qualifierBox,
                summarizerBox,
                buttonsBox,
                resultsBox
        );

        this.getChildren().add(mainContainer);
    }

    private void setupEventHandlers() {
        generateButton.setOnAction(e -> generateAllSummaries());
        saveButton.setOnAction(e -> saveToTxt());
    }


    private void generateAllSummaries() {
        String selectedSubject1Name = subject1ComboBox.getValue();
        String selectedSubject2Name = subject2ComboBox.getValue();
        Map.Entry<String, String> selectedSummarizer = summarizerComboBox.getValue();

        if (selectedSummarizer == null) {
            showAlert("Błąd", "Wybierz cechę");
            return;
        }

        if (selectedSubject1Name.equals(selectedSubject2Name)) {
            showAlert("Błąd", "Porównywane lata muszą być różne.");
            return;
        }

        Set<Match> data1 = allDataSets.get(selectedSubject1Name);
        Set<Match> data2 = allDataSets.get(selectedSubject2Name);

        if (data1 == null || data1.isEmpty() || data2 == null || data2.isEmpty()) {
            showAlert("Błąd", "Brak danych dla wybranych lat");
            return;
        }

        try {
            currentResults.clear();

            Map.Entry<String, String> selectedQualifier = getActualQualifier(qualifierComboBox);

            generateForm1Summaries(selectedSubject1Name, selectedSubject2Name, selectedSummarizer, data1, data2);

            generateForm4Summary(selectedSubject1Name, selectedSubject2Name, selectedSummarizer, data1, data2);

            if (selectedQualifier != null) {
                generateForm2Summaries(selectedSubject1Name, selectedSubject2Name, selectedQualifier, selectedSummarizer, data1, data2);
                generateForm3Summaries(selectedSubject1Name, selectedSubject2Name, selectedQualifier, selectedSummarizer, data1, data2);
            }


            currentResults.sort((r1, r2) -> Double.compare(r2.truthDegree, r1.truthDegree));

            displayResults();

            saveButton.setDisable(currentResults.isEmpty());

        } catch (Exception ex) {
            showAlert("Błąd", "Błąd: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private Map.Entry<String, String> getActualQualifier(ComboBox<Map.Entry<String, String>> comboBox) {
        return (comboBox.getValue() != null && "Nie".equals(comboBox.getValue().getKey())) ? null : comboBox.getValue();
    }

    private void generateForm1Summaries(String subject1, String subject2,
                                        Map.Entry<String, String> summarizer,
                                        Set<Match> data1, Set<Match> data2) {
        for (Quantifier quantifier : allRelativeQuantifiers) {
            TwoSubjectLinguisticSummary summary = new TwoSubjectLinguisticSummary(
                    subject1, subject2, allLV, quantifier, summarizer);
            String summaryText = summary.generateSummaryTextForm1();
            double truthDegree = summary.calculateTfor1(data1, data2);
            currentResults.add(new SummaryResult(summaryText, truthDegree, quantifier.getLabel(), "Forma 1"));
        }
    }

    private void generateForm2Summaries(String subject1, String subject2,
                                        Map.Entry<String, String> qualifier,
                                        Map.Entry<String, String> summarizer,
                                        Set<Match> data1, Set<Match> data2) {
        for (Quantifier quantifier : allRelativeQuantifiers) {
            TwoSubjectLinguisticSummary summary = new TwoSubjectLinguisticSummary(
                    subject1, subject2, allLV, quantifier, qualifier, summarizer, true);
            String summaryText = summary.generateSummaryTextForm2();
            double truthDegree = summary.calculateTfor2(data1, data2);
            currentResults.add(new SummaryResult(summaryText, truthDegree, quantifier.getLabel(), "Forma 2"));
        }
    }

    private void generateForm3Summaries(String subject1, String subject2,
                                        Map.Entry<String, String> qualifier,
                                        Map.Entry<String, String> summarizer,
                                        Set<Match> data1, Set<Match> data2) {
        for (Quantifier quantifier : allRelativeQuantifiers) {
            TwoSubjectLinguisticSummary summary = new TwoSubjectLinguisticSummary(
                    subject1, subject2, allLV, quantifier, qualifier, summarizer, false);
            String summaryText = summary.generateSummaryTextForm3();
            double truthDegree = summary.calculateTfor3(data1, data2);
            currentResults.add(new SummaryResult(summaryText, truthDegree, quantifier.getLabel(), "Forma 3"));
        }
    }

    private void generateForm4Summary(String subject1, String subject2,
                                      Map.Entry<String, String> summarizer,
                                      Set<Match> data1, Set<Match> data2) {
        TwoSubjectLinguisticSummary summary = new TwoSubjectLinguisticSummary(
                subject1, subject2, allLV, summarizer);
        String summaryText = summary.generateSummaryTextForm4();
        Set<Match> matches = new HashSet<>();
        for (Set<Match> match : allDataSets.values()) {
            matches.addAll(match);
        }
        double truthDegree = summary.calculateTfor4(matches, data1, data2);
        currentResults.add(new SummaryResult(summaryText, truthDegree, "Brak", "Forma 4"));
    }

    private void displayResults() {
        resultTextArea.clear();
        resultTextArea.appendText("Wyniki posortowane od najwyższego stopnia prawdziwości:\n\n");

        for (SummaryResult result : currentResults) {
            resultTextArea.appendText(String.format("Stopień prawdziwości: %.4f | %s\n",
                    result.truthDegree, result.formName));
            resultTextArea.appendText(result.summaryText + "\n");
            resultTextArea.appendText("--------------------------------\n");
        }
    }

    private void saveToTxt() {
        try {
            int countToSave;
            try {
                countToSave = Integer.parseInt(saveCountField.getText());
                if (countToSave <= 0) {
                    showAlert("Błąd", "Liczba wyników do zapisania musi być większa od 0");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Błąd", "Wprowadź poprawną liczbę wyników do zapisania");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Zapisz najlepsze podsumowania");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Pliki tekstowe (*.txt)", "*.txt"));
            File file = fileChooser.showSaveDialog(primaryStage);

            if (file != null) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    int count = Math.min(countToSave, currentResults.size());
                    writer.write("Najlepsze " + count + " podsumowań dwupodmiotowych\n");
                    writer.write("Podmiot P1: " + subject1ComboBox.getValue() + "\n");
                    writer.write("Podmiot P2: " + subject2ComboBox.getValue() + "\n");
                    writer.write("Sumaryzator: " + summarizerComboBox.getValue().getKey() + " " +
                            summarizerComboBox.getValue().getValue() + "\n");

                    Map.Entry<String, String> selectedQualifier = qualifierComboBox.getValue();
                    if (selectedQualifier != null && !"Nie".equals(selectedQualifier.getKey())) {
                        writer.write("Kwalifikator: " + selectedQualifier.getKey() + " " + selectedQualifier.getValue() + "\n");
                    }
                    writer.write("\n");

                    for (int i = 0; i < count; i++) {
                        SummaryResult result = currentResults.get(i);
                        writer.write(String.format("%d. Stopień prawdziwości: %.4f | Kwantyfikator: %s | Forma: %s\n",
                                i+1, result.truthDegree, result.quantifierLabel, result.formName));
                        writer.write(result.summaryText + "\n");
                        writer.write("--------------------------------\n\n");
                    }

                    showAlert("Sukces", "Pomyślnie zapisano " + count + " podsumowań do pliku: " + file.getAbsolutePath());
                } catch (IOException e) {
                    showAlert("Błąd zapisu", "Wystąpił błąd podczas zapisywania do pliku: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            showAlert("Błąd", "Wystąpił nieoczekiwany błąd: " + e.getMessage());
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void updateSumm(List<Map.Entry<String, String>> newPredicates) {
        summarizerComboBox.setItems(FXCollections.observableArrayList(newPredicates));

        List<Map.Entry<String, String>> qualifierOptions = new ArrayList<>();
        qualifierOptions.add(new AbstractMap.SimpleEntry<>("Nie", ""));
        qualifierOptions.addAll(newPredicates);
        qualifierComboBox.setItems(FXCollections.observableArrayList(qualifierOptions));
    }

    public void updateQuantifiers(List<Quantifier> relativeQuantifiers) {
        this.allRelativeQuantifiers = relativeQuantifiers;
    }

    private static class SummaryResult {
        final String summaryText;
        final double truthDegree;
        final String quantifierLabel;
        final String formName;

        public SummaryResult(String summaryText, double truthDegree, String quantifierLabel, String formName) {
            this.summaryText = summaryText;
            this.truthDegree = truthDegree;
            this.quantifierLabel = quantifierLabel;
            this.formName = formName;
        }
    }

    private static class SummString extends javafx.util.StringConverter<Map.Entry<String, String>> {
        @Override
        public String toString(Map.Entry<String, String> predicateInfo) {
            if (predicateInfo == null) return null;
            return "Nie".equals(predicateInfo.getKey()) ? "Nie" :
                    predicateInfo.getKey() + " " + predicateInfo.getValue();
        }

        @Override
        public Map.Entry<String, String> fromString(String string) {
            return null;
        }
    }
}