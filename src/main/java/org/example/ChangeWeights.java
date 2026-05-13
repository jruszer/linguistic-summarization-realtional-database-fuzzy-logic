package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ChangeWeights extends VBox {
    private TextField t1WeightField;
    private TextField t2WeightField;
    private TextField t3WeightField;
    private TextField t4WeightField;
    private TextField t5WeightField;
    private TextField t6WeightField;
    private TextField t7WeightField;
    private TextField t8WeightField;
    private TextField t9WeightField;
    private TextField t10WeightField;
    private TextField t11WeightField;

    public ChangeWeights() {
        initialize();
        layoutComponents();
    }

    private void initialize() {
        t1WeightField = createWeightField("0.8");
        t2WeightField = createWeightField("0.02");
        t3WeightField = createWeightField("0.02");
        t4WeightField = createWeightField("0.02");
        t5WeightField = createWeightField("0.02");
        t6WeightField = createWeightField("0.02");
        t7WeightField = createWeightField("0.02");
        t8WeightField = createWeightField("0.02");
        t9WeightField = createWeightField("0.02");
        t10WeightField = createWeightField("0.02");
        t11WeightField = createWeightField("0.02");
    }

    private TextField createWeightField(String initialValue) {
        TextField field = new TextField(initialValue);
        field.setPrefWidth(60);

        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                field.setText(oldValue);
            }
        });

        return field;
    }

    private void layoutComponents() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));

        gridPane.add(new Label("Ustaw wagi dla miar jakości podsumowań (jak ważne są dane miary przy obliczaniu podsumowania optymalnego):"), 0, 0, 2, 1);

        HBox weightsRow1 = new HBox(5);
        weightsRow1.setAlignment(Pos.CENTER_LEFT);
        weightsRow1.getChildren().addAll(
                weightLabelLines("T1:", t1WeightField),
                weightLabelLines("T2:", t2WeightField),
                weightLabelLines("T3:", t3WeightField),
                weightLabelLines("T4:", t4WeightField),
                weightLabelLines("T5:", t5WeightField),
                weightLabelLines("T6:", t6WeightField)
        );
        gridPane.add(weightsRow1, 0, 1, 2, 1);

        HBox weightsRow2 = new HBox(5);
        weightsRow2.setAlignment(Pos.CENTER_LEFT);
        weightsRow2.getChildren().addAll(
                weightLabelLines("T7:", t7WeightField),
                weightLabelLines("T8:", t8WeightField),
                weightLabelLines("T9:", t9WeightField),
                weightLabelLines("T10:", t10WeightField),
                weightLabelLines("T11:", t11WeightField)
        );
        gridPane.add(weightsRow2, 0, 2, 2, 1);

        Label infoLabel = new Label("Suma wag musi wynosić 1.0");
        gridPane.add(infoLabel, 0, 3, 2, 1);

        this.getChildren().add(gridPane);
    }

    private HBox weightLabelLines(String labelText, TextField field) {
        HBox pair = new HBox(2);
        Label label = new Label(labelText);
        field.setPrefWidth(40);
        pair.getChildren().addAll(label, field);
        return pair;
    }

    public boolean validateWeights() {
        double sum = 0.0;

        sum += weightToDouble(t1WeightField.getText());
        sum += weightToDouble(t2WeightField.getText());
        sum += weightToDouble(t3WeightField.getText());
        sum += weightToDouble(t4WeightField.getText());
        sum += weightToDouble(t5WeightField.getText());
        sum += weightToDouble(t6WeightField.getText());
        sum += weightToDouble(t7WeightField.getText());
        sum += weightToDouble(t8WeightField.getText());
        sum += weightToDouble(t9WeightField.getText());
        sum += weightToDouble(t10WeightField.getText());
        sum += weightToDouble(t11WeightField.getText());

        return Math.abs(sum - 1.0) < 0.0001; //jak da sie == to zle dziala
    }

    private double weightToDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public double getT1Weight() { return weightToDouble(t1WeightField.getText()); }
    public double getT2Weight() { return weightToDouble(t2WeightField.getText()); }
    public double getT3Weight() { return weightToDouble(t3WeightField.getText()); }
    public double getT4Weight() { return weightToDouble(t4WeightField.getText()); }
    public double getT5Weight() { return weightToDouble(t5WeightField.getText()); }
    public double getT6Weight() { return weightToDouble(t6WeightField.getText()); }
    public double getT7Weight() { return weightToDouble(t7WeightField.getText()); }
    public double getT8Weight() { return weightToDouble(t8WeightField.getText()); }
    public double getT9Weight() { return weightToDouble(t9WeightField.getText()); }
    public double getT10Weight() { return weightToDouble(t10WeightField.getText()); }
    public double getT11Weight() { return weightToDouble(t11WeightField.getText()); }
}