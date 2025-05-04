# Quick Start

## Prerequisites
- **MySQL** server running locally  
- **Java 17 JDK** (`javac` & `java`)  
- **JavaFX SDK** matching your JDK (e.g., JavaFX 17 for JDK 17)  
  - Download from [GluonHQ](https://gluonhq.com/products/javafx/) and unzip to `javafx-sdk/`  
- **MySQL Connector/J** JAR in `lib/mysql-connector.jar`

---

## 1. Clone & configure
```bash
git clone https://github.com/purvischa/CSc3350-Penicillin.git
cd SoftwareDev_FinalProject
git checkout complete
```

Copy or create `.env` in the project root:
```ini
DB_HOST=localhost
DB_PORT=3306
DB_NAME=employeeData
DB_USER=root
DB_PASS=your_mysql_password
JDBC_DRIVER_JAR=lib/mysql-connector.jar
```

---

## 2. Create the database
Log into MySQL/DBeaver and run:
```sql
CREATE DATABASE IF NOT EXISTS employeeData;
USE employeeData;

/* --- Core tables --- */

CREATE TABLE employees (
  empid         INT AUTO_INCREMENT PRIMARY KEY,
  Fname         VARCHAR(50),
  Lname         VARCHAR(50),
  email         VARCHAR(100),
  phone_number  VARCHAR(20),
  gender        VARCHAR(10),
  race          VARCHAR(20),
  SSN           VARCHAR(11),
  DOB           DATE,
  HireDate      DATE,
  Salary        DOUBLE
);

CREATE TABLE address (
  empid    INT PRIMARY KEY,
  street   VARCHAR(100),
  city_id  INT,
  state_id INT,
  zip      VARCHAR(10),
  FOREIGN KEY(empid)    REFERENCES employees(empid),
  FOREIGN KEY(city_id)  REFERENCES city(city_id),
  FOREIGN KEY(state_id) REFERENCES state(state_id)
);

CREATE TABLE job_titles (
  job_title_id INT AUTO_INCREMENT PRIMARY KEY,
  job_title    VARCHAR(100)
);

CREATE TABLE employee_job_titles (
  empid         INT PRIMARY KEY,
  job_title_id  INT,
  FOREIGN KEY(empid)        REFERENCES employees(empid),
  FOREIGN KEY(job_title_id) REFERENCES job_titles(job_title_id)
);

CREATE TABLE division (
  ID   INT AUTO_INCREMENT PRIMARY KEY,
  Name VARCHAR(100)
);

CREATE TABLE employee_division (
  empid  INT PRIMARY KEY,
  div_ID INT,
  FOREIGN KEY(empid) REFERENCES employees(empid),
  FOREIGN KEY(div_ID) REFERENCES division(ID)
);

CREATE TABLE payroll (
  pay_id   INT AUTO_INCREMENT PRIMARY KEY,
  empid    INT,
  pay_date DATE,
  earnings DOUBLE,
  FOREIGN KEY(empid) REFERENCES employees(empid)
);

CREATE TABLE city (
  city_id       INT AUTO_INCREMENT PRIMARY KEY,
  name_of_city  VARCHAR(100)
);

CREATE TABLE state (
  state_id       INT AUTO_INCREMENT PRIMARY KEY,
  name_of_state  VARCHAR(100)
);
```

---

## 2. Populate the database

Log into MySQL/DBeaver and run:
```sql
USE employeeData;

-- 1) States
INSERT INTO state (state_id, name_of_state) VALUES
  (1, 'New York'),
  (2, 'California'),
  (3, 'Illinois'),
  (4, 'Texas'),
  (5, 'Florida');

-- 2) Cities
INSERT INTO city (city_id, name_of_city) VALUES
  (1, 'New York'),
  (2, 'Los Angeles'),
  (3, 'Chicago'),
  (4, 'Houston'),
  (5, 'Miami');

-- 3) Divisions
INSERT INTO division (ID, Name) VALUES
  (1, 'Engineering'),
  (2, 'Sales'),
  (3, 'Human Resources'),
  (4, 'Marketing'),
  (5, 'Finance');

-- 4) Job Titles
INSERT INTO job_titles (job_title_id, job_title) VALUES
  (1, 'Software Engineer'),
  (2, 'Sales Representative'),
  (3, 'HR Manager'),
  (4, 'Marketing Specialist'),
  (5, 'Financial Analyst'),
  (6, 'Data Scientist'),
  (7, 'QA Engineer'),
  (8, 'Product Manager');

-- 5) Employees (20 total)
INSERT INTO employees (empid, Fname, Lname, email, phone_number, gender, race, SSN, DOB, HireDate, Salary) VALUES
  ( 1, 'John',    'Smith',    'john.smith@example.com',     '212-555-0147', 'M', 'White',    '123-45-6789','1985-04-23','2015-08-01',  90000),
  ( 2, 'Emily',   'Johnson',  'emily.johnson@example.com',  '310-555-0198', 'F', 'Asian',    '987-65-4321','1990-11-12','2018-02-15',  75000),
  ( 3, 'Carlos',  'Martinez', 'carlos.martinez@example.com','213-555-0176', 'M', 'Hispanic', '456-78-9123','1982-07-05','2010-06-20', 120000),
  ( 4, 'Aisha',   'Ali',      'aisha.ali@example.com',      '312-555-0112', 'F', 'Black',    '234-56-7891','1978-02-28','2008-09-10', 110000),
  ( 5, 'Michael', 'Brown',    'michael.brown@example.com',  '713-555-0134', 'M', 'White',    '321-54-9876','1992-01-17','2020-01-03',  68000),
  ( 6, 'Sophia',  'Davis',    'sophia.davis@example.com',   '305-555-0165', 'F', 'White',    '654-32-1987','1988-09-30','2017-05-22',  82000),
  ( 7, 'David',   'Lee',      'david.lee@example.com',      '212-555-0183', 'M', 'Asian',    '789-12-3456','1984-12-11','2012-11-05', 100000),
  ( 8, 'Olivia',  'Garcia',   'olivia.garcia@example.com',  '310-555-0101', 'F', 'Hispanic', '567-89-1234','1995-03-14','2021-07-19',  72000),
  ( 9, 'James',   'Wilson',   'james.wilson@example.com',   '312-555-0145', 'M', 'Black',    '890-12-5678','1975-10-22','2005-04-30', 130000),
  (10,'Isabella','Moore',    'isabella.moore@example.com', '713-555-0156', 'F', 'White',    '210-98-7654','1993-06-08','2019-10-01',  77000),
  -- Additional 10
  (11, 'Liam',    'Clark',    'liam.clark@example.com',     '646-555-0123', 'M', 'White',    '345-67-8901','1987-02-14','2016-05-10',  88000),
  (12, 'Mia',     'Scott',    'mia.scott@example.com',      '415-555-0167', 'F', 'Black',    '456-78-9012','1991-07-25','2019-11-01',  74000),
  (13, 'Noah',    'Evans',    'noah.evans@example.com',     '202-555-0189', 'M', 'White',    '567-89-0123','1983-12-30','2011-02-20', 105000),
  (14, 'Ava',     'Turner',   'ava.turner@example.com',     '213-555-0190', 'F', 'Asian',    '678-90-1234','1994-08-05','2020-03-15',  70000),
  (15, 'Ethan',   'Parker',   'ethan.parker@example.com',   '312-555-0171', 'M', 'Hispanic', '789-01-2345','1986-06-17','2014-09-25',  96000),
  (16, 'Chloe',   'Wright',   'chloe.wright@example.com',   '305-555-0142', 'F', 'White',    '890-12-3456','1992-10-11','2018-07-30',  82000),
  (17, 'Lucas',   'Adams',    'lucas.adams@example.com',    '646-555-0135', 'M', 'Black',    '901-23-4567','1985-01-22','2013-12-05', 112000),
  (18, 'Ella',    'Bennett',  'ella.bennett@example.com',   '415-555-0118', 'F', 'Hispanic', '012-34-5678','1996-05-08','2022-01-10',  68000),
  (19, 'Oliver',  'Brooks',   'oliver.brooks@example.com',   '202-555-0154', 'M', 'White',    '123-45-6780','1989-09-18','2017-03-22',  94000),
  (20, 'Grace',   'Campbell', 'grace.campbell@example.com', '213-555-0162', 'F', 'Asian',    '234-56-7890','1990-11-29','2016-10-01',  78000);

-- 6) Addresses
INSERT INTO address (empid, street, city_id, state_id, zip) VALUES
  ( 1, '123 Main St',        1, 1, '10001'),
  ( 2, '456 Sunset Blvd',    2, 2, '90028'),
  ( 3, '789 Lake Shore Dr',  3, 3, '60611'),
  ( 4, '1011 Bayou Rd',      4, 4, '77002'),
  ( 5, '1213 Ocean Dr',      5, 5, '33139'),
  ( 6, '1415 Broadway',      1, 1, '10036'),
  ( 7, '1617 Hollywood St',  2, 2, '90068'),
  ( 8, '1819 Wacker Dr',     3, 3, '60606'),
  ( 9, '2021 Westheimer',    4, 4, '77006'),
  (10, '2223 Brickell Ave',  5, 5, '33131'),
  (11, '2324 Madison Ave',   1, 1, '10029'),
  (12, '2526 Vine St',       2, 2, '90038'),
  (13, '2728 Michigan Ave',  3, 3, '60605'),
  (14, '2930 Kirby Dr',      4, 4, '77098'),
  (15, '3132 Bayshore Blvd', 5, 5, '33141'),
  (16, '3334 Fifth Ave',     1, 1, '10028'),
  (17, '3536 Sunset Plaza',  2, 2, '90069'),
  (18, '3738 Lake Blvd',     3, 3, '60610'),
  (19, '3940 Shepherd Dr',   4, 4, '77098'),
  (20, '4142 Coral Way',     5, 5, '33145');

-- 7) Employee ↔ Job Title mappings
INSERT INTO employee_job_titles (empid, job_title_id) VALUES
  ( 1,1),( 2,6),( 3,5),( 4,3),( 5,7),
  ( 6,1),( 7,6),( 8,4),( 9,8),(10,2),
  (11,3),(12,4),(13,5),(14,6),(15,7),
  (16,8),(17,1),(18,2),(19,3),(20,4);

-- 8) Employee ↔ Division mappings
INSERT INTO employee_division (empid, div_ID) VALUES
  ( 1,1),( 2,1),( 3,5),( 4,3),( 5,2),
  ( 6,1),( 7,1),( 8,4),( 9,2),(10,2),
  (11,3),(12,4),(13,5),(14,1),(15,2),
  (16,3),(17,5),(18,4),(19,1),(20,2);

-- 9) Payroll history (March & April 2025 for all 20)
INSERT INTO payroll (empid, pay_date, earnings) VALUES
  /* March */
  ( 1,'2025-03-31',ROUND(90000/12,2)),  ( 2,'2025-03-31',ROUND(75000/12,2)),
  ( 3,'2025-03-31',ROUND(120000/12,2)), ( 4,'2025-03-31',ROUND(110000/12,2)),
  ( 5,'2025-03-31',ROUND(68000/12,2)),  ( 6,'2025-03-31',ROUND(82000/12,2)),
  ( 7,'2025-03-31',ROUND(100000/12,2)), ( 8,'2025-03-31',ROUND(72000/12,2)),
  ( 9,'2025-03-31',ROUND(130000/12,2)), (10,'2025-03-31',ROUND(77000/12,2)),
  (11,'2025-03-31',ROUND(88000/12,2)),  (12,'2025-03-31',ROUND(74000/12,2)),
  (13,'2025-03-31',ROUND(105000/12,2)), (14,'2025-03-31',ROUND(70000/12,2)),
  (15,'2025-03-31',ROUND(96000/12,2)),  (16,'2025-03-31',ROUND(82000/12,2)),
  (17,'2025-03-31',ROUND(112000/12,2)), (18,'2025-03-31',ROUND(68000/12,2)),
  (19,'2025-03-31',ROUND(94000/12,2)),  (20,'2025-03-31',ROUND(78000/12,2)),
  /* April */
  ( 1,'2025-04-30',ROUND(90000/12,2)),  ( 2,'2025-04-30',ROUND(75000/12,2)),
  ( 3,'2025-04-30',ROUND(120000/12,2)), ( 4,'2025-04-30',ROUND(110000/12,2)),
  ( 5,'2025-04-30',ROUND(68000/12,2)),  ( 6,'2025-04-30',ROUND(82000/12,2)),
  ( 7,'2025-04-30',ROUND(100000/12,2)), ( 8,'2025-04-30',ROUND(72000/12,2)),
  ( 9,'2025-04-30',ROUND(130000/12,2)), (10,'2025-04-30',ROUND(77000/12,2)),
  (11,'2025-04-30',ROUND(88000/12,2)),  (12,'2025-04-30',ROUND(74000/12,2)),
  (13,'2025-04-30',ROUND(105000/12,2)), (14,'2025-04-30',ROUND(70000/12,2)),
  (15,'2025-04-30',ROUND(96000/12,2)),  (16,'2025-04-30',ROUND(82000/12,2)),
  (17,'2025-04-30',ROUND(112000/12,2)), (18,'2025-04-30',ROUND(68000/12,2)),
  (19,'2025-04-30',ROUND(94000/12,2)),  (20,'2025-04-30',ROUND(78000/12,2));
```

---

## 3. Compile & run the Java **console** app
```bash
# Clean old classes
rm -rf bin/*

# Compile
javac -d bin \
  -cp lib/mysql-connector.jar \
  src/main/java/Database.java \
  src/main/java/EmployeeDAO.java \
  src/main/java/Menu.java \
  src/main/java/Main.java \
  src/main/java/model/*.java

# Run
java -cp "bin:lib/mysql-connector.jar" Main
```
Log in as **admin/admin123** or **fname_lname** + your **empid**.

---

## 4. Compile & run the JavaFX **GUI** app

1. Ensure `javafx-sdk/` holds your JavaFX SDK.  
2. Compile:
   ```bash
   rm -rf bin/*
   javac --module-path javafx-sdk/lib \
         --add-modules javafx.controls,javafx.fxml \
         -cp lib/mysql-connector.jar \
         -d bin \
         src/main/java/*.java src/main/java/model/*.java
   ```
3. Launch:
   ```bash
   java --module-path javafx-sdk/lib \
        --add-modules javafx.controls,javafx.fxml \
        -cp "bin:lib/mysql-connector.jar" \
        MainFX
   ```

---

## Optional: Run using Maven
```bash
# Console version
mvn clean compile exec:java@run-console

# JavaFX GUI
mvn clean javafx:run

# Run all tests
mvn test

# Run a specific test (e.g., EmployeeDAOTest)
mvn -Dtest=EmployeeDAOTest test
```

---

## 5. How It Works

### Core Layers

1. **`Database.java`**  
   - Reads your `.env` file  
   - Dynamically loads the MySQL driver  
   - Provides `getConnection()` for DAO methods

2. **`EmployeeDAO.java`**  
   - All SQL access lives here:  
     - **Authentication**  
     - **Search** (by name, ID, DOB, SSN)  
     - **CRUD** on employees + addresses  
     - **Bulk salary updates**  
     - **Reports** (pay history, total pay by job/division)

3. **Models** in `model/`  
   - `Employee.java`, `PayStatement.java`, `City.java`, `State.java`  
   - Each offers `fromResultSet()` or constructors + `toString()`

4. **Tests**  
   - `EmployeeDAOTest.java` covers every DAO method and key scenarios

### Console UI (`Main.java` + `Menu.java`)

- **Login**  
  - Admin: `admin` / `admin123`  
  - Employee: `fname_lname` / `<empid>`  
- **Main menu**  
  - **Admin** sees:  
    1. Search  
    2. Update fields (one at a time)  
    3. Bulk salary update  
    4. Reports (pay history, by job title, by division)  
    5. Insert new employee  
    6. Delete employee  
    0. Exit  
  - **Employee** sees:  
    1. View their own record  
    2. Update allowed fields (name, email, phone, DOB, SSN, salary)  
    3. View pay history  
    0. Exit

### JavaFX GUI (`MainFX.java`)

1. **Login Screen**  
   - Same credentials as console; redirects to main UI.

2. **Tabs for Admin**  
   - **Search**: choose criterion (Name, ID, DOB, SSN), view matching records.  
   - **Update/Delete**: load by EmpID, edit all demographic fields & address or delete.  
   - **Bulk Salary**: specify min, max and percentage to apply to all in that range.  
   - **Reports**:  
     - **History**: enter EmpID (0 = all), see pay statements sorted by date.  
     - **By Job** / **By Div**: pick year/month, see total gross pay grouped accordingly.  
   - **Insert**: form to add a full new employee record (demographics, SSN, job, division, address).

3. **Tabs for Employee**  
   - **View**: read-only display of own record.  
   - **Update**: same form as admin but pre-filled and limited to allowed fields.  
   - **Pay History**: shows only this user’s pay statements.

---
