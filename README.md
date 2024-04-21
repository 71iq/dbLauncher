### Database Launcher Application

This Java application provides a graphical interface for managing MySQL databases. Users can perform various CRUD (Create, Read, Update, Delete) operations on database tables through a user-friendly GUI.

### Features

- **Search**: Retrieve data from database tables based on specified criteria.
- **Add**: Insert new records into database tables.
- **Delete**: Remove existing records from database tables.
- **Update**: Modify existing records in database tables.
- **Foreign Key Support**: Handles foreign key constraints and allows selection from related values.
- **Error Handling**: Displays alerts for database errors and invalid user inputs.
- **Dynamic GUI**: The interface dynamically updates based on user actions and database queries.

### Prerequisites

- Java Development Kit (JDK) installed
- MySQL database server running locally or remotely
- MySQL Connector/J library for JDBC connectivity

### Installation

1. Clone the repository to your local machine:

```bash
git clone https://github.com/71iq/dbLauncher
```

2. Import the project into your preferred Java IDE (e.g., IntelliJ IDEA, Eclipse).

3. Ensure that MySQL Connector/J library is added to the project build path.

4. Compile and run the `bsbs.java` file to launch the application.

### Usage

1. Launch the application.
2. Enter the MySQL database name when prompted.
3. Interact with the GUI to perform database operations:
    - Click on table names to view and manage their data.
    - Use buttons for search, add, delete, and update operations.
    - Input fields and combo boxes facilitate data entry and selection.
4. Handle any error alerts that may occur during operations.

### Notes

- Ensure that the MySQL database server is running and accessible.
- Make sure to handle sensitive data securely, especially when dealing with database credentials.

### Credits

This project was developed by Ihab Maali as part of a database management course.

### License

This project is licensed under the [MIT License](LICENSE). Feel free to modify and distribute it as per the license terms.