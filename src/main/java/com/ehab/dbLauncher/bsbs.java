package com.ehab.dbLauncher;

import java.sql.*;
import java.util.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.application.Application;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.scene.Scene;
import javafx.scene.layout.*;

public class bsbs extends Application {
    ObservableList<ArrayList<String>> data = FXCollections.observableArrayList();
    TableView<ArrayList<String>> tableview = new TableView<>();
    ArrayList<String> tables = new ArrayList<>(), colNames = new ArrayList<>();
    ArrayList<TextField> inputs = new ArrayList<>(), updates = new ArrayList<>();
    ArrayList<ComboBox<String>> combos = new ArrayList<>(), combosU = new ArrayList<>();
    ArrayList<Boolean> isCombo = new ArrayList<>(), isComboU = new ArrayList<>(), isColChar = new ArrayList<>();
    HashMap<String, HashMap<String, ArrayList<String>>> fItems = new HashMap<>();
    VBox input = new VBox();
    String dbName;
    private int cur = 0;

    public static boolean notInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return true;
        }
        return false;
    }

    public void getForeignKeys() {
        ResultSet keys = query("select TABLE_NAME, COLUMN_NAME, REFERENCED_COLUMN_NAME, REFERENCED_TABLE_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE REFERENCED_TABLE_SCHEMA = '" + dbName + "';", 's');
        try {
            while (keys.next()) {
                ResultSet i = query("select distinct " + keys.getString(3) + " from " + keys.getString(4), 's');
                ArrayList<String> arr = new ArrayList<>();
                while (i.next()) arr.add(i.getString(1));
                fItems.get(keys.getString(1)).put(keys.getString(2), arr);
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.WARNING, String.valueOf(e), new ButtonType("OK")).show();
        }
    }

    public boolean confirmed(char t) {
        return new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to perform the action '" + (t == 's' ? "Search" : (t == 'd' ? "Delete" : (t == 'u' ? "Update" : "Insert"))) + "'?").showAndWait().filter(buttonType -> buttonType == ButtonType.OK).isPresent();
    }

    public void buildData(ResultSet rs, boolean message, char type) {
        isColChar.clear();
        combos.clear();
        combosU.clear();
        isCombo.clear();
        isComboU.clear();
        inputs.clear();
        updates.clear();
        input.getChildren().clear();
        tableview.getItems().clear();
        tableview.getColumns().clear();
        colNames.clear();
        try {
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn<ArrayList<String>, String> col = new TableColumn<>(rs.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j)));
                col.setStyle("-fx-alignment: CENTER;");
                tableview.getColumns().addAll(col);
            }
            String tableName = tables.get(cur);

            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                String colName = rs.getMetaData().getColumnName(i + 1);
                String colType = rs.getMetaData().getColumnTypeName(i + 1);
                boolean isF = false;

                ArrayList<String> fItem = new ArrayList<>();
                if (fItems.get(tableName).containsKey(colName))
                    fItem = fItems.get(tableName).get(colName);
                if (!fItem.isEmpty()) isF = true;

                Button text = new Button(colName), updTo = new Button("update " + colName + " to");
                text.setPrefWidth(100);
                updTo.setPrefWidth(150);
                TextField textField = new TextField(), upd = new TextField();
                ComboBox<String> list = new ComboBox<>(), listU = new ComboBox<>();
                if (isF) {
                    list.getItems().addAll(fItems.get(tableName).get(colName));
                    listU.getItems().addAll(fItems.get(tableName).get(colName));
                }

                colNames.add(colName);
                isColChar.add(colType.equals("VARCHAR"));
                inputs.add(textField);
                combos.add(list);
                combosU.add(listU);
                isCombo.add(isF);
                isComboU.add(isF);
                updates.add(upd);
                list.setPrefWidth(171);
                listU.setPrefWidth(171);

                HBox h1 = new HBox(text), h2 = new HBox(updTo), inputH = new HBox(h1, h2);
                if (isF) {
                    h1.getChildren().add(list);
                    h2.getChildren().add(listU);
                } else {
                    h1.getChildren().add(textField);
                    h2.getChildren().add(upd);
                }

                h1.setSpacing(25);
                h2.setSpacing(25);
                inputH.setPadding(new Insets(5, 30, 5, 30));
                inputH.setSpacing(50);
                input.getChildren().add(inputH);
            }

            while (rs.next()) {
                ArrayList<String> row = new ArrayList<>();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
                    row.add(rs.getString(i));
                data.add(row);
            }
            tableview.setItems(data);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Query has been processed successfully", new ButtonType("OK"));
            if (message)
                alert.show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, String.valueOf(e), new ButtonType("OK")).show();
        }
        getForeignKeys();
    }

    private ResultSet query(String query, char type) {
        ResultSet rs = null;
        if (type != 's' && !confirmed(type)) query = "select * from " + tables.get(cur);
        try {
            String db = "jdbc:mysql://localhost:3306/", user = "root", pass = "";
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection c = DriverManager.getConnection(db + dbName, user, pass);

            if (query.charAt(0) != 's') {
                c.createStatement().executeUpdate(query);
                String[] trash = query.split(" ");
                query = "select * from " + trash[(query.charAt(0) == 'u' ? 1 : 2)];
            }
            rs = c.createStatement().executeQuery(query);

            if (query.equals("show tables")) while (rs.next()) {
                tables.add(rs.getString(1));
                fItems.put(rs.getString(1), new HashMap<>());
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, String.valueOf(e), new ButtonType("OK")).show();
        }
        return rs;
    }

    StringBuilder conditions(char type) throws Exception {
        StringBuilder sb = new StringBuilder();
        ArrayList<String> condition = new ArrayList<>();
        if (type == 'i') sb.append("(");
        for (int i = 0; i < colNames.size(); i++) {
            String com = combos.get(i).getValue(), inp = inputs.get(i).getText();
            if ((inp != null && !inp.isEmpty()) || (com != null && !com.isEmpty())) {
                if (type == 'i' && i == 0 && query("select * from " + tables.get(cur) + " where " + colNames.get(i) + " = " + (isColChar.get(i) ? "'" : "") + (isCombo.get(i) ? com : inp) + (isColChar.get(i) ? "'" : ""), 's').isBeforeFirst())
                    throw new Exception("PrimaryKey entered already exists");
                if (!isColChar.get(i) && notInteger(isCombo.get(i) ? com : inp))
                    throw new Exception("String Entered in place of integer");
                condition.add((type != 'i' ? colNames.get(i) + " = " : "") + (isColChar.get(i) ? "'" : "") + (!isCombo.get(i) ? inp : com) + (isColChar.get(i) ? "'" : ""));
            }
        }
        if (condition.isEmpty()) {
            if (type == 'i')
                throw new Exception("Please Enter value to be inserted");
            return new StringBuilder("1 = 1");
        }

        if (condition.size() < colNames.size() && type == 'i')
            throw new Exception("Please fill all the required fields");

        for (int i = 0; i < condition.size(); i++)
            sb.append(condition.get(i)).append(i != condition.size() - 1 ? (type == 'i' ? ", " : " and ") : (type == 'i' ? ")" : ""));
        return sb;
    }

    StringBuilder getUpdate() throws Exception {
        StringBuilder sb = new StringBuilder();
        ArrayList<String> update = new ArrayList<>();
        for (int i = 0; i < colNames.size(); i++) {
            String com = combosU.get(i).getValue(), up = updates.get(i).getText();
            if ((up != null && !up.isEmpty()) || (com != null && !com.isEmpty())) {
                if (i == 0 && query("select * from " + tables.get(cur) + " where " + colNames.get(i) + " = " + (isColChar.get(i) ? "'" : "") + (isComboU.get(i) ? up : com) + (isColChar.get(i) ? "'" : ""), 's').isBeforeFirst())
                    throw new Exception("Updated PrimaryKey already exists");
                if (!isColChar.get(i) && notInteger(isCombo.get(i) ? com : up))
                    throw new Exception("String Entered in place of integer");
                update.add(colNames.get(i) + " = " + (isColChar.get(i) ? "'" : "") + (!isComboU.get(i) ? up : com) + (isColChar.get(i) ? "'" : ""));
            }
        }
        if (update.isEmpty()) throw new Exception("Please Enter the Updated value");
        for (int i = 0; i < update.size(); i++)
            sb.append(update.get(i)).append(i != update.size() - 1 ? ", " : "");
        return sb;
    }

    public void start(Stage stage) {
        Scanner sc = new Scanner(System.in);
        dbName = sc.next();
        query("show tables", 's');
        tableview.setMaxHeight(Screen.getPrimary().getBounds().getMaxY());
        tableview.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableview.setMaxWidth(Screen.getPrimary().getBounds().getMaxX());

        getForeignKeys();

        Button sch = new Button("Search"), ins = new Button("Add"), del = new Button("Delete"), upd = new Button("Update");
        input.setPadding(new Insets(30, 0, 0, 0));

        sch.setPrefWidth(150);
        ins.setPrefWidth(150);
        del.setPrefWidth(150);
        upd.setPrefWidth(150);

        sch.setOnAction(e -> {
            try {
                String cond = conditions('s').toString();
                buildData(query("select * from " + tables.get(cur) + " where " + cond, 's'), false, 's');
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, String.valueOf(ex), new ButtonType("OK")).show();
            }
        });
        ins.setOnAction(e -> {
            try {
                String cond = conditions('i').toString();
                buildData(query("insert into " + tables.get(cur) + " values " + cond, 'i'), true, 'i');
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, String.valueOf(ex), new ButtonType("OK")).show();
            }
        });
        del.setOnAction(e -> {
            try {
                String cond = conditions('d').toString();
                buildData(query("delete from " + tables.get(cur) + " where " + cond, 'd'), true, 'd');
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, String.valueOf(ex), new ButtonType("OK")).show();
            }
        });
        upd.setOnAction(e -> {
            try {
                String cond = conditions('u').toString();
                buildData(query("update " + tables.get(cur) + " set " + getUpdate() + " where " + cond, 'u'), true, 'u');
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, String.valueOf(ex), new ButtonType("OK")).show();
            }
        });

        buildData(query("select * from " + tables.get(0), 's'), false, 's');
        Text author = new Text("                 Made by Ihab Maali 202109371"), tab = new Text(" Address: ");
        tab.setId("tableName");
        author.setStyle("-fx-fill: #61dafb; -fx-font-size: 28px;");
        tab.setStyle("-fx-fill: #61dafb; -fx-font-size: 28px;");
        HBox header = new HBox(), func = new HBox();
        func.getChildren().addAll(sch, ins, del, upd);
        func.setPadding(new Insets(5));
        func.setSpacing(50);
        VBox v = new VBox(header, tab, input, func, tableview, author);
        v.setSpacing(10);
        Scene scene = new Scene(v);
        scene.getStylesheets().add("style.css");
        tables.forEach(table -> {
            Button b = new Button(table);
            b.setPrefWidth(110);
            header.getChildren().add(b);
            b.setOnAction(e -> {
                tab.setText(" " + tables.get(tables.indexOf(table)) + ": ");
                cur = tables.indexOf(table);
                buildData(query("select * from " + table, 's'), false, 's');
            });
        });

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}