# CSc3350 Penicillin Employee Management Backend

This Node.js/Express service provides an Employee Management System backend with MySQL for Company “Z”. It implements reporting features and basic employee data access.

## Features Implemented

1. **Health check** (`GET /`)  
2. **List all employees** (`GET /employees`)  
3. **Full‑time employee pay statement history** (`GET /reports/pay-history/:empid`)  
4. **Total pay by job title** (`GET /reports/total-pay/job-title?month=&year=`)  
5. **Total pay by division** (`GET /reports/total-pay/division?month=&year=`)

## Prerequisites

- **Node.js** (v14+) and **npm**  
- **MySQL** server with database `employeeData` and tables:  
  - `employees`  
  - `payroll`  
  - `employee_job_titles`, `job_titles`  
  - `employee_division`, `division`  
  - (and any others per schema)

## Installation

1. Clone the repo:  
   ```bash
   git clone https://github.com/purvischa/CSc3350-Penicillin.git
   cd CSc3350-Penicillin
   git checkout Diana's-branch
   ```

2. Install packages:
    ```bash
    npm install
    ```

3. Create a .env file in project root and add:
    ```bash
    DB_HOST=localhost
    DB_USER=root
    DB_PASS=your_mysql_password
    DB_NAME=employeeData
    PORT=3000
    ```

4. Start the server:
    ```bash
    npm start
    ```
    or, for auto‑reload:
    ```bash
    npm run dev
    ```

## Installation
Use curl, Postman, or your favorite HTTP client.

### Health Check
```bash
curl http://localhost:3000/
```
Expected: "Hello from your backend server!"

### List All Employees
```bash
curl http://localhost:3000/employees
```

### Pay History for One Employee
```bash
curl http://localhost:3000/reports/pay-history/1
```

### Total Pay by Job Title
```bash
curl "http://localhost:3000/reports/total-pay/job-title?month=12&year=2024"
```

### Total Pay by Division
```bash
curl "http://localhost:3000/reports/total-pay/division?month=12&year=2024"
```