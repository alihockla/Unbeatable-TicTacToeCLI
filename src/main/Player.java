package main;

public class Player {
	String playerName;
	String playerCharacter; // X or O
	
	public Player(String name, String character) {
		this.playerName = name;
		this.playerCharacter = character;
	}

	@Override
	public String toString() {
		return playerName + " (" + playerCharacter + ")";
	}
}
