package snakegame;

import javax.swing.JFrame;
import java.util.LinkedList;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Point;

public class Snake extends JFrame {

    private static final long serialVersionUID = 4452837901773886482L;

    //Saniyede gösterilecek kare sayısı.
    final long FRAME_TIME = 1000 / 50L;

    //Oyun alanının eni ve boyu.
    final int BOARD_SIZE = 25;

    //Yılanın başlangıçta uzayacağı uzunluk.
    final int MIN_SNAKE_LENGTH = 5;

    //Yılanın hızının artması için gereken puan.
    final int LEVEL_UP_LIMIT = 500;

    //Yılanın maksimum hızlanabileceği değer, yani saniyede 9 kareden başlayıp maksimum (9+10) kareye kadar çıkabilir.
    final int LEVEL_LIMIT = 10;

    //Oyunun başlayıp başlamadığını kontrol eder.
    boolean isGameStart = false;

    //Oyunun bitip bitmediğini kontrol eder.
    boolean isGameOver = false;

    //Oyunun durdurulup durdurulmadığını kontprintlnrol eder.
    boolean isPaused = false;

    //Oyun alanı içinde yılanın koordinatlarını tutan liste.
    LinkedList<Point> snakeBody;

    //Oyun alanı içinde duvarların koordinatlarını tutan liste.
    LinkedList<Point> walls;

    //Oyunun yenilenme süresini geçerse, yılanın hareketlerini hafızada tutan liste.
    LinkedList<Direction> directions;

    Direction direction;

    Direction lastDirection;

    TileType[][] tile;

    Point food;

    Point head;

    BoardPanel board;

    Clock clock;

