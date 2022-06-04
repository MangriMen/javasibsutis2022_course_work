import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    static final String DB_URL = "jdbc:postgresql://localhost:5432/course_db";
    static final String USER = "course_user";
    static final String PASSWORD = "course_password";
    static final String DATA_TABLE = "DATA";

    public static void main(String[] args)  {
        Properties properties = readPropertiesFromFile("config.cfg");

        createDBTable();

        int period = Integer.parseInt(properties.getProperty("period", "1000"));
        new Timer().schedule(createMainTask(), 0, period);
    }

    public static void createDBTable() {
        final String createTableSQL = "CREATE TABLE IF NOT EXISTS " + DATA_TABLE + "("
                + "CPU BIGINT NOT NULL,"
                + "MEMORY_TOTAL BIGINT NOT NULL,"
                + "MEMORY_USED BIGINT NOT NULL,"
                + "DISK_USAGE BIGINT NOT NULL"
                + ")";

        try (Connection connection = tryConnectToDB(); Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection tryConnectToDB() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path ");
            e.printStackTrace();
        }

        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Connection Failed");
        }

        return connection;
    }

    public static TimerTask createMainTask() {
        return new TimerTask() {
            final Connection connection = tryConnectToDB();

            @Override
            public void run() {
                int CPU = getCPU();
                int DiskUsage = getDiskUsage();
                HashMap<String, Integer> MEMORY = getMemory();

                System.out.println("CPU: "+ CPU+ "%");
                System.out.println("Memory: "+ MEMORY.get("used")+"/"+MEMORY.get("total"));
                System.out.println("DiskUsage: "+ DiskUsage+ "%");

                final String insertValueSQL = "INSERT INTO " + DATA_TABLE
                        + "(CPU, MEMORY_TOTAL, MEMORY_USED, DISK_USAGE) " + "VALUES"
                        + "(" + CPU + "," + MEMORY.get("total") + "," + MEMORY.get("used") + "," + DiskUsage + ")";

                try (Statement statement = connection.createStatement()) {
                    statement.execute(insertValueSQL);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static Properties readPropertiesFromFile(String filename) {
        Properties properties = new Properties();

        try {
            properties.load(Files.newInputStream(Paths.get(filename)));
        } catch (IOException e) {
            File file = new File(filename);
            try {
                if(!file.createNewFile()){
                    System.out.println("Не удалось создать файл конфигурации.");
                }
                else{
                    try(FileWriter writer = new FileWriter(filename, false)) {
                        writer.write("period = 1000");
                    }
                }
            } catch (IOException ex) {
                System.out.println("Не удалось создать файл конфигурации.");
            }
        }

        return properties;
    }

    public static int getCPU()  {
        ProcessBuilder builder = new ProcessBuilder("vmstat", "1", "2", "-w").redirectErrorStream(true);
        Process p;
        try {
            p = builder.start();
        } catch (IOException e) {
            return -1;
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String temp;
        String line = null;

        while (true) {
            try {
                temp = r.readLine();
            } catch (IOException e) {
                return -1;
            }
            if (temp == null) {
                break;
            }

            line=temp;
        }

        assert line != null;
        String[] per = line.replaceAll("\\s+"," ").split(" ");

        return Integer.parseInt(per[13]);
    }

    public static HashMap<String, Integer> getMemory(){
        ProcessBuilder builder = new ProcessBuilder("free").redirectErrorStream(true);
        Process p;
        try {
            p = builder.start();
        } catch (IOException e) {
            return new HashMap<>();
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String temp;
        String[] line = new String[3];
        int count =0;

        while (true) {
            try {
                temp = r.readLine();
            } catch (IOException e) {
                return new HashMap<>();
            }
            if (temp == null) {
                break;
            }

            line[count]=temp;
            count++;
        }

        String[] per = line[1].replaceAll("\\s+"," ").split(" ");

        return new HashMap<String, Integer>(){
            {put("total", Integer.parseInt(per[1])); put("used", Integer.parseInt(per[2]));}};
    }

    public static int getDiskUsage()  {
        ProcessBuilder builder = new ProcessBuilder("df", "-h", "--total").redirectErrorStream(true);
        Process p;
        try {
            p = builder.start();
        } catch (IOException e) {
            return -1;
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String temp;
        String line = null;

        while (true) {
            try {
                temp = r.readLine();
            } catch (IOException e) {
                return -1;
            }
            if (temp == null) {
                break;
            }

            line=temp;
        }

        assert line != null;
        String[] per = line.replaceAll("\\s+"," ").split(" ");

        return Integer.parseInt(per[4].replaceAll("\\D", ""));
    }

}
