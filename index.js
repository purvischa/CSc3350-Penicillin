require('dotenv').config();

const express = require('express');
const mysql = require('mysql2');
const cors = require('cors');
const app = express();

// Use port from .env or default to 3000
const port = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// ─── Database Connection ───────────────────────────────────────────────────────
const db = mysql.createConnection({
  host:     process.env.DB_HOST,
  user:     process.env.DB_USER,
  password: process.env.DB_PASS,
  database: process.env.DB_NAME
});

db.connect(err => {
  if (err) console.error('Database connection failed:', err);
  else console.log('Connected to MySQL database.');
});

// ─── Routes ────────────────────────────────────────────────────────────────────

// Health check
app.get('/', (req, res) => {
  res.send('Hello from your backend server!');
});

// List all employees
app.get('/employees', (req, res) => {
  db.query('SELECT * FROM employees', (err, results) => {
    if (err) throw err;
    res.json(results);
  });
});

// Get employee by ID with all related info
app.get('/employees/:empid', (req, res) => {
  const empid = req.params.empid;
  const sql = `
    SELECT 
      e.*,
      a.street, a.city_id, a.state_id, a.zip,
      d.Name as division_name,
      jt.job_title
    FROM employees e
    LEFT JOIN address a ON e.empid = a.empid
    LEFT JOIN employee_division ed ON e.empid = ed.empid
    LEFT JOIN division d ON ed.division_id = d.ID
    LEFT JOIN employee_job_title ejt ON e.empid = ejt.empid
    LEFT JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id
    WHERE e.empid = ?
  `;
  db.query(sql, [empid], (err, results) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(results[0] || null);
  });
});

// Get all divisions
app.get('/divisions', (req, res) => {
  const sql = 'SELECT ROW_NUMBER() OVER (ORDER BY ID) - 1 as display_id, ID, Name FROM division ORDER BY ID';
  db.query(sql, (err, results) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(results);
  });
});

// Get all job titles
app.get('/job-titles', (req, res) => {
  const sql = 'SELECT job_title_id - 1 as job_id, job_title FROM job_titles ORDER BY job_title';
  db.query(sql, (err, results) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(results);
  });
});

// Get all cities
app.get('/cities', (req, res) => {
  const sql = 'SELECT city_id, name_of_city FROM city ORDER BY name_of_city';
  db.query(sql, (err, results) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(results);
  });
});

// Get all states
app.get('/states', (req, res) => {
  const sql = 'SELECT state_id, name_of_state FROM state ORDER BY name_of_state';
  db.query(sql, (err, results) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(results);
  });
});

// Add new employee
app.post('/employees', async (req, res) => {
  const {
    empid, Fname, Lname, email, HireDate, Salary, SSN, phone_number,
    gender, race, DOB, street, city_id, state_id, zip, division_id, job_title_id
  } = req.body;

  // Start transaction
  const conn = await db.promise().getConnection();
  try {
    await conn.beginTransaction();

    // Check for existing employee
    const [existing] = await conn.query(
      'SELECT * FROM employees WHERE email = ? OR SSN = ?',
      [email, SSN]
    );

    if (existing.length > 0) {
      return res.status(409).json({ 
        error: 'Employee with this email or SSN already exists',
        existing: existing[0]
      });
    }

    // Insert employee
    await conn.query(
      'INSERT INTO employees (empid, Fname, Lname, email, HireDate, Salary, SSN, phone_number, gender, race, DOB) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
      [empid, Fname, Lname, email, HireDate, Salary, SSN, phone_number, gender, race, DOB]
    );

    // Insert address
    await conn.query(
      'INSERT INTO address (empid, street, city_id, state_id, zip) VALUES (?, ?, ?, ?, ?)',
      [empid, street, city_id, state_id, zip]
    );

    // Insert job title relationship
    await conn.query(
      'INSERT INTO employee_job_title (empid, job_title_id) VALUES (?, ?)',
      [empid, job_title_id + 1]
    );

    // Insert division relationship
    await conn.query(
      'INSERT INTO employee_division (empid, division_id) VALUES (?, ?)',
      [empid, division_id + 1]
    );

    await conn.commit();

    // Get the newly inserted employee with all related info
    const sql = `
      SELECT 
        e.*,
        a.street, a.city_id, a.state_id, a.zip,
        d.Name as division_name,
        jt.job_title
      FROM employees e
      LEFT JOIN address a ON e.empid = a.empid
      LEFT JOIN employee_division ed ON e.empid = ed.empid
      LEFT JOIN division d ON ed.division_id = d.ID
      LEFT JOIN employee_job_title ejt ON e.empid = ejt.empid
      LEFT JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id
      WHERE e.empid = ?
    `;
    const [employee] = await conn.query(sql, [empid]);
    res.status(201).json(employee[0]);

  } catch (err) {
    await conn.rollback();
    res.status(500).json({ error: err.message });
  } finally {
    conn.release();
  }
});

// Update employee
app.put('/employees/:empid', async (req, res) => {
  const empid = req.params.empid;
  const {
    Fname, Lname, email, HireDate, Salary, SSN, phone_number,
    gender, race, DOB, street, city_id, state_id, zip, division_id, job_title_id
  } = req.body;

  const conn = await db.promise().getConnection();
  try {
    await conn.beginTransaction();

    // Update employee
    await conn.query(
      'UPDATE employees SET Fname = ?, Lname = ?, email = ?, HireDate = ?, Salary = ?, SSN = ?, phone_number = ?, gender = ?, race = ?, DOB = ? WHERE empid = ?',
      [Fname, Lname, email, HireDate, Salary, SSN, phone_number, gender, race, DOB, empid]
    );

    // Update address
    await conn.query(
      'UPDATE address SET street = ?, city_id = ?, state_id = ?, zip = ? WHERE empid = ?',
      [street, city_id, state_id, zip, empid]
    );

    // Update job title
    await conn.query(
      'UPDATE employee_job_title SET job_title_id = ? WHERE empid = ?',
      [job_title_id + 1, empid]
    );

    // Update division
    await conn.query(
      'UPDATE employee_division SET division_id = ? WHERE empid = ?',
      [division_id + 1, empid]
    );

    await conn.commit();

    // Get the updated employee
    const sql = `
      SELECT 
        e.*,
        a.street, a.city_id, a.state_id, a.zip,
        d.Name as division_name,
        jt.job_title
      FROM employees e
      LEFT JOIN address a ON e.empid = a.empid
      LEFT JOIN employee_division ed ON e.empid = ed.empid
      LEFT JOIN division d ON ed.division_id = d.ID
      LEFT JOIN employee_job_title ejt ON e.empid = ejt.empid
      LEFT JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id
      WHERE e.empid = ?
    `;
    const [employee] = await conn.query(sql, [empid]);
    res.json(employee[0]);

  } catch (err) {
    await conn.rollback();
    res.status(500).json({ error: err.message });
  } finally {
    conn.release();
  }
});

// Start the server
app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}`);
});
