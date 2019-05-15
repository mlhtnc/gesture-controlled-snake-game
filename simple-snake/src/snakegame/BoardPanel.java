package snakegame;

import javax.swing.JPanel;
import java.awt.Point;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

public class BoardPanel extends JPanel {

	private static final long serialVersionUID = -4302948406828260249L;
	
	final int TILE_SIZE = 19;
	
	Font largeFont = null;
	Font smallFont = null;
	Font mediumFont = null;
	
	GraphicsEnvironment ge;
	
	Snake game;
	
	public BoardPanel(Snake game) {
		super();
		this.game = game;
		setBackground(Color.BLACK);
		
		try {
			smallFont = Font.createFont(Font.TRUETYPE_FONT,
					this.getClass().getResourceAsStream("/font/Lato-Regular.ttf"));
			
			ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(smallFont);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		largeFont = smallFont.deriveFont(Font.BOLD, 28);
		mediumFont = smallFont.deriveFont(Font.BOLD, 25);
		smallFont = smallFont.deriveFont(Font.BOLD, 22);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (game.isGameStart && !game.isGameOver) {
			
			g.setColor(Color.LIGHT_GRAY);
			for(Point snake : game.snakeBody) {
				g.fillRect((snake.x * 20) + 1 , (snake.y * 20) + 1, TILE_SIZE, TILE_SIZE);
			}
			
			g.setColor(Color.ORANGE);
			g.fillOval((game.food.x * 20) + 5, (game.food.y * 20) + 5, 10, 10);
			
			g.setColor(Color.DARK_GRAY);
			g.drawLine(0, 500, 500, 500);	
		}
		
		if(!game.isGameStart && !game.isGameOver) {
			g.setColor(Color.ORANGE);
			g.setFont(largeFont);
			g.drawString("Please wait...", 
					getWidth() / 2 - g.getFontMetrics().stringWidth("Please wait...") / 2, 150);
		}
		
		if(game.isGameOver) {
			g.setColor(Color.ORANGE);
			g.setFont(largeFont);
			g.drawString("Game over", 
					getWidth() / 2 - g.getFontMetrics().stringWidth(
							"Game over") / 2, 100);
			
			g.setFont(mediumFont);
			g.drawString("Press Enter to play again",
					getWidth() / 2 - g.getFontMetrics().stringWidth(
							"Press Enter to play again") / 2, 200);
		}
		
		Toolkit.getDefaultToolkit().sync();
	}
}