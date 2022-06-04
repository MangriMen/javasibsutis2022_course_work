import pkg from "pg";
const { Pool } = pkg;

const pool = new Pool({
  host: "localhost",
  port: "5432",
  user: "course_user",
  password: "course_password",
  database: "course_db",
});

export function getAllFrom() {
  return new Promise(function (resolve, reject) {
    pool.query(`SELECT * FROM data`, (error, results) => {
      if (error) {
        reject(error);
      }
      resolve(results.rows);
    });
  });
}
