package org.example;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class OneSubjectWindow extends VBox {

    private Map<String, Set<Match>> allDataSets;
    private Map<String, LinguisticVariable<Double>> allLV;
    private List<Quantifier> allRelativeQuantifiers;
    private List<Quantifier> allAbsoluteQuantifiers;
    private List<SelectSumm> allSummarizers;

    private ComboBox<String> subjectComboBox;
    private ComboBox<Map.Entry<String, String>> qualifierComboBox;
    private TreeView<SelectSumm> summarizerTreeView;

    private ComboBox<String> sortByComboBox;
    private Button generateButton;
    private TextArea resultTextArea;
    private Button saveButton;
    private TextField saveCountField;

    private List<SummaryResult> currentResults;
    private Stage primaryStage;
    private ChangeWeights changeWeights;

    public OneSubjectWindow(Map<String, Set<Match>> allDataSets, Map<String, LinguisticVariable<Double>> allLV, List<Quantifier> allRelativeQuantifiers,
                            List<Quantifier> allAbsoluteQuantifiers, List<Map.Entry<String, String>> initialSumme, Stage primaryStage, ChangeWeights changeWeights) {
        this.allDataSets = allDataSets;
        this.allLV = allLV;
        this.allRelativeQuantifiers = allRelativeQuantifiers;
        this.allAbsoluteQuantifiers = allAbsoluteQuantifiers;
        this.allSummarizers = initialSumme.stream()
                .map(SelectSumm::new)
                .collect(Collectors.toList());
        this.primaryStage = primaryStage;
        this.currentResults = new ArrayList<>();
        this.changeWeights = changeWeights;

        initialize();
        layoutComponents();
        setupEventHandlers();
    }

    private void initialize() {
        subjectComboBox = new ComboBox<>(FXCollections.observableArrayList(allDataSets.keySet()));
        if (!allDataSets.isEmpty()) {
            subjectComboBox.setValue(allDataSets.keySet().iterator().next());
        }

        List<Map.Entry<String, String>> qualifierOptions = new ArrayList<>();
        qualifierOptions.add(new AbstractMap.SimpleEntry<>("Nie", "Nie"));
        qualifierOptions.addAll(allSummarizers.stream()
                .map(SelectSumm::getSummarizerData)
                .collect(Collectors.toList()));
        qualifierComboBox = new ComboBox<>(FXCollections.observableArrayList(qualifierOptions));
        qualifierComboBox.setConverter(new SummarizerToStr());
        qualifierComboBox.setValue(qualifierOptions.get(0));

        TreeItem<SelectSumm> rootItem = new TreeItem<>(new SelectSumm(new AbstractMap.SimpleEntry<>("Sumaryzatory", "")));
        rootItem.setExpanded(true);

        for (SelectSumm summarizer : allSummarizers) {
            rootItem.getChildren().add(new TreeItem<>(summarizer));
        }

        summarizerTreeView = new TreeView<>(rootItem);
        summarizerTreeView.setShowRoot(false);
        summarizerTreeView.setPrefHeight(200);
        summarizerTreeView.setPrefWidth(250);
        summarizerTreeView.setCellFactory(tv -> new SummaTreeCell());

        List<String> sortOptions = Arrays.asList(
                "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T"
        );
        sortByComboBox = new ComboBox<>(FXCollections.observableArrayList(sortOptions));
        sortByComboBox.setValue("T1");

        generateButton = new Button("Generuj podsumowania");
        resultTextArea = new TextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setWrapText(true);
        resultTextArea.setPrefRowCount(30);

        saveCountField = new TextField("10");
        saveCountField.setPrefWidth(50);
        saveButton = new Button("Zapisz najlepsze wyniki");
        saveButton.setDisable(true);
    }

    private void layoutComponents() {
        GridPane leftPanel = new GridPane();
        leftPanel.setHgap(10);
        leftPanel.setVgap(10);
        leftPanel.setPadding(new Insets(10));

        int row = 0;
        leftPanel.add(new Label("Wybierz z którego roku mecze chcesz podsumować:"), 0, row);
        leftPanel.add(subjectComboBox, 1, row++);

        leftPanel.add(new Label("Czy chcesz dodatkowo ograniczyć mecze?"), 0, row);
        leftPanel.add(qualifierComboBox, 1, row++);

        leftPanel.add(new Label("Sortuj według:"), 0, row);
        leftPanel.add(sortByComboBox, 1, row++);

        HBox savePanel = new HBox(10);
        savePanel.setAlignment(Pos.CENTER_LEFT);
        savePanel.getChildren().addAll(
                new Label("Zapisz:"),
                saveCountField,
                new Label("wyników"),
                saveButton
        );
        leftPanel.add(savePanel, 0, row, 2, 1);
        row++;

        VBox rightPanel = new VBox(5);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setAlignment(Pos.TOP_LEFT);
        rightPanel.getChildren().addAll(
                new Label("Jakie mecze cię interesują (wybierz maksymalnie 4):"),
                summarizerTreeView
        );
        VBox.setVgrow(summarizerTreeView, Priority.ALWAYS);

        HBox topSection = new HBox(10);
        topSection.setPadding(new Insets(10));
        topSection.getChildren().addAll(leftPanel, rightPanel);

        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        this.getChildren().addAll(topSection, generateButton, resultTextArea);
        VBox.setMargin(generateButton, new Insets(0, 10, 0, 10));
        VBox.setMargin(resultTextArea, new Insets(10, 10, 10, 10));
    }

    private void setupEventHandlers() {
        generateButton.setOnAction(e -> generateAllSummaries());
        saveButton.setOnAction(e -> saveToTxt());
    }

    private void generateAllSummaries() {
        if (!changeWeights.validateWeights()) {
            showAlert("Błąd", "Suma wag musi wynosić 1");
            return;
        }

        String selectedSubjectName = subjectComboBox.getValue();
        Map.Entry<String, String> selectedQualifier = qualifierComboBox.getValue();
        String sortBy = sortByComboBox.getValue();

        double t1Weight = changeWeights.getT1Weight();
        double t2Weight = changeWeights.getT2Weight();
        double t3Weight = changeWeights.getT3Weight();
        double t4Weight = changeWeights.getT4Weight();
        double t5Weight = changeWeights.getT5Weight();
        double t6Weight = changeWeights.getT6Weight();
        double t7Weight = changeWeights.getT7Weight();
        double t8Weight = changeWeights.getT8Weight();
        double t9Weight = changeWeights.getT9Weight();
        double t10Weight = changeWeights.getT10Weight();
        double t11Weight = changeWeights.getT11Weight();

        Set<Match> currentDataSet = allDataSets.get(selectedSubjectName);

        if (currentDataSet == null || currentDataSet.isEmpty()) {
            showAlert("Błąd", "Brak danych dla '" + selectedSubjectName + "'.");
            return;
        }

        try {
            List<Quantifier> allQuantifiers = new ArrayList<>();
            allQuantifiers.addAll(allRelativeQuantifiers);
            allQuantifiers.addAll(allAbsoluteQuantifiers);

            List<Map.Entry<String, String>> selectedSummarizers = summarizerTreeView.getRoot().getChildren().stream()
                    .filter(item -> item.getValue().isSelected())
                    .map(item -> item.getValue().getSummarizerData())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (selectedSummarizers.isEmpty()) {
                showAlert("Błąd", "Wybierz przynajmniej jedną cechę z listy.");
                return;
            }

            Map.Entry<String, String> actualQualifier =
                    (selectedQualifier != null && "Nie".equals(selectedQualifier.getKey())) ? null : selectedQualifier;

            currentResults.clear();

            System.out.println("\n=== Wyniki podsumowań ===");

            for (Quantifier quantifier : allQuantifiers) {
                if (quantifier instanceof AbsoluteQuantifier && actualQualifier != null) {
                    continue;
                }
                OneSubjectLinguisticSummary summary = new OneSubjectLinguisticSummary(
                        selectedSubjectName,
                        allLV,
                        quantifier,
                        actualQualifier,
                        selectedSummarizers
                );

                String summaryText = summary.writeSummary();
                double t1 = summary.calculateT1(currentDataSet);
                double t2 = summary.calculateT2();
                double t3 = summary.calculateT3(currentDataSet);
                double t4 = summary.calculateT4(currentDataSet);
                double t5 = summary.calculateT5();
                double t6 = summary.calculateT6(currentDataSet);
                double t7 = summary.calculateT7();
                double t8 = summary.calculateT8();
                double t9 = summary.calculateT9();
                double t10 = summary.calculateT10();
                double t11 = summary.calculateT11();
                double t = summary.calculateT(currentDataSet,
                        t1Weight, t2Weight, t3Weight, t4Weight, t5Weight,
                        t6Weight, t7Weight, t8Weight, t9Weight, t10Weight, t11Weight);

                currentResults.add(new SummaryResult(summaryText, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t, quantifier.getLabel()));

                System.out.println(summaryText);
                System.out.printf("%s\n%s\n%s\n%s\n%s%n",
                        format(t1), format(t2), format(t3),
                        format(t4), format(t5));
                System.out.printf("%s\n%s\n%s\n%s\n%s%n",
                        format(t6), format(t7), format(t8),
                        format(t9), format(t10));
                System.out.printf("%s\n%s%n%n",
                        format(t11), format(t));
            }

            currentResults.sort((r1, r2) -> {
                double value1 = getMeasureValue(r1, sortBy);
                double value2 = getMeasureValue(r2, sortBy);
                return Double.compare(value2, value1);
            });

            resultTextArea.clear();
            resultTextArea.appendText("Wyniki posortowane od najwyższego " + sortBy + ":\n\n");

            for (SummaryResult result : currentResults) {
                resultTextArea.appendText(result.summaryText + "\n");
                resultTextArea.appendText(String.format(
                        "Miary: T1=%s, T2=%s, T3=%s, T4=%s, T5=%s, " +
                                "T6=%s, T7=%s, T8=%s, T9=%s, T10=%s, " +
                                "T11=%s, T=%s\n",
                        format(result.t1), format(result.t2),
                        format(result.t3), format(result.t4),
                        format(result.t5),
                        format(result.t6), format(result.t7),
                        format(result.t8), format(result.t9),
                        format(result.t10),
                        format(result.t11), format(result.t)
                ));
                resultTextArea.appendText("--------------------------------\n");
            }

            saveButton.setDisable(currentResults.isEmpty());

        } catch (Exception ex) {
            showAlert("Wystąpił błąd", "Wystąpił błąd: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String format(double value) {
        if (value < 0.005 && value != 0) {
            return "<0.01";
        }
        return String.format("%.2f", Math.round(value * 100) / 100.0);
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
                    writer.write("Najlepsze " + count + " podsumowań lingwistycznych\n");
                    writer.write("Sortowane według: " + sortByComboBox.getValue() + "\n\n");

                    for (int i = 0; i < count; i++) {
                        SummaryResult result = currentResults.get(i);
                        writer.write(result.summaryText + "\n");
                        writer.write(String.format(
                                "Miary: T1=%.4f, T2=%.4f, T3=%.4f, T4=%.4f, T5=%.4f,\n" +
                                        "       T6=%.4f, T7=%.4f, T8=%.4f, T9=%.4f, T10=%.4f,\n" +
                                        "       T11=%.4f, T=%.4f\n",
                                result.t1, result.t2, result.t3, result.t4, result.t5,
                                result.t6, result.t7, result.t8, result.t9, result.t10,
                                result.t11, result.t
                        ));
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

//    private boolean validateWeights() {
//        double sum = 0.0;
//
//        sum += parseWeight(t1WeightField.getText());
//        sum += parseWeight(t2WeightField.getText());
//        sum += parseWeight(t3WeightField.getText());
//        sum += parseWeight(t4WeightField.getText());
//        sum += parseWeight(t5WeightField.getText());
//        sum += parseWeight(t6WeightField.getText());
//        sum += parseWeight(t7WeightField.getText());
//        sum += parseWeight(t8WeightField.getText());
//        sum += parseWeight(t9WeightField.getText());
//        sum += parseWeight(t10WeightField.getText());
//        sum += parseWeight(t11WeightField.getText());
//
//        return Math.abs(sum - 1.0) < 0.0001;
//    }

    private double getMeasureValue(SummaryResult result, String measure) {
        switch (measure) {
            case "T1": return result.t1;
            case "T2": return result.t2;
            case "T3": return result.t3;
            case "T4": return result.t4;
            case "T5": return result.t5;
            case "T6": return result.t6;
            case "T7": return result.t7;
            case "T8": return result.t8;
            case "T9": return result.t9;
            case "T10": return result.t10;
            case "T11": return result.t11;
            case "T": return result.t;
            default: return result.t1;
        }
    }

    private static class SummaryResult {
        String summaryText;
        double t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t;
        String quantifierLabel;

        public SummaryResult(String summaryText,
                             double t1, double t2, double t3, double t4, double t5,
                             double t6, double t7, double t8, double t9, double t10,
                             double t11, double t, String quantifierLabel) {
            this.summaryText = summaryText;
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
            this.t6 = t6;
            this.t7 = t7;
            this.t8 = t8;
            this.t9 = t9;
            this.t10 = t10;
            this.t11 = t11;
            this.t = t;
            this.quantifierLabel = quantifierLabel;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void updateSummar(List<Map.Entry<String, String>> newSummarizersData) {
        Map<String, Boolean> currentSelectionStates = new HashMap<>();
        for (SelectSumm ss : this.allSummarizers) {
            currentSelectionStates.put(ss.getSummarizerData().getKey() + "|" + ss.getSummarizerData().getValue(), ss.isSelected());
        }

        this.allSummarizers = newSummarizersData.stream()
                .map(entry -> {
                    SelectSumm newSs = new SelectSumm(entry);
                    String id = entry.getKey() + "|" + entry.getValue();
                    if (currentSelectionStates.containsKey(id)) {
                        newSs.setSelected(currentSelectionStates.get(id));
                    }
                    return newSs;
                })
                .collect(Collectors.toList());

        List<Map.Entry<String, String>> qualifierOptions = new ArrayList<>();
        qualifierOptions.add(new AbstractMap.SimpleEntry<>("Nie", "Nie"));
        qualifierOptions.addAll(newSummarizersData);
        qualifierComboBox.setItems(FXCollections.observableArrayList(qualifierOptions));

        TreeItem<SelectSumm> rootItem = new TreeItem<>(new SelectSumm(new AbstractMap.SimpleEntry<>("Sumaryzatory", "")));
        rootItem.setExpanded(true);
        for (SelectSumm summarizer : this.allSummarizers) {
            rootItem.getChildren().add(new TreeItem<>(summarizer));
        }
        summarizerTreeView.setRoot(rootItem);
    }

    public void updateQuantifiers(List<Quantifier> relativeQuantifiers, List<Quantifier> absoluteQuantifiers) {
        this.allRelativeQuantifiers = relativeQuantifiers;
        this.allAbsoluteQuantifiers = absoluteQuantifiers;
    }

    private static class SummarizerToStr extends javafx.util.StringConverter<Map.Entry<String, String>> {
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

    public static class SelectSumm {
        private final Map.Entry<String, String> summarizerData;
        private final BooleanProperty selected;

        public SelectSumm(Map.Entry<String, String> summarizerData) {
            this.summarizerData = summarizerData;
            this.selected = new SimpleBooleanProperty(false);
        }

        public Map.Entry<String, String> getSummarizerData() {
            return summarizerData;
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        @Override
        public String toString() {
            return summarizerData.getKey() + " " + summarizerData.getValue() + " (Selected: " + isSelected() + ")";
        }
    }

    private static class SummaTreeCell extends TreeCell<SelectSumm> {
        private final HBox graphicContainer = new HBox(5);
        private final Label label = new Label();
        private final CheckBox checkBox = new CheckBox();
        private final BooleanProperty updating = new SimpleBooleanProperty(false);

        public SummaTreeCell() {
            graphicContainer.setAlignment(Pos.CENTER_LEFT);
            graphicContainer.getChildren().addAll(checkBox, label);
            HBox.setHgrow(label, Priority.ALWAYS);
            graphicContainer.setPadding(new Insets(0, 5, 0, 0));

            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (!updating.get() && getItem() != null) {
                    updating.set(true);
                    getItem().setSelected(newVal);
                    updating.set(false);
                }
            });
        }

        @Override
        protected void updateItem(SelectSumm item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null || item.getSummarizerData().getKey().equals("Sumaryzatory")) {
                setGraphic(null);
                setText(null);
            } else {
                label.setText(item.getSummarizerData().getKey() + " " + item.getSummarizerData().getValue());
                setGraphic(graphicContainer);

                updating.set(true);
                checkBox.setSelected(item.isSelected());
                updating.set(false);
            }
        }
    }
}
