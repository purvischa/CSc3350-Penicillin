public class Main {
    public static void main(String[] args) {
        try {
            while (true) {
                String[] authResult = Menu.showLoginMenu();
                if (authResult == null) {
                    System.out.println("\nGoodbye!");
                    break;
                }

                String role = authResult[0];
                int userId = Integer.parseInt(authResult[1]);
                Menu.initialize(role, userId);

                if (role.equals("admin")) {
                    Menu.showAdminMenu();
                } else {
                    Menu.showEmployeeMenu(userId);
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
