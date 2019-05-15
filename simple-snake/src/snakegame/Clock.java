package snakegame;

public class Clock {

	long sonGuncelleme;
	
	float artanZaman;
	
	float saniyeBasiAdim;
	
	float donguBasiMilisaniye;
	
	int tasanSure;
	
	Snake game;
	
	public Clock(Snake game) {
		this.game = game;
		sonGuncelleme = guncelZaman();
	}
	
	public void update() {
		long guncelZaman = guncelZaman();
		float gecenZaman = (float) (guncelZaman - sonGuncelleme) + artanZaman;

		donguBasiMilisaniye();
		if(game.isGameStart && !game.isPaused)
		{
			tasanSure += (int) Math.floor(gecenZaman / donguBasiMilisaniye);
			artanZaman = gecenZaman % donguBasiMilisaniye;
		}
		sonGuncelleme = guncelZaman;
	}
	
	public boolean tasmaVarMi() {
		if(tasanSure > 0) {
			tasanSure--;
			return true;
		}
		return false;
	}
	
	public void donguBasiMilisaniye() {
		donguBasiMilisaniye = (1000 / saniyeBasiAdim);
	}
	
	public long guncelZaman() {
		return System.nanoTime() / 1000000L;
	}
}
