package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

public class MainApp extends Application {

    private LoadData loadData;
    private Map<String, LinguisticVariable<Double>> allLVs;
    private List<Map.Entry<String, String>> allPredicates;
    private OneSubjectWindow oneSubjectWindow;
    private TwoSubjectWindow twoSubjectWindow;
    private List<Quantifier> allRelativeQuantifiers;
    private List<Quantifier> allAbsoluteQuantifiers;
    private ChangeWeights changeWeights;

    @Override
    public void start(Stage primaryStage) {
        loadData = new LoadData("src/main/resources/matches.csv");
        Set<Match> allMatches = loadData.getAllRecords();
        allLVs = loadData.getAllLVs();

        Map<String, Set<Match>> dataSetsByYear = new HashMap<>();
        dataSetsByYear.put("mecze ze wszystkich lat", allMatches);

        for (Match record : allMatches) {
            String tourneyDate = record.getStringValue("tourney_date");
            if (tourneyDate != null && tourneyDate.length() >= 4) {
                try {
                    String year = tourneyDate.substring(0, 4);
                    int parsedYear = Integer.parseInt(year);
                    if (parsedYear >= 2015 && parsedYear <= 2020) {
                        dataSetsByYear.computeIfAbsent(year, k -> new HashSet<>()).add(record);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        allRelativeQuantifiers = loadData.getRelativeQuantifiers();
        allAbsoluteQuantifiers = loadData.getAbsoluteQuantifiers();
        allPredicates = loadData.getAllPredicates();
        changeWeights = new ChangeWeights();

        TabPane tabPane = new TabPane();

        Tab oneSubjectTab = new Tab("Podsumowania bez porównań między latami");
        oneSubjectTab.setClosable(false);
        oneSubjectWindow = new OneSubjectWindow(
                dataSetsByYear, allLVs, allRelativeQuantifiers,
                allAbsoluteQuantifiers, allPredicates, primaryStage, changeWeights);
        oneSubjectTab.setContent(oneSubjectWindow);

        Tab twoSubjectTab = new Tab("Podsumowania porównujące mecze z dwóch lat");
        twoSubjectTab.setClosable(false);
        twoSubjectWindow = new TwoSubjectWindow(
                dataSetsByYear, allLVs, allRelativeQuantifiers,
                allPredicates, primaryStage);
        ScrollPane twoSubjectScrollPane = new ScrollPane();
        twoSubjectScrollPane.setContent(twoSubjectWindow);
        twoSubjectScrollPane.setFitToWidth(true);
        twoSubjectTab.setContent(twoSubjectWindow);

        Tab addLabelTab = new Tab("Dodaj etykietę");
        addLabelTab.setClosable(false);
        addLabelTab.setContent(createAddLabelPane());

        Tab addQuantifierTab = new Tab("Dodaj kwantyfikator");
        addQuantifierTab.setClosable(false);
        addQuantifierTab.setContent(createAddQuantifierPane());

        Tab weightsTab = new Tab("Zmień wagi miar");
        weightsTab.setClosable(false);
        weightsTab.setContent(changeWeights);

        tabPane.getTabs().addAll(oneSubjectTab, twoSubjectTab, addLabelTab, addQuantifierTab, weightsTab);

        Scene scene = new Scene(tabPane, 1000, 700);
        primaryStage.setTitle("Podsumowania lingwistyczne jedno i dwupodmiotowe");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createAddQuantifierPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));

        ComboBox<String> quantifierTypeCombo = new ComboBox<>();
        quantifierTypeCombo.getItems().addAll("Względny", "Bezwzględny");
        quantifierTypeCombo.setValue("Względny (0-1)");

        TextField nameField = new TextField();
        nameField.setPromptText("Nazwa kwantyfikatora");

        ComboBox<String> functionTypeCombo = new ComboBox<>();
        functionTypeCombo.getItems().addAll("Trapezoidalna", "Trójkątna", "Gaussa");
        functionTypeCombo.setValue("Trapezoidalna");

        TextField param1Field = new TextField();
        TextField param2Field = new TextField();
        TextField param3Field = new TextField();
        TextField param4Field = new TextField();

        Label param1Label = new Label("Parametr a:");
        Label param2Label = new Label("Parametr b:");
        Label param3Label = new Label("Parametr c:");
        Label param4Label = new Label("Parametr d:");

        updateParameterFields(functionTypeCombo.getValue(),
                param1Field, param2Field, param3Field, param4Field,
                param1Label, param2Label, param3Label, param4Label);

        functionTypeCombo.setOnAction(e -> {
            String selectedType = functionTypeCombo.getValue();
            updateParameterFields(selectedType,
                    param1Field, param2Field, param3Field, param4Field,
                    param1Label, param2Label, param3Label, param4Label);
        });

        quantifierTypeCombo.setOnAction(e -> {
            String selectedType = quantifierTypeCombo.getValue();
            String range = selectedType.equals("Względny (0-1)") ? "(0-1)" : "";
            updateParameterPrompts(functionTypeCombo.getValue(),
                    param1Field, param2Field, param3Field, param4Field,
                    range);
        });

        Button addButton = new Button("Dodaj kwantyfikator");
        Label statusLabel = new Label();

        int row = 0;
        gridPane.add(new Label("Typ kwantyfikatora:"), 0, row);
        gridPane.add(quantifierTypeCombo, 1, row++);
        gridPane.add(new Label("Nazwa:"), 0, row);
        gridPane.add(nameField, 1, row++);
        gridPane.add(new Label("Typ funkcji:"), 0, row);
        gridPane.add(functionTypeCombo, 1, row++);
        gridPane.add(param1Label, 0, row);
        gridPane.add(param1Field, 1, row++);
        gridPane.add(param2Label, 0, row);
        gridPane.add(param2Field, 1, row++);
        gridPane.add(param3Label, 0, row);
        gridPane.add(param3Field, 1, row++);
        gridPane.add(param4Label, 0, row);
        gridPane.add(param4Field, 1, row++);
        gridPane.add(addButton, 0, row++, 2, 1);
        gridPane.add(statusLabel, 0, row++, 2, 1);

        addButton.setOnAction(e -> {
            try {
                String name = nameField.getText();
                String quantifierType = quantifierTypeCombo.getValue();
                String functionType = functionTypeCombo.getValue();

                if (name == null || name.isEmpty()) {
                    statusLabel.setText("Wprowadź nazwę kwantyfikatora!");
                    return;
                }

                MembershipFunction function;
                UniverseOfDiscourse<Double> universe;

                if (quantifierType.equals("Względny (0-1)")) {
                    universe = new UniverseOfDiscourse<>(0.0, 1.0);
                } else {
                    universe = new UniverseOfDiscourse<>(0.0, 12000.0);
                }

                switch (functionType) {
                    case "Trapezoidalna":
                        double a = Double.parseDouble(param1Field.getText());
                        double b = Double.parseDouble(param2Field.getText());
                        double c = Double.parseDouble(param3Field.getText());
                        double d = Double.parseDouble(param4Field.getText());

                        if (!(a < b && b < c && c < d)) {
                            statusLabel.setText("Musi zachodzić a < b < c < d");
                            return;
                        }

                        function = new TrapezoidalFun(a, b, c, d);
                        break;

                    case "Trójkątna":
                        double aTri = Double.parseDouble(param1Field.getText());
                        double bTri = Double.parseDouble(param2Field.getText());
                        double cTri = Double.parseDouble(param3Field.getText());

                        if (!(aTri < bTri && bTri < cTri)) {
                            statusLabel.setText("Musi zachodzić a < b < c");
                            return;
                        }

                        function = new TriangularFun(aTri, bTri, cTri);
                        break;

                    case "Gaussa":
                        double mean = Double.parseDouble(param1Field.getText());
                        double sigma = Double.parseDouble(param2Field.getText());

                        if (sigma <= 0) {
                            statusLabel.setText("Szerokość musi być > 0");
                            return;
                        }

                        function = new GaussFun(mean, sigma);
                        break;

                    default:
                        statusLabel.setText("Wybierz poprawny typ funkcji!");
                        return;
                }

                if (!validateFunctionRange(function, universe)) {
                    statusLabel.setText(String.format("Funkcja musi mieścić się w zakresie [%.2f, %.2f]",
                            universe.getMinX(), universe.getMaxX()));
                    return;
                }

                FuzzySet<Double> fuzzySet = new FuzzySet<>(universe, function);
                Quantifier newQuantifier;

                if (quantifierType.equals("Względny (0-1)")) {
                    newQuantifier = new RelativeQuantifier(name, fuzzySet);
                    allRelativeQuantifiers.add(newQuantifier);
                } else {
                    newQuantifier = new AbsoluteQuantifier(name, fuzzySet);
                    allAbsoluteQuantifiers.add(newQuantifier);
                }

                oneSubjectWindow.updateQuantifiers(allRelativeQuantifiers, allAbsoluteQuantifiers);
                twoSubjectWindow.updateQuantifiers(allRelativeQuantifiers);

                statusLabel.setText("Pomyślnie dodano kwantyfikator: " + name);
                clearFields(nameField, param1Field, param2Field, param3Field, param4Field);

            } catch (NumberFormatException ex) {
                statusLabel.setText(" Wprowadź poprawne liczby");
            } catch (Exception ex) {
                statusLabel.setText("Błąd: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        return new VBox(10, gridPane);
    }

    private void updateParameterFields(String functionType,
                                       TextField param1Field, TextField param2Field,
                                       TextField param3Field, TextField param4Field,
                                       Label param1Label, Label param2Label,
                                       Label param3Label, Label param4Label) {
        switch (functionType) {
            case "Trapezoidalna":
                param1Label.setText("start_up:");
                param2Label.setText("end_up:");
                param3Label.setText("start_down:");
                param4Label.setText("end_down:");
                param3Label.setVisible(true);
                param3Field.setVisible(true);
                param4Label.setVisible(true);
                param4Field.setVisible(true);
                break;
            case "Trójkątna":
                param1Label.setText("start_up:");
                param2Label.setText("peak:");
                param3Label.setText("end_down:");
                param4Label.setVisible(false);
                param4Field.setVisible(false);
                param3Label.setVisible(true);
                param3Field.setVisible(true);
                break;
            case "Gaussa":
                param1Label.setText("środek:");
                param2Label.setText("szerokość:");
                param3Label.setVisible(false);
                param3Field.setVisible(false);
                param4Label.setVisible(false);
                param4Field.setVisible(false);
                break;
        }
    }

    private void updateParameterPrompts(String functionType,
                                        TextField param1Field, TextField param2Field,
                                        TextField param3Field, TextField param4Field,
                                        String range) {
        switch (functionType) {
            case "Trapezoidalna":
                param1Field.setPromptText("a " + range);
                param2Field.setPromptText("b " + range);
                param3Field.setPromptText("c " + range);
                param4Field.setPromptText("d " + range);
                break;
            case "Trójkątna":
                param1Field.setPromptText("a " + range);
                param2Field.setPromptText("b " + range);
                param3Field.setPromptText("c " + range);
                break;
            case "Gaussa":
                param1Field.setPromptText("μ " + range);
                param2Field.setPromptText("σ " + range);
                break;
        }
    }

    private boolean validateFunctionRange(MembershipFunction function, UniverseOfDiscourse<Double> universe) {
        if (universe.getMaxX() == 1.0) {
            if (function instanceof TrapezoidalFun) {
                TrapezoidalFun trapezoidal = (TrapezoidalFun) function;
                if (trapezoidal.getStart_up() < 0 || trapezoidal.getEnd_down() > 1.0) {
                    return false;
                }
            } else if (function instanceof TriangularFun) {
                TriangularFun triangular = (TriangularFun) function;
                if (triangular.getStart_up() < 0 || triangular.getEnd_down() > 1.0) {
                    return false;
                }
            } else if (function instanceof GaussFun) {
                GaussFun gauss = (GaussFun) function;
                if (gauss.gethMaxX() - 3*gauss.getWidth() < 0 ||
                        gauss.gethMaxX() + 3*gauss.getWidth() > 1.0) {
                    return false;
                }
            }
        }
        return true;
    }

    private void clearFields(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    private VBox createAddLabelPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));

        ComboBox<String> variableCombo = new ComboBox<>();
        variableCombo.setItems(FXCollections.observableArrayList(allLVs.keySet()));
        variableCombo.setPromptText("Wybierz zmienną lingwistyczną");

        TextField labelNameField = new TextField();
        labelNameField.setPromptText("Nazwa nowej etykiety");

        ComboBox<String> functionTypeCombo = new ComboBox<>();
        functionTypeCombo.getItems().addAll("Trapezoidalna", "Trójkątna", "Gaussa");
        functionTypeCombo.setValue("Trapezoidalna");

        Label param1Label = new Label("start_up:");
        TextField param1Field = new TextField();
        param1Field.setPromptText("");

        Label param2Label = new Label("end_up:");
        TextField param2Field = new TextField();
        param2Field.setPromptText("");

        Label param3Label = new Label("start_down:");
        TextField param3Field = new TextField();
        param3Field.setPromptText("");

        Label param4Label = new Label("end_down:");
        TextField param4Field = new TextField();
        param4Field.setPromptText("");

        param3Label.setVisible(true);
        param3Field.setVisible(true);
        param4Label.setVisible(true);
        param4Field.setVisible(true);

        functionTypeCombo.setOnAction(e -> {
            String selectedType = functionTypeCombo.getValue();
            switch (selectedType) {
                case "Trapezoidalna":
                    param1Label.setText("start_up:");
                    param2Label.setText("end_up:");
                    param3Label.setText("start_down:");
                    param4Label.setText("end_down:");
                    param3Label.setVisible(true);
                    param3Field.setVisible(true);
                    param4Label.setVisible(true);
                    param4Field.setVisible(true);
                    break;
                case "Trójkątna":
                    param1Label.setText("start_up:");
                    param2Label.setText("peak:");
                    param3Label.setText("end_up:");
                    param3Label.setVisible(true);
                    param3Field.setVisible(true);
                    param4Label.setVisible(false);
                    param4Field.setVisible(false);
                    break;
                case "Gaussa":
                    param1Label.setText("środek:");
                    param2Label.setText("szerokość:");
                    param3Label.setVisible(false);
                    param3Field.setVisible(false);
                    param4Label.setVisible(false);
                    param4Field.setVisible(false);
                    break;
            }
        });

        Button addLabelButton = new Button("Dodaj etykietę");
        Label statusLabel = new Label();

        int row = 0;
        gridPane.add(new Label("Zmienna lingwistyczna:"), 0, row);
        gridPane.add(variableCombo, 1, row++);
        gridPane.add(new Label("Nazwa etykiety:"), 0, row);
        gridPane.add(labelNameField, 1, row++);
        gridPane.add(new Label("Typ funkcji:"), 0, row);
        gridPane.add(functionTypeCombo, 1, row++);
        gridPane.add(param1Label, 0, row);
        gridPane.add(param1Field, 1, row++);
        gridPane.add(param2Label, 0, row);
        gridPane.add(param2Field, 1, row++);
        gridPane.add(param3Label, 0, row);
        gridPane.add(param3Field, 1, row++);
        gridPane.add(param4Label, 0, row);
        gridPane.add(param4Field, 1, row++);
        gridPane.add(addLabelButton, 0, row++, 2, 1);
        gridPane.add(statusLabel, 0, row++, 2, 1);

        addLabelButton.setOnAction(e -> {
            try {
                String selectedVariable = variableCombo.getValue();
                String labelName = labelNameField.getText();
                String functionType = functionTypeCombo.getValue();

                if (selectedVariable == null || selectedVariable.isEmpty()) {
                    statusLabel.setText("Wybierz zmienną lingwistyczną!");
                    return;
                }

                if (labelName == null || labelName.isEmpty()) {
                    statusLabel.setText("Wprowadź nazwę etykiety!");
                    return;
                }

                LinguisticVariable<Double> lv = allLVs.get(selectedVariable);
                if (lv == null) {
                    statusLabel.setText("Wybrana zmienna nie istnieje!");
                    return;
                }

                MembershipFunction function;
                UniverseOfDiscourse<Double> universe = lv.getUoD();

                switch (functionType) {
                    case "Trapezoidalna":
                        double a = Double.parseDouble(param1Field.getText());
                        double b = Double.parseDouble(param2Field.getText());
                        double c = Double.parseDouble(param3Field.getText());
                        double d = Double.parseDouble(param4Field.getText());

                        if (!(a < b && b < c && c < d)) {
                            statusLabel.setText("Musi zachodzić a < b < c < d");
                            return;
                        }

                        if (a < universe.getMinX() || d > universe.getMaxX()) {
                            statusLabel.setText(String.format("Parametry muszą mieścić się w zakresie [%.2f, %.2f]",
                                    universe.getMinX(), universe.getMaxX()));
                            return;
                        }

                        function = new TrapezoidalFun(a, b, c, d);
                        break;

                    case "Trójkątna":
                        double aTri = Double.parseDouble(param1Field.getText());
                        double bTri = Double.parseDouble(param2Field.getText());
                        double cTri = Double.parseDouble(param3Field.getText());

                        if (!(aTri < bTri && bTri < cTri)) {
                            statusLabel.setText("Musi zachodzić a < b < c");
                            return;
                        }

                        if (aTri < universe.getMinX() || cTri > universe.getMaxX()) {
                            statusLabel.setText(String.format("Parametry muszą mieścić się w zakresie [%.2f, %.2f]",
                                    universe.getMinX(), universe.getMaxX()));
                            return;
                        }

                        function = new TriangularFun(aTri, bTri, cTri);
                        break;

                    case "Gaussa":
                        double mean = Double.parseDouble(param1Field.getText());
                        double sigma = Double.parseDouble(param2Field.getText());

                        if (sigma <= 0) {
                            statusLabel.setText("Szerokość musi być większa od 0");
                            return;
                        }

                        if (mean - 3*sigma < universe.getMinX() || mean + 3*sigma > universe.getMaxX()) {
                            statusLabel.setText(String.format("Krzywa  musi być w zakresie [%.2f, %.2f]",
                                    universe.getMinX(), universe.getMaxX()));
                            return;
                        }

                        function = new GaussFun(mean, sigma);
                        break;

                    default:
                        statusLabel.setText("Wybierz poprawny typ funkcji");
                        return;
                }

                lv.addLabel(labelName, new FuzzySet<>(universe, function));

                allPredicates = loadData.getAllPredicates();
                oneSubjectWindow.updateSummar(allPredicates);
                twoSubjectWindow.updateSumm(allPredicates);

                statusLabel.setText("Pomyślnie dodano etykietę '" + labelName + "' (" + functionType + ")");

                labelNameField.clear();
                param1Field.clear();
                param2Field.clear();
                param3Field.clear();
                param4Field.clear();

            } catch (NumberFormatException ex) {
                statusLabel.setText("Wprowadź poprawne liczby");
            } catch (Exception ex) {
                statusLabel.setText("Błąd: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        return new VBox(10, gridPane);
    }

    public static void main(String[] args) {
        launch(args);
    }
}