    // Test
    public Snake() {
        super();
        setTitle("Snake Game");
        setSize(500, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        board = new BoardPanel(this);
        snakeBody = new LinkedList<>();
        directions = new LinkedList<>();
        tile = new TileType[BOARD_SIZE][BOARD_SIZE];
        add(board);

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        if (isGameStart && !isPaused) {
                            if (lastDirection != Direction.SOUTH && lastDirection != Direction.NORTH) {
                                directions.add(Direction.NORTH);
                                lastDirection = Direction.NORTH;
                            }
                        }
                        break;

                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        if (isGameStart && !isPaused) {
                            if (lastDirection != Direction.SOUTH && lastDirection != Direction.NORTH) {
                                directions.add(Direction.SOUTH);
                                lastDirection = Direction.SOUTH;
                            }
                        }
                        break;

                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        if (isGameStart && !isPaused) {
                            if (lastDirection != Direction.EAST && lastDirection != Direction.WEST) {
                                directions.add(Direction.WEST);
                                lastDirection = Direction.WEST;
                            }
                        }
                        break;

                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        if (isGameStart && !isPaused) {
                            if (lastDirection != Direction.EAST && lastDirection != Direction.WEST) {
                                directions.add(Direction.EAST);
                                lastDirection = Direction.EAST;
                            }
                        }
                        break;

                    case KeyEvent.VK_ENTER:
                        if (!isGameStart) {
                            isGameStart = true;
                            isGameOver = false;
                            reset();
                        }
                        break;

                    case KeyEvent.VK_P:
                        if (isGameStart) {
                            isPaused = !isPaused;
                        }
                        break;
                        
                    case KeyEvent.VK_Q:
                        System.exit(0);
                }
            }
        });
        setVisible(true);
    }
    
    public void pressEnter() {
        clock = new Clock(this);
        if (!isGameStart) {
            isGameStart = true;
            isGameOver = false;
            reset();
        }
    }
    
    public void goNorth() {
        if (isGameStart && !isPaused) {
            if (lastDirection != Direction.SOUTH && lastDirection != Direction.NORTH) {
                directions.add(Direction.NORTH);
                lastDirection = Direction.NORTH;
            }
        }
    }
    
    public void goSouth() {
        if (isGameStart && !isPaused) {
            if (lastDirection != Direction.SOUTH && lastDirection != Direction.NORTH) {
                directions.add(Direction.SOUTH);
                lastDirection = Direction.SOUTH;
            }
        }
    }
    
    public void goWest() {
        if (isGameStart && !isPaused) {
            if (lastDirection != Direction.EAST && lastDirection != Direction.WEST) {
                directions.add(Direction.WEST);
                lastDirection = Direction.WEST;
            }
        }
    }
    
    public void goEast() {
        if (isGameStart && !isPaused) {
            if (lastDirection != Direction.EAST && lastDirection != Direction.WEST) {
                directions.add(Direction.EAST);
                lastDirection = Direction.EAST;
            }
        }
    }

    public void start() {        
        while (true) {
            long start = System.nanoTime();
            clock.update();

            if (clock.tasmaVarMi()) {
                newDirection();
                update();
            }
            board.repaint();

            long delta = (System.nanoTime() - start) / 1000000L;
            if (delta < FRAME_TIME) {
                try {
                    Thread.sleep(FRAME_TIME - delta);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void reset() {
        clearBoard();
        snakeBody.clear();
        head = new Point(BOARD_SIZE / 2, BOARD_SIZE / 2);
        snakeBody.addFirst(new Point(head));
        direction = Direction.EAST;
        lastDirection = direction;
        clock.saniyeBasiAdim = 4.0f;

        spawnFood();
    }

    public void update() {
        nextPoint(head);

        if (checkCrash()) {
            isGameStart = false;
            isGameOver = true;
        } else if (tile[head.x][head.y] == TileType.FOOD) {
            grow();
            spawnFood();
            tile[head.x][head.y] = TileType.EMPTY;
        } else if (snakeBody.size() < MIN_SNAKE_LENGTH) {
            grow();
        } else {
            move();
        }
    }

    public void newDirection() {
        if (directions.size() > 0) {
            direction = directions.remove();
        }
    }

    public void grow() {
        snakeBody.addFirst(new Point(head));
    }

    public void move() {
        snakeBody.addFirst(new Point(head));

        Point tail = snakeBody.removeLast();
        tile[tail.x][tail.y] = TileType.EMPTY;
    }

    public boolean checkCrash() {
        for (Point snake : snakeBody) {
            if (snake.equals(head)) {
                return true;
            }
        }

        return false;
    }

    public void clearBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                tile[i][j] = TileType.EMPTY;
            }
        }
    }

    public void spawnFood() {
        int x, y;

        do {
            x = (int) (Math.random() * BOARD_SIZE);
            y = (int) (Math.random() * BOARD_SIZE);
            food = new Point(x, y);
        } while (checkFoodCrash());

        tile[x][y] = TileType.FOOD;
    }

    public boolean checkFoodCrash() {
        for (Point snake : snakeBody) {
            if (snake.equals(food)) {
                return true;
            }
        }

        return false;
    }

    public void nextPoint(Point head) {

        switch (direction) {

            case NORTH:
                if (head.y - 1 < 0) {
                    head.y = BOARD_SIZE - 1;
                } else {
                    head.y--;
                }
                break;

            case SOUTH:
                if (head.y + 1 > BOARD_SIZE - 1) {
                    head.y = 0;
                } else {
                    head.y++;
                }
                break;

            case EAST:
                if (head.x + 1 > BOARD_SIZE - 1) {
                    head.x = 0;
                } else {
                    head.x++;
                }
                break;

            case WEST:
                if (head.x - 1 < 0) {
                    head.x = BOARD_SIZE - 1;
                } else {
                    head.x--;
                }
                break;
        }
        this.head = head;
    }

    public static void main(String[] args) {
        
        Snake game = new Snake();
        
        ServerThread st = new ServerThread(game, 8080);
        Thread th = new Thread(st);
        th.start();
        
        game.pressEnter();
        game.start();
        
    }
}
