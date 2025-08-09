/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package studentmanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.sql.*;

public class AdminController {

    @FXML
    private TableView<Student> studentTable;

    @FXML
    private TableColumn<Student, Integer> idColumn;

    @FXML
    private TableColumn<Student, String> nameColumn;

    @FXML
    private TableColumn<Student, String> emailColumn;

    @FXML
    private TableColumn<Student, String> phoneColumn;

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private Label messageLabel;

    private ObservableList<Student> studentList;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        loadStudents();

        studentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                emailField.setText(newSelection.getEmail());
                phoneField.setText(newSelection.getPhone());
            }
        });
    }

    private void loadStudents() {
        studentList = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM students";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                studentList.add(new Student(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone")
                ));
            }
            studentTable.setItems(studentList);

        } catch (Exception e) {
            messageLabel.setText("Error loading students: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            messageLabel.setText("Please fill all fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO students (name, email, phone) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.executeUpdate();

            messageLabel.setText("Student added successfully.");
            clearFields();
            loadStudents();

        } catch (Exception e) {
            messageLabel.setText("Error adding student: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select a student to update.");
            return;
        }

        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            messageLabel.setText("Please fill all fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE students SET name=?, email=?, phone=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setInt(4, selected.getId());
            ps.executeUpdate();

            messageLabel.setText("Student updated successfully.");
            clearFields();
            loadStudents();

        } catch (Exception e) {
            messageLabel.setText("Error updating student: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select a student to delete.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM students WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, selected.getId());
            ps.executeUpdate();

            messageLabel.setText("Student deleted successfully.");
            clearFields();
            loadStudents();

        } catch (Exception e) {
            messageLabel.setText("Error deleting student: " + e.getMessage());
        }
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
    }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) studentTable.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            Scene scene = new Scene(root);
            stage.setTitle("Student Manager Login");
            stage.setScene(scene);
        } catch (Exception e) {
            messageLabel.setText("Error during logout: " + e.getMessage());
        }
    }
}
