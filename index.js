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
    if (err) return res.status(500).json({ error: err.message });
    res.json(results);
  });
});

// 1) Full‑time employee pay statement history
app.get('/reports/pay-history/:empid', (req, res) => {
  const empid = req.params.empid;
  const sql = `
    SELECT
      p.empid,
      p.pay_date AS payDate,
      p.earnings AS grossPay,
      (p.earnings 
         - (p.fed_tax + p.fed_med + p.fed_SS
            + p.state_tax + p.retire_401k + p.health_care)
      )               AS netPay,
      (p.fed_tax + p.fed_med + p.fed_SS
         + p.state_tax + p.retire_401k + p.health_care
      )               AS deductions
    FROM payroll p
    WHERE p.empid = ?
    ORDER BY p.empid, p.pay_date
  `;
  db.query(sql, [empid], (err, results) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(results);
  });
});

// 2) Total pay for month by job title
app.get('/reports/total-pay/job-title', (req, res) => {
  const month = parseInt(req.query.month, 10);
  const year  = parseInt(req.query.year,  10);
  const sql = `
    SELECT
      jt.job_title,
      SUM(p.earnings) AS totalGrossPay,
      SUM(
        p.earnings 
        - (p.fed_tax + p.fed_med + p.fed_SS
           + p.state_tax + p.retire_401k + p.health_care)
      ) AS totalNetPay
    FROM payroll p
    JOIN employees e ON p.empid = e.empid
    JOIN employee_job_titles ejt ON e.empid = ejt.empid
    JOIN job_titles jt       ON ejt.job_title_id = jt.job_title_id
    WHERE MONTH(p.pay_date) = ?
      AND YEAR(p.pay_date)  = ?
    GROUP BY jt.job_title
    ORDER BY jt.job_title
  `;
  db.query(sql, [month, year], (err, results) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(results);
  });
});

// 3) Total pay for month by division
app.get('/reports/total-pay/division', (req, res) => {
  const month = parseInt(req.query.month, 10);
  const year  = parseInt(req.query.year,  10);
  const sql = `
    SELECT
      d.Name  AS divisionName,
      SUM(p.earnings) AS totalGrossPay,
      SUM(
        p.earnings 
        - (p.fed_tax + p.fed_med + p.fed_SS
           + p.state_tax + p.retire_401k + p.health_care)
      ) AS totalNetPay
    FROM payroll p
    JOIN employees e ON p.empid = e.empid
    JOIN employee_division ed ON e.empid = ed.empid
    JOIN division d ON ed.div_ID = d.ID
    WHERE MONTH(p.pay_date) = ?
      AND YEAR(p.pay_date)  = ?
    GROUP BY d.Name
    ORDER BY d.Name
  `;
  db.query(sql, [month, year], (err, results) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(results);
  });
});

// ─── Start Server ─────────────────────────────────────────────────────────────
app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}`);
});
