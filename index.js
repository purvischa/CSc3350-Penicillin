const express = require('express');
const mysql = require('mysql2');
const cors = require('cors');
const app = express();
const port = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// Database connection (You can update this later for a remote MySQL server)
const db = mysql.createConnection({
  host: 'localhost',      // If you're using a local MySQL server
  user: 'root',           // Your MySQL username
  password: 'Ag2025tha?!',           // Your MySQL password
  database: 'employeeData' // Your MySQL database name
});

db.connect(err => {
  if (err) console.error('Database connection failed:', err);
  else console.log('Connected to MySQL database.');
});

// Simple route to test the server
app.get('/', (req, res) => {
  res.send('Hello from your backend server!');
});

// Get all employees
app.get('/employees', (req, res) => {
  db.query('SELECT * FROM employees', (err, results) => {
    if (err) throw err;
    res.json(results);
  });
});

// Start the server
app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}`);
});
