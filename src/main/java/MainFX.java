import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Employee;
import model.PayStatement;
import model.City;
import model.State;
import java.util.List;
import java.util.Map;

/**
 * MainFX is the JavaFX-based GUI entry point for the Employee Management System.
 * It handles user login and presents different tabs depending on the user's role
 * (admin vs. general employee).
 */
public class MainFX extends Application {
    /** Stores the role of the logged-in user ("admin" or "employee"). */
    private String role;

    /** Stores the empId of the logged-in user (0 for admin). */
    private int userId;

    /**
     * JavaFX start method: called after launch().
     * @param primaryStage The primary window for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        showLogin(primaryStage);
    }

    /**
     * Builds and displays the login screen.
     * On successful login, transitions to the main UI.
     */
    private void showLogin(Stage stage) {
        // Input fields for username and password
        TextField userField = new TextField();
        PasswordField passField = new PasswordField();
        Button loginBtn = new Button("Login");

        // Handle login button click
        loginBtn.setOnAction(e -> {
            String auth = EmployeeDAO.authenticateUser(
                userField.getText().trim(),
                passField.getText().trim()
            );
            if (auth != null) {
                // Parse "role|empId" returned by DAO
                String[] parts = auth.split("\\|");
                role   = parts[0];
                userId = Integer.parseInt(parts[1]);
                showMainUI(stage);  // proceed to main application
            } else {
                showAlert("Invalid credentials");
            }
        });

        // Layout the login form
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(new Label("Username:"), 0, 0);
        grid.add(userField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passField, 1, 1);
        grid.add(loginBtn, 1, 2);

        // Show the scene
        stage.setScene(new Scene(grid));
        stage.setTitle("Login");
        stage.show();
    }

    /**
     * After login, builds the main UI with tabs appropriate to the user's role.
     * @param stage The same primary stage as used for login.
     */
    private void showMainUI(Stage stage) {
        TabPane tabPane = new TabPane();

        // Set tab styles and spacing
        HBox headers = (HBox) tabPane.lookup(".headers-region");
        if (headers != null) {
            headers.setSpacing(20);
        }

        if ("admin".equals(role)) {
            // Admin sees all tabs
            tabPane.getTabs().addAll(
                tabSearch(),
                tabUpdate(),
                tabBulkSalary(),
                tabReports(),
                tabInsert()
            );
        } else {
            // Employee sees only view, update, and pay history
            Tab tView = new Tab("View");
            TextArea taView = new TextArea();
            taView.setEditable(false);
            Employee emp = EmployeeDAO.getEmployee(userId);
            taView.setText(emp != null ? emp.toString() : "Employee not found.");
            tView.setContent(new VBox(taView));

            Tab tUpdate = tabUpdate();
            // Prefill update form with own empId when selected
            tUpdate.setOnSelectionChanged(e -> {
                if (tUpdate.isSelected()) {
                    ((TextField)((HBox)((VBox)tUpdate.getContent()).getChildren().get(0))
                      .getChildren().get(1))
                      .setText(String.valueOf(userId));
                }
            });

            Tab tHistory = new Tab("Pay History");
            TextArea taHist = new TextArea();
            taHist.setEditable(false);
            // Fetch and display pay-statement history for current user
            List<PayStatement> ph = EmployeeDAO.getPayStatementHistory(userId);
            StringBuilder sb = new StringBuilder();
            for (var ps : ph) sb.append(ps).append("\n----\n");
            taHist.setText(sb.toString());
            tHistory.setContent(new VBox(taHist));

            tabPane.getTabs().addAll(tView, tUpdate, tHistory);
        }

        // Wrap tabs in a border pane and display
        BorderPane root = new BorderPane(tabPane);
        Scene scene = new Scene(root, 900, 600);
        stage.setMinWidth(900);
        scene.getStylesheets().add("file:style.css");
        stage.setScene(scene);
        stage.setTitle("Employee Management");
        stage.show();
    }

