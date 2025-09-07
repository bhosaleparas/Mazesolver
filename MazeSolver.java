
import java.util.*;

public class MazeSolver {
    // Maze representation
    private char[][] maze;
    private int rows, cols;
    private Point start, end;

    // For path reconstruction
    private Map<Point, Point> parentMap;

    // Movement directions (up, right, down, left)
    private static final int[][] DIRECTIONS = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

    public static void main(String[] args) {
        MazeSolver solver = new MazeSolver();
        solver.run();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== AI Maze Solver ===");
            System.out.println("1. Enter a new maze");
            System.out.println("2. Solve using BFS");
            System.out.println("3. Solve using DFS");
            System.out.println("4. Solve using A*");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear invalid input
                continue;
            }

            switch (choice) {
                case 1:
                    inputMaze(scanner);
                    break;
                case 2:
                    if (maze != null) {
                        solveBFS();
                    } else {
                        System.out.println("Please enter a maze first.");
                    }
                    break;
                case 3:
                    if (maze != null) {
                        solveDFS();
                    } else {
                        System.out.println("Please enter a maze first.");
                    }
                    break;
                case 4:
                    if (maze != null) {
                        solveAStar();
                    } else {
                        System.out.println("Please enter a maze first.");
                    }
                    break;
                case 5:
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
        scanner.close();
    }

    private void inputMaze(Scanner scanner) {
        System.out.println("Enter the number of rows:");
        rows = scanner.nextInt();
        System.out.println("Enter the number of columns:");
        cols = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        maze = new char[rows][cols];
        System.out.println("Enter the maze row by row (use 's' for start, 'e' for end, '#' for walls, '.' for paths):");

        for (int i = 0; i < rows; i++) {
            String line = scanner.nextLine().trim();
            for (int j = 0; j < cols && j < line.length(); j++) {
                maze[i][j] = line.charAt(j);
                if (maze[i][j] == 's') {
                    start = new Point(i, j);
                } else if (maze[i][j] == 'e') {
                    end = new Point(i, j);
                }
            }
        }

        // Validate maze
        if (start == null || end == null) {
            System.out.println("Maze must have a start (s) and end (e) point.");
            maze = null;
            return;
        }

        System.out.println("Maze entered successfully!");
        printMaze();
    }

    private void solveBFS() {
        System.out.println("Solving using BFS...");
        parentMap = new HashMap<>();
        Queue<Point> queue = new LinkedList<>();
        Set<Point> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            if (current.equals(end)) {
                System.out.println("Path found!");
                reconstructPath();
                return;
            }

            for (int[] direction : DIRECTIONS) {
                int newRow = current.row + direction[0];
                int newCol = current.col + direction[1];

                if (isValidMove(newRow, newCol) && !visited.contains(new Point(newRow, newCol))) {
                    Point neighbor = new Point(newRow, newCol);
                    queue.add(neighbor);
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                }
            }
        }

        System.out.println("No path exists from start to end.");
    }

    private void solveDFS() {
        System.out.println("Solving using DFS...");
        parentMap = new HashMap<>();
        Set<Point> visited = new HashSet<>();

        if (dfs(start, visited)) {
            System.out.println("Path found!");
            reconstructPath();
        } else {
            System.out.println("No path exists from start to end.");
        }
    }

    private boolean dfs(Point current, Set<Point> visited) {
        if (current.equals(end)) {
            return true;
        }

        visited.add(current);

        for (int[] direction : DIRECTIONS) {
            int newRow = current.row + direction[0];
            int newCol = current.col + direction[1];
            Point neighbor = new Point(newRow, newCol);

            if (isValidMove(newRow, newCol) && !visited.contains(neighbor)) {
                parentMap.put(neighbor, current);
                if (dfs(neighbor, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void solveAStar() {
        System.out.println("Solving using A*...");
        parentMap = new HashMap<>();
        Map<Point, Integer> gScore = new HashMap<>(); // Cost from start to node
        Map<Point, Integer> fScore = new HashMap<>(); // Estimated total cost (g + h)

        // Initialize scores
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Point p = new Point(i, j);
                gScore.put(p, Integer.MAX_VALUE);
                fScore.put(p, Integer.MAX_VALUE);
            }
        }

        gScore.put(start, 0);
        fScore.put(start, heuristic(start, end));

        PriorityQueue<Point> openSet = new PriorityQueue<>(
                (a, b) -> Integer.compare(fScore.get(a), fScore.get(b))
        );
        openSet.add(start);

        Set<Point> closedSet = new HashSet<>();

        while (!openSet.isEmpty()) {
            Point current = openSet.poll();

            if (current.equals(end)) {
                System.out.println("Path found!");
                reconstructPath();
                return;
            }

            closedSet.add(current);

            for (int[] direction : DIRECTIONS) {
                int newRow = current.row + direction[0];
                int newCol = current.col + direction[1];
                Point neighbor = new Point(newRow, newCol);

                if (!isValidMove(newRow, newCol) || closedSet.contains(neighbor)) {
                    continue;
                }

                int tentativeGScore = gScore.get(current) + 1;

                if (tentativeGScore < gScore.get(neighbor)) {
                    parentMap.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + heuristic(neighbor, end));

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        System.out.println("No path exists from start to end.");
    }

    private int heuristic(Point a, Point b) {
        // Manhattan distance
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols && maze[row][col] != '#';
    }

    private void reconstructPath() {
        // Create a copy of the maze to mark the path
        char[][] solvedMaze = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(maze[i], 0, solvedMaze[i], 0, cols);
        }

        // Reconstruct path from end to start
        Point current = end;
        while (current != null && !current.equals(start)) {
            solvedMaze[current.row][current.col] = '*';
            current = parentMap.get(current);
        }

        // Print the solved maze
        System.out.println("Solution path (marked with '*'):");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(solvedMaze[i][j]);
            }
            System.out.println();
        }
    }

    private void printMaze() {
        System.out.println("Current maze:");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(maze[i][j]);
            }
            System.out.println();
        }
    }

    // Helper class to represent a point in the maze
    private static class Point {
        int row, col;

        Point(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Point point = (Point) obj;
            return row == point.row && col == point.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }

        @Override
        public String toString() {
            return "(" + row + ", " + col + ")";
        }
    }
}