    /**
     * Creates the "Search" tab UI.
     * Allows searching by Name, ID, DOB, or SSN.
     */
    private Tab tabSearch() {
        Tab tab = new Tab("Search");

        ComboBox<String> cb = new ComboBox<>(
            FXCollections.observableArrayList("Name","ID","DOB","SSN")
        );
        TextField tf = new TextField();
        Button btn = new Button("Go");
        TextArea ta = new TextArea();
        ta.setEditable(false);

        btn.setOnAction(e -> {
            String by = cb.getValue();
            String val = tf.getText().trim();
            if (by == null || val.isEmpty()) {
                showAlert("Select a criterion and enter a value");
                return;
            }
            List<Employee> list;
            switch (by) {
                case "Name" -> list = EmployeeDAO.searchByName(val);
                case "ID"   -> {
                    try {
                        Employee emp = EmployeeDAO.getEmployee(Integer.parseInt(val));
                        list = emp == null ? List.of() : List.of(emp);
                    } catch (NumberFormatException ex) { list = List.of(); }
                }
                case "DOB"  -> list = EmployeeDAO.searchByDOB(val);
                case "SSN"  -> list = EmployeeDAO.searchBySSN(val);
                default     -> list = List.of();
            }
            // Display search results
            StringBuilder sb = new StringBuilder();
            if (list.isEmpty()) sb.append("No results found");
            else list.forEach(e2 -> sb.append(e2).append("\n----\n"));
            ta.setText(sb.toString());
        });

        HBox searchControls = new HBox(10, new Label("By"), cb, tf, btn);
        searchControls.setPadding(new Insets(10));
        VBox layout = new VBox(10, searchControls, ta);
        layout.setPadding(new Insets(10));
        tab.setContent(layout);

        return tab;
    }

    /**
     * Creates the "Update/Delete" tab UI.
     * Admin can load any employee by ID, modify fields, or delete.
     */
    @SuppressWarnings("unchecked")
    private Tab tabUpdate() {
        Tab tab = new Tab("Update/Delete");

        // Top row: EmpID input + Load button
        TextField idField = new TextField();
        Button loadBtn = new Button("Load");
        HBox top = new HBox(10, new Label("EmpID:"), idField, loadBtn);
        top.setPadding(new Insets(10));

        // Form fields for employee data
        TextField fn = new TextField();
        TextField ln = new TextField();
        TextField emField = new TextField();
        TextField ph = new TextField();
        TextField genderField = new TextField();
        TextField raceField = new TextField();
        TextField dobField = new TextField();
        TextField hireDateField = new TextField();
        TextField sal = new TextField();
        TextField streetField = new TextField();
        ComboBox<City> cityCB = new ComboBox<>(
            FXCollections.observableArrayList(EmployeeDAO.getCities())
        );
        ComboBox<State> stateCB = new ComboBox<>(
            FXCollections.observableArrayList(EmployeeDAO.getStates())
        );
        TextField zip = new TextField();

        Button saveBtn = new Button("Save Changes");
        Button deleteBtn = new Button("Delete Employee");

        // Load button action: fetch employee data by ID and populate fields
        loadBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Employee emp = EmployeeDAO.getEmployee(id);
                if (emp == null) {
                    showAlert("Employee not found.");
                    return;
                }
                fn.setText(emp.getFname());
                ln.setText(emp.getLname());
                emField.setText(emp.getEmail());
                ph.setText(emp.getPhone_number());
                genderField.setText(emp.getGender());
                raceField.setText(emp.getRace());
                dobField.setText(emp.getDOB());
                hireDateField.setText(emp.getHireDate());
                sal.setText(String.valueOf(emp.getSalary()));
                streetField.setText(emp.getStreet());
                cityCB.getSelectionModel().select(
                    EmployeeDAO.getCities().stream()
                    .filter(c -> c.getCityId() == emp.getCity_id())
                    .findFirst().orElse(null)
                );
                stateCB.getSelectionModel().select(
                    EmployeeDAO.getStates().stream()
                    .filter(s -> s.getStateId() == emp.getState_id())
                    .findFirst().orElse(null)
                );
                zip.setText(emp.getZip());
            } catch (NumberFormatException ex) {
                showAlert("Invalid ID");
            }
        });

        // Save button action: update employee fields one by one
        saveBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                boolean ok = true;
                ok &= EmployeeDAO.updateEmployee(id, "Fname", fn.getText().trim());
                ok &= EmployeeDAO.updateEmployee(id, "Lname", ln.getText().trim());
                ok &= EmployeeDAO.updateEmployee(id, "email", emField.getText().trim());
                ok &= EmployeeDAO.updateEmployee(id, "phone_number", ph.getText().trim());
                ok &= EmployeeDAO.updateEmployee(id, "gender", genderField.getText().trim());
                ok &= EmployeeDAO.updateEmployee(id, "race", raceField.getText().trim());
                ok &= EmployeeDAO.updateEmployee(id, "DOB", dobField.getText().trim());
                ok &= EmployeeDAO.updateEmployee(id, "HireDate", hireDateField.getText().trim());
                ok &= EmployeeDAO.updateEmployee(id, "Salary", sal.getText().trim());
                ok &= EmployeeDAO.updateAddress(
                    id,
                    streetField.getText().trim(),
                    cityCB.getValue().getCityId(),
                    stateCB.getValue().getStateId(),
                    zip.getText().trim()
                );
                showAlert(ok ? "Updated successfully." : "Update failed.");
            } catch (Exception ex) {
                showAlert("Error saving changes.");
            }
        });

        // Delete button action: remove employee and all related records
        deleteBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                boolean deleted = EmployeeDAO.deleteEmployee(id);
                showAlert(deleted ? "Deleted employee " + id : "Delete failed.");
            } catch (NumberFormatException ex) {
                showAlert("Invalid ID");
            }
        });

        // Layout form in a grid
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);
        grid.addRow(0, new Label("First:"), fn, new Label("Last:"), ln);
        grid.addRow(1, new Label("Email:"), emField, new Label("Phone:"), ph);
        grid.addRow(2, new Label("Gender:"), genderField, new Label("Race:"), raceField);
        grid.addRow(3, new Label("DOB:"), dobField, new Label("Hire Date:"), hireDateField);
        grid.addRow(4, new Label("Salary:"), sal);
        grid.addRow(5, new Label("Street:"), streetField);
        grid.addRow(6, new Label("City:"), cityCB, new Label("State:"), stateCB, new Label("ZIP:"), zip);
        HBox buttons = new HBox(10, saveBtn, deleteBtn);
        grid.add(buttons, 1, 7);

        VBox layout = new VBox(10, top, grid);
        layout.setPadding(new Insets(10));
        tab.setContent(layout);
        return tab;
    }

    /**
     * Creates the "Bulk Salary" tab UI.
     * Allows admin to apply a percentage salary change to all employees within a range.
     */
    private Tab tabBulkSalary() {
        Tab tab = new Tab("Bulk Salary");
        TextField minField = new TextField();
        TextField maxField = new TextField();
        TextField pctField = new TextField();
        Button btn = new Button("Apply");

        btn.setOnAction(e -> {
            try {
                double min = Double.parseDouble(minField.getText().trim());
                double max = Double.parseDouble(maxField.getText().trim());
                double pct = Double.parseDouble(pctField.getText().trim());
                int updated = EmployeeDAO.updateSalariesInRange(min, max, pct);
                showAlert(updated + " records updated");
            } catch (NumberFormatException ex) {
                showAlert("Enter valid numbers");
            }
        });

        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setVgap(10);
        form.setHgap(10);
        form.addRow(0, new Label("Min:"), minField);
        form.addRow(1, new Label("Max:"), maxField);
        form.addRow(2, new Label("% Change:"), pctField);
        form.add(btn, 1, 3);

        tab.setContent(form);
        return tab;
    }

    /**
     * Creates the "Reports" tab UI.
     * Admin can view pay history, total pay by job, or total pay by division.
     */
    private Tab tabReports() {
        Tab tab = new Tab("Reports");
        TextArea area = new TextArea();
        area.setEditable(false);

        // Pay History controls
        TextField phId = new TextField("0");
        Button phBtn = new Button("History");
        phBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(phId.getText().trim());
                List<PayStatement> list = EmployeeDAO.getPayStatementHistory(id);
                StringBuilder sb = new StringBuilder();
                if (list.isEmpty()) sb.append("No statements");
                else list.forEach(ps -> sb.append(ps).append("\n----\n"));
                area.setText(sb.toString());
            } catch (NumberFormatException ex) {
                showAlert("Invalid ID");
            }
        });

        // Total pay by job or division
        TextField year  = new TextField("2025");
        TextField month = new TextField("1");
        Button byJob = new Button("By Job");
        Button byDiv = new Button("By Div");

        byJob.setOnAction(e -> {
            try {
                int y = Integer.parseInt(year.getText().trim());
                int m = Integer.parseInt(month.getText().trim());
                Map<String, Double> map = EmployeeDAO.getTotalPayByJobTitle(y, m);
                StringBuilder sb = new StringBuilder();
                map.forEach((k, v) -> sb.append(k).append(": $").append(v).append("\n"));
                area.setText(sb.toString());
            } catch (NumberFormatException ex) {
                showAlert("Enter valid year/month");
            }
        });

        byDiv.setOnAction(e -> {
            try {
                int y = Integer.parseInt(year.getText().trim());
                int m = Integer.parseInt(month.getText().trim());
                Map<String, Double> map = EmployeeDAO.getTotalPayByDivision(y, m);
                StringBuilder sb = new StringBuilder();
                map.forEach((k, v) -> sb.append(k).append(": $").append(v).append("\n"));
                area.setText(sb.toString());
            } catch (NumberFormatException ex) {
                showAlert("Enter valid year/month");
            }
        });

        HBox controls = new HBox(10,
            new Label("ID:"), phId, phBtn,
            new Label("Year:"), year,
            new Label("Mon:"), month, byJob, byDiv
        );
        controls.setPadding(new Insets(10));

        VBox layout = new VBox(10, controls, area);
        layout.setPadding(new Insets(10));
        tab.setContent(layout);
        return tab;
    }

    /**
     * Creates the "Insert" tab UI.
     * Admin can add a new employee, including demographics, SSN, address, job & division.
     */
    @SuppressWarnings("unchecked")
    private Tab tabInsert() {
        Tab tab = new Tab("Insert");

        // Form fields
        TextField fn           = new TextField();
        TextField ln           = new TextField();
        TextField emField      = new TextField();
        TextField ph           = new TextField();
        TextField genderField  = new TextField();
        TextField raceField    = new TextField();
        TextField ssnField     = new TextField();
        TextField dobField     = new TextField();
        TextField hireDateField= new TextField();
        TextField sal          = new TextField();
        TextField streetField  = new TextField();
        ComboBox<Integer> jobCB   = new ComboBox<>(FXCollections.observableArrayList(
            EmployeeDAO.getJobTitles().keySet()
        ));
        ComboBox<Integer> divCB   = new ComboBox<>(FXCollections.observableArrayList(
            EmployeeDAO.getDivisions().keySet()
        ));
        ComboBox<City>    cityCB  = new ComboBox<>(FXCollections.observableArrayList(EmployeeDAO.getCities()));
        ComboBox<State>   stateCB = new ComboBox<>(FXCollections.observableArrayList(EmployeeDAO.getStates()));
        TextField zip       = new TextField();

        Button addBtn       = new Button("Add");
        addBtn.setOnAction(e -> {
            try {
                double sVal = Double.parseDouble(sal.getText().trim());
                int jid    = jobCB.getValue();
                int did    = divCB.getValue();
                int cityId = cityCB.getValue().getCityId();
                int stateId= stateCB.getValue().getStateId();

                // Call DAO to insert new employee
                int newId = EmployeeDAO.insertEmployee(
                    fn.getText().trim(),            // 1) First Name
                    ln.getText().trim(),            // 2) Last Name
                    emField.getText().trim(),       // 3) Email address
                    ph.getText().trim(),            // 4) Phone number
                    genderField.getText().trim(),   // 5) Gender
                    raceField.getText().trim(),     // 6) Race/ethnicity
                    ssnField.getText().trim(),      // 7) Social Security Number
                    dobField.getText().trim(),      // 8) Date of Birth (YYYY-MM-DD)
                    hireDateField.getText().trim(), // 9) Hire Date (YYYY-MM-DD)
                    sVal,                           // 10) Salary
                    jid,                            // 11) Job Title ID
                    did,                            // 12) Division ID
                    streetField.getText().trim(),   // 13) Street address
                    cityId,                         // 14) City ID
                    stateId,                        // 15) State ID
                    zip.getText().trim()            // 16) ZIP code
                );
                showAlert(newId > 0 ? "Added! ID=" + newId : "Insert failed");
            } catch (Exception ex) {
                showAlert("Ensure all fields are filled correctly");
            }
        });

        // Layout the insert form
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);
        grid.addRow(0, new Label("First:"), fn, new Label("Last:"), ln);
        grid.addRow(1, new Label("Email:"), emField, new Label("Phone:"), ph);
        grid.addRow(2, new Label("Gender:"), genderField, new Label("Race:"), raceField);
        grid.addRow(3, new Label("SSN:"), ssnField);
        grid.addRow(4, new Label("DOB:"), dobField, new Label("Hire Date:"), hireDateField);
        grid.addRow(5, new Label("Salary:"), sal);
        grid.addRow(6, new Label("Job ID:"), jobCB, new Label("Div ID:"), divCB);
        grid.addRow(7, new Label("Street:"), streetField);
        grid.addRow(8, new Label("City:"), cityCB, new Label("State:"), stateCB, new Label("ZIP:"), zip);
        grid.add(addBtn, 1, 9);

        tab.setContent(grid);
        return tab;
    }

    /**
     * Utility method to show an information alert.
     * @param msg The message to display.
     */
    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }

    /**
     * Launches the JavaFX application.
     * @param args Command-line arguments (ignored)
     */
    public static void main(String[] args) {
        launch(args);
    }
}